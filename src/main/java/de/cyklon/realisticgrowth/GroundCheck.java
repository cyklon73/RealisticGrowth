package de.cyklon.realisticgrowth;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.function.Predicate;

public class GroundCheck {

	private Predicate<Location> check;

	private GroundCheck(Predicate<Location> check) {
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
				.and(l -> checkAround(Material.AIR).test(l.clone().add(0, 1, 0)));
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
				.and(l -> checkAround(Material.WATER).test(l.clone().add(0, 1, 0)));
	}


	private static Predicate<Location> checkAround(Material material) {
		return l -> {
			Material m1 = l.clone().add(1, 0, 0).getBlock().getType();
			Material m2 = l.clone().add(0, 0, 1).getBlock().getType();
			Material m3 = l.clone().subtract(1, 0, 0).getBlock().getType();
			Material m4 = l.clone().subtract(0, 0, 1).getBlock().getType();

			return Arrays.asList(m1, m2, m3, m4).contains(material);
		};
	}

	public static GroundCheck checkBelow(Material... material) {
		return new GroundCheck(l -> Arrays.asList(material).contains(l.getBlock().getType()));
	}

	public boolean check(Location location) {
		location = location.clone();
		return location.getBlock().getType().equals(Material.AIR) && check.test(location.subtract(0, 1, 0));
	}

	private GroundCheck and(Predicate<Location> check) {
		this.check = this.check.and(check);
		return this;
	}
}
