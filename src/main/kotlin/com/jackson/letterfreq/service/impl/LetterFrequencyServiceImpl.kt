package com.jackson.letterfreq.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jackson.letterfreq.listener.FileProcessingListener
import com.jackson.letterfreq.model.RepoDir
import com.jackson.letterfreq.model.RepoFile
import com.jackson.letterfreq.model.RepoItem
import com.jackson.letterfreq.service.LetterFrequencyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


@Service
class LetterFrequencyServiceImpl(
    private val fileProcessingListener: FileProcessingListener,
    @Value("\${github.url}") private val githubUrl: String,
    @Value("\${github.token}") private val githubToken: String,
    private val objectMapper: ObjectMapper
) : LetterFrequencyService {

    private val client = HttpClient.newHttpClient()

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun calculateLetterFrequency(): Map<Char, Int> {
        val items = fetchRepoItems(githubUrl)
        val flowList = processItems(items)

        flowList
            .asFlow()
            .flatMapMerge { it }
            .collect()

        val letterFrequency = fileProcessingListener.getGlobalLetterFrequency()
        return letterFrequency.entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    private fun processItems(items: List<RepoItem>): List<Flow<Map.Entry<Char, Int>>> {
        return items.flatMap { item ->
            when (item) {
                is RepoFile -> {
                    if (item.name.endsWith(".js") || item.name.endsWith(".ts")) {
                        listOf(fileProcessingListener.handleFileProcessingEvent(item.downloadUrl))
                    } else emptyList()
                }
                is RepoDir -> {
                    val dirItems: List<RepoItem> = runBlocking { fetchRepoItems(item.url) }
                    processItems(dirItems)
                }
            }
        }
    }

    override suspend fun fetchRepoItems(apiUrl: String): List<RepoItem> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization", "token $githubToken")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw IOException("Failed to fetch items from GitHub: ${response.statusCode()} - ${response.body()}")
        }

        objectMapper.readValue(response.body(), object : TypeReference<List<RepoItem>>() {})
    }

    override fun getItemUrl(): String = githubUrl

}