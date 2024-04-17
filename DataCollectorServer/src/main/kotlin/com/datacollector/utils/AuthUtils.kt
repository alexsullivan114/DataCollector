package com.datacollector.utils

fun String.isValidEmail(): Boolean {
  val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
  return matches(emailRegex)
}
