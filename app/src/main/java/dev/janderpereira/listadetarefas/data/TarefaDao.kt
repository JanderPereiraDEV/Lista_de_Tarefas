package dev.janderpereira.listadetarefas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import dev.janderpereira.listadetarefas.model.TarefaEntity

@Dao
interface TarefaDao {
    @Query("SELECT * FROM tarefas ORDER BY id ASC")
    fun getAllTarefas(): Flow<List<TarefaEntity>>

    @Query("SELECT * FROM tarefas WHERE id = :id")
    suspend fun getTarefaById(id: Int): TarefaEntity?

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM tarefas")
    suspend fun obterProximoId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefa(tarefa: TarefaEntity)

    @Delete
    suspend fun deleteTarefa(tarefa: TarefaEntity)
}
