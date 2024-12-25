package de.cyklon.realisticgrowth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;

import java.util.Arrays;
import java.util.function.Predicate;

public class GroundCheck {

	private BiPredicate<GroundCheck, Location> check;
	private LargeField field;

	private GroundCheck(BiPredicate<GroundCheck, Location> check) {
		this.check = check;
	}

	public static GroundCheck checkTreeGround() {
		return checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.MOSS_BLOCK, Material.MYCELIUM, Material.ROOTED_DIRT);
	}

	public static GroundCheck checkMangroveGround() {
		return checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM, Material.ROOTED_DIRT);
	}

	public static GroundCheck checkBambooGround() {
		return checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.SAND, Material.RED_SAND, Material.MUD, Material.MYCELIUM, Material.ROOTED_DIRT);
	}

	public static GroundCheck checkSugarGround() {
		return checkBelow(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.SAND, Material.RED_SAND, Material.MUD)
				.and(checkAround(Material.WATER));
	}

	public static GroundCheck checkCactusGround() {
		return checkBelow(Material.SAND, Material.RED_SAND)
				.and((gc, l) -> checkAround(Material.AIR).test(gc, l.clone().add(0, 1, 0)));
	}

	public static GroundCheck checkFlowerGround() {
		return checkBelow(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.MOSS_BLOCK, Material.MYCELIUM);
	}

	public static GroundCheck checkMushroomGround() {
		return checkBelow(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM);
	}

	public static GroundCheck checkNetherGround() {
		return checkBelow(Material.NETHERRACK, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.SOUL_SOIL);
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
		return Arrays.asList(Material.AIR, Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN).contains(block.getType());
	}

	public boolean check(Location location) {
		location = location.clone();
		return checkTargetBlock(location.getBlock()) && check.test(this, location.subtract(0, 1, 0));
	}

	private GroundCheck and(BiPredicate<GroundCheck, Location> check) {
		this.check = this.check.and(check);
		return this;
	}
}
