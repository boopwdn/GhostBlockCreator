package water.or.gbcreator.utils

import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.Event
import org.apache.logging.log4j.Logger
import water.or.gbcreator.GhostBlockCreator
import water.or.gbcreator.Tags
import water.or.gbcreator.config.GBCConfig

val CFG_DIR = Loader.instance().configDir.resolve(Tags.MOD_ID).also { it.mkdir() }
val logger: Logger = GhostBlockCreator.logger
inline val config: GBCConfig get() = GhostBlockCreator.config

fun IChatComponent.msgMe() = Unit.also { mc.thePlayer?.addChatComponentMessage(this) }

fun msgTranslate(key: String, vararg args: Any?) = ChatComponentText(I18n.format(key, *args)).msgMe()

fun Event.postAndCatch(): Boolean = runCatching {
        MinecraftForge.EVENT_BUS.post(this)
}.onFailure {
        it.printStackTrace()
        logger.error("An error occurred!", it)
}.getOrDefault(isCanceled)

inline val mc: Minecraft get() = Minecraft.getMinecraft()

inline val IBlockState.meta: Int get() = block.getMetaFromState(this)

inline val sidebarLines: List<String>
        get() {
                val sb = mc.theWorld?.scoreboard ?: return emptyList()
                val ob = sb.getObjectiveInDisplaySlot(1) ?: return emptyList()
                
                return sb.getSortedScores(ob)
                        .filter { it?.playerName?.startsWith("#") == false }
                        .let { if (it.size > 15) it.drop(15) else it }
                        .map { ScorePlayerTeam.formatPlayerName(sb.getPlayersTeam(it.playerName), it.playerName) }
        }

