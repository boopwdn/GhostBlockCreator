package water.or.gbcreator.command

import net.minecraft.block.Block
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandBase.parseDouble
import net.minecraft.command.ICommandSender
import net.minecraft.command.NumberInvalidException
import net.minecraft.util.BlockPos
import water.or.gbcreator.GhostBlockCreator
import water.or.gbcreator.blocks.BlockCtrl
import water.or.gbcreator.utils.config
import water.or.gbcreator.utils.mc
import water.or.gbcreator.utils.msgTranslate
import java.util.stream.Collectors
import kotlin.math.floor

private open class RegisterSubCmd(key: String, private val runs: Array<out String>.() -> Unit) {
        fun run(args: Array<out String>): Unit = runs(args)
        
        init {
                if (REGISTRY[key] != null) {
                        throw IllegalArgumentException("Key $key has already been registered"); }
                REGISTRY[key] = @Suppress("LeakingThis") this
        }
}

private class RegisterSubCmdEdit(key: String, val range: Int, runs: Array<out String>.(BlockPos) -> Unit) : RegisterSubCmd(
        key,
        execute@{
                if (size <= range) msgTranslate("command.error.few_args", range, size).run { return@execute }
                if (!BlockCtrl.edit()) msgTranslate("command.error.not_edit").run { return@execute }
                if (!BlockCtrl.inBoss()) msgTranslate("command.error.req_boss").run { return@execute }
                reqEnabled { parsePos(this)?.let { runs(it) } }
        }
)

private val REGISTRY = HashMap<String, RegisterSubCmd>()

class CommandMe : CommandBase() {
        override fun getCommandName(): String = "ghostblockcreator"
        
        override fun getCommandUsage(sender: ICommandSender): String = "command.help"
        
        override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender == mc.thePlayer
        
        override fun addTabCompletionOptions(sender: ICommandSender, args: Array<out String>, pos: BlockPos): MutableList<String> {
                if (args.isEmpty()) return mutableListOf()
                if (args.size == 1) return REGISTRY.keys.stream().filter { it.startsWith(args[0]) }.collect(Collectors.toList())
                if (REGISTRY[args[0]] !is RegisterSubCmdEdit) return mutableListOf()
                if (args.size <= 4) return func_175771_a(args, 1, pos)
                return if (args[0] == "set" && args.size == 5) getListOfStringsMatchingLastWord(args, Block.blockRegistry.keys) else mutableListOf()
        }
        
        override fun processCommand(sender: ICommandSender, args: Array<out String>) = if (args.isEmpty()) msgTranslate("command.help")
        else REGISTRY[args[0]].run { if (this != null) run(args) else msgTranslate("command.n_exist", args[0]) }
        
        override fun getCommandAliases(): MutableList<String> = mutableListOf("gbc", "gbcreator", "ghostblockc")
        
        companion object {
                init {
                        RegisterSubCmd("help") { REGISTRY.keys.forEach { msgTranslate("command.$it.desc") } }
                        RegisterSubCmd("edit") { reqEnabled { BlockCtrl.editToggle() } }
                        RegisterSubCmd("cfg") { GhostBlockCreator.config.openGui() }
                        RegisterSubCmdEdit("set", 4) { it addBlock this }
                        RegisterSubCmdEdit("del", 3) { BlockCtrl del it }
                        RegisterSubCmdEdit("get", 3) { BlockCtrl get it }
                }
        }
}

private fun parsePos(args: Array<out String>): BlockPos? = runCatching {
        BlockPos(
                floor(parseDouble(mc.thePlayer.posX, args[1], false)),
                floor(parseDouble(mc.thePlayer.posY, args[2], false)),
                floor(parseDouble(mc.thePlayer.posZ, args[3], false))
        )
}.onFailure { if (it is NumberInvalidException) msgTranslate("command.error.not_numb", it.errorObjects?.get(0)) }.getOrNull()

private fun Array<out String>.reqEnabled(run: Array<out String>.() -> Unit): Unit = if (config.enabled) run() else msgTranslate("command.error.disabled")

private infix fun BlockPos.addBlock(args: Array<out String>) =
        BlockCtrl.set(
                this, args[4].toBlock() ?: msgTranslate("command.error.na_block", args[4]).run { return@addBlock },
                if (args.size > 5) args[5].toMeta() ?: msgTranslate("command.error.not_numb", args[5]).run { return@addBlock } else 0
        )

private fun String.toBlock(): Block? = Block.getBlockFromName(this).also { if (it == null) msgTranslate("command.error.na_block", this) }

private fun String.toMeta(): Int? = runCatching { toInt() }.getOrNull()