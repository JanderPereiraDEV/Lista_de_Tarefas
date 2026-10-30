package dev.janderpereira.listadetarefas.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarefas")
data class TarefaEntity (
    @PrimaryKey
    val id: Int,
    val titulo: String,
    val descricao: String?,
)