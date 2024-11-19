package org.gmautostop.hitchlogmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform