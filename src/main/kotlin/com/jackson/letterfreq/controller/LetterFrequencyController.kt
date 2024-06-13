package com.jackson.letterfreq.controller

import com.jackson.letterfreq.service.LetterFrequencyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to expose the letter frequency API
 */
@RestController
@RequestMapping("/api/letters")
class LetterFrequencyController (
    private val letterFrequencyService: LetterFrequencyService
) {

    @GetMapping("/frequency")
    suspend fun getLetterFrequency(): Map<Char, Int> {
        return letterFrequencyService.calculateLetterFrequency()
    }

}