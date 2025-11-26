package uk.ac.tees.mad.savesmart.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import uk.ac.tees.mad.savesmart.data.api.FrankfurterApi
import uk.ac.tees.mad.savesmart.data.local.DepositDao
import uk.ac.tees.mad.savesmart.data.local.GoalDao
import uk.ac.tees.mad.savesmart.data.local.SaveSmartDatabase
import uk.ac.tees.mad.savesmart.data.local.UserPreferencesManager
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.jvm.java

// ADD THIS QUALIFIER
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FrankfurterRetrofit


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun providesFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    // db
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SaveSmartDatabase{
        return Room.databaseBuilder(
            context=context,
            SaveSmartDatabase::class.java,
            "savesmart_database"
        ).build()
    }


    // gaol dao
    @Provides
    @Singleton
    fun provideGoalDao(database: SaveSmartDatabase): GoalDao {
        return database.goalDao()
    }

    //user preferences

    @Provides
    @Singleton
    fun provideUserPreferencesManager(
        @ApplicationContext context: Context
    ): UserPreferencesManager {
        return UserPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideDepositDao(database: SaveSmartDatabase): DepositDao {  // ✅ Added
        return database.depositDao()
    }

    //  Provide Json (if not provided elsewhere)
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    //  Provide OkHttpClient (if not provided elsewhere)
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ✅ ADD QUALIFIER HERE
    @Provides
    @Singleton
    @FrankfurterRetrofit
    fun provideFrankfurterRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FrankfurterApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    // USE QUALIFIER HERE
    @Provides
    @Singleton
    fun provideFrankfurterApi(
        @FrankfurterRetrofit retrofit: Retrofit
    ): FrankfurterApi {
        return retrofit.create(FrankfurterApi::class.java)
    }



}