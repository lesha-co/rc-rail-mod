package co.lesha.minecraftmods.rc_rail_mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RailRemoteControlMod implements ModInitializer {
  // This logger is used to write text to the console and the log file.
  // It is considered best practice to use your mod id as the logger's name.
  // That way, it's clear which mod wrote info, warnings, and errors.
  public static final Logger LOGGER = LoggerFactory.getLogger("rc_rail_mod");

  @Override
  public void onInitialize() {

    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.
    Register();
    ServerTickEvents.END_WORLD_TICK.register(getEndWorldTick());
  }

  private static void Register() {
    final RailRemoteControlItem CUSTOM_ITEM =
      new RailRemoteControlItem(new FabricItemSettings().maxCount(1));
    Registry.register(Registries.ITEM, new Identifier("rc_rail_mod", "pult"), CUSTOM_ITEM);
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content ->
      content.addAfter(Items.ACTIVATOR_RAIL, CUSTOM_ITEM)
    );
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content ->
      content.addAfter(Items.ACTIVATOR_RAIL, CUSTOM_ITEM)
    );
  }

  /**
   * Returns Minecart entity for given player if they're in one
   * @param player
   * @return
   */
  public static @Nullable MinecartEntity GetMinecart(@NotNull PlayerEntity player) {
    if (player.getVehicle() instanceof MinecartEntity mc) {
      return mc;
    }
    return null;
  }
  public static @Nullable BlockPos GetRailUnderMinecartPos(@NotNull MinecartEntity mce) {
    BlockPos bp = mce.getBlockPos();
    if (isRailBlock(mce.world.getBlockState(bp))) {
      return bp;
    }
    BlockPos bp2 = bp.down();

    if (isRailBlock(mce.world.getBlockState(bp2))) {
      return bp2;
    }
    return null;
  }
  public static @Nullable BlockPos GetRailUnderMinecartPos(@NotNull PlayerEntity player) {
    var mce = GetMinecart(player);
    if (mce != null) {
      return GetRailUnderMinecartPos(mce);
    }
    return null;
  }

  /**
   * Returns block state of rail piece under minecart
   *
   * @param mce minecart instance
   * @return block state
   */
  public static @Nullable BlockState GetRailUnderMinecart(MinecartEntity mce) {
    var pos = GetRailUnderMinecartPos(mce);
    if (pos != null) {

      return mce.world.getBlockState(pos);
    }
    return null;
  }

  public static @Nullable BlockState GetRailUnderMinecart(@NotNull PlayerEntity player) {
    var mce = GetMinecart(player);
    if (mce != null) {
      return GetRailUnderMinecart(mce);
    }
    return null;
  }

  public static boolean isRailBlock(@NotNull BlockState bs) {
    return bs.isIn(BlockTags.RAILS);
  }


  @NotNull
  private static ServerTickEvents.EndWorldTick getEndWorldTick() {
    return world -> {
      for (var player : world.getPlayers()) {
        var blockPos = GetRailUnderMinecartPos(player);
        if (blockPos == null) continue;
        var shape = new RailWalker(world, blockPos).getShape();
        if (shape == null) continue;
//        LOGGER.info(shape.asString());
      }

    };
  }
}
