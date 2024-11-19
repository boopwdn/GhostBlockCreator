package water.or.gbcreator.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import water.or.gbcreator.event.ClickBlockEvent;
import water.or.gbcreator.utils.UtilsKt;

@Mixin (PlayerControllerMP.class)
public class MixinPlayerControllerMP {
        @Inject (method = "clickBlock", at = @At ("HEAD"), cancellable = true)
        private void preClickBlock(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
                if (UtilsKt.postAndCatch(new ClickBlockEvent(pos, side))) {
                        cir.setReturnValue(true);
                }
        }
}
