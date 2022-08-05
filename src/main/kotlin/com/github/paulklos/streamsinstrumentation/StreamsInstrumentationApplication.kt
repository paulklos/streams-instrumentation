package com.github.paulklos.streamsinstrumentation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StreamsInstrumentationApplication

fun main(args: Array<String>) {
	runApplication<StreamsInstrumentationApplication>(*args)
}
