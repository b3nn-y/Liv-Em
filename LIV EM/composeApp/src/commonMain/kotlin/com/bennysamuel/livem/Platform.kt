package com.bennysamuel.livem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform