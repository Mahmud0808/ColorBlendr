package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.models.CommunityTheme

// Holds a pasted theme (PR review / moderation) for the details screen; not
// persisted, replaced on every paste.
object TestThemeHolder {

    const val TEST_THEME_ID = "test-preview"

    var theme: CommunityTheme? = null
}
