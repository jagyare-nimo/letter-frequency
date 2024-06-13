package com.jackson.letterfreq.listener

import com.jackson.letterfreq.util.Logger.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Component to handle file processing and calculate letter frequency
 */
@Component
class FileProcessingListener {

    private val client = HttpClient.newHttpClient()
    private val globalLetterFrequency = mutableMapOf<Char, Int>()

    /**
     * Function to handle file processing event
      */
    fun handleFileProcessingEvent(fileUrl: String): Flow<Map.Entry<Char, Int>> = flow {
        logger.info {"Reading file content for :: $fileUrl "}
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val content = response.body()

            // Process content and calculate letter frequency
            val letterFrequency = processContent(content)

            // Aggregate the results into the global letter frequency
            aggregateLetterFrequency(letterFrequency)

            logger.info("Aggregated letter frequency:: $globalLetterFrequency ")

            letterFrequency.entries.forEach { emit(it) }

        } catch (exception: IOException) {
            logger.error { "Error processing file: ${exception.message}"}
        }

    }.flowOn(Dispatchers.IO)


    /**
     Function to process content and calculate letter frequency
     */
    private fun processContent(content: String): Map<Char, Int> {
        val letterFrequency = mutableMapOf<Char, Int>()
        for (c in content) {
            if (c.isLetter()) {
                val char = c.lowercaseChar()
                letterFrequency[char] = letterFrequency.getOrDefault(char, 0) + 1
            }
        }
        return letterFrequency
    }

    /**
      Function to aggregate letter frequency into the global frequency
      */
    private fun aggregateLetterFrequency(letterFrequency: Map<Char, Int>) {
        for ((key, value) in letterFrequency) {
            globalLetterFrequency[key] = globalLetterFrequency.getOrDefault(key, 0) + value
        }
    }

    /**
     * Function to get the global letter frequency
    */
    fun getGlobalLetterFrequency(): Map<Char, Int> {
        return globalLetterFrequency
    }

}