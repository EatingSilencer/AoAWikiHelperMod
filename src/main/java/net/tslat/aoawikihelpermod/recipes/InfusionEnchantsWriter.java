package net.tslat.aoawikihelpermod.recipes;

import net.minecraft.command.ICommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.common.container.recipe.InfusionRecipe;

import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;

public class InfusionEnchantsWriter {
    public static File configDir = null;
    private static PrintWriter writer = null;

    public static void printImbuingEntries(ICommandSource sender, RecipeManager manager, boolean copyToClipboard) {
        String fileName = "Infusion Enchants Printout " + AdventOfAscension.VERSION + ".txt";

        enableWriter(fileName);

        ArrayList<InfusionRecipe> imbuingRecipes = new ArrayList<InfusionRecipe>();

        for (IRecipe recipe: manager.getRecipes()) {
            if (recipe.getId() == null || !recipe.getId().getNamespace().equals("aoa3"))
                continue;

            if (recipe instanceof InfusionRecipe) {
                InfusionRecipe infusionRecipe = (InfusionRecipe)recipe;

                if (infusionRecipe.isEnchanting()) {
                    imbuingRecipes.add(infusionRecipe);
                }
            }

        }

        imbuingRecipes.sort(new InfusionRecipeComparator());

        write("{|cellpadding=\"5\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
        write("|-style=\"background-color:#f2f2f2\"|");
        write("! Enchantment !! Description !! Applies to !! Base Recipe !! Infusion Level");
        write("|-");

        Enchantment lastEnchantRow = null;
        ArrayList<Integer> enchantLevels = new ArrayList<Integer>();
        ArrayList<Integer> enchantLevelValues = new ArrayList<Integer>();

        for (InfusionRecipe recipe : imbuingRecipes) {
            EnchantmentData enchData = getEnchantmentData(recipe);

            if (enchData.enchantment == lastEnchantRow) {
                enchantLevels.add(enchData.enchantmentLevel);
                enchantLevelValues.add(recipe.getInfusionReq());
            }
            else {
                if (lastEnchantRow != null) {
                    StringBuilder builder = new StringBuilder();

                    for (Integer lvl : enchantLevels) {
                        builder.append(" !! Level ").append(lazyNumberToRoman(lvl));
                    }

                    write(builder.toString().substring(2));
                    write("|-");

                    builder = new StringBuilder();

                    for (Integer lvl : enchantLevelValues) {
                        builder.append(" || ").append(lvl);
                    }

                    write(builder.toString().substring(2));
                    write("|}");
                    write("|-");
                    enchantLevels.clear();
                    enchantLevelValues.clear();
                }

                String enchantName = I18n.format(enchData.enchantment.getName());
                String desc = I18n.format(enchData.enchantment.getName() + ".desc");
                String appliesTo = RecipeInterfaceInfusion.getImbuingApplicableTo(enchData.enchantment);
                RecipeInterfaceInfusion recipeInterface = (RecipeInterfaceInfusion)RecipeWriter.findRecipeInterface(recipe);

                if (desc.equals(enchData.enchantment.getName() + ".desc"))
                    desc = "?";

                write("| '''" + enchantName + "''' || " + desc + " || " + appliesTo + " ||");
                write("{{Infusion");

                for (String line : recipeInterface.buildAdditionalTemplateLines(recipe.getEnchantmentAsBook(), true)) {
                    write(line);
                }

                write("}}");
                write("||");
                write("{|class=\"wikitable\"");
                write("|-");
                enchantLevels.add(enchData.enchantmentLevel);
                enchantLevelValues.add(recipe.getInfusionReq());

                lastEnchantRow = enchData.enchantment;
            }
        }

        StringBuilder builder = new StringBuilder();

        for (Integer lvl : enchantLevels) {
            builder.append(" !! Level ").append(lazyNumberToRoman(lvl));
        }

        write(builder.toString().substring(2));
        write("|-");

        builder = new StringBuilder();

        for (Integer lvl : enchantLevelValues) {
            builder.append(" || ").append(lvl);
        }

        write(builder.toString().substring(2));
        write("|}");
        write("|-");
        enchantLevels.clear();
        enchantLevelValues.clear();

        write("|}");

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Generated data file: ", new File(configDir, fileName), "Infusion Enchants", copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    private static String lazyNumberToRoman(int number) {
        switch (number) {
            case 0:
                return "0";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return String.valueOf(number);
        }
    }

    private static void enableWriter(final String fileName) {
        configDir = AoAWikiHelperMod.prepConfigDir("Misc Printouts");

        File streamFile = new File(configDir, fileName);

        try {
            if (streamFile.exists())
                streamFile.delete();

            streamFile.createNewFile();

            writer = new PrintWriter(streamFile);
        }
        catch (Exception e) {}
    }

    private static void disableWriter() {
        if (writer != null)
            IOUtils.closeQuietly(writer);

        writer = null;
    }

    private static void write(String line) {
        if (writer != null)
            writer.println(line);
    }

    private static EnchantmentData getEnchantmentData(InfusionRecipe recipe) {
        CompoundNBT enchantTag = recipe.getEnchantmentAsBook().getTag().getList("StoredEnchantments", 10).getCompound(0);

        return new EnchantmentData(Registry.ENCHANTMENT.getOrDefault(new ResourceLocation(enchantTag.getString("id"))), enchantTag.getShort("lvl"));
    }

    private static class InfusionRecipeComparator implements Comparator<InfusionRecipe> {
        @Override
        public int compare(InfusionRecipe recipe1, InfusionRecipe recipe2) {
            try {
                EnchantmentData enchant1 = getEnchantmentData(recipe1);
                EnchantmentData enchant2 = getEnchantmentData(recipe2);

                return (I18n.format(enchant1.enchantment.getName()) + enchant1.enchantmentLevel).compareTo(I18n.format(enchant2.enchantment.getName()) + enchant2.enchantmentLevel);
            }
            catch (Exception e) {
                System.out.println("Found invalid enchanted book data from infusion recipe, skipping sort operation on this item");
            }

            return 0;
        }
    }
}