package edu.hebut.retrofittest.assistant.entity

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(val userId: String, val message: String)