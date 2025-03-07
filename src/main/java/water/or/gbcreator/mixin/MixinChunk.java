package water.or.gbcreator.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import water.or.gbcreator.event.BlockChangeEvent;
import water.or.gbcreator.utils.UtilsKt;

@Mixin (Chunk.class)
public abstract class MixinChunk {
        @Shadow @Final private World worldObj;
        
        @Inject (method = "setBlockState", at = @At ("HEAD"), cancellable = true)
        private void onBlockChange(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
                if (UtilsKt.postAndCatch(new BlockChangeEvent(pos, getBlockState(pos), state, worldObj))) {
                        cir.setReturnValue(getBlockState(pos));
                }
        }
        
        @Shadow public abstract IBlockState getBlockState(final BlockPos pos);
}
