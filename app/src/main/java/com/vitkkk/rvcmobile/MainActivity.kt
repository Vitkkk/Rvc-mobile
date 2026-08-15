package com.vitkkk.rvcmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vitkkk.rvcmobile.data.ModelImporter
import com.vitkkk.rvcmobile.data.ModelRepository
import com.vitkkk.rvcmobile.model.RvcModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { RvcMobileApp() } }
    }
}

private enum class Screen { HOME, CONVERT, COVER, MODELS, TRAIN, FILES, SETTINGS }

private data class HomeAction(
    val title: String,
    val subtitle: String,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RvcMobileApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (screen == Screen.HOME) "RVC Mobile" else screenTitle(screen)) },
                navigationIcon = {
                    if (screen != Screen.HOME) {
                        OutlinedButton(onClick = { screen = Screen.HOME }) { Text("Voltar") }
                    }
                }
            )
        }
    ) { padding ->
        when (screen) {
            Screen.HOME -> HomeScreen(Modifier.padding(padding)) { screen = it }
            Screen.MODELS -> ModelsScreen(Modifier.padding(padding))
            Screen.CONVERT -> VoiceConversionScreen(Modifier.padding(padding))
            else -> ComingSoonScreen(Modifier.padding(padding), screenTitle(screen))
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, onOpen: (Screen) -> Unit) {
    val actions = listOf(
        HomeAction("Voice Conversion", "Converter voz ou áudio usando um modelo RVC", Screen.CONVERT),
        HomeAction("AI Cover", "Separar vocal, converter e mixar novamente", Screen.COVER),
        HomeAction("Voice Models", "Importar, organizar e testar modelos", Screen.MODELS),
        HomeAction("Train Model", "Criar dataset e treinar uma nova voz", Screen.TRAIN),
        HomeAction("Files", "Gerenciar áudios, datasets, covers e exports", Screen.FILES),
        HomeAction("Settings", "Desempenho, componentes e benchmark", Screen.SETTINGS)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "RVC para Android, feito para touchscreen.",
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(actions) { action ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(action.title, style = MaterialTheme.typography.titleLarge)
                    Text(action.subtitle)
                    Button(onClick = { onOpen(action.screen) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Abrir")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelsScreen(modifier: Modifier) {
    val context = LocalContext.current
    val repository = remember { ModelRepository(context) }
    val importer = remember { ModelImporter(context, repository) }
    var models by remember { mutableStateOf(repository.list()) }
    var status by remember { mutableStateOf("Importe um .pth, .index ou .zip do armazenamento.") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { importer.importUri(uri) }
                .onSuccess {
                    status = it.message
                    models = repository.list()
                }
                .onFailure { status = "Falha ao importar: ${it.message ?: "erro desconhecido"}" }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { launcher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Importar modelo") }
        }
        item { Text(status, style = MaterialTheme.typography.bodyMedium) }
        item { Text("${models.size} modelo(s) • ${formatBytes(repository.totalSizeBytes())}") }

        if (models.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("Sua biblioteca está vazia.", modifier = Modifier.padding(18.dp))
                }
            }
        }

        items(models, key = { it.id }) { model ->
            ModelCard(model) {
                repository.delete(model.id)
                models = repository.list()
                status = "${model.name} removido."
            }
        }
    }
}

@Composable
private fun ModelCard(model: RvcModel, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(model.name, style = MaterialTheme.typography.titleLarge)
            Text("${model.version} • ${model.sampleRate?.let { "$it Hz" } ?: "sample rate a detectar"}")
            Text(if (model.checkpointPath != null) "✓ Checkpoint PTH" else "○ Checkpoint ausente")
            Text(if (model.indexPath != null) "✓ Index disponível" else "○ Sem index")
            Text(formatBytes(model.sizeBytes))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) { Text("Usar") }
                OutlinedButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun VoiceConversionScreen(modifier: Modifier) {
    val context = LocalContext.current
    val models = remember { ModelRepository(context).list() }
    var pitch by remember { mutableFloatStateOf(0f) }
    var indexRate by remember { mutableFloatStateOf(0.75f) }
    var protect by remember { mutableFloatStateOf(0.33f) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (models.isEmpty()) "Nenhum modelo importado. Vá em Voice Models primeiro."
                else "Modelo: ${models.first().name}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        item {
            Text("Pitch: ${pitch.toInt()} semitons")
            Slider(value = pitch, onValueChange = { pitch = it }, valueRange = -24f..24f, steps = 47)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { pitch = -12f }) { Text("-12") }
                OutlinedButton(onClick = { pitch -= 1f }) { Text("-1") }
                OutlinedButton(onClick = { pitch = 0f }) { Text("0") }
                OutlinedButton(onClick = { pitch += 1f }) { Text("+1") }
                OutlinedButton(onClick = { pitch = 12f }) { Text("+12") }
            }
        }
        item {
            Text("F0: RMVPE (backend será conectado na Fase 2)")
        }
        item {
            Text("Index Rate: ${"%.2f".format(indexRate)}")
            Slider(value = indexRate, onValueChange = { indexRate = it })
        }
        item {
            Text("Protect: ${"%.2f".format(protect)}")
            Slider(value = protect, onValueChange = { protect = it }, valueRange = 0f..0.5f)
        }
        item {
            Button(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("CONVERTER — runtime ainda não instalado")
            }
        }
    }
}

@Composable
private fun ComingSoonScreen(modifier: Modifier, title: String) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text("Estrutura criada. Esta área será implementada nas próximas fases sem depender da interface desktop do RVC.")
    }
}

private fun screenTitle(screen: Screen): String = when (screen) {
    Screen.HOME -> "RVC Mobile"
    Screen.CONVERT -> "Voice Conversion"
    Screen.COVER -> "AI Cover"
    Screen.MODELS -> "Voice Models"
    Screen.TRAIN -> "Train Model"
    Screen.FILES -> "Files"
    Screen.SETTINGS -> "Settings"
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    return "%.2f GB".format(mb / 1024.0)
}
