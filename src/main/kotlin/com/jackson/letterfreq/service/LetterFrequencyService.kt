package com.jackson.letterfreq.service

import com.jackson.letterfreq.model.RepoItem

/**
 * Service interface for letter frequency operations
 */
interface LetterFrequencyService {
    suspend fun calculateLetterFrequency(): Map<Char, Int>
    suspend fun fetchRepoItems(apiUrl: String): List<RepoItem>
    fun getItemUrl(): String
}