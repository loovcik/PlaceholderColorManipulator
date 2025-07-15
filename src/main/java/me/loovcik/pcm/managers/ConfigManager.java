package me.loovcik.pcm.managers;

import me.loovcik.core.ChatHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import me.loovcik.core.managers.ConfigurationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager extends ConfigurationManager {
	public CustomPlaceholders custom = new CustomPlaceholders();

	@Override
	public void load() {
		custom.placeholders.clear();
		ConfigurationSection customSection = getConfigurationSection("custom", Map.of(), "W poniższej sekcji możesz skonfigurować własne\nplaceholdery obsługiwane przez plugin");
		if (customSection != null){
			for (var section : customSection.getKeys(false)){
				CustomPlaceholder customPlaceholder = new CustomPlaceholder();
				customPlaceholder.format = customSection.getString(section+".format");
				ConfigurationSection conditionsSection = customSection.getConfigurationSection(section+".conditions");
				if (conditionsSection != null) {
					for (var conditionKey : conditionsSection.getKeys(false)) {
						Condition condition = new Condition();
						condition.format = conditionsSection.getString(conditionKey+".format");
						condition.placeholder = conditionsSection.getString(conditionKey+".placeholder");
						condition.yesValue = conditionsSection.getString(conditionKey+".trueValue");
						condition.noValue = conditionsSection.getString(conditionKey+".falseValue");
						customPlaceholder.conditions.add(condition);
					}
				}

				custom.placeholders.put(section.toLowerCase(), customPlaceholder);
				ChatHelper.console("Registered custom placeholder '"+section.toLowerCase()+"'");
			}
		}

		plugin.saveConfig();
	}

	@Override
	public String header() {
		return """
				
				  (C) 2025 Loovcik
				==============================================
				
				Wspierane kodowanie kolorów:
				- Natywne &[0-9a-f]
				- MiniMessage (Zalecane)
				
				==============================================
				
				""";
	}

	public ConfigManager(Plugin plugin) {
		super(plugin);
		config.options().copyDefaults(true);
	}

	public static class CustomPlaceholders {
		public final Map<String, CustomPlaceholder> placeholders = new HashMap<>();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CustomPlaceholders={size=").append(placeholders.size()).append(", placeholders=[");
			for (Map.Entry<String, CustomPlaceholder> entry : placeholders.entrySet()){
				builder.append(entry.getKey()).append("={").append(entry.getValue()).append("}");
			}
			builder.append("]}");
			return builder.toString();
		}
	}

	public static class CustomPlaceholder {
		public String format;
		public final List<Condition> conditions = new ArrayList<>();

		@Override
		public String toString(){
			StringBuilder builder = new StringBuilder();
			builder.append("{size=").append(conditions.size()).append(", conditions=[");
			for (Condition condition : conditions){
				builder.append(condition.toString());
			}
			builder.append("]}");
			return builder.toString();
		}
	}

	public static class Condition {
		public String placeholder = "";
		public String yesValue = "";
		public String noValue = "";
		public String format = "";

		@Override
		public String toString() {
			return "Condition={placeholder="+placeholder+", format="+format+", yes="+yesValue+", no="+noValue+"}";
		}
	}
}