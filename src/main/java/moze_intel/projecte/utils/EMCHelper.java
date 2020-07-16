package moze_intel.projecte.utils;

import com.google.common.collect.Maps;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.emc.SimpleStack;
import moze_intel.projecte.utils.Constants;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for EMC.
 * Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class EMCHelper
{
    /**
     * Consumes EMC from fuel items or Klein Stars
     * Any extra EMC is discarded !!! To retain remainder EMC use ItemPE.consumeFuel()
     */
    public static long consumePlayerFuel(EntityPlayer player, long minFuel)
    {
        if (player.capabilities.isCreativeMode)
        {
            return minFuel;
        }

        IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        Map<Integer, Integer> map = new LinkedHashMap<>();
        boolean metRequirement = false;
        long emcConsumed = 0;

        ItemStack offhand = player.getHeldItemOffhand();

        if (!offhand.isEmpty() && offhand.getItem() instanceof IItemEmc)
        {
            IItemEmc itemEmc = ((IItemEmc) offhand.getItem());
            if (itemEmc.getStoredEmc(offhand) >= minFuel)
            {
                itemEmc.extractEmc(offhand, minFuel);
                player.inventoryContainer.detectAndSendChanges();
                return minFuel;
            }
        }

        for (int i = 0; i < inv.getSlots(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty())
            {
                continue;
            }
            else if (stack.getItem() instanceof IItemEmc)
            {
                IItemEmc itemEmc = ((IItemEmc) stack.getItem());
                if (itemEmc.getStoredEmc(stack) >= minFuel)
                {
                    itemEmc.extractEmc(stack, minFuel);
                    player.inventoryContainer.detectAndSendChanges();
                    return minFuel;
                }
            }
            else if (!metRequirement)
            {
                if(FuelMapper.isStackFuel(stack))
                {
                    long emc = getEmcValue(stack);
                    int toRemove = (int)Math.ceil((double) (minFuel - emcConsumed) / emc);

                    if (stack.getCount() >= toRemove)
                    {
                        map.put(i, toRemove);
                        emcConsumed += emc * toRemove;
                        metRequirement = true;
                    }
                    else
                    {
                        map.put(i, stack.getCount());
                        emcConsumed += emc * stack.getCount();

                        if (emcConsumed >= minFuel)
                        {
                            metRequirement = true;
                        }
                    }

                }
            }
        }

        if (metRequirement)
        {
            for (Map.Entry<Integer, Integer> entry : map.entrySet())
            {
                inv.extractItem(entry.getKey(), entry.getValue(), false);
            }

            player.inventoryContainer.detectAndSendChanges();
            return emcConsumed;
        }

        return -1;
    }

    public static boolean doesBlockHaveEmc(Block block)
    {
        return false;
    }

    public static boolean doesItemHaveEmc(ItemStack stack)
    {
        return false;
    }

    public static boolean doesItemHaveEmc(Item item)
    {
        return item != null && doesItemHaveEmc(new ItemStack(item));
    }

    public static long getEmcValue(Block block)
    {
        return 0;
    }

    public static long getEmcValue(Item item)
    {
        return 0;
    }

    /**
     * Does not consider stack size
     */
    public static long getEmcValue(ItemStack stack)
    {
        return 0;
    }

    private static long getEnchantEmcBonus(ItemStack stack)
    {
        long result = 0;

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);

        if (!enchants.isEmpty())
        {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet())
            {
                Enchantment ench = entry.getKey();
                if (ench == null || ench.getRarity().getWeight() == 0)
                {
                    continue;
                }

                result += Constants.ENCH_EMC_BONUS / ench.getRarity().getWeight() * entry.getValue();
            }
        }

        return result;
    }

    public static long getEmcSellValue(ItemStack stack)
    {
        return 0;
    }

    public static String getEmcSellString(ItemStack stack, int stackSize)
    {
        if (EMCMapper.covalenceLoss == 1.0)
        {
            return " ";
        }

        BigInteger emc = BigInteger.valueOf(EMCHelper.getEmcSellValue(stack));

        return " (" + Constants.EMC_FORMATTER.format(emc.multiply(BigInteger.valueOf(stackSize))) + ")";
    }

    public static long getKleinStarMaxEmc(ItemStack stack)
    {
        return Constants.MAX_KLEIN_EMC[stack.getItemDamage()];
    }

    private static long getStoredEMCBonus(ItemStack stack) {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("StoredEMC")) {
            return stack.getTagCompound().getLong("StoredEMC");
        } else if (stack.getItem() instanceof IItemEmc) {
            return ((IItemEmc) stack.getItem()).getStoredEmc(stack);
        }
        return 0;
    }

    public static long getEMCPerDurability(ItemStack stack) {
        if(stack.isEmpty())
            return 0;

        if(ItemHelper.isItemRepairable(stack)){
            ItemStack stackCopy = stack.copy();
            stackCopy.setItemDamage(0);
            long emc = (long)Math.ceil(EMCHelper.getEmcValue(stackCopy) / (double) stack.getMaxDamage());
            return emc > 1 ? emc : 1;
        }
        return 1;
    }

    /**
     * Adds the given amount to the amount of unprocessed EMC the stack has.
     * The amount returned should be used for figuring out how much EMC actually gets removed.
     * While the remaining fractional EMC will be stored in UnprocessedEMC.
     * @param stack The stack to set the UnprocessedEMC tag to.
     * @param amount The partial amount of EMC to add with the current UnprocessedEMC
     * @return The amount of non fractional EMC no longer being stored in UnprocessedEMC.
     */
    public static long removeFractionalEMC(ItemStack stack, double amount)
    {
        double unprocessedEMC = ItemHelper.getOrCreateCompound(stack).getDouble("UnprocessedEMC");
        unprocessedEMC += amount;
        long toRemove = (long) unprocessedEMC;
        unprocessedEMC -= toRemove;
        stack.getTagCompound().setDouble("UnprocessedEMC", unprocessedEMC);
        return toRemove;
    }
}