package water.or.gbcreator.utils

import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.BlockPos

fun getFloor(): Floor {
        val sidebarLines = run {
                val sb = mc.theWorld?.scoreboard ?: return@run emptyList()
                val ob = sb.getObjectiveInDisplaySlot(1) ?: return@run emptyList()
                
                return@run sb.getSortedScores(ob)
                .filter { it?.playerName?.startsWith("#") == false }
                .let { if (it.size > 15) it.drop(15) else it }
                .map { ScorePlayerTeam.formatPlayerName(sb.getPlayersTeam(it.playerName), it.playerName) }
        }
        
        for (i in sidebarLines) {
                return Floor.valueOf(
                        (Regex("The Catacombs \\((\\w+)\\)\$").find(cleanSB(i)) ?: continue).groupValues[1]
                )
                
        }
        return Floor.None
}

fun isF7(): Boolean = getFloor().floorNum == 7
fun BlockPos.isInBoss(f: Floor) = when (f.floorNum) {
        1       -> x > -71 && z > -39
        in 2..4 -> x > -39 && z > -39
        in 5..6 -> x > -39 && z > -7
        7       -> x > -7 && z > -7
        else    -> false
}

fun meInBoss() = mc.thePlayer.position.isInBoss(getFloor())