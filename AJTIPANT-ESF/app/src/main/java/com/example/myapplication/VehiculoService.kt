package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface VehiculoService {
    @GET("/api/vehiculos/{id}")
    fun isVehiculoDisponible(@Path("id") id: Int): Call<Boolean>
}