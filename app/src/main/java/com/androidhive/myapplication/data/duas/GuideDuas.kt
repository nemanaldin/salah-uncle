package com.androidhive.myapplication.data.duas

/** Explicit Hanafi placements only; no guessing a prayer type from an arbitrary translated heading. */
fun guideDuaIds(lessonId: String, stepId: String): List<String> = when (lessonId) {
    "prayer-hanafi" -> when (stepId) {
        "opening" -> listOf("takbir", "thana", "taawwudh-basmala")
        "bowing" -> listOf("ruku")
        "standing" -> listOf("tasmi-tahmid")
        "prostrations" -> listOf("sujud")
        "sitting" -> listOf("tashahhud", "salawat", "rabbana-prayer", "zalamtu")
        "ending" -> listOf("salam")
        else -> emptyList()
    }
    "purification-hanafi" -> when (stepId) {
        "intention-and-hands" -> listOf("start-wudu")
        "after-wudu" -> listOf("after-wudu")
        else -> emptyList()
    }
    "wudu-remembrance" -> if (stepId == "after-wudu") listOf("after-wudu") else emptyList()
    else -> emptyList()
}
