package com.pizzadelivery.android.di

import android.content.Context
import com.google.gson.Gson
import com.pizzadelivery.android.data.api.WebSocketManager
import com.pizzadelivery.android.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager =
        TokenManager(context)

    @Provides
    @Singleton
    fun provideWebSocketManager(
        okHttpClient: OkHttpClient,
        tokenManager: TokenManager,
        gson: Gson
    ): WebSocketManager = WebSocketManager(okHttpClient, tokenManager, gson)
}
