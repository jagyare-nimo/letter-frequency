package com.jackson.letterfreq.controller

import com.jackson.letterfreq.service.LetterFrequencyService
import com.jackson.letterfreq.util.Logger.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to expose the letter frequency API
 */
@RestController
@RequestMapping("/api/letters")

class LetterFrequencyController(
    private val letterFrequencyService: LetterFrequencyService
) {
    @GetMapping("/frequency")
    suspend fun getLetterFrequency(): ResponseEntity<Map<Char, Int>> {
        logger.info { "Received request to fetch files" }
        return try {
            val frequency = letterFrequencyService.calculateLetterFrequency()
            ResponseEntity.ok(frequency)
        } catch (exception: Exception) {
            logger.error { "Error serving request:: ${exception.message}" }
            ResponseEntity.internalServerError().build()
        }
    }

}