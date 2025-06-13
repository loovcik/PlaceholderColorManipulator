package me.loovcik.pcm;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import me.loovcik.core.ChatHelper;
import me.loovcik.pcm.managers.ConfigManager;

public final class PlaceholderColorManipulator extends JavaPlugin
{
	public PlaceholderAPI placeholderAPI;
	public ConfigManager configManager;

	@Override
	public void onEnable()
	{
		ChatHelper.setPlugin(this);
		ChatHelper.setPrefix("&8[PCM]&r");
		configManager = new ConfigManager(this);
		configManager.load();
		placeholderAPI = new PlaceholderAPI(this);
		if (placeholderAPI.isEnabled())
			placeholderAPI.register();
	}

	@Override
	public void onDisable()
	{
		if (placeholderAPI.isEnabled())
			placeholderAPI.unregister();
	}

	public boolean isAFK(Player player){
		String placeholder = placeholderAPI.process(player, "%afkmagic_status%");
		if (!placeholder.equalsIgnoreCase("%afkmagic_status%"))
			return placeholder.equalsIgnoreCase("tak");
		placeholder = placeholderAPI.process(player, "%essentials_afk%");
		if (!placeholder.equalsIgnoreCase("%essentials_afk%"))
			return placeholder.equalsIgnoreCase("yes");
		return false;
	}

	public boolean isVanished(Player player){
		if (player == null) return false;
		for (MetadataValue meta : player.getMetadata("vanished"))
			if (meta.asBoolean()) return true;
		return false;
	}
}