package com.jackson.letterfreq.listener

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@OptIn(ExperimentalCoroutinesApi::class)
class FileProcessingListenerTest {

    private lateinit var fileProcessingListener: FileProcessingListener
    private val httpClient: HttpClient = mockk()
    private val dispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fileProcessingListener = FileProcessingListener()

        fileProcessingListener.javaClass.getDeclaredField("client").apply {
            isAccessible = true
            set(fileProcessingListener, httpClient)
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `handleFileProcessingEvent should process file and calculate letter frequency`() = runBlocking {

        val fileUrl = "https://raw.githubusercontent.com/lodash/lodash/main/.commitlintrc.js"
        val fileContent = "Sample content with letters"
        val httpResponse: HttpResponse<String> = mockk()

        coEvery { httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns httpResponse
        coEvery { httpResponse.statusCode() } returns 200
        coEvery { httpResponse.body() } returns fileContent

        val result = fileProcessingListener.handleFileProcessingEvent(fileUrl).toList()

        val expectedFrequency = mapOf(
            's' to 2,
            'a' to 1,
            'm' to 1,
            'p' to 1,
            'l' to 2,
            'e' to 4,
            'c' to 1,
            'o' to 1,
            'n' to 2,
            't' to 5,
            'w' to 1,
            'i' to 1,
            'h' to 1,
            'r' to 1
        )
        val expectedResult = expectedFrequency.entries.toList()
        assertEquals(expectedResult, result)
    }

    @Test
    fun `handleFileProcessingEvent should handle IOException`() = runBlocking {

        val fileUrl = "https://raw.githubusercontent.com/lodash/lodash/main/.commitlintrc.js"

        coEvery { httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } throws IOException("Failed to fetch file")

        val result = fileProcessingListener.handleFileProcessingEvent(fileUrl).toList()

        assertEquals(emptyList<Map.Entry<Char, Int>>(), result)
    }
}