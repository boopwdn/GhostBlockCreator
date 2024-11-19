package water.or.gbcreator.event

import net.minecraftforge.fml.common.eventhandler.Event
import water.or.gbcreator.utils.ChunkPos

data class ChunkLoadedEvent(val pos: ChunkPos) : Event()
