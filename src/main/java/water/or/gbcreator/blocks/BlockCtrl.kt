package water.or.gbcreator.blocks

import net.minecraft.block.Block
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.BlockPos
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import water.or.gbcreator.event.BlockChangeEvent
import water.or.gbcreator.event.ChunkLoadedEvent
import water.or.gbcreator.event.ClickBlockEvent
import water.or.gbcreator.event.SBlockChangeEvent
import water.or.gbcreator.utils.*

object BlockCtrl {
        private var edit = false
        private var curr = BlockStore.EMPTY
        
        fun empty() = curr == BlockStore.EMPTY
        
        fun edit() = edit
        
        fun editToggle() {
                if (empty()) return
                if (edit) config.save()
                edit = !edit
                msgTranslate("edit_mode.${if (edit) "en" else "dis"}abled")
                
        }
        
        fun inactive(repl: BlockStore = BlockStore.EMPTY) {
                if (edit) editToggle()
                cleanupAll()
                if (!empty()) curr.save()
                curr = repl
        }
        
        fun reactive() {
                loadRawAll()
                replaceAll()
        }
        
        private fun replaceAll() = curr.forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data.replaceIt(pos) }
        private fun cleanupAll() = curr.forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data.cleanupIt(pos) }
        private fun loadRawAll() = curr.forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data.loadRawIt(pos) }
        
        fun set(pos: BlockPos, block: Block, meta: Int) {
                curr.set(pos, BlockData(block, meta)).replaceIt(pos)
                        .run { msgTranslate("set_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        fun del(pos: BlockPos) {
                (curr.del(pos) ?: return).cleanupIt(pos)
                        .run { msgTranslate("del_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        fun get(pos: BlockPos) {
                (world?.getBlockState(pos) ?: return)
                        .run { msgTranslate("get_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        @SubscribeEvent
        fun onEvent(event: ClickBlockEvent) {
                if (!config.enabled || !edit || empty()) return
                del(event.pos)
        }
        
// TODO: ?!
//        @SubscribeEvent
//        fun onEvent(event: BlockChangeEvent) {
//                if (!config.enabled || edit || empty()) return
//                (curr.get(event.pos) ?: return).takeIf { it.nowState != event.result }?.replaceIt(event.pos).run { event.isCanceled = true }
//        }
        
        @SubscribeEvent
        fun onEvent(event: ChunkLoadedEvent) {
                if (!config.enabled || empty()) return
                curr.forEachInChunk(event.pos) { pos, data ->
                        data.loadRawIt(pos).replaceIt(pos)
                }
        }
        
        @SubscribeEvent
        fun onEvent(event: SBlockChangeEvent) {
                if (!config.enabled || empty()) return
                curr.get(event.pos)?.run { raw(event.update) }
        }
        
        @SubscribeEvent
        fun onEvent(e: WorldEvent.Unload) {
                if (!config.enabled || empty()) return
                inactive()
        }
        
        @SubscribeEvent
        fun onEvent(e: TickEvent.ClientTickEvent) {
                if (!config.enabled) return
                replaceAll()
                val r = if (isF7()) BlockStore.F7Store else BlockStore.EMPTY
                if (r == curr) return
                inactive(r)
                reactive()
        }
}

private inline val world: WorldClient? get() = mc.theWorld

private fun BlockData.replaceIt(pos: BlockPos) = apply { world?.run { if (getBlockState(pos) != nowState) setBlockState(pos, nowState) } }

private fun BlockData.cleanupIt(pos: BlockPos) = apply { if (rawState != null) world?.setBlockState(pos, rawState) }.apply { rawState = null }

private fun BlockData.loadRawIt(pos: BlockPos) = apply { if (rawState == null) raw(world?.getBlockState(pos)) }