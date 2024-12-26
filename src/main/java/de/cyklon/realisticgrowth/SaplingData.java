package de.cyklon.realisticgrowth;

import org.bukkit.Material;

public record SaplingData(Material sapling, boolean normalAllowed, boolean largeTree, GroundCheck largeCheck, GroundCheck normalCheck) {}
