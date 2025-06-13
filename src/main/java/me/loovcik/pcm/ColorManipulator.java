package me.loovcik.pcm;

import me.loovcik.core.ChatHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorManipulator {
	private static final Pattern minimessageHexPattern = Pattern.compile("(?<=\\<)(?:\\/?)#(?:[0-9a-fA-F]){6}(?=>)");
	private static final Pattern alternateMinimessagePattern = Pattern.compile("(?<=\\<(/{1})?)([^>]+)(?=>)");
	private static final Pattern legacyHexPattern = Pattern.compile("(?<=&x)((?:&)([0-9a-fA-F])){6}");
	private static final Pattern legacyNoStylesPattern = Pattern.compile("&([0-9a-fA-F])");
	private static final Pattern plainHexPattern = Pattern.compile("(?<=#)([0-9a-fA-F]){6}");
	private static final Pattern legacyPlainHexPattern = Pattern.compile("(?<=&#)([0-9a-fA-F]){6}");
	private static final Pattern legacyPattern = Pattern.compile("&([a-fA-F0-9lnmo])");

	private static final Pattern mcHexPattern = Pattern.compile("&x&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])");

	private static final Map<String, String> replacements;

	public static String convert(String text, ColorFormat outputFormat){
		text = text.replaceAll("§", "&");
		ColorFormat originalFormat = detect(text);
		String migrateText = switch (originalFormat) {
			case MINI_MESSAGE -> fromMiniMessage(text);
			case LEGACY_HEX -> fromLegacyHex(text);
			case LEGACY, UNKNOWN, PLAIN, PLAIN_WITH_STYLES -> text;
			case LEGACY_PLAIN_HEX -> fromLegacyPlainHex(text);
			case PLAIN_HEX -> fromPlainHex(text);
		};

		migrateText = switch (outputFormat) {
			case MINI_MESSAGE -> toMiniMessage(migrateText);
			case LEGACY_HEX, LEGACY -> migrateText;
			case PLAIN -> toPlain(migrateText);
			case PLAIN_WITH_STYLES -> toPlainWithStyles(migrateText);
			case LEGACY_PLAIN_HEX -> toLegacyPlainHex(migrateText);
			case PLAIN_HEX -> toPlainHex(migrateText);
			case UNKNOWN -> throw new IllegalArgumentException("No output format specified");
		};

		return migrateText;
	}

	private static String toMiniMessage(String source){
		Matcher matcher = mcHexPattern.matcher(source);
		if (matcher.find()) source = matcher.replaceAll("<#$1$2$3$4$5$6>");
		for(Map.Entry<String, String> entry:replacements.entrySet())
			source = source.replaceAll(entry.getKey(), entry.getValue());

		return source;
	}

	private static String toLegacyPlainHex(String source){
		Matcher matcher = mcHexPattern.matcher(source);
		if (matcher.find())
			source = matcher.replaceAll("&#$1$2$3$4$5$6");
		return source;
	}

	private static String toPlainHex(String source){
		Matcher matcher = mcHexPattern.matcher(source);
		if (matcher.find())
			source = matcher.replaceAll("#$1$2$3$4$5$6");
		return source;
	}

	private static String toPlain(String source){
		Matcher matcher = mcHexPattern.matcher(source);
		if (matcher.find())
			source = matcher.replaceAll("");
		matcher = legacyPattern.matcher(source);
		if (matcher.find())
			source = matcher.replaceAll("");
		return source;
	}

	private static String toPlainWithStyles(String source){
		Matcher matcher = mcHexPattern.matcher(source);
		if (matcher.find())
			source = matcher.replaceAll("");
		matcher = legacyNoStylesPattern.matcher(source);
		if (matcher.find())
			source = matcher.replaceAll("");
		return source;
	}

	private static String fromMiniMessage(String text){
		text = replaceMinimessageConstToLegacyHex(text);
		Matcher matcher = minimessageHexPattern.matcher(text);
		while (matcher.find()){
			ChatHelper.console("group="+matcher.group());
			if (matcher.group().contains("/"))
				text = text.replaceAll(matcher.group(), "");
			else {
				text = text.replaceAll(matcher.group(), buildLegacy(matcher.group()));
			}
		}
		text = text.replaceAll("<", "");
		text = text.replaceAll(">", "");
		return text;
	}

	private static String fromPlainHex(String text){
		Matcher matcher = plainHexPattern.matcher(text);
		while (matcher.find()){
			text = text.replaceAll(matcher.group(), buildLegacy(matcher.group()));
		}
		text = text.replaceAll("#", "");
		return text;
	}

	private static String buildLegacy(String color) {
		String[] chars = color.split("");
		String replacement = "&x";
		replacement+="&"+String.join("&", chars);
		return replacement;
	}
	private static String fromLegacyPlainHex(String text) {
		Matcher matcher = legacyPlainHexPattern.matcher(text);
		while (matcher.find()){
			text = text.replaceAll(matcher.group(), buildLegacy(matcher.group()));
		}
		text = text.replaceAll("&#", "");
		return text;
	}

	private static String fromLegacyHex(String text) {
		return text;
	}

	private static String replaceMinimessageConstToLegacyHex(String text) {
		for (Map.Entry<String, String> entry : replacements.entrySet()){
			text = text.replaceAll(entry.getValue(), entry.getKey());
		}
		return text;
	}

	private static ColorFormat detect(String text){
		Matcher matcher = minimessageHexPattern.matcher(text);
		if (matcher.find()) return ColorFormat.MINI_MESSAGE;
		matcher = legacyHexPattern.matcher(text);
		if (matcher.find()) return ColorFormat.LEGACY_HEX;
		matcher = legacyPlainHexPattern.matcher(text);
		if (matcher.find()) return ColorFormat.LEGACY_PLAIN_HEX;
		matcher = plainHexPattern.matcher(text);
		if (matcher.find()) return ColorFormat.PLAIN_HEX;
		matcher = legacyPattern.matcher(text);
		if (matcher.find()) return ColorFormat.LEGACY;
		matcher = alternateMinimessagePattern.matcher(text);
		if (matcher.find()) return ColorFormat.MINI_MESSAGE;
		return ColorFormat.UNKNOWN;
	}

	static {
		replacements = new HashMap<>();
		replacements.put("&0", "<black>");
		replacements.put("&1", "<dark_blue>");
		replacements.put("&2", "<dark_green>");
		replacements.put("&3", "<dark_aqua>");
		replacements.put("&4", "<dark_red>");
		replacements.put("&5", "<dark_purple>");
		replacements.put("&6", "<gold>");
		replacements.put("&7", "<grey>");
		replacements.put("&8", "<dark_grey>");
		replacements.put("&9", "<blue>");
		replacements.put("&a", "<green>");
		replacements.put("&b", "<aqua>");
		replacements.put("&c", "<red>");
		replacements.put("&d", "<light_purple>");
		replacements.put("&e", "<yellow>");
		replacements.put("&f", "<white>");
		replacements.put("&l", "<b>");
		replacements.put("&n", "<u>");
		replacements.put("&m", "<s>");
		replacements.put("&r", "<reset>");
	}

	public enum ColorFormat {
		UNKNOWN,
		MINI_MESSAGE,
		LEGACY_HEX,
		PLAIN_HEX,
		LEGACY_PLAIN_HEX,
		LEGACY,
		PLAIN_WITH_STYLES,
		PLAIN
	}
	private ColorManipulator() {}
}