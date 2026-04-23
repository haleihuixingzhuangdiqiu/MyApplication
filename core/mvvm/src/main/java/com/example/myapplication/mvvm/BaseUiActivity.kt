package com.example.myapplication.mvvm

import android.content.Intent
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.common.FeatureStandaloneToolbarHost
import com.google.android.material.appbar.MaterialToolbar
import java.util.WeakHashMap

/**
 * 业务 `Activity` 的**壳层基类**（[BaseBindingActivity] / [BaseBindingVmActivity] 等均继承此类）。
 * 负责与「业务长什么样」弱相关、与「全 App 表现一致」强相关的几件事，避免每个页面自己处理一遍。
 *
 * ## 本类管什么
 * 1) **[BarMode] / [setBarMode]**：沉浸式与系统栏显隐。带系统栏时仍可用 `WindowCompat` 做边到边，由 [shouldApplyRootStatusBarInsets] 给根布局补 `statusBars` 顶部 padding。
 * 2) **[ActivityTransition] / [startActivityWithTransition] / [finishWithTransition]**：在 `startActivity` / `finish` 后统一加进入退出动画，避免各页复制 `overridePendingTransition`。
 * 3) **独立 Toolbar 壳**：[standaloneShellLayoutId] + [standaloneToolbarId] 非空时，在 [onCreate] 里自动 `setContentView` 并接好返回键，适合沙箱/独立二级页。与 [BaseBindingActivity] 的 DataBinding 根二选一，勿两边都配。
 * 4) **顶栏/Fragment 与主壳的协作**：实现 [FeatureStandaloneToolbarHost]；子 Fragment 见 [ToolbarHostFragmentKtx] 等。
 *
 * ## 本类不管什么
 * - **不**内建 [com.example.myapplication.mvvm.BaseViewModel]；要 VM + DataBinding 请用 [BaseBindingVmActivity]。
 * - **不**内建 [PageOverlayHost]；整页蒙层在 [BaseBindingActivity] 里可选挂载，或子类在 `setContentView` 后自行 [attachPageOverlayHost] 并自己 `collect` [com.example.myapplication.mvvm.BaseViewModel.pageOverlay]。
 *
 * ## 屏幕密度（AndroidAutoSize）
 * 应用启动时通过 [FrameworkAutoAdaptStrategy] 与 [com.example.myapplication.mvvm.BaseViewModel] 无关；[useAutoSize] 为 `false` 的页面等效于官方 `CancelAdapt`。
 * 已刻意**不再**在 [getResources] 里做密度改写，避免子线程读 `Resources`（如 Lottie）时踩主线程校验。
 */
abstract class BaseUiActivity : AppCompatActivity(), FeatureStandaloneToolbarHost {

    /**
     * 与 [initialBarMode] / [setBarMode] 搭配：带系统栏（适合绝大多数业务页）或全屏隐藏系统栏（视频、引导等）。
     */
    enum class BarMode {
        WITH_SYSTEM_BARS,
        FULLSCREEN,
    }

    /**
     * 成对的进入/离开动画资源（在 `startActivity` 或 `finish` **之后**用 [android.app.Activity.overridePendingTransition] 应用）。
     * [NO_ANIM] 表示不覆盖过渡；常用预设见 [FADE]、[SLIDE_HORIZONTAL]。
     */
    data class ActivityTransition(
        @param:AnimRes val enterAnim: Int = NO_ANIM,
        @param:AnimRes val exitAnim: Int = NO_ANIM,
    ) {
        companion object {
            const val NO_ANIM = -1
            val NONE = ActivityTransition()
            val FADE = ActivityTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            val SLIDE_HORIZONTAL = ActivityTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    /** 初始系统栏模式：默认带系统栏。 */
    protected open val initialBarMode: BarMode = BarMode.WITH_SYSTEM_BARS

    /** startActivity 后默认应用的过渡动画。 */
    protected open val defaultOpenTransition: ActivityTransition = ActivityTransition.NONE

    /** finish 后默认应用的过渡动画。 */
    protected open val defaultCloseTransition: ActivityTransition = ActivityTransition.NONE

    /**
     * 是否由基类对根布局自动应用状态栏顶部 inset。
     * 闪屏等需要维持沉浸式视觉、但又不想隐藏系统栏时，可关闭此行为自行处理。
     */
    protected open val shouldApplyRootStatusBarInsets: Boolean = true

    /**
     * 是否由 [FrameworkAutoAdaptStrategy] 对当前页做 AndroidAutoSize 密度适配。
     * 默认 `true`；闪屏/固定像素页等可改为 `false`（效果同 [me.jessyan.autosize.internal.CancelAdapt]）。
     */
    open val useAutoSize: Boolean get() = true

    /**
     * 独立业务壳（与 [BaseBindingActivity] 互斥）：二者均非 null 时，在 [onCreate] 内 [setContentView] 并绑定 Toolbar。
     */
    @get:LayoutRes
    protected open val standaloneShellLayoutId: Int? = null

    @get:IdRes
    protected open val standaloneToolbarId: Int? = null

    protected var standaloneMaterialToolbar: MaterialToolbar? = null
        private set

    private var currentBarMode: BarMode = initialBarMode
    private var insetsTargetView: View? = null
    private val basePaddings = WeakHashMap<View, Padding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyBarMode(initialBarMode)
        installStandaloneShellIfConfigured()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        installInsetsHandlerIfNeeded()
    }

    private fun installStandaloneShellIfConfigured() {
        val layoutId = standaloneShellLayoutId ?: return
        val toolbarId = standaloneToolbarId ?: return
        setContentView(layoutId)
        val tb = findViewById<MaterialToolbar>(toolbarId)
        standaloneMaterialToolbar = tb
        bindMaterialToolbar(tb) { onStandaloneToolbarNavigateUp() }
    }

    /** 独立壳 Toolbar 导航点击，默认 [finish]。 */
    protected open fun onStandaloneToolbarNavigateUp() {
        finish()
    }

    /** ViewBinding 详情等：在 [setContentView] 根布局之后绑定顶栏。 */
    protected fun bindMaterialToolbar(
        toolbar: MaterialToolbar,
        showHomeAsUp: Boolean = true,
        onNavigateUp: () -> Unit,
    ) {
        setSupportActionBar(toolbar)
        toolbar.isTitleCentered = true
        toolbar.setNavigationOnClickListener { onNavigateUp() }
        supportActionBar?.setDisplayHomeAsUpEnabled(showHomeAsUp)
        supportActionBar?.setDisplayShowHomeEnabled(showHomeAsUp)
    }

    override fun setFeatureToolbarTitle(title: CharSequence) {
        supportActionBar?.title = title
    }

    override fun setFeatureToolbarBackVisible(visible: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(visible)
        supportActionBar?.setDisplayShowHomeEnabled(visible)
    }

    protected fun setBarMode(mode: BarMode) {
        if (mode == currentBarMode) return
        applyBarMode(mode)
    }

    protected fun startActivityWithTransition(
        intent: Intent,
        transition: ActivityTransition = defaultOpenTransition,
    ) {
        startActivity(intent)
        applyTransition(transition)
    }

    protected fun finishWithTransition(
        transition: ActivityTransition = defaultCloseTransition,
    ) {
        finish()
        applyTransition(transition)
    }

    private fun applyBarMode(mode: BarMode) {
        currentBarMode = mode
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        when (mode) {
            BarMode.WITH_SYSTEM_BARS -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

            BarMode.FULLSCREEN -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        requestInsetsIfNeeded()
    }

    @Suppress("DEPRECATION")
    private fun applyTransition(transition: ActivityTransition) {
        if (transition.enterAnim == ActivityTransition.NO_ANIM &&
            transition.exitAnim == ActivityTransition.NO_ANIM
        ) {
            return
        }
        overridePendingTransition(transition.enterAnim, transition.exitAnim)
    }

    private fun installInsetsHandlerIfNeeded() {
        if (!shouldApplyRootStatusBarInsets) {
            insetsTargetView = null
            return
        }
        val content = findViewById<ViewGroup>(android.R.id.content) ?: return
        val root = content.getChildAt(0) ?: return
        if (insetsTargetView === root) {
            requestInsetsIfNeeded()
            return
        }
        insetsTargetView = root
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val base = basePaddings.getOrPut(view) {
                Padding(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
            }
            // 基类只补状态栏顶部 inset，底部导航栏/键盘等交给具体页面按需处理，
            // 这样可以避免统一策略误伤带底部栏或沉浸式内容的页面。
            val statusBarTop = if (currentBarMode == BarMode.WITH_SYSTEM_BARS) {
                windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            } else {
                0
            }
            view.updatePadding(
                left = base.left,
                top = base.top + statusBarTop,
                right = base.right,
                bottom = base.bottom,
            )
            windowInsets
        }
        requestInsetsIfNeeded()
    }

    private fun requestInsetsIfNeeded() {
        insetsTargetView?.let { ViewCompat.requestApplyInsets(it) }
    }

    /**
     * 未使用 [BaseBindingActivity] 时，在 [setContentView] 之后挂载与 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay] 配套的遮罩层；
     * 需在生命周期内自行 [kotlinx.coroutines.flow.collect] 并调用 [PageOverlayHost.render]。
     */
    protected fun attachPageOverlayHost(onRetryClick: () -> Unit): PageOverlayHost =
        PageOverlayHost.attachToActivityContent(this, onRetryClick)

    private data class Padding(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    )
}
