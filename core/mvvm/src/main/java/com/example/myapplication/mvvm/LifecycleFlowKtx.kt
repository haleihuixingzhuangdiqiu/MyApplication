package com.example.myapplication.mvvm

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 在 [repeatOnLifecycle] 的块内，用「多路 [launch] + [Flow.collect]」的 **DSL 简写**（见 [collectFlows]）。
 */
class FlowCollectInRepeatScope internal constructor(
    private val coroutineScope: CoroutineScope,
) {
    /**
     * 对此 [Flow] 启动一次 [collect]（在内部 [CoroutineScope] 上 [launch]），与其它 `onValue` 并行。
     *
     * 命名上避免与 [kotlinx.coroutines.flow.onEach]（中间件算子、返回新 Flow）混淆。
     */
    fun <T> Flow<T>.onValue(consumer: (T) -> Unit) {
        coroutineScope.launch {
            collect(consumer)
        }
    }
}

/**
 * 在 [androidx.lifecycle.repeatOnLifecycle]（[minState] 默认 [Lifecycle.State.STARTED]）下并行 collect 多路 [Flow]；
 * 无参调用即「页面对用户可见/活跃时」收流，常配合 [FlowCollectInRepeatScope.onValue] 使用。
 *
 * 需要 [Lifecycle.State.CREATED] 等其它起始状态时传 [minState]。
 *
 * - **Activity / 普通 [LifecycleOwner]**：用本方法。
 * - **Fragment 且与 View 强相关**：用 [collectViewFlows]，或自行在 [androidx.fragment.app.Fragment.getViewLifecycleOwner] 上调用 [collectFlows]。
 */
fun LifecycleOwner.collectFlows(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    block: FlowCollectInRepeatScope.() -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(minState) {
            FlowCollectInRepeatScope(this).block()
        }
    }
}

/**
 * 在 [Fragment] 的 [androidx.fragment.app.Fragment.getViewLifecycleOwner] 上 [collectFlows]（默认 [minState] 为 [Lifecycle.State.STARTED]），
 * 适合与 View 强绑定的 Flow；[androidx.fragment.app.Fragment.onDestroyView] 后随 View 停止收集。
 */
fun Fragment.collectViewFlows(
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    block: FlowCollectInRepeatScope.() -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(minState) {
            FlowCollectInRepeatScope(this).block()
        }
    }
}
