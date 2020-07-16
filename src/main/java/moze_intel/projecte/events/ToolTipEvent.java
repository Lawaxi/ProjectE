package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = PECore.MODID)
public class ToolTipEvent 
{
	@SubscribeEvent
	public static void tTipEvent(ItemTooltipEvent event)
	{
		ItemStack current = event.getItemStack();
		if (current.isEmpty())
		{
			return;
		}
		Item currentItem = current.getItem();
		Block currentBlock = Block.getBlockFromItem(currentItem);
		EntityPlayer clientPlayer = Minecraft.getMinecraft().player;

		if (ProjectEConfig.misc.pedestalToolTips
			&& currentItem instanceof IPedestalItem)
		{
			event.getToolTip().add(TextFormatting.DARK_PURPLE + I18n.format("pe.pedestal.on_pedestal") + " ");
			List<String> description = ((IPedestalItem) currentItem).getPedestalDescription();
			if (description.isEmpty())
			{
				event.getToolTip().add(IPedestalItem.TOOLTIPDISABLED);
			}
			else
			{
				event.getToolTip().addAll(((IPedestalItem) currentItem).getPedestalDescription());
			}
		}

		if (ProjectEConfig.misc.odToolTips)
		{
			for (int id : OreDictionary.getOreIDs(current))
			{
				event.getToolTip().add("OD: " + OreDictionary.getOreName(id));
			}
			if (currentBlock instanceof BlockFluidBase) {
				event.getToolTip().add("Fluid: " + ((BlockFluidBase) currentBlock).getFluid().getName());
			}
		}

		/*
		if (ProjectEConfig.misc.emcToolTips)
		{
			if (EMCHelper.doesItemHaveEmc(current))
			{
				long value = EMCHelper.getEmcValue(current);

				event.getToolTip().add(TextFormatting.YELLOW +
						I18n.format("pe.emc.emc_tooltip_prefix") + " " + TextFormatting.WHITE + Constants.EMC_FORMATTER.format(value) + TextFormatting.BLUE + EMCHelper.getEmcSellString(current, 1));

				if (current.getCount() > 1)
				{
					event.getToolTip().add(TextFormatting.YELLOW + I18n.format("pe.emc.stackemc_tooltip_prefix") + " " +
							TextFormatting.WHITE + Constants.EMC_FORMATTER.format(BigInteger.valueOf(value).multiply(BigInteger.valueOf(current.getCount()))) +
							TextFormatting.BLUE + EMCHelper.getEmcSellString(current, current.getCount()));
				}

				if (GuiScreen.isShiftKeyDown()
						&& clientPlayer != null
						&& clientPlayer.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null).hasKnowledge(current))
				{
					event.getToolTip().add(TextFormatting.YELLOW + I18n.format("pe.emc.has_knowledge"));
				}
			}
		}*/

		if (ProjectEConfig.misc.statToolTips)
		{
			/*
			 * Collector ToolTips
			 */
			String unit = I18n.format("pe.emc.name");
			String rate = I18n.format("pe.emc.rate");

			/*
			 * Relay ToolTips
			 */
			if (currentBlock == ObjHandler.relay)
			{
				event.getToolTip().add(TextFormatting.DARK_PURPLE
						+ String.format(I18n.format("pe.emc.maxoutrate_tooltip")
						+ TextFormatting.BLUE + " %d " + rate, Constants.RELAY_MK1_OUTPUT));
				event.getToolTip().add(TextFormatting.DARK_PURPLE
						+ String.format(I18n.format("pe.emc.maxstorage_tooltip")
						+ TextFormatting.BLUE + " %d " + unit, Constants.RELAY_MK1_MAX));
			}

			if (currentBlock == ObjHandler.relayMK2)
			{
				event.getToolTip().add(TextFormatting.DARK_PURPLE
						+ String.format(I18n.format("pe.emc.maxoutrate_tooltip")
						+ TextFormatting.BLUE + " %d " + rate, Constants.RELAY_MK2_OUTPUT));
				event.getToolTip().add(TextFormatting.DARK_PURPLE
						+ String.format(I18n.format("pe.emc.maxstorage_tooltip")
						+ TextFormatting.BLUE + " %d " + unit, Constants.RELAY_MK2_MAX));
			}

			if (currentBlock == ObjHandler.relayMK3)
			{
				event.getToolTip().add(TextFormatting.DARK_PURPLE
						+ String.format(I18n.format("pe.emc.maxoutrate_tooltip")
						+ TextFormatting.BLUE + " %d " + rate, Constants.RELAY_MK3_OUTPUT));
				event.getToolTip().add(TextFormatting.DARK_PURPLE
						+ String.format(I18n.format("pe.emc.maxstorage_tooltip")
						+ TextFormatting.BLUE + " %d " + unit, Constants.RELAY_MK3_MAX));
			}
		}
	}
}
