package org.gmautostop.hitchlogmp.domain

import kotlin.js.Promise

@JsModule("jszip")
@JsNonModule
external class JSZip {
    fun file(name: String, data: dynamic): JSZip
    fun generateAsync(options: dynamic): Promise<Any>
}
