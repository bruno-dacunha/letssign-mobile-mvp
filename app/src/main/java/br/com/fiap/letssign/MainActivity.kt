package br.com.fiap.letssign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.fiap.letssign.core.ui.theme.LetsSignTheme
import br.com.fiap.letssign.presentation.navegacao.LetsSignNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetsSignTheme {
                LetsSignNavGraph()
            }
        }
    }
}
