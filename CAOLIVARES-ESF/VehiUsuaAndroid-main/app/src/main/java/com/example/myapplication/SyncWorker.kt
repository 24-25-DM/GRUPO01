package com.example.myapplication

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@Suppress("KotlinConstantConditions")
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.P)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dbHelper = DatabaseHelper(applicationContext)
        val networkManager = NetworkManager(applicationContext)

        // Mejor configuración del cliente HTTP
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS) // Reducido a 15 segundos
            .readTimeout(15, TimeUnit.SECONDS)    // Reducido a 15 segundos
            .writeTimeout(15, TimeUnit.SECONDS)   // Reducido a 15 segundos
            .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS)) // Permite reutilizar conexiones
            .retryOnConnectionFailure(true)       // Reintentar conexiones fallidas
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://54.211.53.245:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        Log.i("SyncWorker", "Iniciando sincronización con configuración optimizada")
        val apiService = retrofit.create(ApiService::class.java)
        val syncManager = SyncManager(dbHelper, apiService, networkManager)

        return@withContext try {
            // Agregar medición de tiempo de ejecución
            val startTime = System.currentTimeMillis()

            syncManager.syncData()

            val endTime = System.currentTimeMillis()
            Log.i("SyncWorker", "Sincronización completada en ${endTime - startTime} ms")

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error en la sincronización: ${e.message}")
            return@withContext if (e is IOException || e is SocketTimeoutException) {
                Log.w("SyncWorker", "Error de red, programando reintento")
                Result.retry()
            } else {
                Log.e("SyncWorker", "Error fatal en la sincronización", e)
                Result.failure()
            }
        }
    }
}