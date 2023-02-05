package co.lesha.minecraftmods.rc_rail_mod.mixin;

import co.lesha.minecraftmods.rc_rail_mod.RailRemoteControlMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PlayerEnterVehicleMixin {
  @Inject(method = "startRiding()Z", at = @At("RETURN"), cancellable = true)
  private void onEnterVehicle(Entity entity, CallbackInfoReturnable<Boolean> ci) {
    Entity maybePlayer = ((Entity) (Object) this);
    boolean thisIsPlayer = maybePlayer instanceof PlayerEntity;
    boolean entityIsMinecart = entity instanceof MinecartEntity;
    if (entityIsMinecart && thisIsPlayer && ci.getReturnValue()) {
      RailRemoteControlMod.LOGGER.debug("onEnterVehicle " + maybePlayer.getName() +"->"+ entity.getName());
    }
  }

  @Inject(method = "stopRiding()V", at = @At("RETURN"), cancellable = true)
  private void onExitVehicle(CallbackInfo ci) {
    Entity maybePlayer = ((Entity) (Object) this);
    boolean thisIsPlayer = maybePlayer instanceof PlayerEntity;
    if (thisIsPlayer) {
      RailRemoteControlMod.LOGGER.debug("onExitVehicle " + maybePlayer.getName());
    }
  }
}
