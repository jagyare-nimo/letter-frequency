package com.jackson.letterfreq.service

import com.jackson.letterfreq.model.RepoItem

interface LetterFrequencyService {
    suspend fun calculateLetterFrequency(): Map<Char, Int>
    suspend fun fetchRepoItems(apiUrl: String): List<RepoItem>
    fun getItemUrl(): String
}