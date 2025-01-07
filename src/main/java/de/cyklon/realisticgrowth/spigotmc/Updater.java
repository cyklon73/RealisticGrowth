package de.cyklon.realisticgrowth.spigotmc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cyklon.realisticgrowth.Permission;
import de.cyklon.realisticgrowth.RealisticGrowth;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
	private final File jarFile;
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

	public Updater(RealisticGrowth plugin, File file)
	{
		this.plugin = plugin;
		this.updateFolder = plugin.getServer().getUpdateFolderFile();
		this.jarFile = file;
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
			else if(jsonArray.isEmpty())
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

	public boolean download()
	{
		BufferedInputStream in = null;
		FileOutputStream fout = null;

		try
		{
			URL url = new URL(downloadUrl);
			in = new BufferedInputStream(url.openStream());
			fout = new FileOutputStream(new File(updateFolder, jarFile.getName()));

			final byte[] data = new byte[4096];
			int count;
			while ((count = in.read(data, 0, 4096)) != -1) {
				fout.write(data, 0, count);
			}
			return true;
		}
		catch (Exception e)
		{
			log.severe("Updater cant download the update.");
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
					if (plugin.isCompatibilityMode()) {
						String msg = """
                            %s Update available!
                            %s %sCurrent Version: %s %s %s
                            %s %sNew Version: %s %s %s
                            %s Update using %s/realistic-growth update%s
                            %s Or Update Manually by downloading the new Version on %s[SpigotMC] %s
                            %s %s
                            """.formatted(
								PREFIX,
								PREFIX, ChatColor.GOLD, ChatColor.RED, currentVersion, ChatColor.RESET,
								PREFIX, ChatColor.GOLD, ChatColor.GREEN, version, ChatColor.RESET,
								PREFIX, AQUA, RESET,
								PREFIX, ChatColor.YELLOW, ChatColor.RESET,
								PREFIX, downloadUrl
						);

						if (sender==null) Bukkit.broadcast(msg, Permission.UPDATE.getName());
						else sender.sendMessage(msg);
					} else {
						BaseComponent[] components = new ComponentBuilder(PREFIX + " Update available!\n")
								.append(PREFIX + " Current Version: ").color(GOLD)
								.append(currentVersion).color(RED)
								.append("\n" + PREFIX + " New Version: ").color(GOLD)
								.append(version).color(GREEN)
								.append("\n" + PREFIX + "Update Automatically by Pressing")
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Auto Update")))
								.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/realistic-growth update"))
								.append("\n" + PREFIX + " Or Update Manually by downloading the new Version on ")
								.append("[SpigotMC]").color(YELLOW)
								.event(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl))
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Realistic ").color(GREEN)
										.append("Growth").color(AQUA)
										.append(" | ").color(GRAY)
										.append("SpigotMC").color(GOLD)
										.create())))
								.create();

						if (sender==null) {
							Bukkit.getConsoleSender().spigot().sendMessage(components);
							Bukkit.getOnlinePlayers().forEach(p -> {
								if (p.hasPermission(Permission.UPDATE)) p.spigot().sendMessage(components);
							});
						} else sender.spigot().sendMessage(components);
					}
				}
			}
		}
	}
}
