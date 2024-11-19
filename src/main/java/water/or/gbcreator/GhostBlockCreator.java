package water.or.gbcreator;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import water.or.gbcreator.blocks.BlockCtrl;
import water.or.gbcreator.blocks.BlockStore;
import water.or.gbcreator.command.CommandMe;
import water.or.gbcreator.config.GBCConfig;
import water.or.gbcreator.utils.UtilsKt;

@Mod (modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.MOD_VERSION)
public class GhostBlockCreator {
        public static final Logger logger = LogManager.getLogger(Tags.MOD_NAME);
        public static GBCConfig config;
        
        @Mod.EventHandler
        public void onInit(FMLInitializationEvent event) {
                config = new GBCConfig();
                MinecraftForge.EVENT_BUS.register(BlockCtrl.INSTANCE);
                ClientCommandHandler.instance.registerCommand(new CommandMe());
                
                UtilsKt.getCFG_DIR();
        }
        
        @Mod.EventHandler
        public void postInit(FMLPostInitializationEvent event) { BlockStore.Companion.load(); }
}
