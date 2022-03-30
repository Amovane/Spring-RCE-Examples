package com.example

import lombok.extern.slf4j.Slf4j
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Flux
import java.util.function.Supplier

@SpringBootApplication
@Slf4j
open class VictimApplication {

    @Bean
    open fun foo(): Supplier<Flux<Foo>> {
        return Supplier<Flux<Foo>> { Flux.fromArray(arrayOf(Foo("foo1"), Foo("foo2"))).log() }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(VictimApplication::class.java, *args)
        }
    }
}