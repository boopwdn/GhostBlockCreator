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
        @JvmStatic private var edit = false
        @JvmStatic private var curr = BlockStore.EMPTY
        
        @JvmStatic fun notEmpty() = curr != BlockStore.EMPTY
        
        @JvmStatic fun valid() = !config.enabled && notEmpty()
        
        @JvmStatic fun edit() = edit
        
        @JvmStatic fun editToggle() = if (notEmpty()) {
                if (edit) config.save()
                edit = !edit
                msgTranslate("edit_mode.${if (edit) "en" else "dis"}abled")
        } else msgTranslate("edit_mode.invalid")
        
        @JvmStatic fun inactive(repl: BlockStore = BlockStore.EMPTY) {
                if (edit) editToggle()
                curr forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data cleanup pos }
                if (curr != BlockStore.EMPTY) curr.save()
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
        fun onEvent(event: ClickBlockEvent) = runIf(valid() && edit) { del(event.pos) }
        
        // TODO: ?!
//        @SubscribeEvent
//        fun onEvent(event: BlockChangeEvent) = runIf(valid() && !edit) {
//                (curr.get(event.pos) ?: return@runIf)
//                        .takeIf { it.rawState != null && it.nowState != event.result }
//                        ?.replace(event.pos)
//                        .run { event.isCanceled = true }
//        }
        
        @SubscribeEvent
        fun onEvent(event: ChunkLoadedEvent) =
                runIf(valid()) { curr.forEachInChunk(event.pos) { pos, data -> data loadRaw pos } }
        
        @SubscribeEvent
        fun onEvent(event: SBlockChangeEvent) =
                runIf(valid()) { curr.get(event.pos)?.run { raw(event.update) } }
        
        @SubscribeEvent
        fun onEvent(e: WorldEvent.Unload) =
                runIf(valid()) { inactive() }
        
        @SubscribeEvent
        fun onEvent(e: TickEvent.ClientTickEvent) = runIf(config.enabled) {
                curr forEach { pos, data -> if (world?.isBlockLoaded(pos) ?: return@forEach) data replace pos }
                ((if (isF7()) BlockStore.F7Store else BlockStore.EMPTY)
                         .takeIf { it != curr }?.also { inactive(it) } ?: return@runIf).also { reactive() }
        }
}

private inline val world: WorldClient? get() = mc.theWorld

private infix fun BlockData.replace(pos: BlockPos) =
        apply { world?.run { if (getBlockState(pos) != nowState) setBlockState(pos, nowState) } }

private infix fun BlockData.cleanup(pos: BlockPos) =
        apply { if (rawState != null) world?.setBlockState(pos, rawState) }.apply { rawState = null }

private infix fun BlockData.loadRaw(pos: BlockPos) =
        apply { if (rawState == null) raw(world?.getBlockState(pos)) }.replace(pos)