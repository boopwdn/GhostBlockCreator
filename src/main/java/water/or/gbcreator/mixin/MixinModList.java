package water.or.gbcreator.mixin;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import water.or.gbcreator.Tags;

import java.util.List;
import java.util.Map;

@Mixin (FMLHandshakeMessage.ModList.class)
public class MixinModList {
        @Shadow (remap = false) private Map<String, String> modTags;
        
        @Inject (method = "<init>(Ljava/util/List;)V", at = @At ("RETURN"), remap = false)
        private void hide(List<ModContainer> list, CallbackInfo cbi) {
                if (!Minecraft.getMinecraft().isSingleplayer()) { modTags.remove(Tags.MOD_ID); }
        }
}
