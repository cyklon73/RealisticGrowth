package de.cyklon.realisticgrowth;

import de.cyklon.realisticgrowth.command.MainCommand;
import de.cyklon.realisticgrowth.spigotmc.Updater;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public final class RealisticGrowth extends JavaPlugin implements Listener {

    private static final int ID = 24240;

    public static final String PREFIX = "%s[%sRealistic %sGrowth%s]%s".formatted(ChatColor.GOLD, ChatColor.GREEN, ChatColor.AQUA, ChatColor.GOLD, ChatColor.RESET);

    private static final String UPPER = "[half=upper]";

    private Map<Material, SaplingData> saplings;
    private Map<Material, Material> replaces;
    private Map<Material, Consumer<Location>> placeHandler;

    private Updater updater;

    private boolean compatibilityMode;

    private FileConfiguration config;

    private double replant_chance;

    private Random random;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        Metrics metrics = new Metrics(this, ID);

        this.saplings = new HashMap<>();
        this.replaces = new HashMap<>();
        this.placeHandler = new HashMap<>();

        this.config = getConfig();

        if (isSpigot()) getLogger().info("Server Running on Spigot (or Spigot fork)");
        else getLogger().info("Server Running on Bukkit");

        this.compatibilityMode = !isSpigot();

        if (config.getBoolean("force-compatibility-mode", false)) this.compatibilityMode = true;

        if (compatibilityMode) getLogger().info("Start in Compatibility mode");
        else getLogger().info("Running on Spigot, Start in normal mode");

        this.updater = new Updater(this, getFile());
        if (config.getBoolean("check-updates", true)) updater.check();

        this.replant_chance = config.getInt("replant-chance", 90)/100d;

        if (replant_chance > 1) replant_chance = 1;
        if (replant_chance < 0) replant_chance = 0;

        metrics.addCustomChart(new SimplePie("check-updates", () -> String.valueOf(config.getBoolean("check-updates"))));
        metrics.addCustomChart(new SimplePie("replant-chance", () -> String.valueOf(((int)(replant_chance*100d)))));

        if (replant_chance != 1 && replant_chance != 0) random = new Random();

        put(GroundCheck.checkTreeGround(), Material.OAK_SAPLING, Material.BIRCH_SAPLING, Material.ACACIA_SAPLING);

        put(GroundCheck.checkTreeGround(), GroundCheck.checkLargeTreeGround(), Material.SPRUCE_SAPLING, Material.JUNGLE_SAPLING, Material.DARK_OAK_SAPLING);

        putLarge(GroundCheck.checkLargeTreeGround(), Material.PALE_OAK_SAPLING);

        put(GroundCheck.checkMangroveGround(), Material.MANGROVE_PROPAGULE);

        put(GroundCheck.checkBambooGround(), Material.BAMBOO_SAPLING, Material.BAMBOO);

        put(GroundCheck.checkSugarGround(), Material.SUGAR_CANE);

        put(GroundCheck.checkCactusGround(), Material.CACTUS);

        put(GroundCheck.checkFlowerGround(), Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE);

        put(GroundCheck.checkLargeFlowerGround(), Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY);

        put(GroundCheck.checkFlowerGround(), Material.FERN, Material.SHORT_GRASS);

        put(GroundCheck.checkLargeFlowerGround(), Material.LARGE_FERN, Material.TALL_GRASS);

        put(GroundCheck.checkMushroomGround(), Material.RED_MUSHROOM, Material.BROWN_MUSHROOM);

        put(GroundCheck.checkNetherGround(), Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS, Material.CRIMSON_ROOTS, Material.WARPED_ROOTS);

        put(GroundCheck.checkCoralGround(), Material.DEAD_BRAIN_CORAL_FAN, Material.DEAD_BUBBLE_CORAL_FAN, Material.DEAD_FIRE_CORAL_FAN, Material.DEAD_HORN_CORAL_FAN, Material.DEAD_TUBE_CORAL_FAN);

        put(GroundCheck.checkBelow(Material.FARMLAND), Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.POTATO, Material.CARROT);

        put(GroundCheck.checkBelow(Material.GRASS_BLOCK, Material.FARMLAND), Material.PUMPKIN_SEEDS, Material.MELON_SEEDS);

        put(GroundCheck.checkBelow(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL), Material.SWEET_BERRIES);

        replaces.put(Material.BAMBOO, Material.BAMBOO_SAPLING);
        replaces.put(Material.SWEET_BERRIES, Material.SWEET_BERRY_BUSH);

        registerLarge(Material.TALL_GRASS);
        registerLarge(Material.LARGE_FERN);

        registerLarge(Material.SUNFLOWER);
        registerLarge(Material.LILAC);
        registerLarge(Material.ROSE_BUSH);
        registerLarge(Material.PEONY);


        getServer().getPluginManager().registerEvents(this, this);
        registerCommand("realistic-growth", new MainCommand(this, updater));
    }

    private PluginCommand registerCommand(String name, CommandExecutor executor) {
        PluginCommand pc = Bukkit.getPluginCommand(name);
        pc.setExecutor(executor);
        return pc;
    }

    private void registerLarge(Material type) {
        placeHandler.put(type, l -> {
            l.getBlock().setType(type);
            l.clone().add(0, 1, 0).getBlock().setBlockData(Bukkit.createBlockData(type, UPPER));
        });
    }

    private void put(GroundCheck check, Material... types) {
        put(check, null, types);
    }

    private void putLarge(GroundCheck largeCheck, Material... types) {
        put(null, largeCheck, types);
    }

    private void put(GroundCheck check, GroundCheck largeCheck, Material... types) {
        for (Material type : types) {
            String key = type.getKey().getKey();
            if (config.getBoolean("replant." + key, true)) {
                getLogger().config("Replanting " + key + " enabled");
                saplings.put(type, new SaplingData(type, check!=null, largeCheck!=null, largeCheck, check));
            } else getLogger().config("Replanting " + key + " disabled");
        }
    }
    @Override
    public void onDisable() {
        saplings.clear();
        replaces.clear();
        placeHandler.clear();
    }

    private boolean random() {
        if (replant_chance==0) return false;
        if (replant_chance==1) return true;
        return random.nextDouble() < replant_chance;
    }

    private void place(Location location, Material material) {
        Consumer<Location> handler = placeHandler.get(material);
        if (handler==null) location.getBlock().setType(material);
        else handler.accept(location);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();
        Material mat = stack.getType();
        Location location = event.getLocation();
        int amount = stack.getAmount();
        SaplingData data;
        if ((data = saplings.get(mat))!=null) {
            mat = replaces.getOrDefault(mat, mat);
            GroundCheck check = null;
            if (random()) {
                boolean largeCheck;
                if ((largeCheck = (data.largeTree() && (check = data.largeCheck()).check(location))) && amount >= 4) {
                    GroundCheck.LargeField field = check.getField();
                    Location loc1 = field.getLocation_1();
                    Location loc2 = field.getLocation_2();
                    Location loc3 = field.getLocation_3();
                    Location loc4 = field.getLocation_4();

                    getLogger().finest(String.format("replant large tree %s at %s { 1 = { x=%s, y=%s, z=%s }, 2 = { x=%s, y=%s, z=%s }, 3 = { x=%s, y=%s, z=%s }, 4 = { x=%s, y=%s, z=%s } }",
                            mat.getKey().getKey(), location.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ(),
                            loc3.getX(), loc3.getY(), loc3.getZ(), loc4.getX(), loc4.getY(), loc4.getZ()));

                    for (Location l : field.getLocations()) {
                        place(l, mat);
                    }
                } else if (largeCheck && (amount >= 2 || !data.normalAllowed())) {
                    GroundCheck.LargeField field = check.getField();
                    List<Location> locations = field.getLocations().subList(0, amount);

                    for (Location l : locations) {
                        place(l, mat);
                    }
                } else if(data.normalAllowed()) {
                    if (data.normalCheck().check(location)) {
                        getLogger().finest(String.format("replant %s at %s {x=%s, y=%s, z=%s}", mat.getKey().getKey(), location.getWorld(), location.getX(), location.getX(), location.getZ()));
                        place(location, mat);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (p.hasPermission(Permission.UPDATE)) updater.check(p);
    }

    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
	        return false;
        }
    }

    public static boolean isSpigot() {
        return isClassPresent("org.spigotmc.SpigotConfig");
    }

    public boolean isCompatibilityMode() {
        return compatibilityMode;
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            YamlConfiguration defaultConfig = new YamlConfiguration();
            try (InputStream in = getResource("config.yml")) {
                if (in==null) {
                    getLogger().severe("Default config not found");
                    return;
                }
                defaultConfig.load(new InputStreamReader(in));
            } catch (IOException | InvalidConfigurationException e) {
	            getLogger().severe("Could not load default config");
            }
            FileConfiguration config = getConfig();
            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) config.set(key, defaultConfig.get(key));
            }
            saveConfig();
        } else super.saveDefaultConfig();
    }
}
