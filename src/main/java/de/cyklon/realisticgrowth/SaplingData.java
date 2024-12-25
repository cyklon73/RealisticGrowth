package de.cyklon.realisticgrowth;

import org.bukkit.Material;

public record SaplingData(Material sapling, boolean largeTree, GroundCheck largeCheck, GroundCheck normalCheck) {}
