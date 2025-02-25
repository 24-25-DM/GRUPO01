package com.example.myapplication

import com.example.myapplication.dto.UsuarioDTO
import com.example.myapplication.dto.VehiculoDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @POST("api/usuarios")
    @Headers("Content-Type: application/json")
    suspend fun syncUsuarios(@Body usuarios: UsuarioDTO): Response<Void>

    @POST("api/vehiculos")
    @Headers("Content-Type: application/json")
    suspend fun syncVehiculos(@Body vehiculos: VehiculoDTO): Response<Void>

    @GET("api/usuarios")
    suspend fun getUsuarios(): Response<List<UsuarioDTO>>

    @GET("api/vehiculos")
    suspend fun getVehiculos(): Response<List<VehiculoDTO>>

    @POST("api/usuarios/add")
    @Headers("Content-Type: application/json")
    suspend fun createUsuario(@Body usuario: UsuarioDTO): Response<UsuarioDTO>

    companion object {
        const val TIMEOUT_SECONDS = 30L
    }

}