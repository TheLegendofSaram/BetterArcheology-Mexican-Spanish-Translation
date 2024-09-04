package net.Pandarix.betterarcheology.mixin;

import net.Pandarix.betterarcheology.BetterArcheologyConfig;
import net.Pandarix.betterarcheology.enchantment.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MiningToolItem.class)
public class TunnelingEnchantmentMixin
{
    @Inject(method = "postMine", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void injectMethod(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> cir)
    {

        //if it is enabled in the config and the stack exists, has Enchantments & is Tunneling
        if (BetterArcheologyConfig.artifactsEnabled.get() && BetterArcheologyConfig.tunnelingEnabled.get() && !miner.isSneaking() && !stack.isEmpty() && stack.hasEnchantments() && EnchantmentHelper.getLevel(ModEnchantments.TUNNELING, stack) == 1)
        {
            //if the tool is right for the block that should be broken
            //if the difference of the hardness of the block below is not more than 3,75
            BlockPos ba$downPos = pos.down();
            BlockState ba$blockStateBelow = world.getBlockState(ba$downPos);

            if (stack.isSuitableFor(state) && stack.isSuitableFor(ba$blockStateBelow) && Math.abs((world.getBlockState(ba$downPos).getHardness(world, ba$downPos) - world.getBlockState(pos).getHardness(world, pos))) <= 3.75)
            {
                if (miner instanceof PlayerEntity player)
                {
                    BlockEntity ba$blockEntityBelow = world.getBlockEntity(ba$downPos);
                    Block ba$block = ba$blockStateBelow.getBlock();
                    ba$block.onBreak(world, ba$downPos, ba$blockStateBelow, player);
                    boolean ba$removeSuccess = world.removeBlock(ba$downPos, false);
                    if (ba$removeSuccess)
                    {
                        ba$block.onBroken(world, ba$downPos, ba$blockStateBelow);
                    }

                    if (!player.isCreative())
                    {
                        ItemStack ba$stackCopy = stack.copy();
                        boolean bl2 = player.canHarvest(ba$blockStateBelow);
                        if (ba$removeSuccess && bl2)
                        {
                            ba$block.afterBreak(world, player, ba$downPos, ba$blockStateBelow, ba$blockEntityBelow, ba$stackCopy);
                        }
                    }
                }
            }
        }
    }
}
