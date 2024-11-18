package water.or.gbcreator.utils

enum class Floor(val floor: String) {
        E("Entrance"),
        F1("Floor 1"),
        F2("Floor 2"),
        F3("Floor 3"),
        F4("Floor 4"),
        F5("Floor 5"),
        F6("Floor 6"),
        F7("Floor 7"),
        M1("Master 1"),
        M2("Master 2"),
        M3("Master 3"),
        M4("Master 4"),
        M5("Master 5"),
        M6("Master 6"),
        M7("Master 7"),
        None("");
        
        val floorNum: Int
                get() {
                        return when (this) {
                                E      -> 0
                                F1, M1 -> 1
                                F2, M2 -> 2
                                F3, M3 -> 3
                                F4, M4 -> 4
                                F5, M5 -> 5
                                F6, M6 -> 6
                                F7, M7 -> 7
                                None   -> -1
                        }
                }
        
        val isMM: Boolean
                get() {
                        return when (this) {
                                E, F1, F2, F3, F4, F5, F6, F7, None -> false
                                M1, M2, M3, M4, M5, M6, M7          -> true
                        }
                }
}
