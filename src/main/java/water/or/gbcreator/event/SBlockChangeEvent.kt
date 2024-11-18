package water.or.gbcreator.event

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

data class SBlockChangeEvent(val pos: BlockPos, var update: IBlockState) : Event()