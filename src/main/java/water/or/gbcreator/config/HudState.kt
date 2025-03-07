package water.or.gbcreator.config

import cc.polyfrost.oneconfig.hud.SingleTextHud
import water.or.gbcreator.blocks.BlockCtrl

object HudState : SingleTextHud("State HUD", true) {
        override fun getText(example: Boolean): String = if (BlockCtrl.edit() || example) "Edit Mode Enabled!" else ""
        
        override fun getLines(lines: MutableList<String>, example: Boolean): Unit = with(lines) {
                add(getText(example))
        }
}