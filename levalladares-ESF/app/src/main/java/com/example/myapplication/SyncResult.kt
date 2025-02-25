package com.example.myapplication

class SyncResult {
    data class Success(val syncedCount: Int)
    sealed class Error {
        object Network : Error()
        object Server : Error()
        data class Database(val exception: Exception) : Error()
    }
}