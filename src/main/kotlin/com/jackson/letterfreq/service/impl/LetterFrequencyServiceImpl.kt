package com.jackson.letterfreq.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jackson.letterfreq.listener.FileProcessingListener
import com.jackson.letterfreq.model.RepoDir
import com.jackson.letterfreq.model.RepoFile
import com.jackson.letterfreq.model.RepoItem
import com.jackson.letterfreq.service.LetterFrequencyService
import com.jackson.letterfreq.util.Logger.logger
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

/**
  Service implementation for letter frequency operations
 */
@Service
class LetterFrequencyServiceImpl(
    private val fileProcessingListener: FileProcessingListener,
    @Value("\${github.url}") private val githubUrl: String,
    @Value("\${github.token}") private val githubToken: String,
    private val objectMapper: ObjectMapper
) : LetterFrequencyService {

    private val client = HttpClient.newHttpClient()

    /**
      Function to calculate the frequency of letters in the items
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun calculateLetterFrequency(): Map<Char, Int> {
        return try {
           logger.info { "Starting letter frequency calculation" }

            val items = fetchRepoItems(githubUrl)
            val flowList = processItems(items)

            flowList
                .asFlow()
                .flatMapMerge { it }
                .collect()

            val letterFrequency = fileProcessingListener.getGlobalLetterFrequency()

            logger.info { "Successfully calculated letter frequency" }

            letterFrequency.entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }

        } catch (exception: Exception) {
            logger.error { "Error occurred during letter frequency calculation: ${exception.message}" }
            throw exception
        }
    }

    /**
      Function to process items
     */
    fun processItems(items: List<RepoItem>): List<Flow<Map.Entry<Char, Int>>> {
        return items.flatMap { item ->
            try {
                return@flatMap when (item) {

                    is RepoFile -> {
                        if (item.name.endsWith(".js") || item.name.endsWith(".ts")) {
                            logger.info { "Processing file: ${item.name}" }
                            listOf(fileProcessingListener.handleFileProcessingEvent(item.downloadUrl))
                        } else {
                            logger.warn { "Skipping non-JS/TS file: ${item.name}" }
                            emptyList()
                        }
                    }

                    is RepoDir -> {
                        logger.info { "Processing directory: ${item.name}" }
                        val dirItems: List<RepoItem> = runBlocking {
                            try {
                                fetchRepoItems(item.url)
                            } catch (e: Exception) {
                                logger.error { "Error fetching items from directory: ${item.url}, ${e.message}" }
                                throw e
                            }
                        }

                        processItems(dirItems)
                    }

                    else -> {
                        logger.warn { "Unknown item type: $item" }
                        emptyList()
                    }
                }
            } catch (exception: Exception) {
                logger.error { "Error processing item: $item, ${exception.message}" }
                emptyList()
            }
        }
    }

    /**
     * Function to fetch items from the gitHub repository API
     */
    override suspend fun fetchRepoItems(apiUrl: String): List<RepoItem> = withContext(Dispatchers.IO) {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "token $githubToken")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            logger.info { "Sending request to GitHub API: $apiUrl" }

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                val errorMessage = "Failed to fetch items from GitHub: ${response.statusCode()} - ${response.body()}"
                logger.error { errorMessage }
                throw IOException(errorMessage)
            }

            logger.info { "Successfully fetched items from GitHub: $apiUrl" }

            objectMapper.readValue(response.body(), object : TypeReference<List<RepoItem>>() {})

        } catch (ioException: IOException) {
            logger.error { "Error occurred while fetching items from GitHub: ${ioException.message}" }
            throw ioException
        } catch (inException: InterruptedException) {
            logger.error { "Request to GitHub was interrupted: ${inException.message}" }
            throw inException
        }
    }

    override fun getItemUrl(): String = githubUrl

}