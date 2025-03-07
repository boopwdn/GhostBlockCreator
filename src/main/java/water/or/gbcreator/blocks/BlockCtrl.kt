package water.or.gbcreator.blocks

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import water.or.gbcreator.config.GBCConfig
import water.or.gbcreator.event.ChunkLoadedEvent
import water.or.gbcreator.event.ClickBlockEvent
import water.or.gbcreator.event.SBlockChangeEvent
import water.or.gbcreator.utils.isF7
import water.or.gbcreator.utils.mc
import water.or.gbcreator.utils.meInBoss
import water.or.gbcreator.utils.meta
import water.or.gbcreator.utils.msgTranslate
import water.or.gbcreator.utils.runIf

object BlockCtrl {
        private var edit = false
        private var curr = BlockStore.EMPTY
        
        fun notEmpty() = curr !== BlockStore.EMPTY
        
        fun inBoss() = !GBCConfig.onlyInBoss || meInBoss()
        
        fun valid() = GBCConfig.enabled && inBoss() && notEmpty()
        
        fun edit() = edit
        
        fun editToggle() = if (notEmpty()) {
                if (edit) GBCConfig.save()
                edit = !edit
                msgTranslate("edit_mode.${if (edit) "en" else "dis"}abled")
        } else msgTranslate("edit_mode.invalid")
        
        fun inactive(repl: BlockStore = BlockStore.EMPTY, save: Boolean = true) {
                if (edit) editToggle()
                curr forEach { pos, data -> data cleanup pos }
                if (curr !== BlockStore.EMPTY && save) curr.save()
                curr = repl
        }
        
        fun reactive() = curr forEach { pos, data -> data loadRaw pos }
        
        fun set(pos: BlockPos, block: Block, meta: Int) {
                (curr.set(pos, BlockData(block, meta)) loadRaw pos)
                .run { msgTranslate("set_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        infix fun del(pos: BlockPos) {
                ((curr.del(pos) ?: return) cleanup pos)
                .run { msgTranslate("del_block.message", block.registryName, meta, pos.x, pos.y, pos.z) }
        }
        
        infix fun get(pos: BlockPos) {
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
        runIf(valid()) { curr.forEachInChunk(event.pos) { pos, data -> data raw null; data loadRaw pos } }
        
        @SubscribeEvent
        fun onEvent(event: SBlockChangeEvent) =
        runIf(valid()) { curr.get(event.pos)?.run { runIfBlockLoaded(event.pos) { raw(event.update) } } }
        
        @SubscribeEvent
        @Suppress("Unused")
        fun onEvent(event: WorldEvent.Unload) = runIf(valid()) { inactive() }
        
        @SubscribeEvent
        @Suppress("Unused")
        fun onEvent(event: TickEvent.ClientTickEvent) = runIf(GBCConfig.enabled) {
                curr forEach { pos, data -> data replace pos }
                (if (isF7() && inBoss()) BlockStore.F7Store else BlockStore.EMPTY)
                .takeIf { it != curr }?.also { inactive(it); reactive() }
        }
}

private inline val world: WorldClient? get() = mc.theWorld

private inline fun BlockData.runIfBlockLoaded(pos: BlockPos, crossinline run: WorldClient.() -> Any): BlockData =
also { world?.run { if (getChunkFromBlockCoords(pos).isLoaded && isBlockLoaded(pos)) run(this) } }

private infix fun BlockData.replace(pos: BlockPos) =
runIfBlockLoaded(pos) { if (getBlockState(pos) != nowState) setBlockStateT(pos, nowState) }

private infix fun BlockData.cleanup(pos: BlockPos) =
runIfBlockLoaded(pos) { rawState?.let { setBlockStateT(pos, it) }; raw(null) }

private infix fun BlockData.loadRaw(pos: BlockPos) =
runIfBlockLoaded(pos) { if (rawState == null) raw(getBlockState(pos)); replace(pos) }

private fun World.setBlockStateT(pos: BlockPos, state: IBlockState) {
        setBlockState(pos, state)
        state.block.createTileEntity(this, state)?.let { setTileEntity(pos, it) } ?: removeTileEntity(pos)
}