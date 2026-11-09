package br.com.fiap.letssign.data.local.preferencias

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStoreSessao: DataStore<Preferences> by preferencesDataStore(
    name = "letssign_sessao_prefs"
)

@Singleton
class SessaoUsuarioPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Chaves {
        val USUARIO_LOGADO_ID = stringPreferencesKey("usuario_logado_id")
    }

    val usuarioLogadoIdFlow: Flow<String?> = context.dataStoreSessao.data.map { prefs ->
        prefs[Chaves.USUARIO_LOGADO_ID]
    }

    suspend fun salvarSessaoUsuario(usuarioId: String) {
        context.dataStoreSessao.edit { prefs ->
            prefs[Chaves.USUARIO_LOGADO_ID] = usuarioId
        }
    }

    suspend fun limparSessao() {
        context.dataStoreSessao.edit { prefs ->
            prefs.remove(Chaves.USUARIO_LOGADO_ID)
        }
    }
}
