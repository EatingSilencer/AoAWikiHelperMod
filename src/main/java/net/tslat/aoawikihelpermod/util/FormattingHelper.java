package net.tslat.aoawikihelpermod.util;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.BinomialRange;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.IRandomRange;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public class FormattingHelper {
	public static String bold(String text) {
		return "'''" + text + "'''";
	}

	public static String tooltip(String text, String tooltip) {
		return "{{tooltip|" + text + "|" + tooltip + "}}";
	}

	public static String healthValue(float value) {
		return "{{hp|" + NumberUtil.roundToNthDecimalPlace(value, 2) + "}}";
	}

	public static String createImageBlock(IItemProvider item) {
		return createImageBlock(ObjectHelper.getItemName(item));
	}

	public static String createImageBlock(String name) {
		return createImageBlock(name, 32);
	}

	public static String createImageBlock(String name, int size) {
		return "[[File:" + name + ".png|" + size + "px|link=]]";
	}

	public static String createLinkableItem(ItemStack object, boolean shouldLink) {
		return createLinkableItem(object.getItem(), object.getCount() > 1, shouldLink);
	}

	public static String createLinkableItem(IItemProvider object, boolean pluralise, boolean shouldLink) {
		return createLinkableText(ObjectHelper.getItemName(object.asItem()), pluralise, object.asItem().getRegistryName().getNamespace().equals("minecraft"), shouldLink);
	}

	public static String createLinkableTag(String tag) {
		StringBuilder builder = new StringBuilder("[[");
		ResourceLocation tagId = new ResourceLocation(tag);
		String tagName = StringUtil.toTitleCase(tagId.getPath());

		//if (tagId.getNamespace().equals("minecraft"))
		//	builder.append("mcw:"); Not redirecting mcw links anymore

		builder.append(tagName);
		builder.append("|");
		builder.append(lazyPluralise(tagName));
		builder.append(" (Any)]]");

		return tooltip(builder.toString(), "Any item in tag collection");
	}

	public static String createLinkableText(String text, boolean pluralise, boolean isVanilla, boolean shouldLink) {
		shouldLink = shouldLink;// | isVanilla;

		StringBuilder builder = new StringBuilder(shouldLink ? "[[" : "");
		String pluralName = pluralise ? lazyPluralise(text) : text;

		if (false && isVanilla) { // Not redirecting mcw links anymore
			builder.append("mcw:");
			builder.append(text);
			builder.append("|");
		}
		else if (shouldLink && !pluralName.equals(text)) {
			builder.append(text);
			builder.append("|");
		}

		builder.append(pluralName);

		if (shouldLink)
			builder.append("]]");

		return builder.toString();
	}

	public static String lazyPluralise(String text) {
		return !text.endsWith("s") && !text.endsWith("y") ? text.endsWith("x") || text.endsWith("o") ? text + "es" : text + "s" : text;
	}

	public static IFormattableTextComponent generateResultMessage(File file, String linkName, @Nullable String clipboardContent) {
		String fileUrl = file.getAbsolutePath().replace("\\", "/");
		IFormattableTextComponent component = new StringTextComponent("Generated data file: ")
				.append(new StringTextComponent(linkName).withStyle(style -> style.withColor(TextFormatting.BLUE)
						.setUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, fileUrl))));

		if (clipboardContent != null) {
				component
						.append(new StringTextComponent(" "))
						.append(new StringTextComponent("(Copy)").withStyle(style -> style.withColor(TextFormatting.BLUE)
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clipboardContent))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Copy contents of file to clipboard")))));
		}

		return component;
	}

	public static String listToString(List<String> list, boolean nativeLineBreaks) {
		String newLineDelimiter = nativeLineBreaks ? System.lineSeparator() : "<br/>";
		StringBuilder builder = new StringBuilder();

		for (String str : list) {
			if (builder.length() > 0)
				builder.append(newLineDelimiter);

			builder.append(str);
		}

		return builder.toString();
	}

	public static String getStringFromRange(IRandomRange range) {
		if (range instanceof ConstantRange)
			return String.valueOf(((ConstantRange)range).value);

		if (range instanceof RandomValueRange) {
			RandomValueRange randomRange = (RandomValueRange)range;

			if (randomRange.getMin() != randomRange.getMax()) {
				return NumberUtil.roundToNthDecimalPlace(randomRange.getMin(), 3) + "-" + NumberUtil.roundToNthDecimalPlace(randomRange.getMax(), 3);
			}
			else {
				return NumberUtil.roundToNthDecimalPlace(randomRange.getMin(), 3);
			}
		}

		if (range instanceof BinomialRange)
			return "0+";

		return "1";
	}

	public static String getTimeFromTicks(int ticks) {
		StringBuilder builder = new StringBuilder();

		if (ticks > 1200) {
			builder.append(ticks / 1200).append("m");

			if (ticks % 1200 != 0)
				builder.append(", ").append(NumberUtil.roundToNthDecimalPlace((ticks % 1200) / 20f, 2)).append("s");
		}
		else {
			builder.append(NumberUtil.roundToNthDecimalPlace(ticks % 1200 / 20f, 2)).append("s");
		}

		return builder.toString();
	}
}
