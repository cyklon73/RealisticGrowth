package de.cyklon.realisticgrowth.spigotmc;

import de.cyklon.realisticgrowth.RealisticGrowth;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
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
			try (InputStream in = new URL(API_URL).openStream(); Scanner scanner = new Scanner(in)) {
				if (scanner.hasNext()) consumer.accept(scanner.next());
			} catch (IOException e) {
				plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
			}
		});
	}

	public static void checkUpdate(Plugin plugin) {
		getVersion(plugin, version -> {
			String currentVersion = plugin.getDescription().getVersion();
			if (toVersionInt(version) > toVersionInt(currentVersion)) {
				BaseComponent[] components = new ComponentBuilder(RealisticGrowth.PREFIX + " Update available!\n")
						.append(RealisticGrowth.PREFIX + " Current Version: ").color(ChatColor.GOLD)
								.append(currentVersion).color(ChatColor.RED)
								.append("\n" + RealisticGrowth.PREFIX + " New Version: ").color(ChatColor.GOLD)
								.append(version).color(ChatColor.GREEN)
								.append("\n" + RealisticGrowth.PREFIX + " New Version available on ")
								.append("[SpigotMC]").color(ChatColor.YELLOW)
						.event(new ClickEvent(ClickEvent.Action.OPEN_URL, DOWNLOAD_URL))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Realistic ").color(ChatColor.GREEN)
								.append("Growth").color(ChatColor.AQUA)
								.append(" | ").color(ChatColor.GRAY)
								.append("SpigotMC").color(ChatColor.GOLD)
								.create())))
						.create();

				Bukkit.getConsoleSender().spigot().sendMessage(components);
				Bukkit.getOnlinePlayers().forEach(p -> {
					if (p.hasPermission("rg.update-msg")) p.spigot().sendMessage(components);
				});
			}
		});
	}

	private static int toVersionInt(String version) {
		return Integer.parseInt(version.replace(".", "").split("-")[0]);
	}
}
