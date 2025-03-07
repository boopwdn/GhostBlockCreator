package water.or.gbcreator.blocks

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import water.or.gbcreator.utils.CFG_DIR
import water.or.gbcreator.utils.ChunkPos

class BlockStore(name: String) {
        private val file = File(CFG_DIR, "$name.json")
        
        private val chunks = HashMap<ChunkPos, HashSet<BlockPos>>()
        private val blocks = HashMap<BlockPos, BlockData>()
        
        init {
                registry.add(this)
        }
        
        fun set(pos: BlockPos, data: BlockData): BlockData = with(pos) {
                data.also {
                        chunks.getOrPut(chunk, ::HashSet).add(pos)
                        it raw blocks[this]?.rawState
                        blocks[this] = it
                }
        }
        
        fun del(pos: BlockPos): BlockData? = with(pos) {
                chunks[chunk]?.remove(pos)
                blocks.remove(pos)
        }
        
        fun get(pos: BlockPos): BlockData? = blocks[pos]
        
        fun load() = if (file.exists()) JsonReader(FileReader(file)).use {
                with(it) {
                        beginArray()
                        while (hasNext()) runCatching {
                                nextString().split(',').let { inp ->
                                        if (inp.size != 5) return@runCatching
                                        set(BlockPos(inp[0].toInt(), inp[1].toInt(), inp[2].toInt()),
                                            BlockData(requireNotNull(Block.getBlockFromName(inp[3])), inp[4].toInt()))
                                }
                        }
                        endArray()
                }
        } else Unit
        
        fun save(): JsonWriter = JsonWriter(FileWriter(file)).use {
                with(it) {
                        setIndent("\t")
                        beginArray()
                        blocks.forEach { (pos, data) ->
                                value("${pos.x},${pos.y},${pos.z},${data.block.registryName},${data.meta}")
                        }
                        endArray()
                }
        }
        
        infix fun forEach(action: (BlockPos, BlockData) -> (Unit)) = blocks.forEach { action(it.key, it.value) }
        
        fun forEachInChunk(pos: ChunkPos, action: (BlockPos, BlockData) -> (Unit)) =
        chunks[pos]?.forEach { action(it, blocks[it]!!) }
        
        companion object {
                val EMPTY = BlockStore("ignored")
                
                val F7Store = BlockStore("floor7")
                
                fun loadAll() = registry.forEach { if (it != EMPTY) it.load() }
        }
}

private val registry: MutableList<BlockStore> = ArrayList()

private inline val BlockPos.chunk: ChunkPos get() = ChunkPos(x shr 4, z shr 4)