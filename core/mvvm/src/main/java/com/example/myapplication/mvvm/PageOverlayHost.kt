package com.example.myapplication.mvvm

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.myapplication.mvvm.PageOverlayState
import com.google.android.material.button.MaterialButton

/**
 * 全屏遮罩视图控制器；由 [attachToActivityContent] 挂到 [android.R.id.content]，或由 Fragment 包一层 [android.widget.FrameLayout] 后挂入。
 */
class PageOverlayHost internal constructor(
    private val root: View,
    private val onRetryClick: () -> Unit,
) {

    private val loadingPanel: View = root.findViewById(R.id.framework_overlay_loading)
    private val emptyText: TextView = root.findViewById(R.id.framework_overlay_empty)
    private val errorPanel: View = root.findViewById(R.id.framework_overlay_error)
    private val errorMessage: TextView = root.findViewById(R.id.framework_overlay_error_message)
    private val retryButton: MaterialButton = root.findViewById(R.id.framework_overlay_retry)

    init {
        retryButton.setOnClickListener { onRetryClick() }
        render(PageOverlayState.Hidden)
    }

    fun render(state: PageOverlayState) {
        when (state) {
            PageOverlayState.Hidden -> {
                root.isVisible = false
            }

            PageOverlayState.Loading -> {
                root.isVisible = true
                loadingPanel.isVisible = true
                emptyText.isVisible = false
                errorPanel.isVisible = false
            }

            is PageOverlayState.Empty -> {
                root.isVisible = true
                loadingPanel.isVisible = false
                emptyText.isVisible = true
                val hint = state.hint?.takeIf { it.isNotBlank() }
                emptyText.text = hint ?: root.context.getString(R.string.framework_page_overlay_empty_default)
                errorPanel.isVisible = false
            }

            is PageOverlayState.Error -> {
                root.isVisible = true
                loadingPanel.isVisible = false
                emptyText.isVisible = false
                errorPanel.isVisible = true
                errorMessage.text = state.message
                retryButton.isVisible = state.allowRetry
            }
        }
    }

    companion object {
        fun attachToActivityContent(activity: Activity, onRetryClick: () -> Unit): PageOverlayHost {
            val content = activity.findViewById<ViewGroup>(android.R.id.content)
            val overlay = LayoutInflater.from(activity).inflate(
                R.layout.framework_page_overlay,
                content,
                false,
            )
            // 遮罩直接挂在 content 最上层，不侵入业务布局结构；
            // 这样 DataBinding / ViewBinding / 纯 View 页面都能复用同一套全屏状态 UI。
            content.addView(
                overlay,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
            return PageOverlayHost(overlay, onRetryClick)
        }
    }
}
