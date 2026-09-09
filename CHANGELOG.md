# Changelog

Todas as mudanças notáveis deste projeto são documentadas aqui.

Formato inspirado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).

---

## [2.0] — 2026-09-09

Primeira release pública do **PokeSocial** no GitHub.

### Adicionado

- App completo estilo Instagram em Kotlin + Jetpack Compose
- Seed automático da Gen 1 via PokeAPI → Room (usuários, posts, stories, comentários, chats)
- Conta fixa **@lucar_10** (Lucario #448) com perfil, bio e grid de posts
- **Feed** com stories, like / double-tap, bookmark, comentários e paginação
- **Explore** em grade aleatória
- **Reels** com vídeos sample (Media3 / ExoPlayer)
- **Inbox / DM** com auto-reply
- Visualizador de **stories**
- Splash com progresso do seed
- Tema claro/escuro inspirado no Instagram
- Integração Coil (imagens) + Picsum (fotos de posts)

### Técnico

- `minSdk` 26 · `targetSdk` / `compileSdk` 35
- `versionCode` 2 · `versionName` 2.0
- Room + KSP, Retrofit + Moshi, Navigation Compose, lifecycle ViewModels

---

## [1.0] — não publicada

Protótipo interno anterior ao seed v2 e à publicação no repositório `poke-social`.
