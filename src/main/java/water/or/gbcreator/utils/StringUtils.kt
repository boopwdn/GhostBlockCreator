package water.or.gbcreator.utils

private val FORMATTING_CODE_PATTERN = Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE)

val String?.noControlCodes: String get() = this?.let { FORMATTING_CODE_PATTERN.replace(it, "") } ?: ""

fun cleanSB(sb: String?): String = sb.noControlCodes.filter { it.code in 21..126 }