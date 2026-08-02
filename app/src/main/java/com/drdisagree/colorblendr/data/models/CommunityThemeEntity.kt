package com.drdisagree.colorblendr.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_THEME_TABLE

// Cached community theme: raw validated payload + sortable columns. Payload
// re-validated through the codec on every read, so stale/corrupt cache rows
// degrade to "absent", never to a malformed theme.
@Entity(tableName = COMMUNITY_THEME_TABLE)
data class CommunityThemeEntity(
    @PrimaryKey val id: String,
    val payloadJson: String,
    val upvotes: Int,
    val downloads: Int,
    val createdAt: Long,
    val fetchedAt: Long
)
