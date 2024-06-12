package com.jackson.letterfreq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LetterFrequencyApplication

fun main(args: Array<String>) {
	runApplication<LetterFrequencyApplication>(*args)
}
