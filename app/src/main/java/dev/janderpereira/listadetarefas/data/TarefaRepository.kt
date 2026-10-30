package dev.janderpereira.listadetarefas.data

import dev.janderpereira.listadetarefas.Tarefa
import dev.janderpereira.listadetarefas.model.TarefaEntity
import dev.janderpereira.listadetarefas.data.TarefaDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TarefaRepository(
    private val tarefaDao: TarefaDao
) {
    fun observarTarefas(): Flow<List<Tarefa>> {
        return tarefaDao.getAllTarefas().map { entidades ->
            entidades.map { entidade ->
                Tarefa(
                    id = entidade.id,
                    titulo = entidade.titulo,
                    descricao = entidade.descricao
                )
            }
        }
    }

    suspend fun adicionarTarefa(
        titulo: String,
        descricao: String?
    ) {
        val novaTarefa = Tarefa(
            id = tarefaDao.obterProximoId(),
            titulo = titulo,
            descricao = descricao
        )

        tarefaDao.insertTarefa(
            TarefaEntity(
                id = novaTarefa.id,
                titulo = novaTarefa.titulo,
                descricao = novaTarefa.descricao
            )
        )
    }

    suspend fun buscarTarefaPorId(id: Int): Tarefa? {
        val tarefa = tarefaDao.getTarefaById(id)

        return tarefa?.let {
            Tarefa(
                id = it.id,
                titulo = it.titulo,
                descricao = it.descricao
            )
        }
    }
}