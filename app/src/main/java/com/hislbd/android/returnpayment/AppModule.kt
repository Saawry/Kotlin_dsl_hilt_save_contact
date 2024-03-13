package com.hislbd.android.returnpayment

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // or ActivityComponent::class if providing dependencies for activity scope
object AppModule {

    @Singleton
    @Provides
    fun provideSomeDependency(): SomeDependency {
        return SomeDependency()
    }
}
