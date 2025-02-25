package de.cyklon.realisticgrowth.util;

import de.cyklon.realisticgrowth.RealisticGrowth;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Scheduler {

    private final boolean folia;

    public Scheduler() {
        this.folia = RealisticGrowth.isFolia();
    }

    public void runTask(Plugin plugin, Runnable runnable) {
        if (folia) Bukkit.getGlobalRegionScheduler().run(plugin, t -> runnable.run());
        else Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void runTaskAsync(Plugin plugin, Runnable runnable) {
        if (folia) Bukkit.getAsyncScheduler().runNow(plugin, t -> runnable.run());
        else Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
