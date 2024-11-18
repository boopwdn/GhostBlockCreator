package water.or.gbcreator.event

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
data class ClickBlockEvent(val pos: BlockPos, val side: EnumFacing) : Event()
