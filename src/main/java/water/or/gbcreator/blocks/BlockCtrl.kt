package water.or.gbcreator.blocks

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentTranslation
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import water.or.gbcreator.event.BlockChangeEvent
import water.or.gbcreator.event.ChunkLoadedEvent
import water.or.gbcreator.event.ClickBlockEvent
import water.or.gbcreator.event.SBlockChangeEvent
import water.or.gbcreator.utils.config
import water.or.gbcreator.utils.isF7
import water.or.gbcreator.utils.mc
import water.or.gbcreator.utils.msgMe

object BlockCtrl {
        
        private var edit = false
        private val raw = HashMap<BlockPos, BlockData>()
        private var current: BlockStore = BlockStore.EMPTY
        
        private fun empty(): Boolean = current == BlockStore.EMPTY
        
        fun toggle() {
                if (!config.enabled || empty()) return
                edit = !edit
                ChatComponentTranslation("edit_mode.${if (edit) "en" else "dis"}abled").msgMe()
                if (!edit) current.save()
                refresh(true)
        }
        
        fun edit(): Boolean = edit
        
        fun refresh(enabled: Boolean) {
                if (empty() || mc.theWorld == null) return
                if (enabled) {
                        if (raw.isEmpty()) current.blocks.keys.forEach {
                                raw[it] = BlockData(it, mc.theWorld.getBlockState(it))
                                mc.theWorld.setBlockState(it, current.blocks[it]?.state ?: return)
                        }
                } else {
                        if (raw.isNotEmpty()) raw.keys.forEach {
                                mc.theWorld.setBlockState(it, raw[it]?.state ?: return)
                        }
                        if (edit) {
                                current.save()
                                edit = false
                        }
                        raw.clear()
                }
        }
        
        fun set(pos: BlockPos, block: Block, meta: Int) {
                if (!config.enabled || empty()) return
                current.set(pos, block, meta)
                val state: IBlockState = mc.theWorld.getBlockState(pos)
                if (raw[pos] == null) raw[pos] = BlockData(pos, state.block, state.block.getMetaFromState(state))
        }
        
        fun del(pos: BlockPos) {
                if (!config.enabled || empty()) return
                current.del(pos)
                mc.theWorld.setBlockState(pos, raw.remove(pos)?.state ?: return)
        }
        
        @SubscribeEvent
        fun onEvent(event: ClickBlockEvent) {
                if (!config.enabled || !edit || empty()) return
                if (raw.containsKey(event.pos)) {
                        del(event.pos)
                        event.isCanceled = true
                }
        }
        
        @SubscribeEvent
        fun onEvent(event: BlockChangeEvent) {
                if (!config.enabled || edit || empty()) return
                event.result = current.blocks[event.pos]?.state ?: return
        }
        
        @SubscribeEvent
        fun onEvent(event: ChunkLoadedEvent) {
                if (!config.enabled || empty()) return
                current.chunks[Pair(event.x, event.y)]?.forEach {
                        raw[it.pos] = BlockData(it.pos, mc.theWorld.getBlockState(it.pos))
                        mc.theWorld.setBlockState(it.pos, it.state)
                }
        }
        
        @SubscribeEvent
        fun onEvent(event: SBlockChangeEvent) {
                if (!config.enabled || empty()) return
                if (current.blocks.containsKey(event.pos)) raw[event.pos] = BlockData(event.pos, event.update)
        }
        
        @SubscribeEvent
        fun onEvent(event: WorldEvent.Load) {
                if (!config.enabled || empty()) return
                if (edit) toggle()
                current = BlockStore.EMPTY
                raw.clear()
        }
        
        private var ticks: Int = 0
        
        @SubscribeEvent
        fun onEvent(event: TickEvent.ClientTickEvent) {
                if (!config.enabled) return
                if (ticks == 0) current.blocks.values.forEach { mc.theWorld.setBlockState(it.pos, it.state) }
                ticks = (ticks + 1) % 20
                val result: BlockStore = if (isF7()) BlockStore.F7Store else BlockStore.EMPTY
                if (result == current) return
                if (edit && !empty()) toggle()
                current = result
                refresh(true)
        }
}