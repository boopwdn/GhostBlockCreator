package water.or.gbcreator.blocks

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState

class BlockData(val block: Block, val meta: Int) {
        var rawState: IBlockState? = null
        val nowState: IBlockState = block.getStateFromMeta(meta)
        
        fun raw(rawState: IBlockState?) = Unit.also { this.rawState = rawState }
}

