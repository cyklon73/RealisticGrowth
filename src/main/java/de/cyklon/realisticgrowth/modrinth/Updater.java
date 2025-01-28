package de.cyklon.realisticgrowth.modrinth;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.cyklon.realisticgrowth.RealisticGrowth;
import de.cyklon.realisticgrowth.util.ColorUtil;
import de.cyklon.realisticgrowth.util.MinecraftVersion;
import de.cyklon.realisticgrowth.util.Permission;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.cyklon.realisticgrowth.RealisticGrowth.PREFIX;
import static net.md_5.bungee.api.ChatColor.*;


public class Updater {

	private static final String RESOURCE_ID = "realistic-growth";
	private static final String API_URL = "https://api.modrinth.com/v2/";
	private static final String RESOURCE_URL = "https://modrinth.com/plugin/realistic-growth";

	private final HttpClient client;
	private final Gson gson;
	private final RealisticGrowth plugin;
	private final Logger log;
	private final File updateFolder;

	public Updater(RealisticGrowth plugin) {
		this.client = HttpClient.newHttpClient();
		this.gson = new Gson();
		this.plugin = plugin;
		this.log = plugin.getLogger();
		this.updateFolder = plugin.getServer().getUpdateFolderFile();
	}

	private JsonElement request(String endpoint) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(API_URL + "project/" + RESOURCE_ID + endpoint))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200)
			throw new IOException("Request was not successfully: (" + response.statusCode() + ") " + response.body());
		return gson.fromJson(response.body(), JsonElement.class);
	}

	public List<Version> getVersions() {
		JsonElement response;
		try {
			response = request("/version");
		} catch (IOException | InterruptedException e) {
			return Collections.emptyList();
		}
		List<Version> result = new LinkedList<>();
		for (JsonElement jsonElement : response.getAsJsonArray()) {
			result.add(gson.fromJson(jsonElement, Version.class));
		}
		result.sort((o1, o2) -> MinecraftVersion.compareVersions(o1.getVersion_number(), o2.getVersion_number()));
		return result;
	}


	public boolean hasUpdate(Version version) {
		String currVersion = plugin.getDescription().getVersion().split("-")[0];
		String newVersion = version.getVersion_number().split("-")[0];
		return MinecraftVersion.compareVersions(currVersion, newVersion) < 0;
	}

	public void check(CommandSender sender) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			Version newVersion = getVersions().getLast();
			if (hasUpdate(newVersion)) Bukkit.getScheduler().runTask(plugin, () -> notify(newVersion, sender));
		});
	}

	public void notify(Version version, CommandSender sender) {
		String currentVersion = plugin.getDescription().getVersion();

		String msg = "";
		if (plugin.isCompatibilityMode() || sender==null) {
			msg = """
						Update available!
						%sCurrent Version: %s %s %s
						%sNew Version: %s %s %s
						%sUpdate using %s/realistic-growth update%s
						%sOr Update Manually by downloading the new Version on %s[Modrinth] %s
						%s
						""".formatted(
					ChatColor.GOLD, ChatColor.RED, currentVersion, ChatColor.RESET,
					ChatColor.GOLD, ChatColor.GREEN, version, ChatColor.RESET,
					ChatColor.GREEN, ChatColor.AQUA, ChatColor.RESET,
					ChatColor.GREEN, ChatColor.WHITE, ChatColor.RESET,
					RESOURCE_URL
			);
		}

		if (plugin.isCompatibilityMode()) {
			if (sender==null) sendMessage(msg);
			else sendMessage(sender, msg);
		} else {

			BaseComponent[] hoverComponent = new ComponentBuilder("Realistic ").color(GREEN)
					.append("Growth").color(AQUA)
					.append(" | ").color(GRAY)
					.append("Modrinth").color(GREEN)
					.create();

			ComponentBuilder builder = new ComponentBuilder("");
			if (PREFIX.legacyRequired()) builder.appendLegacy(PREFIX.getLegacy());
			else builder.append(PREFIX.getComponents());
			builder.append(" Update available!\n");

			if (PREFIX.legacyRequired()) builder.appendLegacy(PREFIX.getLegacy());
			else builder.append(PREFIX.getComponents());
			builder.append(" Current Version: ").color(GOLD)
			.append(currentVersion).color(RED)

			.append("\n");
			if (PREFIX.legacyRequired()) builder.appendLegacy(PREFIX.getLegacy());
			else builder.append(PREFIX.getComponents());
			builder.append(" New Version: ").color(GOLD)
			.append(version.getVersion_number()).color(GREEN)

			.append("\n");
			if (PREFIX.legacyRequired()) builder.appendLegacy(PREFIX.getLegacy());
			else builder.append(PREFIX.getComponents());
			builder.append(" ")
			.append("Update Automatically by Pressing")
			.reset()
			.color(GREEN)
			.event(PREFIX.legacyRequired() ? new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtil.legacyGradient("Auto Update", ChatColor.AQUA, ChatColor.GREEN))) : new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.gradient("Auto Update", AQUA, GREEN))))
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/realistic-growth update"))

			.append("\n")
			.reset();
			if (PREFIX.legacyRequired()) builder.appendLegacy(PREFIX.getLegacy());
			else builder.append(PREFIX.getComponents());
			builder.append(" Or Update Manually by downloading the new Version on ")
			.reset()
			.color(GREEN)

			.append("[Modrinth]")
			.color(WHITE)
			.event(PREFIX.legacyRequired() ? new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent) : new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverComponent)))
			.event(new ClickEvent(ClickEvent.Action.OPEN_URL, RESOURCE_URL))
			.create();

			BaseComponent[] components = builder.create();

			if (sender==null) {
				sendMessage(Bukkit.getConsoleSender(), msg);
				forAllowed(s -> s.spigot().sendMessage(components), false);
			} else sender.spigot().sendMessage(components);
		}
	}

	private void sendMessage(String msg) {
		sendMessage(msg, true);
	}

	private void sendMessage(String msg, boolean includeConsole) {
		sendMessage(null, msg, includeConsole);
	}

	public void sendMessage(CommandSender sender, String msg) {
		sendMessage(sender, msg, true);
	}

	private void sendMessage(CommandSender sender, String msg, boolean includeConsole) {
		sendMessage(sender, msg.split("\n"), includeConsole);
	}

	private void sendMessage(CommandSender sender, String[] msg, boolean includeConsole) {
		if (sender == null) forAllowed(s -> sendMessage(s, msg, includeConsole), includeConsole);
		else {
			for (int i = 0; i < msg.length; i++) msg[i] = PREFIX + " " + msg[i];
			sender.sendMessage(msg);
		}
	}

	private void forAllowed(Consumer<CommandSender> consumer, boolean includeConsole) {
		if (includeConsole) consumer.accept(Bukkit.getConsoleSender());
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (p.hasPermission(Permission.UPDATE)) consumer.accept(p);
		});

	}

	public boolean download(Version version) {
		if (!updateFolder.exists()) updateFolder.mkdirs();

		BufferedInputStream in = null;
		FileOutputStream fout = null;

		try
		{
			URL url = new URL(version.getFiles()[0].getUrl());
			in = new BufferedInputStream(url.openStream());
			fout = new FileOutputStream(new File(updateFolder, plugin.getDescription().getName() + "-" + version.getVersion_number() + ".jar"));

			final byte[] data = new byte[4096];
			int count;
			while ((count = in.read(data, 0, 4096)) != -1) {
				fout.write(data, 0, count);
			}
			return true;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Updater cant download the update.", e);
			return false;
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				this.plugin.getLogger().log(Level.SEVERE, null, e);
				e.printStackTrace();
			}
			try {
				if (fout != null) {
					fout.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
				this.plugin.getLogger().log(Level.SEVERE, null, e);
			}
		}
	}
}
