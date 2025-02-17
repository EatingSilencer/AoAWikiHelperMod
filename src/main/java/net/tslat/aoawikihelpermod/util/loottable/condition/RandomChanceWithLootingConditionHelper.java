package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.RandomChanceWithLooting;
import net.tslat.aoa3.util.NumberUtil;

import javax.annotation.Nonnull;

public class RandomChanceWithLootingConditionHelper extends LootConditionHelper<RandomChanceWithLooting> {
	@Nonnull
	@Override
	public String getDescription(RandomChanceWithLooting condition) {
		float chance = condition.percent;
		float lootingMod = condition.lootingMultiplier;

		return "if a fixed random chance check is passed, with a chance of " + NumberUtil.roundToNthDecimalPlace(chance, 3) + "%, with an extra " + NumberUtil.roundToNthDecimalPlace(lootingMod, 3) + " per looting level";
	}
}
