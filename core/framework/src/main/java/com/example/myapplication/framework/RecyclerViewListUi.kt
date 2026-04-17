package com.example.myapplication.framework

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

/**
 * 关闭「内容变更」时的 item 过渡动画。
 *
 * 局部刷新（如仅关注/购物车状态变化触发 [RecyclerView.Adapter.notifyItemChanged]）时，
 * 默认 [androidx.recyclerview.widget.DefaultItemAnimator] 会对整项做 change 动画（常表现为整行含封面短暂 alpha 闪烁），
 * 与 Coil 是否重新加载无关。
 */
fun RecyclerView.disableItemChangeAnimations() {
    val animator = itemAnimator
    if (animator is SimpleItemAnimator) {
        animator.supportsChangeAnimations = false
    }
}
