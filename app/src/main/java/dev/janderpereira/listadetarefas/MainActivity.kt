package dev.janderpereira.listadetarefas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import dev.janderpereira.listadetarefas.data.TarefaDatabase
import dev.janderpereira.listadetarefas.data.TarefaRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inicializa o sistema de navegação do App
                    NavegacaoApp()
                }
            }
        }
    }
}

//// --- BANCO DE DADOS EM MEMÓRIA ---
//val listaDeTarefas = mutableStateListOf(
//    Tarefa(1, "Estudar Null Safety e Exceções", "Praticar try/catch, Elvis e Safe Calls."),// Prioridade.ALTA),
//    Tarefa(2, "Tratar erros na busca", null),// Prioridade.MEDIA), // Exemplo de descrição null
//    Tarefa(3, "Revisar Stack Unwinding", "Entender como erros sobem na pilha.")//, Prioridade.BAIXA)
//)

// Sistema de Navegação
@Composable
fun NavegacaoApp() {
    val context = LocalContext.current
    val repository = remember {
        TarefaRepository(
            TarefaDatabase.getDatabase(context).tarefaDao()
        )
    }

    val navController = rememberNavController()

    // NavHost gerencia as telas e rotas de navegação
    NavHost(navController = navController, startDestination = "tela_lista") {

        // Rota da Tela 1: Lista
        composable("tela_lista") {
            TelaLista(
                repository = repository,
                // Passando uma Lambda como Callback de clique (Função de Alta Ordem)
                onTarefaSelecionada = { idTarefa ->
                    navController.navigate("tela_detalhes/$idTarefa")
                }
            )
        }

        // Rota da Tela 2: Detalhes da Tarefa (Recebe ID por parâmetro)
        composable("tela_detalhes/{tarefaId}") { backStackEntry ->
            val idParametro = backStackEntry.arguments?.getString("tarefaId")

            val idValido: Int = try {
                idParametro?.toInt() ?: 0
            } catch (e: NumberFormatException) {
                0
            }

            TelaDetalhes(
                tarefaId = idValido,
                repository = repository,
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}

// Tela 1: Lista de Tarefas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLista(repository: TarefaRepository, onTarefaSelecionada: (Int) -> Unit) {
    val tarefas by repository.observarTarefas().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    // Estados para formulário de nova tarefa
    var novoTitulo by remember { mutableStateOf("") }
    var novaDescricao by remember { mutableStateOf("") }
    var mensagemErroFormulario by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Minhas Tarefas",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1565C0),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Nova Tarefa", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = novoTitulo,
                onValueChange = { novoTitulo = it },
                label = { Text("Título da tarefa") },
                modifier = Modifier
                    .fillMaxWidth()
            )

            // Operador ELVIS (?:) e SAFE CALL (?.) Para exibir mensagem de ERRO
            mensagemErroFormulario?.let { erro ->
                Text(text = erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Text("Descrição", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = novaDescricao,
                onValueChange = { novaDescricao = it },
                label = { Text("Descrição da nova tarefa") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        try {
                            val tituloValidado = novoTitulo.trim()

                            // Valida antes de iniciar a gravação
                            require(tituloValidado.isNotBlank()) {
                                "O título da tarefa não pode estar em branco!"
                            }

                            scope.launch {
                                try {
                                    repository.adicionarTarefa(
                                        titulo = tituloValidado,
                                        descricao = novaDescricao.trim()
                                            .takeIf { it.isNotEmpty() }
                                    )

                                    novoTitulo = ""
                                    novaDescricao = ""
                                    mensagemErroFormulario = null

                                } catch (e: Exception) {
                                    mensagemErroFormulario =
                                        "Não foi possível salvar a tarefa."
                                }
                            }
                        } catch (e: IllegalArgumentException) {
                            mensagemErroFormulario = e.message ?: "Erro ao criar tarefa."
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        contentColor = Color.White
                    )
                ) {
                    Text("Adicionar")
                }

            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Coleção renderizada em lista rolável
            LazyColumn {
                items (items = tarefas, key = { tarefa -> tarefa.id }) {tarefa ->
                    ItemTarefa(tarefa = tarefa, onClick = { onTarefaSelecionada(tarefa.id) })
                }
            }
        }
    }
}

@Composable
fun ItemTarefa(tarefa: Tarefa, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = tarefa.titulo, style = MaterialTheme.typography.titleMedium)

            // SAFE CALL (?.) + Operador ELVIS (?:)
            // Se a descrição for null, usa a String padrão "Sem descrição cadastrada"
            val descricaoTexto = tarefa.descricao ?: "Sem descrição cadastrada."
            Text(text = descricaoTexto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Tela 2: Detalhes da Tarefa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhes(tarefaId: Int?, repository: TarefaRepository, onVoltar: () -> Unit) {
    var estadoTela by remember {
        mutableStateOf<EstadoTela>(EstadoTela.Carregando)
    }

    LaunchedEffect(tarefaId, repository) {
        estadoTela = EstadoTela.Carregando

        estadoTela = try {
            val tarefaEncontrada = tarefaId?.let { id ->
                repository.buscarTarefaPorId(id)
            }

            if (tarefaEncontrada != null) {
                EstadoTela.Sucesso(listOf(tarefaEncontrada))
            } else {
                EstadoTela.Erro(
                    "Tarefa não encontrada ou ID inválido!"
                )
            }
        } catch (e: Exception) {
            EstadoTela.Erro(
                "Não foi possível carregar a tarefa."
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalhes da Tarefa",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tratamento das variantes do Sealed Class
            when (val estado = estadoTela) {
                is EstadoTela.Carregando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EstadoTela.Sucesso -> {
                    val tarefa = estado.tarefas.first()

                    if (tarefa != null) {
                        Column {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {
                                Text(
                                    text = tarefa.titulo,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // SAFE CALL + LET com valor default usando ELVIS
                            // Exibe a descrição com formatação extra SOMENTE se ela existir
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {
                                tarefa.descricao?.let { desc ->
                                    Text(
                                        text = "Descrição:",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } ?: Text(
                                    text = "Nenhuma descrição detalhada foi fornecida para esta tarefa.",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Button(
                                    onClick = onVoltar,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1565C0),
                                        contentColor = Color.White
                                    )
                                ) { Text("Voltar para Lista") }
                            }
                        }
                    } else {
                        Text("Nenhuma tarefa encontrada.")
                    }
                }

                is EstadoTela.Erro -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = estado.mensagem,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = onVoltar,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1565C0),
                                    contentColor = Color.White
                                )
                            ) { Text("Voltar") }
                        }
                    }
                }
            }
        }
    }
}