package com.example.myapplication.social

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

internal object SearchHighlight {

    /**
     * 将 [needle] 在 [fullText] 中不区分大小写的所有匹配段标为 [highlightColor]（与搜索框字面一致的可视反馈）。
     */
    fun build(fullText: String, needle: String, highlightColor: Int): CharSequence {
        val q = needle.trim()
        if (q.isEmpty()) return fullText
        val sp = SpannableString(fullText)
        val lowerFull = fullText.lowercase()
        val lowerNeedle = q.lowercase()
        var start = 0
        while (true) {
            val idx = lowerFull.indexOf(lowerNeedle, start)
            if (idx < 0) break
            val end = idx + q.length
            if (end <= fullText.length) {
                sp.setSpan(
                    ForegroundColorSpan(highlightColor),
                    idx,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            start = idx + 1
        }
        return sp
    }
}
