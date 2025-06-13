package me.loovcik.pcm;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import me.loovcik.core.ChatHelper;
import me.loovcik.pcm.managers.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders extends PlaceholderExpansion
{
	private final PlaceholderColorManipulator plugin;

	/**
	 * Pobiera identyfikator placeholdera
	 * @return Identyfikator placeholdera
	 */
	@Override
	public @NotNull String getIdentifier()
	{
		return "pcm";
	}

	/**
	 * Pobiera autora
	 * @return Nazwa autora
	 */
	@Override
	public @NotNull String getAuthor()
	{
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	/**
	 * Pobiera wersję pluginu
	 * @return Wersja pluginu
	 */
	@Override
	public @NotNull String getVersion()
	{
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist(){
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer player, @NotNull String params){
		String[] splittedParams = params.split("_");
		if (splittedParams.length > 1) {
			if (splittedParams[0].equalsIgnoreCase("custom")){
				String customName = splittedParams[1].toLowerCase();
				if (plugin.configManager.custom.placeholders.containsKey(customName)){
					ConfigManager.CustomPlaceholder customPlaceholder = plugin.configManager.custom.placeholders.get(customName);
					String format = customPlaceholder.format;
					String placeholder;
					for (ConfigManager.Condition condition : customPlaceholder.conditions){
						if (!condition.placeholder.contains("%")) {
							if (condition.placeholder.equalsIgnoreCase("afk")){
								if (plugin.isAFK(Bukkit.getPlayer(player.getUniqueId()))) {
									format = condition.format;
									break;
								}
							}
							else if (condition.placeholder.equalsIgnoreCase("vanish")){
								if (plugin.isVanished(Bukkit.getPlayer(player.getUniqueId()))) {
									format = condition.format;
									break;
								}
							}
						}
						else {
							placeholder = plugin.placeholderAPI.process(player, condition.placeholder);
							if (!condition.yesValue.isEmpty()) {
								if (placeholder.equalsIgnoreCase(condition.yesValue)) {
									format = condition.format;
									break;
								}
							}
							else if (!condition.noValue.isEmpty())
								if (placeholder.equalsIgnoreCase(condition.noValue)) {
									format = condition.format;
									break;
								}
						}
					}

					format = plugin.placeholderAPI.process(player, format);
					return format;
				}
			}
			else {
				String method = "format";
				String color = "";
				ColorManipulator.ColorFormat destinationFormat = ColorManipulator.ColorFormat.MINI_MESSAGE;
				String placeholderName = "";

				if (splittedParams[0].equalsIgnoreCase("colorize"))
					method = "colorize";
				else if (splittedParams[0].equalsIgnoreCase("custom"))
					method = "custom";

				final Pattern pattern = Pattern.compile("(?<=\\{)(.+?)(?=\\})");
				final Matcher matcher = pattern.matcher(params);

				List<String> options = new ArrayList<>();
				while (matcher.find()){
					options.add(matcher.group());
				}

				if (!options.isEmpty())
				{
					if (options.size() > 2 && method.equalsIgnoreCase("colorize"))
					{
						color = options.get(0);
						destinationFormat = ColorManipulator.ColorFormat.valueOf(options.get(1));
						placeholderName = options.get(2);
					}
					else if (options.size() > 2)
					{
						ChatHelper.console("Zbyt dużo parametrów. Maksymalnie 3, jeśli opcja colorize jest używana!");
						return "N/A";
					}
					else
					{
						ChatHelper.console("format");
						destinationFormat = ColorManipulator.ColorFormat.valueOf(options.getFirst());
						placeholderName = options.get(1);
					}
				}
				else
				{
					ChatHelper.console("<red>Brak parametrów!</red>");
					return "N/A";
				}

				String placeholder = plugin.placeholderAPI.process(player, "%" + placeholderName + "%");

				if (method.equalsIgnoreCase("colorize"))
					placeholder = ColorManipulator.convert(color, ColorManipulator.ColorFormat.PLAIN_HEX) + ColorManipulator.convert(placeholder, ColorManipulator.ColorFormat.PLAIN_WITH_STYLES).replaceAll("&r", "&r"+ColorManipulator.convert(color, ColorManipulator.ColorFormat.PLAIN_HEX));
				placeholder = ColorManipulator.convert(placeholder, destinationFormat);
				return placeholder;
			}

			return "N/A";
		}

		return "";
	}

	public Placeholders(PlaceholderColorManipulator plugin){
		this.plugin = plugin;
	}
}