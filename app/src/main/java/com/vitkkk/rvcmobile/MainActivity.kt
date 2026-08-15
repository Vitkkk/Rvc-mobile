package com.vitkkk.rvcmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RvcMobileHome()
            }
        }
    }
}

data class HomeAction(val title: String, val subtitle: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RvcMobileHome() {
    val actions = listOf(
        HomeAction("Voice Conversion", "Converter voz ou áudio usando um modelo RVC"),
        HomeAction("AI Cover", "Separar vocal, converter e mixar novamente"),
        HomeAction("Voice Models", "Importar, organizar e testar modelos"),
        HomeAction("Train Model", "Criar dataset e treinar uma nova voz"),
        HomeAction("Files", "Gerenciar áudios, datasets, covers e exports"),
        HomeAction("Settings", "Desempenho, componentes e benchmark")
    )

    Scaffold(topBar = { TopAppBar(title = { Text("RVC Mobile") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "RVC para Android, feito para touchscreen.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(actions) { action -> HomeCard(action) }
        }
    }
}

@Composable
private fun HomeCard(action: HomeAction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(action.title, style = MaterialTheme.typography.titleLarge)
            Text(action.subtitle, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Abrir")
            }
        }
    }
}
