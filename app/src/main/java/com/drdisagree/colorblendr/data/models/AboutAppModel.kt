package com.drdisagree.colorblendr.data.models

import com.drdisagree.colorblendr.ui.adapters.AboutAppAdapter.Companion.TYPE_ABOUT_APP
import com.drdisagree.colorblendr.ui.adapters.AboutAppAdapter.Companion.TYPE_CREDITS_HEADER
import com.drdisagree.colorblendr.ui.adapters.AboutAppAdapter.Companion.TYPE_CREDITS_ITEM

class AboutAppModel {

    var layout = 0
    var icon: String = ""
    var title: String = ""
    var desc: String = ""
    var url: String = ""
    var viewType: Int = TYPE_CREDITS_ITEM

    constructor(layout: Int) {
        this.layout = layout
        this.viewType = TYPE_ABOUT_APP
    }

    constructor(title: String) {
        this.title = title
        this.viewType = TYPE_CREDITS_HEADER
    }

    constructor(title: String, desc: String, url: String, icon: String) {
        this.title = title
        this.desc = desc
        this.icon = icon
        this.url = url
        this.viewType = TYPE_CREDITS_ITEM
    }
}
