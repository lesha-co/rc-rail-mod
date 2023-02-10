package co.lesha.minecraftmods.rc_rail_mod;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class RailRemoteControlItem extends Item {

    public RailRemoteControlItem(Settings settings) {
        super(settings);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if(playerEntity.getVehicle() instanceof MinecartEntity mc) {
            playerEntity.playSound(SoundEvents.BLOCK_LEVER_CLICK, 1.0F, 1.0F);

            var bp = RailRemoteControlMod.GetRailUnderMinecartPos(mc);
//            if(bp.isPresent()) {
//                new RailWalker(world, bp.get())
//                  .getNextJunction(playerEntity.getHorizontalFacing(), 80)
//                  .ifPresent(RailJunction::toggle);
            bp.flatMap(blockPos -> new RailWalker(world, blockPos)
              .getNextJunction(playerEntity.getHorizontalFacing(), 80))
              .ifPresent(RailJunction::toggle);

        } else {
            playerEntity.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        }


        return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }
    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        tooltip.add(Text.translatable("item.rc_rail_mod.pult.tooltip"));
    }


}
