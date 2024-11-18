package water.or.gbcreator.utils

import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.Event
import org.apache.logging.log4j.Logger
import water.or.gbcreator.GhostBlockCreator
import water.or.gbcreator.Tags
import water.or.gbcreator.config.GBCConfig
import java.io.File

val CFG_DIR = File(Loader.instance().configDir, Tags.MOD_ID)
val logger: Logger = GhostBlockCreator.logger
val config: GBCConfig get() = GhostBlockCreator.config

fun IChatComponent.msgMe(): Unit = mc.thePlayer.addChatComponentMessage(this)

fun Event.postAndCatch(): Boolean = runCatching {
        MinecraftForge.EVENT_BUS.post(this)
}.onFailure {
        it.printStackTrace()
        logger.error("An error occurred!", it)
}.getOrDefault(isCanceled)

fun Event.post(): Unit = runCatching {
        (MinecraftForge.EVENT_BUS.post(this))
}.onFailure {
        it.printStackTrace()
        logger.error("An error occurred! ", it)
}.let { }

val mc: Minecraft get() = Minecraft.getMinecraft()

val IBlockState.meta: Int get() = block.getMetaFromState(this)

val sidebarLines: List<String>
        get() {
                val sb = mc.theWorld?.scoreboard ?: return emptyList()
                val ob = sb.getObjectiveInDisplaySlot(1) ?: return emptyList()
                
                return sb.getSortedScores(ob)
                        .filter { it?.playerName?.startsWith("#") == false }
                        .let { if (it.size > 15) it.drop(15) else it }
                        .map { ScorePlayerTeam.formatPlayerName(sb.getPlayersTeam(it.playerName), it.playerName) }
        }

private val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

val String?.noControlCodes: String get() = this?.let { FORMATTING_CODE_PATTERN.replace(it, "") } ?: ""

fun cleanSB(sb: String?): String = sb.noControlCodes.filter { it.code in 21..126 }

fun getFloor(): Floor {
        for (i in sidebarLines) {
                return Floor.valueOf(
                        Regex("The Catacombs \\((\\w+)\\)\$").find(cleanSB(i))?.groupValues?.get(1) ?: continue
                )
        }
        return Floor.None
}

fun isF7(): Boolean = getFloor().floorNum == 7