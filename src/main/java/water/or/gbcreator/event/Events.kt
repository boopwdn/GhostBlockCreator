package water.or.gbcreator.event

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event
import water.or.gbcreator.utils.ChunkPos

@Cancelable
data class BlockChangeEvent(val pos: BlockPos, val raw: IBlockState, var result: IBlockState, val world: World) :
Event()

data class ChunkLoadedEvent(val pos: ChunkPos) : Event()

@Cancelable
data class ClickBlockEvent(val pos: BlockPos, val side: EnumFacing) : Event()

data class SBlockChangeEvent(val pos: BlockPos, var update: IBlockState) : Event()