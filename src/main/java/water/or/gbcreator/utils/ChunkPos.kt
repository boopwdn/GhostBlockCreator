package water.or.gbcreator.utils

import net.minecraft.util.BlockPos

data class ChunkPos(val x: Int, val z: Int) {
        constructor(block: BlockPos) : this(block.x shr 4, block.y shr 4)
}