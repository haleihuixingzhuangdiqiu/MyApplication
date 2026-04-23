package com.example.myapplication.navigation

/**
 * ARouter 跳转轨迹（由 [com.alibaba.android.arouter.facade.template.IInterceptor] 在放行前写入）。
 *
 * ARouter **不提供**完整 Activity 返回栈管理；系统栈由 Task + launchMode 控制。
 * 这里仅做**调试/埋点/轻量回溯**，复杂需求请在业务层单独维护状态机。
 */
object RouteStack {

    private const val MAX = 48
    private val deque = ArrayDeque<String>(MAX)
    private val lock = Any()

    fun record(path: String?) {
        val p = path?.trim().orEmpty()
        if (p.isEmpty()) return
        synchronized(lock) {
            if (deque.lastOrNull() != p) {
                deque.addLast(p)
            }
            while (deque.size > MAX) {
                deque.removeFirst()
            }
        }
    }

    fun snapshot(): List<String> = synchronized(lock) { deque.toList() }

    fun clear() {
        synchronized(lock) { deque.clear() }
    }
}
