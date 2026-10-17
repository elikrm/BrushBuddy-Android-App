package com.elnaz.brushbuddy.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//Defines and manages the Room database
/*
*companion object	Provides access to the database instance without creating the class manually
* INSTANCE	Holds the database instance
* @Volatile	Makes updates to INSTANCE visible across threads
* synchronized(this)	Prevents multiple threads from creating separate instances
* Room.databaseBuilder()	Creates or opens the SQLite database
* "brushbuddy_database"	The database filename
 */
@Database(
    entities = [BrushingSession::class],
    version = 1,
    exportSchema = false
)
abstract class BrushBuddyDatabase: RoomDatabase() {
    abstract fun brushingSessionDao(): BrushingSessionDao
    companion object {
    @Volatile
    private var INSTANCE: BrushBuddyDatabase? = null
        fun getDatabase(context: Context): BrushBuddyDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrushBuddyDatabase::class.java,
                    "brushbuddy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}