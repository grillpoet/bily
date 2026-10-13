package com.grillpoet.bily.nav

sealed interface Screen {
    data object Main : Screen
    data object Search : Screen
    data class Video(val bvid: String) : Screen
    data class Space(val mid: Long, val name: String) : Screen
}