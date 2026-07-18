package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.models.CommunityTheme
import java.util.UUID

// Holds a forwarded theme (dev app preview) for the details screen; not
// persisted, replaced on every forward. Each preview gets a unique id so
// the palette cache never collides between distinct forwards.
object TestThemeHolder {

    private const val PREVIEW_ID_PREFIX = "test-preview-"

    var theme: CommunityTheme? = null

    fun newPreviewId(): String =
        PREVIEW_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").take(12)

    fun isPreviewId(id: String): Boolean = id.startsWith(PREVIEW_ID_PREFIX)
}
