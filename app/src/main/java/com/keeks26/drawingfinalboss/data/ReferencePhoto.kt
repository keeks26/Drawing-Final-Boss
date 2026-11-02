package com.keeks26.drawingfinalboss.data

data class ReferencePhoto(
    val id: String,
    val storagePath: String,
    val difficulty: Int,
    val title: String? = null,
    val poseType: String? = null,
    val isFullBody: Boolean? = null,
    val attribution: String? = null,
    val license: String? = null
) {
    val description: String get() = title ?: poseType ?: "Drawing reference"
    val caption: String get() = listOfNotNull(
        title, poseType,
        when (isFullBody) { true -> "Full-body"; false -> "Detail study"; null -> null },
        attribution, license
    ).joinToString(" · ")
}

enum class DrawingLevel(val difficulty: Int, val title: String, val description: String) {
    LEVEL_ONE(1, "Level 1 · Foundations", "Relaxed poses for learning proportion and gesture."),
    LEVEL_TWO(2, "Level 2 · Action", "Movement and weight shifts to practise gesture drawing."),
    FINAL_BOSS(3, "Level 3 · Final Boss", "Difficult angles and viewpoints for a greater challenge.")
}

data class PhotoPage(
    val photos: List<ReferencePhoto>,
    val nextCursor: String? = null,
    val fromCache: Boolean = false
)
