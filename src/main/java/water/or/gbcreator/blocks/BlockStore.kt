package water.or.gbcreator.blocks

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import org.apache.commons.lang3.Validate
import water.or.gbcreator.utils.CFG_DIR
import water.or.gbcreator.utils.ChunkPos
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class BlockStore(name: String) {
        private val file = File(CFG_DIR, "$name.json")
        
        private val chunks = HashMap<ChunkPos, HashSet<BlockPos>>()
        private val blocks = HashMap<BlockPos, BlockData>()
        
        init {
                registry.add(this)
        }
        
        fun set(pos: BlockPos, data: BlockData): BlockData = pos.apply { chunks.getOrPut(chunk) { HashSet() }.add(pos) }.let { data raw blocks[it]?.rawState; blocks[it] = data; data }
        
        fun del(pos: BlockPos): BlockData? = pos.run { chunks[chunk]?.remove(pos); blocks.remove(pos) }
        
        fun get(pos: BlockPos): BlockData? = blocks[pos]
        
        fun load() = if (file.exists()) JsonReader(FileReader(file)).run {
                beginArray()
                while (hasNext()) runCatching {
                        nextString().split(',').takeIf { it.size == 5 }?.let { inp ->
                                set(
                                        BlockPos(inp[0].toInt(), inp[1].toInt(), inp[2].toInt()),
                                        BlockData(Validate.notNull(Block.getBlockFromName(inp[3])), inp[4].toInt())
                                )
                        }
                }
                endArray()
                close()
        } else Unit
        
        fun save() = JsonWriter(FileWriter(file.apply { if (!exists()) createNewFile() })).run {
                setIndent("\t")
                beginArray()
                blocks.forEach { (pos, data) -> value("${pos.x},${pos.y},${pos.z},${data.block.registryName},${data.meta}") }
                endArray()
                close()
        }
        
        infix fun forEach(action: (BlockPos, BlockData) -> (Unit)) = blocks.forEach { action(it.key, it.value) }
        
        fun forEachInChunk(pos: ChunkPos, action: (BlockPos, BlockData) -> (Unit)) = chunks[pos]?.forEach { action(it, blocks[it]!!) }
        
        companion object {
                @JvmStatic val EMPTY = BlockStore("ignored")
                
                @JvmStatic val F7Store = BlockStore("floor7")
                
                @JvmStatic fun loadAll() = registry.forEach { if (it != EMPTY) it.load() }
        }
}

private val registry = ArrayList<BlockStore>()

private inline val BlockPos.chunk: ChunkPos get() = ChunkPos(x shr 4, z shr 4)