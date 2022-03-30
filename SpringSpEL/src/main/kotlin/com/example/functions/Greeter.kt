package com.example.functions

import java.util.function.Function

open class Greeter : Function<String, String> {
    override fun apply(s: String): String {
        return "Hello $s, and welcome to Spring Cloud Function!!!"
    }
}