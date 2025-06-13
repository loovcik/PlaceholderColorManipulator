package me.loovcik.pcm;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.loovcik.core.ChatHelper;

public class PlaceholderAPI
{private PlaceholderAPIHook hook;

	public boolean isEnabled() { return hook != null; }

	public String process(OfflinePlayer op, String input){
		if (isEnabled()) return hook.process(op, input);
		return input;
	}

	public String process(String input){
		return process(null, input);
	}

	public void register(){
		if (isEnabled()) hook.placeholders.register();
	}

	public void unregister(){
		if (isEnabled()) hook.placeholders.unregister();
	}

	public PlaceholderAPI(PlaceholderColorManipulator plugin){
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			hook = new PlaceholderAPIHook(plugin);
			ChatHelper.console("PlaceholderAPI support: <green>Yes</green> ("+ Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + ")");
		}
		else ChatHelper.console("PlaceholderAPI support: <red>No</red>");
	}
}

class PlaceholderAPIHook {
	public final Placeholders placeholders;
	public String getVersion(){
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion();
	}

	public String process(OfflinePlayer op, String input){
		return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(op, input);
	}

	public PlaceholderAPIHook(PlaceholderColorManipulator plugin){
		placeholders = new Placeholders(plugin);
	}
}