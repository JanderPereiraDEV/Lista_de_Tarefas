package dev.janderpereira.listadetarefas

// Data Class utilizando Null Safety
data class Tarefa(
    val id: Int,
    val titulo: String,
    val descricao: String?,
) {
    init {
        // Validação usando a função utilitária require() do Kotlin
        // Lança IllegalArgumentException se o título for vazio
        require(titulo.isNotBlank()) { "O título da tarefa não pode estar em branco!" }
    }
}

sealed class EstadoTela {
    object Carregando : EstadoTela()
    data class Sucesso(val tarefas: List<Tarefa>) : EstadoTela()
    data class Erro(val mensagem: String) : EstadoTela()
}