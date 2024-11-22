package water.or.gbcreator.blocks

import net.minecraft.block.Block
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.BlockPos
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import water.or.gbcreator.event.ChunkLoadedEvent
import water.or.gbcreator.event.ClickBlockEvent
import water.or.gbcreator.event.SBlockChangeEvent
import water.or.gbcreator.utils.*

object BlockCtrl {
        @JvmStatic private var edit = false
        @JvmStatic private var curr = BlockStore.EMPTY
        
        @JvmStatic fun check() = curr != BlockStore.EMPTY
        
        @JvmStatic fun edit() = edit
        
        @JvmStatic fun editToggle() {
                if (check()) {
                        if (edit) config.save()
                        edit = !edit
                        msgTranslate("edit_mode.${if (edit) "en" else "dis"}abled")
                }
        }
        
        @JvmStatic fun inactive(repl: BlockStore = BlockStore.EMPTY) {
                if (edit) editToggle()
                curr forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data cleanup pos }
                if (check()) curr.save()
                curr = repl
        }
        
        @JvmStatic fun reactive() {
                curr forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data loadRaw pos }
        }
        
        @JvmStatic fun set(pos: BlockPos, block: Block, meta: Int) {
                (curr.set(pos, BlockData(block, meta)) loadRaw pos)
                        .run { msgTranslate("set_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        @JvmStatic infix fun del(pos: BlockPos) {
                ((curr.del(pos) ?: return) cleanup pos)
                        .run { msgTranslate("del_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        @JvmStatic infix fun get(pos: BlockPos) {
                (world?.getBlockState(pos) ?: return)
                        .run { msgTranslate("get_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        @SubscribeEvent
        fun onEvent(event: ClickBlockEvent) {
                if (config.enabled && check() && edit) del(event.pos)
        }

// TODO: ?!
//        @SubscribeEvent
//        fun onEvent(event: BlockChangeEvent) {
//                if (config.enabled && check() && !edit)
//                        (curr.get(event.pos) ?: return)
//                                .takeIf { it.rawState != null && it.nowState != event.result }
//                                ?.replace(event.pos)
//                                .run { event.isCanceled = true }
//        }
        
        @SubscribeEvent
        fun onEvent(event: ChunkLoadedEvent) {
                if (config.enabled && check()) curr.forEachInChunk(event.pos) { pos, data -> data loadRaw pos }
        }
        
        @SubscribeEvent
        fun onEvent(event: SBlockChangeEvent) {
                if (config.enabled && check()) curr.get(event.pos)?.run { raw(event.update) }
        }
        
        @SubscribeEvent
        fun onEvent(e: WorldEvent.Unload) {
                if (config.enabled && check()) inactive()
        }
        
        @SubscribeEvent
        fun onEvent(e: TickEvent.ClientTickEvent) {
                if (!config.enabled) return
                curr forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data replace pos }
                ((if (isF7()) BlockStore.F7Store else BlockStore.EMPTY)
                        .takeIf { it != curr }?.also { inactive(it) } ?: return).also { reactive() }
        }
}

private inline val world: WorldClient? get() = mc.theWorld

private infix fun BlockData.replace(pos: BlockPos) =
        apply { world?.run { if (getBlockState(pos) != nowState) setBlockState(pos, nowState) } }

private infix fun BlockData.cleanup(pos: BlockPos) =
        apply { if (rawState != null) world?.setBlockState(pos, rawState) }.apply { rawState = null }

private infix fun BlockData.loadRaw(pos: BlockPos) =
        apply { if (rawState == null) raw(world?.getBlockState(pos)) }.replace(pos)