package com.datacollector.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignupBody(val username: String, val password: String)
