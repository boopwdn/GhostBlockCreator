package water.or.gbcreator.blocks

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import water.or.gbcreator.utils.meta

class BlockData(val pos: BlockPos, val block: Block, val meta: Int) {
        constructor(pos: BlockPos, state: IBlockState) : this(pos, state.block, state.meta)
        
        val state: IBlockState = block.getStateFromMeta(meta)
}