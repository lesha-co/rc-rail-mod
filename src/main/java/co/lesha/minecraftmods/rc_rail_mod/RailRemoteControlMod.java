package co.lesha.minecraftmods.rc_rail_mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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

  public static Optional<BlockPos> GetRailUnderMinecartPos(MinecartEntity mce) {
    BlockPos bp = mce.getBlockPos();
    if (isRailBlock(mce.world.getBlockState(bp))) {
      return Optional.of(bp);
    }
    BlockPos bp2 = bp.down();

    if (isRailBlock(mce.world.getBlockState(bp2))) {
      return Optional.of(bp2);
    }
    return Optional.empty();
  }

  public static boolean isRailBlock( BlockState bs) {
    return bs.isIn(BlockTags.RAILS);
  }


  public static Optional<RailShape> constructShape(Direction d1, Direction d2) {
    if (d1 == d2 || d1 == Direction.UP || d1 == Direction.DOWN || d2 == Direction.UP || d2 == Direction.DOWN) {
      return Optional.empty();
    }
    switch (d1) {
      case NORTH -> {
        switch (d2) {
          case SOUTH -> {
            return Optional.of(RailShape.NORTH_SOUTH);
          }
          case WEST -> {
            return Optional.of(RailShape.NORTH_WEST);
          }
          case EAST -> {
            return Optional.of(RailShape.NORTH_EAST);
          }
        }
      }
      case SOUTH -> {
        switch (d2) {
          case NORTH -> {
            return Optional.of(RailShape.NORTH_SOUTH);
          }
          case WEST -> {
            return Optional.of(RailShape.SOUTH_WEST);
          }
          case EAST -> {
            return Optional.of(RailShape.SOUTH_EAST);
          }
        }
      }
      case WEST -> {
        switch (d2) {
          case SOUTH -> {
            return Optional.of(RailShape.SOUTH_WEST);
          }
          case NORTH -> {
            return Optional.of(RailShape.NORTH_WEST);
          }
          case EAST -> {
            return Optional.of(RailShape.EAST_WEST);
          }
        }
      }
      case EAST -> {
        switch (d2) {
          case SOUTH -> {
            return Optional.of(RailShape.SOUTH_EAST);
          }
          case WEST -> {
            return Optional.of(RailShape.EAST_WEST);
          }
          case NORTH -> {
            return Optional.of(RailShape.NORTH_EAST);
          }
        }
      }
    }
    return Optional.empty();
  }


  private static ServerTickEvents.EndWorldTick getEndWorldTick() {
    return world -> {
//      for (var player : world.getPlayers()) {
//        var blockPos = GetRailUnderMinecartPos(player);
//        if (blockPos.isEmpty()) continue;
//        var shape = new RailWalker(world, blockPos.get()).getShape();
//        if (shape == null) continue;
//        LOGGER.info(shape.asString());
//      }

    };
  }
}
