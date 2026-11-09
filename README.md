# LetsSign App — MVP (Sprint 1)

Aplicativo móvel nativo Android (**Kotlin + Jetpack Compose**) desenvolvido para o Challenge FIAP + LetsSign, estruturado com **Clean Architecture + MVVM + Padrão Repository** e persistência local em **Room (SQLite) + Sistema de Arquivos Privado**, integralmente preparado para a migração para APIs REST na Sprint 2.

---

## Credenciais Pré-Cadastradas (Seed Inicial Automático)

Na tela de Login, você pode tocar nos botões de **Acesso Rápido** ou digitar diretamente:

| Perfil | E-mail | Senha | Papel Sugerido |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin@letssign.com.br` | `123456` | Cria documentos, gerencia signatários, posiciona campos (X, Y) e acompanha envios |
| **Signatário 1** | `joao@cliente.com.br` | `123456` | Contratante |
| **Signatário 2** | `maria@empresa.com.br` | `123456` | Contratada |
| **Signatário 3** | `carlos@parceiro.com.br` | `123456` | Testemunha |

---

## Como Executar no Android Studio

1. Abra o **Android Studio** e selecione **Open** apontando para a pasta `LetsSignApp`.
2. Aguarde o **Gradle Sync** baixar as dependências (Jetpack Compose, Room, Hilt, DataStore e PDFBox-Android).
3. Execute o app (`Shift + F10`) em um emulador ou dispositivo Android (API 26+).
4. **Dica para teste rápido no Emulador:** No Passo 1 do Wizard de Criação de Documento, além do seletor nativo de arquivos PDF do celular, há o botão **"Gerar Contrato PDF Modelo (2 Páginas) p/ Teste"** que cria instantaneamente um PDF de 2 páginas válido caso o emulador não possua arquivos PDF na pasta Downloads.
