package com.grillpoet.bily.nav

import androidx.compose.runtime.mutableStateListOf

class Navigator {

    companion object {
        private const val MAX_STACK = 20
    }

    private val _stack = mutableStateListOf<Screen>(Screen.Main)

    val size: Int get() = _stack.size
    val current: Screen get() = _stack.last()

    fun push(screen: Screen) {
        // 防爆栈
        if (_stack.size >= MAX_STACK) return
        // 相邻重复不入栈
        if (_stack.lastOrNull() == screen) return
        _stack.add(screen)
    }

    fun pop(): Boolean {
        if (_stack.size <= 1) return false
        _stack.removeAt(_stack.lastIndex)
        return true
    }
}