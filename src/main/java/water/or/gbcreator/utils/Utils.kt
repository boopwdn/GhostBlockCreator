package water.or.gbcreator.utils

import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.Event
import org.apache.logging.log4j.Logger
import water.or.gbcreator.GhostBlockCreator
import water.or.gbcreator.Tags

@JvmField
val CFG_DIR = Loader.instance().configDir.resolve(Tags.MOD_ID).also { it.mkdirs() }

val logger: Logger = GhostBlockCreator.logger

fun msgTranslate(key: String, vararg args: Any?) {
        mc.thePlayer?.addChatComponentMessage(ChatComponentText(I18n.format(key, *args)))
}

fun Event.postAndCatch(): Boolean = runCatching {
        MinecraftForge.EVENT_BUS.post(this)
}.onFailure {
        it.printStackTrace()
        logger.error("An error occurred!", it)
}.getOrDefault(isCanceled)

inline val mc: Minecraft get() = Minecraft.getMinecraft()

inline val IBlockState.meta: Int get() = block.getMetaFromState(this)

inline fun runIf(check: Boolean, crossinline run: () -> Unit) {
        if (check) run()
}