package com.jackson.letterfreq.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonProperty


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RepoFile::class, name = "file"),
    JsonSubTypes.Type(value = RepoDir::class, name = "dir")
)
sealed class RepoItem

@JsonIgnoreProperties(ignoreUnknown = true)
data class RepoFile(
    val name: String,
    @JsonProperty("download_url") val downloadUrl: String
) : RepoItem()

@JsonIgnoreProperties(ignoreUnknown = true)
data class RepoDir(
    val name: String,
    val url: String
) : RepoItem()
