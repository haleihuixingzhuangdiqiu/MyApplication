package com.example.myapplication.social.adapter

import androidx.annotation.Keep
import com.example.myapplication.social.SocialFeedEntry

@Keep
data class SocialFeedRowModel(
    val entryId: Int,
    val title: String,
    val anchorLine: String,
    /** 当前搜索框用于高亮的文案（trim 后与列表过滤一致）。 */
    val searchQuery: String,
    val coverUrl: String,
    val isFollowed: Boolean,
) {
    companion object {
        fun from(item: SocialFeedEntry, isFollowed: Boolean, searchQueryTrimmed: String): SocialFeedRowModel =
            SocialFeedRowModel(
                entryId = item.id,
                title = item.title,
                anchorLine = item.anchorLine,
                searchQuery = searchQueryTrimmed,
                coverUrl = item.coverUrl,
                isFollowed = isFollowed,
            )
    }
}
