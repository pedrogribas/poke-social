package com.pokesocial.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MetaEntity::class,
        UserEntity::class,
        FollowEntity::class,
        PostEntity::class,
        PostMediaEntity::class,
        MediaTagEntity::class,
        RepostEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        StoryEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        NoteEntity::class,
        BookmarkEntity::class,
        NotificationEntity::class,
        HighlightEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
    abstract fun userDao(): UserDao
    abstract fun followDao(): FollowDao
    abstract fun postDao(): PostDao
    abstract fun likeDao(): LikeDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    abstract fun chatDao(): ChatDao
    abstract fun noteDao(): NoteDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun notificationDao(): NotificationDao
    abstract fun highlightDao(): HighlightDao
    abstract fun mediaTagDao(): MediaTagDao
    abstract fun repostDao(): RepostDao

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
