package dev.janderpereira.listadetarefas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.janderpereira.listadetarefas.model.TarefaEntity

@Database(
    entities = [TarefaEntity::class],
    version = 1,
    exportSchema = false
)

abstract class TarefaDatabase : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var INSTANCE: TarefaDatabase? = null

        fun getDatabase(context: Context): TarefaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TarefaDatabase::class.java,
                    "tarefas.db"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}