package com.jackson.letterfreq.controller

import com.jackson.letterfreq.service.LetterFrequencyService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [LetterFrequencyController::class])
@Import(LetterFrequencyControllerTest.Config::class)
class LetterFrequencyControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var letterFrequencyService: LetterFrequencyService
    @TestConfiguration
    class Config {
        @Bean
        fun letterFrequencyService() = mockk<LetterFrequencyService>()
    }

    @Test
    fun `should return letter frequency`(): Unit = runBlocking {

        val expectedFrequency = mapOf(
            'a' to 5,
            'b' to 3,
            'c' to 2
        )

        coEvery { letterFrequencyService.calculateLetterFrequency() } returns expectedFrequency

        webTestClient.get()
            .uri("/api/letters/frequency")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.a").isEqualTo(5)
            .jsonPath("$.b").isEqualTo(3)
            .jsonPath("$.c").isEqualTo(2)
    }

}








