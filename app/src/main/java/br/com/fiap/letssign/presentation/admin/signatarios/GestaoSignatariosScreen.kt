package br.com.fiap.letssign.presentation.admin.signatarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary
import br.com.fiap.letssign.domain.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestaoSignatariosScreen(
    viewModel: GestaoSignatariosViewModel,
    aoVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagemSucesso) {
        uiState.mensagemSucesso?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparMensagemSucesso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gestão de Signatários",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "${uiState.signatarios.size} signatário(s) disponível(is)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar ao Dashboard",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignPurplePrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::abrirFormularioNovoSignatario,
                containerColor = LetsSignPurplePrimary,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Novo Signatário"
                    )
                },
                text = { Text("Novo Signatário", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.termoBusca,
                onValueChange = viewModel::aoAlterarTermoBusca,
                placeholder = { Text("Buscar por nome, e-mail ou telefone...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.carregandoLista) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.signatariosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhum signatário encontrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.signatariosFiltrados, key = { it.id }) { signatario ->
                        CardSignatarioItem(
                            signatario = signatario,
                            aoEditar = { viewModel.abrirFormularioEditarSignatario(signatario) }
                        )
                    }
                }
            }
        }

        if (uiState.exibirFormularioDialog) {
            DialogFormularioSignatario(
                uiState = uiState,
                aoAlterarNome = viewModel::aoAlterarNome,
                aoAlterarEmail = viewModel::aoAlterarEmail,
                aoAlterarTelefone = viewModel::aoAlterarTelefone,
                aoAlterarSenha = viewModel::aoAlterarSenha,
                aoConfirmar = viewModel::salvarSignatario,
                aoCancelar = viewModel::fecharFormulario
            )
        }
    }
}

@Composable
private fun CardSignatarioItem(
    signatario: Usuario,
    aoEditar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = signatario.nomeCompleto.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = signatario.nomeCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = signatario.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = signatario.telefone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = aoEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Signatário",
                    tint = LetsSignPurplePrimary
                )
            }
        }
    }
}

@Composable
private fun DialogFormularioSignatario(
    uiState: GestaoSignatariosUiState,
    aoAlterarNome: (String) -> Unit,
    aoAlterarEmail: (String) -> Unit,
    aoAlterarTelefone: (String) -> Unit,
    aoAlterarSenha: (String) -> Unit,
    aoConfirmar: () -> Unit,
    aoCancelar: () -> Unit
) {
    val modoEdicao = uiState.signatarioEmEdicao != null

    AlertDialog(
        onDismissRequest = aoCancelar,
        title = {
            Text(
                text = if (modoEdicao) "Editar Signatário" else "Novo Signatário",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.nomeCompletoInput,
                    onValueChange = aoAlterarNome,
                    label = { Text("Nome Completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.emailInput,
                    onValueChange = aoAlterarEmail,
                    label = { Text("E-mail (Login) *") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.telefoneInput,
                    onValueChange = aoAlterarTelefone,
                    label = { Text("Telefone *") },
                    placeholder = { Text("(11) 99999-9999") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.senhaInput,
                    onValueChange = aoAlterarSenha,
                    label = {
                        Text(
                            if (modoEdicao) "Nova Senha (opcional)" else "Senha de Acesso *"
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.erroFormulario != null) {
                    Text(
                        text = uiState.erroFormulario,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = aoConfirmar,
                enabled = !uiState.salvando
            ) {
                Text(if (modoEdicao) "Salvar Alterações" else "Cadastrar")
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar, enabled = !uiState.salvando) {
                Text("Cancelar")
            }
        }
    )
}
