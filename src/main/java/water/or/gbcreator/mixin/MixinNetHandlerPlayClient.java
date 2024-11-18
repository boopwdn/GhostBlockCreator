package water.or.gbcreator.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import water.or.gbcreator.event.ChunkLoadedEvent;
import water.or.gbcreator.event.SBlockChangeEvent;
import water.or.gbcreator.utils.UtilsKt;

@Mixin (NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
        @Inject (method = "handleChunkData", at = @At ("TAIL"))
        private void postHandleChunkData(@NotNull S21PacketChunkData packetIn, CallbackInfo ci) {
                UtilsKt.post(new ChunkLoadedEvent(packetIn.getChunkX(), packetIn.getChunkZ()));
        }
        
        @Inject (method = "handleMultiBlockChange", at = @At ("TAIL"))
        private void postHandleMultiBlockChange(@NotNull S22PacketMultiBlockChange packetIn, CallbackInfo ci) {
                for (S22PacketMultiBlockChange.BlockUpdateData data : packetIn.getChangedBlocks()) {
                        UtilsKt.post(new SBlockChangeEvent(data.getPos(), data.getBlockState()));
                }
        }
}
