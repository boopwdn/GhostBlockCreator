package water.or.gbcreator.utils

import net.minecraft.util.BlockPos

fun getFloor(): Floor {
        for (i in sidebarLines) {
                return Floor.valueOf(
                        Regex("The Catacombs \\((\\w+)\\)\$").find(cleanSB(i))?.groupValues?.get(1) ?: continue
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