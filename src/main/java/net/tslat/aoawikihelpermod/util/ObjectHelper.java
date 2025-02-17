package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.util.LocaleUtil;
import net.tslat.aoa3.util.StringUtil;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectHelper {
	private static final ArrayList<Pattern> TOOLTIP_BLACKLIST = new ArrayList<Pattern>();

	static {
		TOOLTIP_BLACKLIST.add(Pattern.compile("^[\\d|\\.]+ \\w+ damage$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Firing rate: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Ammo: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Consumes [\\d|\\.]+ \\w+$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^[\\d|\\.]+ Average \\w+ damage$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Runes required:.*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^\\d+ \\w+ Runes?"));
	}

	public static List<Item> scrapeRegistryForItems(Predicate<Item> filter) {
		return ObjectHelper.sortCollection(ForgeRegistries.ITEMS.getValues().stream().filter(filter).collect(Collectors.toList()), ObjectHelper::getItemName);
	}

	public static List<Block> scrapeRegistryForBlocks(Predicate<Block> filter) {
		return ObjectHelper.sortCollection(ForgeRegistries.BLOCKS.getValues().stream().filter(filter).collect(Collectors.toList()), ObjectHelper::getItemName);
	}

	public static List<EntityType<?>> scrapeRegistryForEntities(Predicate<EntityType<?>> filter) {
		return ForgeRegistries.ENTITIES.getValues().stream().filter(filter).collect(Collectors.toList());
	}

	public static Multimap<Attribute, AttributeModifier> getAttributesForItem(Item item) {
		return item.getAttributeModifiers(EquipmentSlotType.MAINHAND, new ItemStack(item));
	}

	public static double getAttributeFromItem(Item item, Attribute attribute) {
		Multimap<Attribute, AttributeModifier> attributes = getAttributesForItem(item);

		if (!attributes.containsKey(attribute))
			return 0d;

		return getAttributeValue(attribute, attributes.get(attribute));
	}

	public static double getAttributeValue(Attribute attribute, Collection<AttributeModifier> modifiers) {
		ModifiableAttributeInstance instance = new ModifiableAttributeInstance(attribute, consumer -> {});

		for (AttributeModifier modifier : modifiers) {
			if (!instance.hasModifier(modifier))
				instance.addTransientModifier(modifier);
		}

		double value = instance.getValue() - attribute.getDefaultValue(); // Remove due to the way instanceless attributes are calculated

		if (attribute == Attributes.ATTACK_DAMAGE) {
			value++;

			if (value < 0)
				value++;
		}

		return value;
	}

	public static <T extends Object, U extends Comparable<? super U>> ArrayList<T> sortCollection(Collection<T> collection, Function<T, U> sortFunction) {
		return (ArrayList<T>)collection.stream().sorted(Comparator.comparing(sortFunction)).collect(Collectors.toList());
	}

	public static Pair<String, String> getIngredientName(JsonObject obj) {
		if ((obj.has("item") && obj.has("tag")) || (!obj.has("item") && !obj.has("tag")))
			throw new JsonParseException("Invalidly formatted ingredient, unable to proceed.");

		String ingredientName;
		String ownerId;

		if (obj.has("item")) {
			return getFormattedItemDetails(new ResourceLocation(JSONUtils.getAsString(obj, "item")));
		}
		else if (obj.has("tag")) {
			ingredientName = JSONUtils.getAsString(obj, "tag");

			if (!ingredientName.contains(":"))
				ingredientName = "minecraft:" + ingredientName;

			ownerId = ingredientName.split(":")[0];

			return new Pair<String, String>(ownerId, ingredientName);
		}
		else {
			throw new JsonParseException("Invalidly formatted ingredient, unable to proceed.");
		}
	}

	@Nullable
	public static ResourceLocation getIngredientItemId(JsonElement element) {
		if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();

			if (obj.has("tag"))
				return null;

			if (obj.has("item")) {
				return new ResourceLocation(JSONUtils.getAsString(obj, "item"));
			}
			else {
				throw new JsonParseException("Invalidly formatted ingredient, unable to proceed.");
			}
		}
		else {
			return new ResourceLocation(element.getAsString());
		}
	}

	public static boolean isItem(ResourceLocation id) {
		return ForgeRegistries.ITEMS.getValue(id) != Items.AIR;
	}

	public static boolean isBlock(ResourceLocation id) {
		return ForgeRegistries.BLOCKS.getValue(id) != Blocks.AIR;
	}

	public static boolean isEntity(ResourceLocation id) {
		return ForgeRegistries.ENTITIES.getValue(id) != EntityType.PIG;
	}

	public static String getItemName(IItemProvider item) {
		ResourceLocation itemId = item.asItem().getRegistryName();
		String suffix = isEntity(itemId) ? " (item)" : "";

		return new ItemStack(item).getHoverName().getString() + suffix;
	}

	public static String getBlockName(Block block) {
		if (block.asItem() != Items.AIR)
			return getItemName(block);

		return StringUtil.toTitleCase(block.getRegistryName().getPath());
	}

	public static String getBiomeName(RegistryKey<Biome> biome) {
		ResourceLocation biomeId = biome.location();

		if (biomeId.getNamespace().equals(AdventOfAscension.MOD_ID))
			return StringUtil.toTitleCase(biomeId.getPath());

		return biomeId.toString();
	}

	public static String getEntityName(EntityType<?> entityType) {
		ResourceLocation id = entityType.getRegistryName();
		String suffix = isItem(id) ? " (entity)" : "";

		return LocaleUtil.getLocaleMessage("entity." + id.getNamespace() + "." + id.getPath()).getString() + suffix;
	}

	public static String getBiomeCategoryName(Biome.Category category) {
		return StringUtil.toTitleCase(category.getName());
	}

	public static String getEnchantmentName(Enchantment enchant, int level) {
		if (level <= 0)
			return new TranslationTextComponent(enchant.getDescriptionId()).getString();

		return enchant.getFullname(level).getString();
	}

	public static Pair<String, String> getFormattedItemDetails(ResourceLocation id) {
		Item item = Registry.ITEM.getOptional(id).orElse(null);

		return new Pair<String, String>(id.getNamespace(), item == null ? StringUtil.toTitleCase(id.getPath()) : ObjectHelper.getItemName(item));
	}

	public static Triple<Integer, String, String> getStackDetailsFromJson(JsonElement element) {
		Pair<String, String> itemName;
		int count = 1;

		if (element.isJsonObject()) {
			JsonObject obj = (JsonObject)element;

			if (obj.has("count"))
				count = obj.get("count").getAsInt();

			itemName = getIngredientName(obj);
		}
		else {
			itemName = getFormattedItemDetails(new ResourceLocation(element.getAsString()));
		}

		return Triple.of(count, itemName.getFirst(), itemName.getSecond());
	}

	public static String attemptToExtractItemSpecificEffects(Item item, @Nullable Item controlItem) {
		StringTextComponent dummyComponent = new StringTextComponent("");

		List<ITextComponent> itemTooltip = new ArrayList<ITextComponent>();
		List<ITextComponent> controlItemTooltip = new ArrayList<ITextComponent>();
		StringBuilder builder = new StringBuilder();

		itemTooltip.add(dummyComponent);
		itemTooltip.add(dummyComponent);
		controlItemTooltip.add(dummyComponent);
		controlItemTooltip.add(dummyComponent);

		ClientHelper.collectTooltipLines(item, itemTooltip, false);

		if (controlItem != null)
			ClientHelper.collectTooltipLines(controlItem, controlItemTooltip, false);

		tooltipLoop:
		for (ITextComponent text : itemTooltip) {
			String line = text.getString();

			if (line.isEmpty())
				continue;

			for (Pattern pattern : TOOLTIP_BLACKLIST) {
				if (pattern.matcher(line).matches())
					continue tooltipLoop;
			}

			for (ITextComponent controlText : controlItemTooltip) {
				if (areStringsSimilar(line, controlText.getString()))
					continue tooltipLoop;
			}

			if (text != dummyComponent) {
				if (builder.length() > 0)
					builder.append("<br/>");

				builder.append(text.getString());
			}
		}

		return builder.toString();
	}

	public static boolean areStringsSimilar(String str1, String str2) {
		if (str1.equals(str2))
			return true;

		if (Math.abs(str1.length() - str2.length()) / (float)str1.length() > 0.5f)
			return false;

		int matches = 0;

		for (int i = 0; i < str1.length(); i++) {
			if (i >= str2.length())
				break;

			if (str1.charAt(i) != str2.charAt(i))
				continue;

			matches++;
		}

		return matches / (float)str1.length() >= 0.75f;
	}
}
