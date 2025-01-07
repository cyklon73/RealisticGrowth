package de.cyklon.realisticgrowth.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;

import java.awt.*;
import java.util.Map;
import java.util.function.BiConsumer;

import static net.md_5.bungee.api.ChatColor.of;

public class ColorUtil {

	public static final char COLOR_CHAR = '\u00A7';

	public static final Map<Character, Color> DEFAULT_COLORS = Map.ofEntries(
			Map.entry('0', new Color(0x000000)),
			Map.entry('1', new Color(0x0000AA)),
			Map.entry('2', new Color(0x00AA00)),
			Map.entry('3', new Color(0x00AAAA)),
			Map.entry('4', new Color(0xAA0000)),
			Map.entry('5', new Color(0xAA00AA)),
			Map.entry('6', new Color(0xFFAA00)),
			Map.entry('7', new Color(0xAAAAAA)),
			Map.entry('8', new Color(0x555555)),
			Map.entry('9', new Color(0x5555FF)),
			Map.entry('a', new Color(0x55FF55)),
			Map.entry('b', new Color(0x55FFFF)),
			Map.entry('c', new Color(0xFF5555)),
			Map.entry('d', new Color(0xFF55FF)),
			Map.entry('e', new Color(0xFFFF55)),
			Map.entry('f', new Color(0xFFFFFF))
	);

	private static Color gradient(float ratio, Color color1, Color color2) {
		int red = (int) (color2.getRed() * ratio + color1.getRed() * (1 - ratio));
		int green = (int) (color2.getGreen() * ratio + color1.getGreen() * (1 - ratio));
		int blue = (int) (color2.getBlue() * ratio + color1.getBlue() * (1 - ratio));
		return new Color(red, green, blue);
	}

	private static void gradient(String text, Color color1, Color color2, BiConsumer<Character, Color> method) {
		int length = 0;
		for (char c : text.toCharArray()) if (!Character.isWhitespace(c)) length++;

		int i1 = 0;
		for (int i = 0; i < text.length(); i++) {
			Color c = new Color(0, 0, 0, 0);
			if (!Character.isWhitespace(text.charAt(i))) {
				c = gradient((float) i1++ /(length), color1, color2);
			}
			method.accept(text.charAt(i), c);
		}
	}

	public static GradientData gradientData(String text, ChatColor color1, ChatColor color2) {
		return gradientData(text, ofLegacyCode(color1.toString()), ofLegacyCode(color2.toString()));
	}

	public static GradientData gradientData(String text, Color color1, Color color2) {
		return gradientData(Color.BLACK, null, text, Color.BLACK, null, color1, color2);
	}

	public static GradientData gradientData(Color beforeColor, Character before, String text, Color afterColor, Character after, Color color1, Color color2) {
		BiList<Character, Color> data = new BiList<>();
		if (before!=null) data.add(before, beforeColor);
		gradient(text, color1, color2, data::add);
		if (after!=null) data.add(after, afterColor);
		return new GD(data);
	}

	public static GradientData gradientData(ChatColor beforeColor, char before, String text, ChatColor afterColor, char after, ChatColor color1, ChatColor color2) {
		return gradientData(ofLegacyCode(beforeColor.toString()), before, text, ofLegacyCode(afterColor.toString()), after, ofLegacyCode(color1.toString()), ofLegacyCode(color2.toString()));
	}

	public static BaseComponent[] gradient(String text, Color color1, Color color2) {
		ComponentBuilder builder = new ComponentBuilder();
		gradient(text, color1, color2, (c, color) -> {
			builder.append(String.valueOf(c))
					.reset();
			if (color!=null) builder.color(of(color));
		});
		return builder.create();
	}

	public static BaseComponent[] gradient(String text, net.md_5.bungee.api.ChatColor color1, net.md_5.bungee.api.ChatColor color2) {
		return gradient(text, color1.getColor(), color2.getColor());
	}

	public static String legacyGradient(String text, Color color1, Color color2) {
		StringBuilder sb = new StringBuilder();
		gradient(text, color1, color2, (c, color) -> {
			if (color!=null) sb.append(toLegacyCode(color));
			sb.append(c);
		});
		return sb.toString();
	}

	public static String legacyGradient(String text, ChatColor color1, ChatColor color2) {
		return legacyGradient(text, ofLegacyCode(color1.toString()), ofLegacyCode(color2.toString()));
	}

	public static String toLegacyCode(Color color)
	{
		String s = String.format("%08x", color.getRGB()).substring(2);
		StringBuilder colorCode = new StringBuilder(COLOR_CHAR + "x");
		for (char c : s.toCharArray()) colorCode.append(COLOR_CHAR).append(c);
		return colorCode.toString();
	}

	public static Color ofLegacyCode(String colorCode) {
		if (colorCode == null || colorCode.isEmpty()) {
			throw new IllegalArgumentException("Color code cannot be null or empty.");
		}

		if (colorCode.length() == 2 && colorCode.charAt(0) == '§') {
			char code = colorCode.charAt(1);
			Color defaultColor = DEFAULT_COLORS.get(code);
			if (defaultColor != null) {
				return defaultColor;
			} else {
				throw new IllegalArgumentException("Invalid Minecraft color code: " + colorCode);
			}
		}

		if (colorCode.length() == 6 || colorCode.length() == 7 && colorCode.charAt(0) == '#') {
			String hexColor = colorCode.charAt(0) == '#' ? colorCode.substring(1) : colorCode;
			try {
				return new Color(
						Integer.valueOf(hexColor.substring(0, 2), 16),
						Integer.valueOf(hexColor.substring(2, 4), 16),
						Integer.valueOf(hexColor.substring(4, 6), 16)
				);
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				throw new IllegalArgumentException("Invalid hex color format: " + colorCode, e);
			}
		}

		throw new IllegalArgumentException("Unrecognized color code format: " + colorCode);
	}

	private record GD(BiList<Character, Color> data) implements GradientData {

			@Override
			public BaseComponent[] getComponents() {
				ComponentBuilder builder = new ComponentBuilder();
				data.forEach((c, color) -> {
					builder.append(String.valueOf(c))
							.reset();
					if (color!=null) builder.color(of(color));
				});
				return builder.create();
			}

			@Override
			public String getLegacy() {
				StringBuilder sb = new StringBuilder();
				data.forEach((c, color) -> {
					if (color!=null) sb.append(toLegacyCode(color).strip());
					sb.append(c);
				});
				return sb.toString();
			}

		@Override
		public String toString() {
			return getLegacy();
		}
	}


	public interface GradientData {

		BaseComponent[] getComponents();

		String getLegacy();

	}


}
