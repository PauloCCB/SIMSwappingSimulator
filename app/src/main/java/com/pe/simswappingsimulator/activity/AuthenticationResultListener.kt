package com.pe.simswappingsimulator.activity

interface AuthenticationResultListener {
    fun onAuthenticationSuccess()
    fun onAuthenticationError(errorMessage: String)
    fun onAuthenticationHelp(helpMessage: String)
    fun onAuthenticationFailed()
}