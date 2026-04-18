package com.huabu.app.core.native

object HuabuNative {
    init {
        System.loadLibrary("huabu_native")
    }

    external fun getPageSize(): String
    external fun is16KBAligned(): Boolean
}
