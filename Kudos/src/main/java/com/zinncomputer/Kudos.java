package com.zinncomputer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// import net.gravitydevelopment.updater.Updater;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Kudos extends JavaPlugin {

	public static Plugin instance;

	protected static HashMap<String, Integer> pkudos = new HashMap<String, Integer>();
	protected static List<Player> cooldowns = new ArrayList<Player>();

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
//		if (getConfig().getBoolean("auto-update")) {
//			Updater updater = new Updater(this, id, this.getFile(), Updater.UpdateType.DEFAULT, true);
//		}
		getCommand("kudos").setExecutor(this);
		loadKudos();
	}

	@Override
	public void onDisable() {
		saveKudos();
	}

	private void loadKudos() {
		File f = new File(getDataFolder(), "kudos.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				getLogger()
						.severe("Unable to create the kudos.yml file! Please report the stack trace between the lines to AniSkywalker.");
				getLogger().severe("--------------");
				e.printStackTrace();
				getLogger().severe("--------------");
			}
		} else {
			FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
			for (String s : fc.getKeys(false)) {
				pkudos.put(s, fc.getInt(s));
			}
		}
	}

	private void saveKudos() {
		File f = new File(getDataFolder(), "kudos.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				getLogger()
						.severe("Unable to create the kudos.yml file! Please report the stack trace between the lines to AniSkywalker. It might be a file permissions issue.");
				getLogger().severe("--------------");
				e.printStackTrace();
				getLogger().severe("--------------");
			}
		} else {
			FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
			for (String s : pkudos.keySet()) {
				fc.set(s, pkudos.get(s));
			}
			try {
				fc.save(f);
			} catch (IOException e) {
				getLogger()
						.severe("Unable to save to the kudos.yml file! Please report the stack trace between the lines to AniSkywalker. It might be a file permissions issue.");
				getLogger().severe("--------------");
				e.printStackTrace();
				getLogger().severe("--------------");
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (label.equalsIgnoreCase("kudos")) {
			if (sender instanceof Player) {
				if (args.length == 1) {
					@SuppressWarnings("deprecation")
					OfflinePlayer t = Bukkit.getOfflinePlayer(args[0]);
					if (t == null) {
						sender.sendMessage(ChatColor.RED + args[0]
								+ " has not played on this server before.");
					} else {
						if (pkudos.containsKey(t.getUniqueId().toString())) {
							sender.sendMessage(ChatColor.GREEN + args[0]
									+ " has "
									+ pkudos.get(t.getUniqueId().toString())
									+ " kudos.");
						} else {
							sender.sendMessage(ChatColor.RED + args[0]
									+ " has no kudos yet!");
						}
					}
				} else {
					openKudosGUI((Player) sender);
				}
			} else {
				sender.sendMessage(ChatColor.RED
						+ "Only players can use this command!");
			}

		}
		return true;
	}

	private void openKudosGUI(final Player p) {
		IconMenu im = new IconMenu(ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("gui-name")
						.replace("%pname", p.getName())),
				toNineDenom(getServer().getOnlinePlayers().size()),
				new IconMenu.OptionClickEventHandler() {
					@SuppressWarnings("deprecation")
					public void onOptionClick(IconMenu.OptionClickEvent event) {
						if (event.getName().equals(event.getPlayer().getName())) {
							p.sendMessage(ChatColor.RED
									+ "You can't give yourself kudos!");
						} else {
							if (!cooldowns.contains(p)) {
								if (Bukkit.getPlayer(event.getName()) != null) {
									Player t = Bukkit.getPlayer(event.getName());
									if (pkudos.containsKey(t.getUniqueId()
											.toString())) {
										pkudos.put(t.getUniqueId().toString(),
												pkudos.get(pkudos.get(t
														.getUniqueId()
														.toString())));
									} else {
										pkudos.put(t.getUniqueId().toString(),
												1);
									}
									cooldowns.add(p);
									BukkitScheduler scheduler = Bukkit
											.getServer().getScheduler();
									scheduler.scheduleSyncDelayedTask(
											instance,
											new Runnable() {
												public void run() {
													cooldowns.remove(p);
												}
											},
											(getConfig().getInt(
													"cooldown-delay") + 20));
								} else {
									// Player isn't on!
									p.sendMessage(ChatColor.RED
											+ event.getName()
											+ " is no longer online!");
								}
							} else {
								p.sendMessage(ChatColor.RED
										+ "You're still on cooldown! Please wait a bit and "
										+ ChatColor.BLUE + "cool off"
										+ ChatColor.RED + ".");
							}
						}
						event.setWillClose(true);
					}
				}, this);
		int i = 0;
		for (Player h : Bukkit.getServer().getOnlinePlayers()) {
			ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
			SkullMeta sm = (SkullMeta) head.getItemMeta();
			sm.setOwner(h.getName());
			head.setItemMeta(sm);
			im = im.setOption(i, head, h.getName(), ChatColor.GREEN
					+ "+1 Kudos :)");
			i++;
		}
		im.open(p);
	}

	protected static int toNineDenom(int from) {
		return ((((from / 9) + ((from % 9 == 0) ? 0 : 1)) * 9));
	}

}
