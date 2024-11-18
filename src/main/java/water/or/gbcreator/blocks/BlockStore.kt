package water.or.gbcreator.blocks

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import org.apache.commons.lang3.Validate
import water.or.gbcreator.utils.CFG_DIR
import water.or.gbcreator.utils.logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class BlockStore(cfg: String) {
        private val storage = File(CFG_DIR, "$cfg.json")
        val chunks = HashMap<Pair<Int, Int>, HashSet<BlockData>>()
        val blocks = HashMap<BlockPos, BlockData>()
        
        init {
                registered.add(this)
        }
        
        fun set(pos: BlockPos, block: Block, meta: Int) {
                chunks[pos.chunk]?.removeIf { it.pos == pos }
                val data = BlockData(pos, block, meta)
                chunks.getOrDefault(pos.chunk, HashSet()).add(data)
                blocks[pos] = data
        }
        
        fun del(pos: BlockPos) {
                chunks[pos.chunk]?.removeIf { it.pos == pos }
                blocks.remove(pos)
        }
        
        fun load() {
                if (!storage.exists()) return
                val jin = JsonReader(FileReader(storage))
                jin.beginArray()
                while (jin.hasNext()) loadBlock(jin.nextString().split(','))
                jin.endArray()
                jin.close()
        }
        
        private fun loadBlock(inp: List<String>) {
                if (inp.size != 5) return
                try {
                        set(
                                BlockPos(
                                        Integer.parseInt(inp[0]),
                                        Integer.parseInt(inp[1]),
                                        Integer.parseInt(inp[2])
                                ),
                                Validate.notNull(Block.getBlockFromName(inp[3])),
                                Integer.parseInt(inp[4])
                        )
                } catch (e: Exception) {
                        logger.warn("Error loading ${StringBuilder().also { inp.forEach { inp0 -> it.append(inp0).append(',') } }} e: ${e.message}")
                }
        }
        
        fun save() {
                if (!storage.exists()) storage.createNewFile()
                val jout = JsonWriter(FileWriter(storage))
                jout.setIndent("\t")
                jout.beginArray()
                blocks.values.forEach { jout.value("${it.pos.x},${it.pos.y},${it.pos.z},${it.block.registryName},${it.meta}") }
                jout.endArray()
                jout.close()
        }
        
        companion object {
                val registered = ArrayList<BlockStore>()
                
                val EMPTY = BlockStore("ignored")
                
                val F7Store = BlockStore("floor7")
                
                fun load() = registered.forEach { if (it != EMPTY) it.load() }
        }
}

private val BlockPos.chunk: Pair<Int, Int> get() = Pair(this.x shr 4, this.y shr 4)