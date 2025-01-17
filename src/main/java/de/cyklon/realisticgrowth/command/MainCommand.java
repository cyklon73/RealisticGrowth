package de.cyklon.realisticgrowth.command;

import de.cyklon.realisticgrowth.RealisticGrowth;
import de.cyklon.realisticgrowth.util.ColorUtil;
import de.cyklon.realisticgrowth.spigotmc.Updater;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.awt.*;
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
				sender.sendMessage("%s %sUpdating%s"
						.formatted(PREFIX, ChatColor.GREEN, ColorUtil.legacyGradient("...", new Color(0x55FF7D), new Color(0x3CEEFF))));
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					boolean downloaded = updater.download();
					Bukkit.getScheduler().runTask(plugin, () -> {
						if (downloaded) {
							sender.sendMessage(PREFIX.getLegacy() + ChatColor.GREEN + " Update Successfully");

							if (plugin.isCompatibilityMode() || sender instanceof ConsoleCommandSender) {
								updater.sendMessage(sender, """
								%sTo apply the changes reload the server with %s/reload %sconfirm
								%sIf the update is not active after reloading, please try to update manually.
								""".formatted(ChatColor.GREEN, ChatColor.AQUA, ChatColor.YELLOW, ChatColor.GREEN));
							} else {
								sender.spigot().sendMessage(new ComponentBuilder()
										.append(PREFIX.getComponents())
										.append(" To apply the changes ")
										.color(GREEN)

										.append("reload")
										.reset()
										.color(YELLOW)
										.bold(true)
										.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.gradient("Reload now", YELLOW, GREEN))))
										.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload confirm"))

										.append(" the server")
										.reset()
										.color(GREEN)

										.append("\n")

										.append(PREFIX.getComponents())

										.append(" If the update is not active after reloading, please try to update manually.")
										.reset()
										.color(GREEN)

										.create()
								);
							}
						}
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
								sender.spigot().sendMessage(new ComponentBuilder()
										.append(PREFIX.getComponents())
										.append(" Update failed.\n")
										.color(RED)
										.append(PREFIX.getComponents())
										.append(" Please update manually by downloading the file from ")
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
					});
				});
			}
			else sender.sendMessage(ChatColor.RED + "No update available");
			return true;
		}
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
