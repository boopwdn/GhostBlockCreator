package water.or.gbcreator

import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import water.or.gbcreator.blocks.BlockCtrl
import water.or.gbcreator.blocks.BlockStore
import water.or.gbcreator.command.CommandMe

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.MOD_VERSION)
class GhostBlockCreator {
        companion object {
                val logger: Logger = LogManager.getLogger(Tags.MOD_NAME)
        }
        
        @Mod.EventHandler
        @Suppress("Unused")
        fun onInit(event: FMLInitializationEvent) {
                MinecraftForge.EVENT_BUS.register(BlockCtrl)
                ClientCommandHandler.instance.registerCommand(CommandMe)
        }
        
        @Suppress("Unused")
        @Mod.EventHandler
        fun postInit(event: FMLPostInitializationEvent) {
                BlockStore.loadAll()
        }
}