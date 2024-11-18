package water.or.gbcreator.config

import cc.polyfrost.oneconfig.hud.SingleTextHud
import water.or.gbcreator.blocks.BlockCtrl

class HudState : SingleTextHud("State HUD", true) {
        private val value: String = "Edit Mode Enabled!"
        
        override fun getText(example: Boolean): String = if (BlockCtrl.edit() || example) value else ""
        
        override fun getLines(lines: MutableList<String>, example: Boolean): Unit = with(lines) { add(getText(example)) }
}