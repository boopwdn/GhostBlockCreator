package water.or.gbcreator.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Button
import cc.polyfrost.oneconfig.config.annotations.Checkbox
import cc.polyfrost.oneconfig.config.annotations.HUD
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import water.or.gbcreator.Tags
import water.or.gbcreator.blocks.BlockCtrl
import water.or.gbcreator.blocks.BlockStore

object GBCConfig : Config(Mod(Tags.MOD_NAME, ModType.SKYBLOCK), Tags.MOD_ID + ".json") {
        @Transient
        @Button(name = "Reload Config", text = "Reload", category = "Main")
        val reloadConfigs = Runnable {
                if (BlockCtrl.edit()) BlockCtrl.editToggle()
                BlockStore.loadAll()
        }
        
        @Checkbox(name = "Only In Boss", category = "Main")
        var onlyInBoss = false
        
        @HUD(name = "State HUD", category = "Main")
        var hudState = HudState()
        
        @Transient
        private var wasEnabled = enabled
        
        init {
                initialize()
        }
        
        override fun save() {
                super.save()
                if (wasEnabled == enabled) return
                wasEnabled = enabled
                if (BlockCtrl.notEmpty()) if (enabled) BlockCtrl.reactive() else BlockCtrl.inactive()
        }
}