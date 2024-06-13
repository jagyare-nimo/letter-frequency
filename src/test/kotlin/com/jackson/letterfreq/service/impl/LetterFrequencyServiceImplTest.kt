package com.jackson.letterfreq.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jackson.letterfreq.listener.FileProcessingListener
import com.jackson.letterfreq.model.RepoDir
import com.jackson.letterfreq.model.RepoFile
import com.jackson.letterfreq.model.RepoItem
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@OptIn(ExperimentalCoroutinesApi::class)
class LetterFrequencyServiceImplTest {

    private lateinit var letterFrequencyService: LetterFrequencyServiceImpl
    private val fileProcessingListener: FileProcessingListener = mockk()
    private val objectMapper: ObjectMapper = mockk()
    private val httpClient: HttpClient = mockk()
    private val dispatcher = TestCoroutineDispatcher()

    @Value("\${github.url}")
    private val githubUrl: String = "https://api.github.com/repos/lodash/lodash/contents/"

    @Value("\${github.token}")
    private val githubToken: String = "your_github_token"

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        letterFrequencyService = spyk(
            LetterFrequencyServiceImpl(
                fileProcessingListener,
                githubUrl,
                githubToken,
                objectMapper
            )
        )

        letterFrequencyService.javaClass.getDeclaredField("client").apply {
            isAccessible = true
            set(letterFrequencyService, httpClient)
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `calculateLetterFrequency should return correct frequency`() = runBlocking {

        val repoFile = RepoFile(name = "test.js", downloadUrl = "https://raw.githubusercontent.com/lodash/lodash/main/test.js")
        val repoDir = RepoDir(name = "dir", url = "https://api.github.com/repos/lodash/lodash/contents/dir")
        val items = listOf<RepoItem>(repoFile, repoDir)
        val responseContent = """[{"type": "file", "name": "test.js", "download_url": "https://raw.githubusercontent.com/lodash/lodash/main/test.js"}]"""
        val httpResponse: HttpResponse<String> = mockk()

        coEvery { letterFrequencyService.fetchRepoItems(any()) } returns items
        every { fileProcessingListener.getGlobalLetterFrequency() } returns mapOf('a' to 5, 'b' to 3)
        coEvery { letterFrequencyService.fetchRepoItems(repoDir.url) } returns listOf(repoFile)
        every { httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns httpResponse
        every { httpResponse.statusCode() } returns 200
        every { httpResponse.body() } returns responseContent
        every { objectMapper.readValue(any<String>(), any<TypeReference<List<RepoItem>>>()) } returns listOf(repoFile)

        coEvery { fileProcessingListener.handleFileProcessingEvent(repoFile.downloadUrl) } returns flowOf(
            mapOf('a' to 1, 'b' to 2).entries.first(),
            mapOf('a' to 1, 'b' to 2).entries.last()
        )

        val result = letterFrequencyService.calculateLetterFrequency()

        assertEquals(mapOf('a' to 5, 'b' to 3), result)
        coVerify { letterFrequencyService.fetchRepoItems(githubUrl) }
    }

    @Test
    fun `fetchRepoItems should fetch items from GitHub API`() = runBlocking {

        val apiUrl = "https://api.github.com/repos/lodash/lodash/contents/"
        val responseContent = """[{"type": "file", "name": "test.js", "download_url": "https://raw.githubusercontent.com/lodash/lodash/main/test.js"}]"""
        val httpResponse: HttpResponse<String> = mockk()
        val expectedItems = listOf(RepoFile("test.js", "https://raw.githubusercontent.com/lodash/lodash/main/test.js"))

        every { httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns httpResponse
        every { httpResponse.statusCode() } returns 200
        every { httpResponse.body() } returns responseContent
        every { objectMapper.readValue(responseContent, any<TypeReference<List<RepoItem>>>()) } returns expectedItems


        val result = letterFrequencyService.fetchRepoItems(apiUrl)

        assertEquals(expectedItems, result)
        verify { httpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) }
    }

}