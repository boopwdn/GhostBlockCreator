package water.or.gbcreator.command

import net.minecraft.block.Block
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandBase.parseBlockPos
import net.minecraft.command.ICommandSender
import net.minecraft.command.NumberInvalidException
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentTranslation
import water.or.gbcreator.GhostBlockCreator
import water.or.gbcreator.blocks.BlockCtrl
import water.or.gbcreator.utils.config
import water.or.gbcreator.utils.mc
import water.or.gbcreator.utils.meta
import water.or.gbcreator.utils.msgMe
import java.io.Serializable
import java.util.stream.Collectors

private data class Info(val sender: ICommandSender, val args: Array<out String>) : Serializable {
        override fun equals(other: Any?): Boolean = this === other || other is Info && sender == other.sender && args.contentDeepEquals(other.args)
        
        override fun hashCode(): Int = 31 * sender.hashCode() + args.contentHashCode()
}

private data class RegisterSubCmd(val key: String, val runs: (Info) -> (Unit)) {
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
        
        override fun addTabCompletionOptions(sender: ICommandSender, args: Array<out String>, pos: BlockPos): MutableList<String> = if (args.size == 1) REGISTRY.keys.stream().filter { it.startsWith(args[0]) }.collect(Collectors.toList()) else mutableListOf()
        
        override fun processCommand(sender: ICommandSender, args: Array<out String>) = if (args.isEmpty())
                ChatComponentTranslation("command.help").msgMe()
        else REGISTRY[args[0]].run { if (this != null) runs(Info(sender, args)) else ChatComponentTranslation("command.n_exist", args[0]).msgMe() }
        
        override fun getCommandAliases(): MutableList<String> = mutableListOf("gbc", "gbcreator", "ghostblockc")
        
        companion object {
                init {
                        RegisterSubCmd("help") { REGISTRY.keys.forEach { ChatComponentTranslation("command.$it.desc").msgMe() } }
                        RegisterSubCmd("edit") { reqEnabled { BlockCtrl.toggle() } }
                        RegisterSubCmd("cfg") { GhostBlockCreator.config.openGui() }
                        RegisterSubCmd("set") { reqArgsCnt(5, it.args.size - 1) { reqEnabled { reqEditing { parsePos(it.sender, it.args)?.addBlock(it.args) } } } }
                        RegisterSubCmd("del") { reqArgsCnt(3, it.args.size - 1) { reqEnabled { reqEditing { parsePos(it.sender, it.args)?.delBlock() } } } }
                        RegisterSubCmd("get") { reqArgsCnt(3, it.args.size - 1) { reqEnabled { parsePos(it.sender, it.args)?.getBlock() } } }
                }
        }
}

private fun parsePos(sender: ICommandSender, args: Array<out String>): BlockPos? = runCatching {
        parseBlockPos(sender, args, 1, false)
}.onFailure {
        if (it is NumberInvalidException) ChatComponentTranslation("command.error.not_numb", it.errorObjects?.get(0))
}.getOrNull()

private inline fun reqArgsCnt(req: Int, argc: Int, runs: () -> (Unit)) = if (argc >= req) runs() else ChatComponentTranslation("command.error.few_args", req, argc).msgMe()

private inline fun reqEnabled(runs: () -> (Unit)) = if (config.enabled) runs() else ChatComponentTranslation("command.error.disabled").msgMe()

private inline fun reqEditing(runs: () -> (Unit)) = if (BlockCtrl.edit()) runs() else ChatComponentTranslation("command.error.not_edit").msgMe()

private fun BlockPos.addBlock(args: Array<out String>) = add(this, Block.getBlockFromName(args[4]), args[5])

private fun add(pos: BlockPos, block: Block?, meta: String) = if (block != null) parse(meta)?.let { BlockCtrl.set(pos, block, it) } else ChatComponentTranslation("command.error.na_block").msgMe()

private fun BlockPos.delBlock() = BlockCtrl.del(this)

private fun BlockPos.getBlock() = mc.theWorld.getBlockState(this)?.let { ChatComponentTranslation("command.get_block.block_msg", x, y, z, it.block, it.meta).msgMe() }

private fun parse(arg: String): Int? = runCatching {
        Integer.parseInt(arg)
}.onFailure {
        ChatComponentTranslation("command.add_block.not_num", arg).msgMe()
}.getOrNull()