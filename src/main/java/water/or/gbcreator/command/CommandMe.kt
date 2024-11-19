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

private data class RegisterSubCmd(val key: String, val havpos: Boolean = false, val runs: (Array<out String>) -> (Unit)) {
        init {
                if (REGISTRY[key] != null) {
                        throw IllegalArgumentException("Key $key has already been registered"); }
                REGISTRY[key] = this
        }
}

private val REGISTRY = HashMap<String, RegisterSubCmd>()

class CommandMe : CommandBase() {
        override fun getCommandName(): String = "ghostblockcreator"
        
        override fun getCommandUsage(sender: ICommandSender): String = "command.help"
        
        override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = sender == mc.thePlayer
        
        override fun addTabCompletionOptions(sender: ICommandSender, args: Array<out String>, pos: BlockPos): MutableList<String> {
                if (args.isEmpty()) return mutableListOf()
                if (args.size == 1) return REGISTRY.keys.stream().filter { it.startsWith(args[0]) }.collect(Collectors.toList())
                if (!(REGISTRY[args[0]]?.havpos ?: return mutableListOf())) return mutableListOf()
                if (args.size <= 4) return func_175771_a(args, 1, pos)
                return if (args[0] == "set" && args.size == 5) getListOfStringsMatchingLastWord(args, Block.blockRegistry.keys) else mutableListOf()
        }
        
        override fun processCommand(sender: ICommandSender, args: Array<out String>) = if (args.isEmpty()) msgTranslate("command.help")
        else REGISTRY[args[0]].run { if (this != null) runs(args) else msgTranslate("command.n_exist", args[0]) }
        
        override fun getCommandAliases(): MutableList<String> = mutableListOf("gbc", "gbcreator", "ghostblockc")
        
        companion object {
                init {
                        RegisterSubCmd("help") { REGISTRY.keys.forEach { msgTranslate("command.$it.desc") } }
                        RegisterSubCmd("edit") { reqEnabled { BlockCtrl.editToggle() } }
                        RegisterSubCmd("cfg") { GhostBlockCreator.config.openGui() }
                        RegisterSubCmd("set", true) { reqArgsCnt(4, it.size - 1) { reqEnabled { reqEditing { parsePos(it)?.addBlock(it) ?: return@RegisterSubCmd } } } }
                        RegisterSubCmd("del", true) { reqArgsCnt(3, it.size - 1) { reqEnabled { reqEditing { BlockCtrl.del(parsePos(it) ?: return@RegisterSubCmd) } } } }
                        RegisterSubCmd("get", true) { reqArgsCnt(3, it.size - 1) { reqEnabled { BlockCtrl.get(parsePos(it) ?: return@RegisterSubCmd) } } }
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


private inline fun reqArgsCnt(req: Int, argc: Int, runs: () -> (Unit)) = if (argc >= req) runs() else msgTranslate("command.error.few_args", req, argc)
private inline fun reqEnabled(runs: () -> (Unit)) = if (config.enabled) runs() else msgTranslate("command.error.disabled")
private inline fun reqEditing(runs: () -> (Unit)) = if (BlockCtrl.edit()) runs() else msgTranslate("command.error.not_edit")

private fun BlockPos.addBlock(args: Array<out String>) =
        BlockCtrl.set(
                this, args[4].toBlock() ?: msgTranslate("command.error.na_block", args[4]).run { return@addBlock },
                if (args.size > 5) args[5].toMeta() ?: msgTranslate("command.error.not_numb", args[5]).run { return@addBlock } else 0
        )

private fun String.toBlock(): Block? = Block.getBlockFromName(this).also { if (it == null) msgTranslate("command.error.na_block", this) }

private fun String.toMeta(): Int? = runCatching { toInt() }.getOrNull()