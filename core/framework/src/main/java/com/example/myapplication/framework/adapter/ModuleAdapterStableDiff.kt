package com.example.myapplication.framework.adapter

import androidx.recyclerview.widget.DiffUtil
import com.tory.module_adapter.base.NormalModuleAdapter

/**
 * ModuleAdapter 内置 [com.tory.module_adapter.base.RvDiffCallback] 用引用相等判断「同一项」，
 * 每次 emit 新的 data class 实例会导致整表被当成变更，出现整行闪烁。
 *
 * 使用稳定业务 id 做 [DiffUtil.Callback.areItemsTheSame]，内容用 [equals]（[areContentsTheSame]），
 * 仅真正变化的行会收到 [androidx.recyclerview.widget.RecyclerView.Adapter.notifyItemChanged]。
 *
 * 库侧局部刷新 API：[NormalModuleAdapter.refresh]（单条替换 + notifyItemChanged），
 * 本扩展适合整列表仍从 Flow 下发、但需正确增量 diff 的场景。
 */
fun NormalModuleAdapter.setItemsWithStableItemDiff(
    newItems: List<Any>,
    stableItemId: (Any) -> Any,
    contentsEqual: (Any, Any) -> Boolean = { a, b -> a == b },
) {
    if (itemCount == 0) {
        setItems(newItems)
        return
    }
    val oldItems = getItems().toList()
    setItemsWithDiff(
        newItems,
        object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                stableItemId(oldItems[oldItemPosition]) == stableItemId(newItems[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                contentsEqual(oldItems[oldItemPosition], newItems[newItemPosition])
        },
    )
}
