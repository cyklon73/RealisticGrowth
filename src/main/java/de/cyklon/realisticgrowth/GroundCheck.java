package de.cyklon.realisticgrowth;

import de.cyklon.realisticgrowth.util.MinecraftVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

public class GroundCheck {

	private final MinecraftVersion version;
	private BiPredicate<GroundCheck, Location> check;
	private LargeField field;

	private GroundCheck(BiPredicate<GroundCheck, Location> check) {
		this.version = RealisticGrowth.getMinecraftVersion();
		this.check = check;
	}

	public static GroundCheck checkTreeGround() {
		GroundCheck check = checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM);
		if (check.version.checkVersion(1, 17, 0)) check = check.or(checkBelow(Material.MOSS_BLOCK, Material.ROOTED_DIRT));
		return check;
	}

	public static GroundCheck checkLargeTreeGround() {
		/*
		x ->    z |

		1		1 2
				3 X

		2		1 2
				X 4

		3		X 2
				3 4

		4		1 X
				3 4
		 */
		return new GroundCheck((gc, l) -> {
			LargeField f_1 = new LargeField(
					checkTreeGround(),
					l.clone().add(-1, 1, -1),
					l.clone().add(0, 1, -1),
					l.clone().add(-1, 1, 0),
					l.clone().add(0, 1, 0)
			);

			LargeField f_2 = new LargeField(
					checkTreeGround(),
					l.clone().add(0, 1, -1),
					l.clone().add(1, 1, -1),
					l.clone().add(0, 1, 0),
					l.clone().add(1, 1, 0)
			);

			LargeField f_3 = new LargeField(
					checkTreeGround(),
					l.clone().add(0, 1, 0),
					l.clone().add(1, 1, 0),
					l.clone().add(0, 1, 1),
					l.clone().add(1, 1, 1)
			);

			LargeField f_4 = new LargeField(
					checkTreeGround(),
					l.clone().add(-1, 1, 0),
					l.clone().add(0, 1, 0),
					l.clone().add(-1, 1, 1),
					l.clone().add(0, 1, 1)
			);

			for (LargeField f : Arrays.asList(f_1, f_2, f_3, f_4)) {
				if (f.check()) {
					gc.field = f;
					return true;
				}
			}
			return false;
		});
	}

	public static GroundCheck checkMangroveGround() {
		GroundCheck check = checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM);
		if (check.version.checkVersion(1, 17, 0)) check = check.or(checkBelow(Material.ROOTED_DIRT));
		return check;
	}

	public static GroundCheck checkBambooGround() {
		GroundCheck check = checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.SAND, Material.RED_SAND, Material.MYCELIUM);
		if (check.version.checkVersion(1, 17, 0)) check = check.or(checkBelow(Material.ROOTED_DIRT));
		if (check.version.checkVersion(1, 19, 0)) check = check.or(checkBelow(Material.MUD));
		return check;
	}

	public static GroundCheck checkSugarGround() {
		GroundCheck check = checkBelow(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.SAND, Material.RED_SAND);
		if (check.version.checkVersion(1, 19, 0)) check.or(checkBelow(Material.MUD));
		return check.and(checkAround(Material.WATER));
	}

	public static GroundCheck checkCactusGround() {
		return checkBelow(Material.SAND, Material.RED_SAND)
				.and((gc, l) -> checkAround(Material.AIR).test(gc, l.clone().add(0, 1, 0)));
	}

	public static GroundCheck checkFlowerGround() {
		GroundCheck check = checkBelow(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM);
		if (check.version.checkVersion(1, 17, 0)) check = check.or(checkBelow(Material.MOSS_BLOCK));
		return check;
	}

	public static GroundCheck checkLargeFlowerGround() {
		return checkFlowerGround().and((gc, l) -> l.clone().add(0, 2, 0).getBlock().getType().equals(Material.AIR));
	}

	public static GroundCheck checkMushroomGround() {
		return checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM)
				.and((gc, l) -> l.getBlock().getLightLevel() <= 12);
	}

	public static GroundCheck checkNetherGround() {
		GroundCheck check = checkBelow(Material.NETHERRACK);
		if (check.version.checkVersion(1, 16, 0)) check = check.or(checkBelow(Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.SOUL_SOIL));
		return check;
	}

	public static GroundCheck checkCoralGround() {
		return checkBelow(Material.SAND, Material.RED_SAND, Material.GRAVEL)
				.and((gc, l) -> checkAround(Material.WATER).test(gc, l.clone().add(0, 1, 0)));
	}


	private static BiPredicate<GroundCheck, Location> checkAround(Material material) {
		return (gc, l) -> {
			Material m1 = l.clone().add(1, 0, 0).getBlock().getType();
			Material m2 = l.clone().add(0, 0, 1).getBlock().getType();
			Material m3 = l.clone().subtract(1, 0, 0).getBlock().getType();
			Material m4 = l.clone().subtract(0, 0, 1).getBlock().getType();

			return Arrays.asList(m1, m2, m3, m4).contains(material);
		};
	}

	public static GroundCheck checkBelow(Material... material) {
		return new GroundCheck((gc, l) -> Arrays.asList(material).contains(l.getBlock().getType()));
	}

	private boolean checkTargetBlock(Block block) {
		if (block.getBlockData() instanceof Snow snow && snow.getLayers() <= 1) return true;
		Material shortGrass = null;
		if (version.checkVersion(1, 20, 3)) shortGrass = Material.SHORT_GRASS;
		return Arrays.asList(Material.AIR, shortGrass, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN).contains(block.getType());
	}

	public boolean check(Location location) {
		location = location.clone();
		return checkTargetBlock(location.getBlock()) && check.test(this, location.subtract(0, 1, 0));
	}

	private GroundCheck and(BiPredicate<GroundCheck, Location> check) {
		this.check = this.check.and(check);
		return this;
	}

	private GroundCheck or(GroundCheck check) {
		this.check = this.check.or(check.check);
		return this;
	}

	public LargeField getField() {
		LargeField field = this.field;
		this.field = null;
		return field;
	}

	public static class LargeField {

		private final GroundCheck check;
		private final Location location_1;
		private final Location location_2;
		private final Location location_3;
		private final Location location_4;

		public LargeField(GroundCheck check, Location location_1, Location location_2, Location location_3, Location location_4) {
			this.check = check;
			this.location_1 = location_1;
			this.location_2 = location_2;
			this.location_3 = location_3;
			this.location_4 = location_4;
		}

		public List<Location> getLocations() {
			return Arrays.asList(location_1, location_2, location_3, location_4);
		}

		public boolean check() {
			return check.check(location_1) &&
					check.check(location_2) &&
					check.check(location_3) &&
					check.check(location_4);
		}

		public GroundCheck getCheck() {
			return check;
		}

		public Location getLocation_1() {
			return location_1;
		}

		public Location getLocation_2() {
			return location_2;
		}

		public Location getLocation_3() {
			return location_3;
		}

		public Location getLocation_4() {
			return location_4;
		}
	}
}
