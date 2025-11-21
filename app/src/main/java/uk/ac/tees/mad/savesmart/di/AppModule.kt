package uk.ac.tees.mad.savesmart.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uk.ac.tees.mad.savesmart.data.local.DepositDao
import uk.ac.tees.mad.savesmart.data.local.GoalDao
import uk.ac.tees.mad.savesmart.data.local.SaveSmartDatabase
import uk.ac.tees.mad.savesmart.data.local.UserPreferencesManager
import javax.inject.Singleton


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

}