package water.or.gbcreator.command

import java.util.stream.Collectors
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import net.minecraft.block.Block
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandBase.*
import net.minecraft.command.ICommandSender
import net.minecraft.command.NumberInvalidException
import net.minecraft.util.BlockPos
import water.or.gbcreator.blocks.BlockCtrl
import water.or.gbcreator.config.GBCConfig
import water.or.gbcreator.utils.mc
import water.or.gbcreator.utils.msgTranslate

private open class RegisterSubCmd(key: String, private val runs: Array<out String>.() -> Unit) {
        fun run(args: Array<out String>): Unit = runs(args)
        
        init {
                if (REGISTRY[key] != null) {
                        throw IllegalArgumentException("Key $key has already been registered"); }
                REGISTRY[key] = @Suppress("LeakingThis") this
        }
}

private class RegisterSubCmdEdit(key: String, val range: Int, runs: Array<out String>.(BlockPos) -> Unit) :
RegisterSubCmd(
        key,
        execute@{
                if (size <= range) {
                        msgTranslate("command.error.few_args", range, size)
                        return@execute
                }
                if (!BlockCtrl.edit()) {
                        msgTranslate("command.error.not_edit")
                        return@execute
                }
                if (!BlockCtrl.inBoss()) {
                        msgTranslate("command.error.req_boss")
                        return@execute
                }
                reqEnabled { parsePos(1)?.let { runs(it) } }
        }
)

private val REGISTRY = HashMap<String, RegisterSubCmd>()

object CommandMe : CommandBase() {
        override fun getCommandName(): String = "ghostblockcreator"
        
        override fun getCommandUsage(sender: ICommandSender): String = "command.help"
        
        override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender == mc.thePlayer
        
        override fun addTabCompletionOptions(
                sender: ICommandSender,
                args: Array<out String>,
                pos: BlockPos
        ): MutableList<String> {
                if (args.isEmpty()) return mutableListOf()
                if (args.size == 1) return REGISTRY.keys.stream().filter { it.startsWith(args[0]) }
                .collect(Collectors.toList())
                if (REGISTRY[args[0]] !is RegisterSubCmdEdit) return mutableListOf()
                if (args.size <= 4) return func_175771_a(args, 1, pos)
                if (args[0] == "set" && args.size == 5)
                        return getListOfStringsMatchingLastWord(args, Block.blockRegistry.keys)
                if (args[0] == "fill") {
                        if (args.size <= 7) return func_175771_a(args, 4, pos)
                        if (args.size == 8) return getListOfStringsMatchingLastWord(args, Block.blockRegistry.keys)
                }
                return mutableListOf()
        }
        
        override fun processCommand(sender: ICommandSender, args: Array<out String>) =
        if (args.isEmpty()) msgTranslate("command.help")
        else REGISTRY[args[0]].run { if (this != null) run(args) else msgTranslate("command.n_exist", args[0]) }
        
        override fun getCommandAliases(): MutableList<String> = mutableListOf("gbc", "gbcreator", "ghostblockc")
        
        init {
                RegisterSubCmd("help") { REGISTRY.keys.forEach { msgTranslate("command.$it.desc") } }
                RegisterSubCmd("edit") { reqEnabled { BlockCtrl.editToggle() } }
                RegisterSubCmd("cfg") { GBCConfig.openGui() }
                RegisterSubCmdEdit("set", 4) {
                        readBlock(4)?.let { (block, meta) ->
                                BlockCtrl.set(it, block, meta)
                        }
                }
                RegisterSubCmdEdit("del", 3) { BlockCtrl del it }
                RegisterSubCmdEdit("get", 3) { BlockCtrl get it }
                RegisterSubCmdEdit("fill", 6) { f ->
                        val t = parsePos(4) ?: return@RegisterSubCmdEdit
                        val minX = min(f.x, t.x)
                        val maxX = max(f.x, t.x)
                        val minY = min(f.y, t.y)
                        val maxY = max(f.y, t.y)
                        val minZ = min(f.z, t.z)
                        val maxZ = max(f.z, t.z)
                        readBlock(7)?.let { (block, meta) ->
                                for (x in minX..maxX) for (y in minY..maxY) for (z in minZ..maxZ) {
                                        BlockCtrl.set(BlockPos(x, y, z), block, meta)
                                }
                        } ?: run {
                                for (x in minX..maxX) for (y in minY..maxY) for (z in minZ..maxZ) {
                                        BlockCtrl del BlockPos(x, y, z)
                                }
                        }
                }
        }
}

private infix fun Array<out String>.parsePos(offset: Int): BlockPos? = runCatching {
        BlockPos(
                floor(parseDouble(mc.thePlayer.posX, this[offset], false)),
                floor(parseDouble(mc.thePlayer.posY, this[offset + 1], false)),
                floor(parseDouble(mc.thePlayer.posZ, this[offset + 2], false))
        )
}.onFailure { if (it is NumberInvalidException) msgTranslate("command.error.not_numb", it.errorObjects?.get(0)) }
.getOrNull()

private fun Array<out String>.reqEnabled(run: Array<out String>.() -> Unit): Unit =
if (GBCConfig.enabled) run() else msgTranslate("command.error.disabled")

private infix fun Array<out String>.readBlock(offset: Int): Pair<Block, Int>? {
        val block = this[offset].toBlock()
        if (block === null) {
                msgTranslate("command.error.na_block", this[4])
                return null
        }
        
        return block to if (this.size > offset + 1) {
                val meta = this[offset + 1].toMeta()
                if (meta === null) {
                        msgTranslate("command.error.not_numb", this[5])
                        return null
                }
                meta
        } else 0
}

private fun String.toBlock(): Block? =
Block.getBlockFromName(this).also { if (it == null) msgTranslate("command.error.na_block", this) }

private fun String.toMeta(): Int? = runCatching { toInt() }.getOrNull()