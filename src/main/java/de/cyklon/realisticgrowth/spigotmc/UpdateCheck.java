package de.cyklon.realisticgrowth.spigotmc;

import de.cyklon.realisticgrowth.RealisticGrowth;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static net.md_5.bungee.api.ChatColor.*;
import static de.cyklon.realisticgrowth.RealisticGrowth.PREFIX;

@Deprecated
public class UpdateCheck {

	public static final int RESOURCE_ID = 121462;

	public static final String API_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID;

	public static final String DOWNLOAD_URL = "https://www.spigotmc.org/resources/realistic-growth.%s/".formatted(RESOURCE_ID);


	public static void getVersion(RealisticGrowth plugin, Consumer<String> consumer) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try (InputStream in = new URL(API_URL).openStream(); Scanner scanner = new Scanner(in)) {
				if (scanner.hasNext()) consumer.accept(scanner.next());
			} catch (IOException e) {
				plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
			}
		});
	}

	public static void checkUpdate(RealisticGrowth plugin) {
		Logger log = plugin.getLogger();
		log.info("Check for update");
		getVersion(plugin, version -> {
			String currentVersion = plugin.getDescription().getVersion();
			if (toVersionInt(version) > toVersionInt(currentVersion)) {
				log.info("Update available");
				if (plugin.isCompatibilityMode()) {
					Bukkit.broadcast("""
                            %s Update available!
                            %s %sCurrent Version: %s %s %s
                            %s %sNew Version: %s %s %s
                            %s New Version available on %s[SpigotMC] %s
                            %s %s
                            """.formatted(
							PREFIX,
							PREFIX, ChatColor.GOLD, ChatColor.RED, currentVersion, ChatColor.RESET,
							PREFIX, ChatColor.GOLD, ChatColor.GREEN, version, ChatColor.RESET,
							PREFIX, ChatColor.YELLOW, ChatColor.RESET,
							PREFIX, DOWNLOAD_URL
					), "rg.update-msg");
				} else {
					BaseComponent[] components = new ComponentBuilder(PREFIX + " Update available!\n")
							.append(PREFIX + " Current Version: ").color(GOLD)
							.append(currentVersion).color(RED)
							.append("\n" + PREFIX + " New Version: ").color(GOLD)
							.append(version).color(GREEN)
							.append("\n" + PREFIX + " New Version available on ")
							.append("[SpigotMC]").color(YELLOW)
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, DOWNLOAD_URL))
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Realistic ").color(GREEN)
									.append("Growth").color(AQUA)
									.append(" | ").color(GRAY)
									.append("SpigotMC").color(GOLD)
									.create())))
							.create();

					Bukkit.getConsoleSender().spigot().sendMessage(components);
					Bukkit.getOnlinePlayers().forEach(p -> {
						if (p.hasPermission("rg.update-msg")) p.spigot().sendMessage(components);
					});
				}
			} else log.info("Plugin is up to date");
		});
	}

	private static int toVersionInt(String version) {
		version = version.split("-")[0];
		if ((version.length() - (version = version.replace(".", "")).length()) <= 2) version += "0";
		return Integer.parseInt(version);
	}
}
