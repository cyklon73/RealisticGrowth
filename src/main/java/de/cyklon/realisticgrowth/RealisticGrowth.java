package de.cyklon.realisticgrowth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class RealisticGrowth extends JavaPlugin implements Listener {

    private Map<Material, GroundCheck> saplings;
    private Map<Material, SaplingData> saplings;
    private Map<Material, Material> replaces;

    private FileConfiguration config;

    private double replant_chance;

    private Random random;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.saplings = new HashMap<>();
        this.replaces = new HashMap<>();

        this.config = getConfig();

        this.replant_chance = config.getInt("replant-chance", 90)/100d;

        if (replant_chance > 1) replant_chance = 1;
        if (replant_chance < 0) replant_chance = 0;

        if (replant_chance != 1 && replant_chance != 0) random = new Random();


        put(GroundCheck.checkTreeGround(), Material.OAK_SAPLING, Material.BIRCH_SAPLING, Material.ACACIA_SAPLING);

        put(GroundCheck.checkTreeGround(), GroundCheck.checkLargeTreeGround(), Material.SPRUCE_SAPLING, Material.JUNGLE_SAPLING, Material.DARK_OAK_SAPLING);

        put(GroundCheck.checkMangroveGround(), Material.MANGROVE_PROPAGULE);

        put(GroundCheck.checkBambooGround(), Material.BAMBOO_SAPLING, Material.BAMBOO);

        put(GroundCheck.checkSugarGround(), Material.SUGAR_CANE);

        put(GroundCheck.checkCactusGround(), Material.CACTUS);

        put(GroundCheck.checkFlowerGround(), Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE);

        put(GroundCheck.checkFlowerGround(), Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY);

        put(GroundCheck.checkFlowerGround(), Material.FERN, Material.LARGE_FERN, Material.SHORT_GRASS, Material.TALL_GRASS);

        put(GroundCheck.checkMushroomGround(), Material.RED_MUSHROOM, Material.BROWN_MUSHROOM);

        put(GroundCheck.checkNetherGround(), Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS, Material.CRIMSON_ROOTS, Material.WARPED_ROOTS);

        put(GroundCheck.checkCoralGround(), Material.DEAD_BRAIN_CORAL_FAN, Material.DEAD_BUBBLE_CORAL_FAN, Material.DEAD_FIRE_CORAL_FAN, Material.DEAD_HORN_CORAL_FAN, Material.DEAD_TUBE_CORAL_FAN);

        put(GroundCheck.checkBelow(Material.FARMLAND), Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.POTATO, Material.CARROT);

        put(GroundCheck.checkBelow(Material.GRASS_BLOCK, Material.FARMLAND), Material.PUMPKIN_SEEDS, Material.MELON_SEEDS);

        put(GroundCheck.checkBelow(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL), Material.SWEET_BERRIES);

        replaces.put(Material.BAMBOO, Material.BAMBOO_SAPLING);
        replaces.put(Material.SWEET_BERRIES, Material.SWEET_BERRY_BUSH);

        getServer().getPluginManager().registerEvents(this, this);
    }

    private void put(GroundCheck check, Material... types) {
        put(check, null, types);
    }

    private void put(GroundCheck check, GroundCheck largeCheck, Material... types) {
        for (Material type : types) {
            String key = type.getKey().getKey();
            if (config.getBoolean("replant." + key, true)) {
                getLogger().config("Replanting " + key + " enabled");
                saplings.put(type, new SaplingData(type, largeCheck!=null, largeCheck, check));
            } else getLogger().config("Replanting " + key + " disabled");
        }
    }
    @Override
    public void onDisable() {
        saplings.clear();
        replaces.clear();
    }

    private boolean random() {
        if (replant_chance==0) return false;
        if (replant_chance==1) return true;
        return random.nextDouble() < replant_chance;
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        Material mat = event.getEntity().getItemStack().getType();
        GroundCheck check;
        if ((check = saplings.get(mat))!=null) {
            if (check.check(event.getLocation()) && random()) {
                Location location = event.getLocation();
                mat = replaces.getOrDefault(mat, mat);
                getLogger().finest(String.format("replant %s at %s {x=%s, y=%s, z=%s}", mat.getKey().getKey(), location.getWorld(), location.getX(), location.getX(), location.getZ()));
                location.getBlock().setType(mat);
            }
        }
    }
}
