package de.cyklon.realisticgrowth.spigotmc;

import de.cyklon.realisticgrowth.RealisticGrowth;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheck {

	public static final int RESOURCE_ID = 121462;

	public static final String API_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID;

	public static final String DOWNLOAD_URL = "https://www.spigotmc.org/resources/realistic-growth.121462/";


	public static void getVersion(Plugin plugin, Consumer<String> consumer) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try (InputStream in = new URL(API_URL + "/~").openStream(); Scanner reader = new Scanner(in)) {
				if (reader.hasNext()) consumer.accept(reader.next());
			} catch (IOException e) {
				plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
			}
		});
	}

	public static void checkUpdate(Plugin plugin) {
		getVersion(plugin, version -> {
			String currentVersion = plugin.getDescription().getVersion();
			if (!version.equals(currentVersion)) {
				BaseComponent[] components = new ComponentBuilder(RealisticGrowth.PREFIX + " Update available!\n")
						.append("Current Version: ").color(net.md_5.bungee.api.ChatColor.GOLD)
								.append(currentVersion).color(net.md_5.bungee.api.ChatColor.RED)
								.append("New Version: ").color(net.md_5.bungee.api.ChatColor.GOLD)
								.append(version).color(net.md_5.bungee.api.ChatColor.GREEN)
								.append("\nNew Version available on ").color(net.md_5.bungee.api.ChatColor.RESET)
								.append("[SpigotMC]").color(net.md_5.bungee.api.ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.OPEN_URL, DOWNLOAD_URL)).create();

				Bukkit.getOnlinePlayers().forEach(p -> {
					if (p.hasPermission("update-msg")) p.spigot().sendMessage(components);
				});
			}
		});
	}

}
