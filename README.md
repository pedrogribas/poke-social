# PokeSocial

Rede social no estilo Instagram, feita em **Kotlin + Jetpack Compose**, onde você é o **Lucario** (`@lucar_10`) e o feed é povoado pela Gen 1 da [PokeAPI](https://pokeapi.co/).

Na primeira abertura o app baixa a Pokédex, persiste tudo no **Room** e gera posts, stories, comentários e conversas. Depois disso funciona offline (exceto mídias remotas).

---

## Destaques

| Área | O que faz |
|------|-----------|
| **Feed** | Stories, likes, double-tap, bookmark, comentários e scroll infinito |
| **Explore** | Grade aleatória de posts |
| **Reels** | Vídeos sample com player Media3 |
| **Inbox** | DMs com resposta automática |
| **Perfil** | Conta fixa do Lucario (#448) |
| **UI** | Chrome visual inspirado no Instagram (claro/escuro) |

Usuário fixo: **`lucar_10`** · Lucario · Pokémon #448

---

## Stack

- **UI:** Jetpack Compose + Material 3 + Navigation
- **Arquitetura:** MVVM + repository
- **Local:** Room (KSP)
- **Rede:** Retrofit + Moshi (PokeAPI)
- **Imagens:** Coil + artwork oficial / Picsum
- **Vídeo:** Media3 ExoPlayer

**Requisitos:** Android 8.0+ (API 26) · internet na 1ª abertura

---

## Como rodar

```bash
./gradlew :app:installDebug
adb shell am start -n com.pokesocial.app/.MainActivity
```

Build release:

```bash
./gradlew :app:assembleRelease
# APK em app/build/outputs/apk/release/
```

---

## Versão

| Campo | Valor |
|-------|--------|
| `versionName` | **2.0** |
| `versionCode` | 2 |
| `applicationId` | `com.pokesocial.app` |

Notas de versão: [CHANGELOG.md](./CHANGELOG.md) · [Releases no GitHub](https://github.com/pedrogribas/poke-social/releases)

---

## Estrutura (resumo)

```
app/src/main/java/com/pokesocial/app/
├── core/           # constantes (Lucario, seeds, URLs)
├── data/
│   ├── local/      # Room + SeedManager (Gen 1)
│   ├── remote/     # PokeAPI (Retrofit)
│   └── repository/ # SocialRepository
├── domain/model/   # User, Post, Story, Chat…
└── ui/             # feed, explore, reels, chat, profile, stories…
```

---

## Aviso

Projeto **educacional / demonstração**. Não afiliado à Nintendo, The Pokémon Company ou Meta. Sprites e nomes de Pokémon vêm da PokeAPI; fotos de posts usam Picsum; vídeos de sample são de buckets públicos de demonstração.
