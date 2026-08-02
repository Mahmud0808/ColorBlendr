package com.drdisagree.colorblendr.data.models

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

    companion object {
        const val TYPE_ABOUT_APP = 0
        const val TYPE_CREDITS_HEADER = 1
        const val TYPE_CREDITS_ITEM = 2
    }
}
