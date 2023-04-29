package kludwisz.mineshafts;

import java.util.List;

import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.loot.item.Items;

public class LootChecker 
{
	public static boolean hasOpLoot(List<ItemStack> chest)
	{
		int diacount = 0;
		int notchcount = 0;
		for (ItemStack is : chest)
		{
			if (is.getItem().getName() == Items.ENCHANTED_GOLDEN_APPLE.getName())
				notchcount += is.getCount();
			if (is.getItem().getName() == Items.DIAMOND.getName())
				diacount += is.getCount();
		}
		
		if (diacount >= 4 && notchcount >= 1)
			return true;
		return false;
	}
}
