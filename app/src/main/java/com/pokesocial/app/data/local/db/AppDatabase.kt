package com.pokesocial.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MetaEntity::class,
        UserEntity::class,
        PostEntity::class,
        PostMediaEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        StoryEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun likeDao(): LikeDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokesocial.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
