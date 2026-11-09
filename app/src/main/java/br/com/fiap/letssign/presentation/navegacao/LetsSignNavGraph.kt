package br.com.fiap.letssign.presentation.navegacao

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.fiap.letssign.domain.model.PerfilUsuario
import br.com.fiap.letssign.presentation.admin.acompanhamento.AcompanhamentoDocumentoScreen
import br.com.fiap.letssign.presentation.admin.acompanhamento.AcompanhamentoViewModel
import br.com.fiap.letssign.presentation.admin.criacao.CriacaoDocumentoViewModel
import br.com.fiap.letssign.presentation.admin.criacao.WizardCriacaoDocumentoScreen
import br.com.fiap.letssign.presentation.admin.dashboard.DashboardAdminScreen
import br.com.fiap.letssign.presentation.admin.dashboard.DashboardAdminViewModel
import br.com.fiap.letssign.presentation.admin.signatarios.GestaoSignatariosScreen
import br.com.fiap.letssign.presentation.admin.signatarios.GestaoSignatariosViewModel
import br.com.fiap.letssign.presentation.autenticacao.LoginScreen
import br.com.fiap.letssign.presentation.autenticacao.LoginViewModel
import br.com.fiap.letssign.presentation.signatario.assinatura.AssinaturaDocumentoScreen
import br.com.fiap.letssign.presentation.signatario.assinatura.AssinaturaViewModel
import br.com.fiap.letssign.presentation.signatario.dashboard.DashboardSignatarioScreen
import br.com.fiap.letssign.presentation.signatario.dashboard.DashboardSignatarioViewModel

@Composable
fun LetsSignNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = RotasTela.Login.rota
    ) {
        // 3.1. Módulo de Autenticação (Comum)
        composable(route = RotasTela.Login.rota) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                aoAutenticarComSucesso = { usuario ->
                    val rotaDestino = when (usuario.perfil) {
                        PerfilUsuario.ADMIN -> RotasTela.DashboardAdmin.criarRota(usuario.id)
                        PerfilUsuario.SIGNATARIO -> RotasTela.DashboardSignatario.criarRota(usuario.id)
                    }
                    navController.navigate(rotaDestino) {
                        popUpTo(RotasTela.Login.rota) { inclusive = true }
                    }
                }
            )
        }

        // 3.2. Tela 1: Dashboard (Admin)
        composable(
            route = RotasTela.DashboardAdmin.rota,
            arguments = listOf(navArgument("adminId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adminId = backStackEntry.arguments?.getString("adminId").orEmpty()
            val viewModel: DashboardAdminViewModel = hiltViewModel()

            DashboardAdminScreen(
                viewModel = viewModel,
                aoIrParaGestaoSignatarios = {
                    navController.navigate(RotasTela.GestaoSignatarios.criarRota(adminId))
                },
                aoCriarNovoDocumento = {
                    navController.navigate(RotasTela.WizardCriacaoDocumento.criarRota(adminId))
                },
                aoAbrirAcompanhamentoDocumento = { documentoId ->
                    navController.navigate(RotasTela.AcompanhamentoDocumento.criarRota(documentoId))
                },
                aoSairDaConta = {
                    navController.navigate(RotasTela.Login.rota) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 3.2. Tela 2: Gestão de Signatários (CRUD Admin)
        composable(
            route = RotasTela.GestaoSignatarios.rota,
            arguments = listOf(navArgument("adminId") { type = NavType.StringType })
        ) {
            val viewModel: GestaoSignatariosViewModel = hiltViewModel()
            GestaoSignatariosScreen(
                viewModel = viewModel,
                aoVoltar = { navController.popBackStack() }
            )
        }

        // 3.2. Tela 3: Fluxo de Criação de Documento (Wizard Completo: Passos 1 a 4)
        composable(
            route = RotasTela.WizardCriacaoDocumento.rota,
            arguments = listOf(navArgument("adminId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adminId = backStackEntry.arguments?.getString("adminId").orEmpty()
            val viewModel: CriacaoDocumentoViewModel = hiltViewModel()

            WizardCriacaoDocumentoScreen(
                viewModel = viewModel,
                aoVoltarAoDashboard = { navController.popBackStack() },
                aoAbrirCadastroRapidoSignatario = {
                    navController.navigate(RotasTela.GestaoSignatarios.criarRota(adminId))
                },
                aoIrParaAcompanhamentoDocumento = { documentoId ->
                    navController.navigate(RotasTela.AcompanhamentoDocumento.criarRota(documentoId)) {
                        popUpTo(RotasTela.WizardCriacaoDocumento.rota) { inclusive = true }
                    }
                }
            )
        }

        // 3.2. Tela 4: Acompanhamento de Documento (Admin)
        composable(
            route = RotasTela.AcompanhamentoDocumento.rota,
            arguments = listOf(navArgument("documentoId") { type = NavType.StringType })
        ) {
            val viewModel: AcompanhamentoViewModel = hiltViewModel()
            AcompanhamentoDocumentoScreen(
                viewModel = viewModel,
                aoVoltar = { navController.popBackStack() }
            )
        }

        // 3.3. Tela 1: Dashboard (Signatário)
        composable(
            route = RotasTela.DashboardSignatario.rota,
            arguments = listOf(navArgument("signatarioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val signatarioId = backStackEntry.arguments?.getString("signatarioId").orEmpty()
            val viewModel: DashboardSignatarioViewModel = hiltViewModel()

            DashboardSignatarioScreen(
                viewModel = viewModel,
                aoAbrirDocumentoParaAssinar = { documentoId ->
                    navController.navigate(
                        RotasTela.AssinaturaDocumento.criarRota(
                            documentoId = documentoId,
                            signatarioId = signatarioId
                        )
                    )
                },
                aoSairDaConta = {
                    navController.navigate(RotasTela.Login.rota) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 3.3. Tela 2: Tela de Assinatura PDF (Jornada do Signatário)
        composable(
            route = RotasTela.AssinaturaDocumento.rota,
            arguments = listOf(
                navArgument("documentoId") { type = NavType.StringType },
                navArgument("signatarioId") { type = NavType.StringType }
            )
        ) {
            val viewModel: AssinaturaViewModel = hiltViewModel()
            AssinaturaDocumentoScreen(
                viewModel = viewModel,
                aoVoltarAoDashboard = { navController.popBackStack() }
            )
        }
    }
}
