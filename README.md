# PokeSocial — Instagram Lucario

Clone visual/funcional do Instagram em **Kotlin + Jetpack Compose**, centrado em **@lucar_10 / Lucario**.

## Rodar

```bash
./gradlew :app:installDebug
adb shell am start -n com.pokesocial.app/.MainActivity
```

Na 1ª abertura o app baixa a Gen 1 (PokeAPI), grava no **Room** e gera posts/stories/comentários/chats. Precisa de internet.

## O que tem

- Feed com stories, likes, double-tap, comentários, infinite scroll
- Explore (grid) + Reels (vídeos sample)
- Chat DM com auto-reply
- Perfil fixo Lucario
- UI estilo Instagram (branco/preto)

## Stack

Compose · MVVM · Room · Retrofit (PokeAPI) · Coil · Media3 · Picsum (fotos)

## User fixo

- username: `lucar_10`
- nome: Lucario (#448)
