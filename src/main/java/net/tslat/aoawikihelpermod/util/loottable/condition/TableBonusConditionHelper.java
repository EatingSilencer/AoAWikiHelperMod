package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.TableBonus;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;

public class TableBonusConditionHelper extends LootConditionHelper<TableBonus> {
	@Nonnull
	@Override
	public String getDescription(TableBonus condition) {
		return "if a chance check is passed, depending on the level of " + FormattingHelper.createLinkableText(ObjectHelper.getEnchantmentName(condition.enchantment, 0), false, condition.enchantment.getRegistryName().getNamespace().equals("minecraft"), true) + " used";
	}
}
