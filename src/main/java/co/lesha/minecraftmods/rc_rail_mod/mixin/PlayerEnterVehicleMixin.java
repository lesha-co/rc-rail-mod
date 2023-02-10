package co.lesha.minecraftmods.rc_rail_mod.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class PlayerEnterVehicleMixin {
//  @Inject(method = "startRiding()Z", at = @At("RETURN"), cancellable = true)
//  private void onEnterVehicle(Entity entity, CallbackInfoReturnable<Boolean> ci) {
//    Entity maybePlayer = ((Entity) (Object) this);
//    boolean thisIsPlayer = maybePlayer instanceof PlayerEntity;
//    boolean entityIsMinecart = entity instanceof MinecartEntity;
//    if (entityIsMinecart && thisIsPlayer && ci.getReturnValue()) {
//      RailRemoteControlMod.LOGGER.debug("onEnterVehicle " + maybePlayer.getName() +"->"+ entity.getName());
//    }
//  }
//
//  @Inject(method = "stopRiding()V", at = @At("RETURN"), cancellable = true)
//  private void onExitVehicle(CallbackInfo ci) {
//    Entity maybePlayer = ((Entity) (Object) this);
//    boolean thisIsPlayer = maybePlayer instanceof PlayerEntity;
//    if (thisIsPlayer) {
//      RailRemoteControlMod.LOGGER.debug("onExitVehicle " + maybePlayer.getName());
//    }
//  }
}
