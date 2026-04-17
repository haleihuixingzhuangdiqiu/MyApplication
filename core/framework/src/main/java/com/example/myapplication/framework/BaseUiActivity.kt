package com.example.myapplication.framework

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
 * Activity 通用 UI 基类（[BaseBindingVmActivity] / [BaseBindingActivity] 等均经此类）：
 * 1) 系统栏模式（带系统栏 / 全屏）
 * 2) 页面跳转动画
 * 3) 可选独立壳：覆写 [standaloneShellLayoutId] + [standaloneToolbarId] 即自动 setContentView + Toolbar，并实现 [FeatureStandaloneToolbarHost]
 * 4) 其它非 DataBinding 页（如 ViewBinding 详情）继承本类后调用 [bindMaterialToolbar]
 */
abstract class BaseUiActivity : AppCompatActivity(), FeatureStandaloneToolbarHost {

    enum class BarMode {
        WITH_SYSTEM_BARS,
        FULLSCREEN,
    }

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
     * 未使用 [BaseBindingActivity] 时，在 [setContentView] 之后挂载与 [BaseViewModel.pageOverlay] 配套的遮罩层；
     * 需在生命周期内自行 [kotlinx.coroutines.flow.collect] [BaseViewModel.pageOverlay] 并调用 [PageOverlayHost.render]。
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
