package de.cyklon.realisticgrowth.command;

import de.cyklon.realisticgrowth.RealisticGrowth;
import de.cyklon.realisticgrowth.spigotmc.Updater;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import static de.cyklon.realisticgrowth.RealisticGrowth.PREFIX;
import static net.md_5.bungee.api.ChatColor.*;

public class MainCommand implements CommandExecutor, TabCompleter {

	private final RealisticGrowth plugin;
	private final Updater updater;

	public MainCommand(RealisticGrowth plugin, Updater updater) {
		this.plugin = plugin;
		this.updater = updater;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("update")) {
			if (!updater.hasChecked()) updater.check(true);
			if (updater.shouldUpdate()) {
				if (updater.download()) sender.sendMessage(PREFIX + ChatColor.GREEN + "Update Successfully");
				else {
					if (plugin.isCompatibilityMode()) {
						sender.sendMessage("""
                            %s %s Update failed. Please update manually by downloading the file from %s[SpigotMC] %s
                            %s %s
                            """.formatted(
								PREFIX, ChatColor.RESET, ChatColor.YELLOW, ChatColor.RESET,
								PREFIX, updater.getDownloadUrl()
						));
					} else {
						sender.spigot().sendMessage(new ComponentBuilder(PREFIX + " Update failed.\n").color(RED)
								.append(PREFIX + " Please update manually by downloading the file from ")
								.append("[SpigotMC]").color(YELLOW)
										.event(new ClickEvent(ClickEvent.Action.OPEN_URL, updater.getDownloadUrl()))
										.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Realistic ").color(GREEN)
												.append("Growth").color(AQUA)
												.append(" | ").color(GRAY)
												.append("SpigotMC").color(GOLD)
												.create())))
								.create());
					}
					sender.sendMessage(ChatColor.RED + "Update failed. Please update manually");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "No update available");
				return false;
			}
			return true;
		}

		sender.sendMessage(ChatColor.RED + "Usage: /realistic-growth update");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> list = new ArrayList<>();

		if (args.length == 1) {
			list.add("update");
		}

		list.removeIf(s -> !s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
		return list;
	}
}
