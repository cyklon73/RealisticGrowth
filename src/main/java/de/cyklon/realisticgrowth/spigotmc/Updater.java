package de.cyklon.realisticgrowth.spigotmc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cyklon.realisticgrowth.util.Permission;
import de.cyklon.realisticgrowth.RealisticGrowth;
import de.cyklon.realisticgrowth.util.ColorUtil;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.cyklon.realisticgrowth.RealisticGrowth.PREFIX;
import static net.md_5.bungee.api.ChatColor.*;


public class Updater {

	private static final int RESOURCE_ID = 121462;
	private static final String USER_AGENT = "Realistic Growth Updater";

	private String downloadUrl;
	private final RealisticGrowth plugin;
	private final File updateFolder;
	private int page = 1;
	private boolean emptyPage;
	private String version;
	private final Logger log;

	private boolean checked = false;
	private boolean manualCheck = false;
	private boolean shouldUpdate = false;
	private CommandSender sender = null;

	private static final String DOWNLOAD = "/download";
	private static final String VERSIONS = "/versions";
	private static final String PAGE = "?page=";
	private static final String API_RESOURCE = "https://api.spiget.org/v2/resources/";
	private static final String RESOURCE_URL = "https://www.spigotmc.org/resources/realistic-growth.121462/";

	public Updater(RealisticGrowth plugin)
	{
		this.plugin = plugin;
		this.updateFolder = plugin.getServer().getUpdateFolderFile();
		this.log = plugin.getLogger();

		downloadUrl = API_RESOURCE + RESOURCE_ID;
	}

	public boolean hasChecked() {
		return checked;
	}

	public void check() {
		check(false);
	}

	public void check(boolean manualCheck) {
		this.manualCheck = manualCheck;
		this.sender = null;
		checked = true;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new UpdaterRunnable());
	}

	public void check(CommandSender sender) {
		check(false);
		this.sender = sender;
	}

	public boolean shouldUpdate() {
		return shouldUpdate;
	}

	private boolean checkResource(String link)
	{
		try
		{
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", USER_AGENT);

			int code = connection.getResponseCode();

			if(code != 200)
			{
				connection.disconnect();
				return false;
			}
			connection.disconnect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	private boolean checkUpdate()
	{
		try
		{
			String page = Integer.toString(this.page);

			URL url = new URL(API_RESOURCE + RESOURCE_ID + VERSIONS + PAGE + page);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", USER_AGENT);

			InputStream inputStream = connection.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream);

			JsonElement element = new JsonParser().parse(reader);
			JsonArray jsonArray = element.getAsJsonArray();

			if(jsonArray.size() == 10 && !emptyPage)
			{
				connection.disconnect();
				this.page++;
				return checkUpdate();
			}
			else if(jsonArray.size()==0)
			{
				emptyPage = true;
				this.page--;
				return checkUpdate();
			}
			else if(jsonArray.size() < 10)
			{
				element = jsonArray.get(jsonArray.size()-1);

				JsonObject object = element.getAsJsonObject();
				element = object.get("name");
				version = element.toString().replaceAll("\"", "").replace("v","");
				log.info("Checking for update...");
				if(shouldUpdate(version, plugin.getDescription().getVersion()))
				{
					shouldUpdate = true;
					log.info("Update found!");
					return true;
				}
				else
				{
					log.info("Update not found");
					return false;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private boolean shouldUpdate(String newVersion, String oldVersion)
	{
		return !newVersion.equalsIgnoreCase(oldVersion);
	}

	public boolean download() {
		if (!updateFolder.exists()) updateFolder.mkdirs();

		BufferedInputStream in = null;
		FileOutputStream fout = null;

		try
		{
			URL url = new URL(downloadUrl);
			in = new BufferedInputStream(url.openStream());
			fout = new FileOutputStream(new File(updateFolder, plugin.getDescription().getName() + "-" + version + ".jar"));

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

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public class UpdaterRunnable implements Runnable
	{

		public void run() {
			if(checkResource(downloadUrl))
			{
				downloadUrl = downloadUrl + DOWNLOAD;
				if (checkUpdate() && !manualCheck) {
					String currentVersion = plugin.getDescription().getVersion();
					log.info("Update available");

					String msg = "";
					if (plugin.isCompatibilityMode() || sender==null) {
						msg = """
                            Update available!
                            %sCurrent Version: %s %s %s
                            %sNew Version: %s %s %s
                            %sUpdate using %s/realistic-growth update%s
                            %sOr Update Manually by downloading the new Version on %s[SpigotMC] %s
                            %s
                            """.formatted(
								ChatColor.GOLD, ChatColor.RED, currentVersion, ChatColor.RESET,
								ChatColor.GOLD, ChatColor.GREEN, version, ChatColor.RESET,
								ChatColor.GREEN, ChatColor.AQUA, ChatColor.RESET,
								ChatColor.GREEN, ChatColor.YELLOW, ChatColor.RESET,
								RESOURCE_URL
						);
					}

					if (plugin.isCompatibilityMode()) {
						if (sender==null) sendMessage(msg);
						else sendMessage(sender, msg);
					} else {

						BaseComponent[] components = new ComponentBuilder()
								.append(PREFIX.getComponents())
								.append(" Update available!\n")

								.append(PREFIX.getComponents())
								.append(" Current Version: ").color(GOLD)
								.append(currentVersion).color(RED)

								.append("\n")
								.append(PREFIX.getComponents())
								.append(" New Version: ").color(GOLD)
								.append(version).color(GREEN)

								.append("\n")
								.append(PREFIX.getComponents())
								.append(" ")
								.append("Update Automatically by Pressing")
								.reset()
								.color(GREEN)
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.gradient("Auto Update", AQUA, GREEN))))
								.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/realistic-growth update"))

								.append("\n")
								.reset()
								.append(PREFIX.getComponents())
								.append(" Or Update Manually by downloading the new Version on ")
								.reset()
								.color(GREEN)

								.append("[SpigotMC]")
								.color(YELLOW)
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Realistic ").color(GREEN)
										.append("Growth").color(AQUA)
										.append(" | ").color(GRAY)
										.append("SpigotMC").color(GOLD)
										.create())))
								.event(new ClickEvent(ClickEvent.Action.OPEN_URL, RESOURCE_URL))
								.create();

						if (sender==null) {
							sendMessage(Bukkit.getConsoleSender(), msg);
							forAllowed(s -> s.spigot().sendMessage(components), false);
						} else sender.spigot().sendMessage(components);
					}
				}
			}
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
}
