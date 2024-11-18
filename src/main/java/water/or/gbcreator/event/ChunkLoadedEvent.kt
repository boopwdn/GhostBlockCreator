package water.or.gbcreator.event

import net.minecraftforge.fml.common.eventhandler.Event

data class ChunkLoadedEvent(val x: Int, val y: Int): Event()
