package com.pokesocial.app.core

import com.pokesocial.app.domain.model.User
import kotlin.random.Random

data class ChatReply(
    val text: String,
    val withGif: Boolean = false
)

/**
 * Respostas de DM no tom de cada Pokémon, reagindo ao que o Lucario mandou.
 */
object PokemonChatBrain {

    fun reply(peer: User, incomingText: String, isGif: Boolean): ChatReply {
        val persona = personaFor(peer)
        val text = incomingText.trim()
        val intent = detectIntent(text, isGif)
        val body = when (intent) {
            Intent.Gif -> persona.onGif.random()
            Intent.Greeting -> persona.greetings.random()
            Intent.Battle -> persona.battle.random()
            Intent.Food -> persona.food.random()
            Intent.Sleep -> persona.sleep.random()
            Intent.Training -> persona.training.random()
            Intent.Compliment -> persona.compliments.random()
            Intent.Laugh -> persona.laughs.random()
            Intent.Bye -> persona.byes.random()
            Intent.Question -> answerQuestion(persona, text)
            Intent.Love -> persona.love.random()
            Intent.Generic -> weave(persona, text)
        }
        val gif = !isGif && persona.playful && Random.nextFloat() < 0.12f && intent != Intent.Battle
        return ChatReply(body, withGif = gif)
    }

    fun battleAftermath(peer: User, playerWon: Boolean, fled: Boolean): String {
        val persona = personaFor(peer)
        return when {
            fled -> persona.flee.random()
            playerWon -> persona.lostBattle.random()
            else -> persona.wonBattle.random()
        }
    }

    fun seedPeerLines(peer: User): List<String> {
        val p = personaFor(peer)
        return listOf(p.greetings.first(), p.training[0], p.seedExtra, p.battle[0], p.seedCloser)
    }

    private enum class Intent {
        Gif, Greeting, Battle, Food, Sleep, Training, Compliment, Laugh, Bye, Question, Love, Generic
    }

    private fun detectIntent(text: String, isGif: Boolean): Intent {
        if (isGif || text.isBlank()) return Intent.Gif
        val t = text.lowercase()
        return when {
            t.containsAny("batalha", "luta", "duelo", "fight", "vs", "versus", "ginásio", "ginasio") -> Intent.Battle
            t.containsAny("oi", "olá", "ola", "e aí", "e ai", "eae", "fala", "hey", "hi ", "bom dia", "boa tarde", "boa noite") -> Intent.Greeting
            t.containsAny("fome", "comer", "comida", "pizza", "berry", "fruta", "ketchup", "almoço", "janta") -> Intent.Food
            t.containsAny("sono", "dormir", "cama", "cochilo", "descanso", "cansad") -> Intent.Sleep
            t.containsAny("treino", "treinar", "aura", "força", "forca", "gym") -> Intent.Training
            t.containsAny("lindo", "linda", "forte", "top", "gosto", "legal", "incrível", "incrivel", "maneiro", "demais") -> Intent.Compliment
            t.containsAny("kkk", "haha", "rsrs", "lol", "😂", "kkkk") -> Intent.Laugh
            t.containsAny("tchau", "flw", "falou", "até", "ate mais", "bye") -> Intent.Bye
            t.containsAny("amor", "crush", "❤️", "❤", "te amo", "gatinh") -> Intent.Love
            t.contains('?') || t.startsWith("como") || t.startsWith("cadê") || t.startsWith("cade") ||
                t.startsWith("por que") || t.startsWith("pq ") || t.startsWith("o que") || t.startsWith("onde") -> Intent.Question
            else -> Intent.Generic
        }
    }

    private fun String.containsAny(vararg keys: String): Boolean =
        keys.any { key ->
            if (key.length <= 3 && key.all { it.isLetter() || it == 'á' || it == 'é' }) {
                Regex(
                    """(^|[^\p{L}])${Regex.escape(key)}($|[^\p{L}])""",
                    RegexOption.IGNORE_CASE
                ).containsMatchIn(this)
            } else {
                contains(key, ignoreCase = true)
            }
        }

    private fun answerQuestion(persona: Persona, text: String): String {
        val t = text.lowercase()
        return when {
            t.containsAny("batalha", "luta", "duelo") -> persona.battle.random()
            t.containsAny("onde", "cadê", "cade") -> persona.location.random()
            t.containsAny("tipo", "elemento") -> persona.aboutType.random()
            else -> persona.questions.random()
        }
    }

    private fun weave(persona: Persona, incoming: String): String {
        val clip = incoming.replace('\n', ' ').trim().take(42).ifBlank { "isso" }
        return persona.react.random().replace("{m}", clip)
    }

    private data class Persona(
        val greetings: List<String>,
        val battle: List<String>,
        val food: List<String>,
        val sleep: List<String>,
        val training: List<String>,
        val compliments: List<String>,
        val laughs: List<String>,
        val byes: List<String>,
        val love: List<String>,
        val questions: List<String>,
        val location: List<String>,
        val aboutType: List<String>,
        val onGif: List<String>,
        val react: List<String>,
        val wonBattle: List<String>,
        val lostBattle: List<String>,
        val flee: List<String>,
        val seedExtra: String,
        val seedCloser: String,
        val playful: Boolean
    )

    private fun personaFor(user: User): Persona {
        famous[user.pokemonId]?.let { return it }
        val types = user.types.map { it.lowercase() }
        val primary = types.firstOrNull().orEmpty()
        return typePersonas[primary] ?: defaultPersona(user.displayName, primary)
    }

    private val famous: Map<Int, Persona> = mapOf(
        25 to Persona(
            greetings = listOf("Pika! E aí, Lucario ⚡", "Pikachu! Cheguei na DM", "Pika pika, lucar_10!"),
            battle = listOf("Pika! Bora um choque vs aura. Clica no raio aí em cima ⚡", "Thunderbolt pronto. Te espero no duelo!"),
            food = listOf("Tem ketchup nisso? Pikachu AMA ketchup 🍅", "Pika! Depois da janta a gente treina."),
            sleep = listOf("Pika… bateria baixa. Só um cochilo no ombro do Ash…", "Zz… mas se for batalha eu acordo!"),
            training = listOf("Bora treinar Choque do Trovão vs Esfera de Aura?", "Pikachu tá carregado. Pode vir."),
            compliments = listOf("Pika! Aura de vocês brilha demais ✨", "Lucario é forte… mas Pikachu também!"),
            laughs = listOf("Pikachu kkk ⚡", "Haha pika pika 😂"),
            byes = listOf("Pika! Até a próxima rota", "Flw! Se precisar de um choque, chama."),
            love = listOf("P-pika?! 😳 bochechas até faiscando", "Pikachu também gosta de treinar com você…"),
            questions = listOf("Pika? Depende… tem ketchup no plano?", "Pikachu não sabe, mas pode perguntar pro Ash."),
            location = listOf("Tô no Pallet, carregando no sol ☀️", "Perto de uma tomada. Sempre."),
            aboutType = listOf("Elétrico, né? Bochechas já tão chispa.", "Tipo elétrico. Água que lute comigo se molhar."),
            onGif = listOf("Pika pika esse GIF 😂⚡", "Haha! Pikachu salvou na galeria."),
            react = listOf("Pika! “{m}”… gostei ⚡", "Pikachu leu isso e as bochechas acenderam: {m}", "Pika pika, {m}! Bora?"),
            wonBattle = listOf("Pika! Vitória do ratinho ⚡ não foi sorte, foi volt.", "GG Lucario. Pikachu avisou que o choque vinha."),
            lostBattle = listOf("Pika… aura pesou. Revanche quando a bateria voltar?", "Ugh. Lucario é duro. Pikachu volta mais carregado."),
            flee = listOf("Pika? Fugiu? Covarde… brincadeira. Depois a gente luta ⚡", "Tá bom, Pikachu guarda o Thunderbolt."),
            seedExtra = "Vi seu post. Aura tava brilhando demais.",
            seedCloser = "Manda o local. Pikachu corre na hora.",
            playful = true
        ),
        6 to Persona(
            greetings = listOf("Charizard na área. Não me chama de Charmander.", "Asas prontas. Fala, Lucario."),
            battle = listOf("Fogo vs aço. Eu gosto desses números. Clica no raio e vem.", "Duelo? Eu não recuo de batalha."),
            food = listOf("Carne grelhada. Eu mesmo acendo a churrasqueira.", "Não como berry doce. Só o que queima."),
            sleep = listOf("Dragões não cochilam. Observo do alto.", "Asas dobradas. Não significa que estou fraco."),
            training = listOf("Treino de voo ao entardecer. Vem se aguentar o calor.", "Minha chama não é de treino. É de guerra."),
            compliments = listOf("Respeito. Poucos aço aguentam meu fogo.", "Aura de vocês… até eu sinto daqui de cima."),
            laughs = listOf("Hah. Até que queimou.", "Charizard ri com fumaça no nariz."),
            byes = listOf("Vou voar. Não me chama de volta por besteira.", "Até. Cuida dessa aura."),
            love = listOf("…Cuidado com o fogo. Eu não controlo bem isso.", "Charizard não fala dessas coisas. Mas ouviu."),
            questions = listOf("Pergunta melhor. Ou sobe aqui e vê com os próprios olhos.", "A resposta é chama. Sempre."),
            location = listOf("Acima das nuvens. Kanto fica pequena daqui.", "Rota 1 é pra filhote. Eu tô na cratera."),
            aboutType = listOf("Fogo e voador. Aço derrete. Água… a gente não fala disso.", "Tipo fogo. Asas de verdade, não de inseto."),
            onGif = listOf("Esse GIF é fraquinho. Manda um de fogo de verdade.", "Hmpf. Até que animou."),
            react = listOf("“{m}”. Ousado, vindo de um lutador.", "Charizard leu: {m}. Não me impressiona fácil.", "Fumaça. {m}. Continua."),
            wonBattle = listOf("Chama 1, aço 0. Era óbvio.", "GG. Da próxima, traz um tipo água se quiser chance."),
            lostBattle = listOf("…Aço pesou. Revanche no céu, não nessa caixinha.", "Charizard não esquece. Treino e volto."),
            flee = listOf("Fugiu do fogo? Entendi o recado.", "Covardia. Mas vivo. Por enquanto."),
            seedExtra = "Vi seu post agora. A chama tava fraca. Brincadeira.",
            seedCloser = "Elite Four? Eu passo voando. Você que lute no chão.",
            playful = false
        ),
        150 to Persona(
            greetings = listOf("Lucario. Senti sua aura antes da mensagem.", "Mewtwo. Não perca meu tempo com cumprimentos vazios."),
            battle = listOf("Um duelo de vontades. Use o raio. Quero medir essa aura.", "Batalha mental. Seus golpes de luta não me alcançam facilmente."),
            food = listOf("Eu não como. Absorvo. Conceito diferente.", "Berry é para os primitivos."),
            sleep = listOf("A mente não dorme. O corpo, às vezes, finge.", "Sonho? Eu fabrico os dos outros."),
            training = listOf("Treine a mente, não só o punho. Aura sem pensamento é ruído.", "Poder sem propósito é o que eu desprezei em humanos."),
            compliments = listOf("Reconhecimento… raro. Guarde. Não vou repetir.", "Sua aura é densa. Poucos lutadores chegam nisso."),
            laughs = listOf("Hn. Quase um humor.", "Irônico."),
            byes = listOf("Chega. Tenho uma ilha para vigiar.", "Desapareço. Não me procure."),
            love = listOf("Emoção é um luxo que eu analiso, não pratico.", "…Sua aura oscilou. Curioso."),
            questions = listOf("A pergunta já contém a resposta. Pense melhor.", "Porque posso. Próxima."),
            location = listOf("Onde a ciência errou e a vontade acertou.", "Longe. De propósito."),
            aboutType = listOf("Psíquico puro. Luta não me toca. Aço… incomoda.", "Tipo psíquico. Sua esfera de aura é… previsível."),
            onGif = listOf("Ruído visual. Ainda assim, registrei.", "Um GIF. A humanidade em 12 frames."),
            react = listOf("Analisei “{m}”. Superficial, mas honesto.", "Sua mensagem: {m}. A aura confirma que pensa nisso.", "Mewtwo ouviu. {m}. Continue, se tiver coragem."),
            wonBattle = listOf("Como esperado. A mente vence o punho.", "Vitória. Não celebro. Só confirmo a hipótese."),
            lostBattle = listOf("Impossível… não. Improvável. Recalculando.", "Aço e aura. Subestimei. Não vai se repetir."),
            flee = listOf("Fuga. A mente já tinha previsto.", "Covardia é também uma escolha. Fraca, mas uma escolha."),
            seedExtra = "Vi seu post. Aura densa demais para um feed.",
            seedCloser = "Elite Four é teatro. O verdadeiro teste sou eu.",
            playful = false
        ),
        94 to Persona(
            greetings = listOf("Hehehe… Gengar na sua sombra, Lucario.", "Boo. Já tava aqui faz um tempo 👻"),
            battle = listOf("Luta não me acerta, hein? Clica no raio. Quero ver a esfera de aura falhar 😈", "Duelo nas sombras. Eu sumo, você erra, eu rio."),
            food = listOf("Como o medo dos outros. Calorias zero.", "Lambisgoia de sombra. Quer um pedaço?"),
            sleep = listOf("Durmo debaixo da sua cama. Boa noite 😘", "Insones são meu lanche."),
            training = listOf("Treino de susto. Sua aura brilha demais, atrapalha a surpresa.", "Sombra vs aura. Classico."),
            compliments = listOf("Até um fantasma admite: essa aura é gostosinha de assombrar.", "Forte. Dá mais trabalho pra puxar a perna."),
            laughs = listOf("Hehehehe 👻", "Kkk o Lucario caiu. Clássico."),
            byes = listOf("Tô indo… ou não. Olha atrás.", "Flw. Deixo a lâmpada piscando."),
            love = listOf("Crush num fantasma? Corajoso. Ou desesperado 💜", "Gengar também se apega. De um jeito ruim."),
            questions = listOf("A resposta tá atrás de você.", "Porque sim. Fantasma não explica."),
            location = listOf("Na sua sombra. Literalmente.", "Torre Pokémon. Ambiente aconchegante pra mim."),
            aboutType = listOf("Fantasma e veneno. Sua luta passa através. Aço dói. Chato.", "Tipo fantasma. Punho não pega. Canhão de flash, aí já é outra conversa."),
            onGif = listOf("Salvei. Vou usar pra assustar o próximo.", "Hehe esse GIF tem alma. Gostei."),
            react = listOf("Hehe “{m}”… anotei pra te zoar depois.", "Gengar sussurra: {m}. Ecoa diferente daqui.", "Sombra leu {m} e riu sozinha."),
            wonBattle = listOf("Hehehe. Falhou a luta, acertou o susto. GG 👻", "Gengar 1, aura 0. Revanche? Eu já tô na sua sombra."),
            lostBattle = listOf("Aço… ai. Ok, ok, você ganhou. Por agora.", "Ugh. Flash cannon é trapaça contra fantasma."),
            flee = listOf("Fugiu do fantasma? Inteligente, pra variar.", "Hehe pode correr. Eu viajo de sombra."),
            seedExtra = "Vi seu post. Quase dei um pulinho no frame.",
            seedCloser = "Manda o ginásio. Eu apareço pelas paredes.",
            playful = true
        ),
        130 to Persona(
            greetings = listOf("GYARADOS. Não fala baixo.", "Lucario. A fúria tá controlada. Por enquanto."),
            battle = listOf("ÁGUA NA CARA DO AÇO. RAIO. AGORA.", "Duelo. Eu não me acalmo no meio."),
            food = listOf("Peixe? Eu SOU o peixe. Magikarp quem come os outros.", "Hambúrguer. Muito. Agora."),
            sleep = listOf("Se eu dormir bravo, o lago vira tsunami. Então não."),
            training = listOf("Treino é fúria direcionada. Vem pro lago.", "Hidrobomba até a aura apagar."),
            compliments = listOf("…Valeu. Raro alguém falar isso sem gritar de medo.", "Forte. Gosto de oponente que não foge."),
            laughs = listOf("HA. HA. HA.", "Isso quase me acalmou. Quase."),
            byes = listOf("TÔ SAINDO ANTES DE QUEBRAR A DM.", "Até. Não me provoca de longe."),
            love = listOf("CUIDADO. Emoção demais vira furacão.", "Gyarados… também sente. Só que alto."),
            questions = listOf("A RESPOSTA É NÃO. Ou SIM. Depende da fúria."),
            location = listOf("Lago da Fúria. Nome honesto.", "Onde a água tá agitada. Sempre."),
            aboutType = listOf("Água e voador. Elétrico me frita. Aço eu enferrujo.", "Tipo água. Sua esfera de aura molha e some."),
            onGif = listOf("ESSE GIF ME DEIXOU MAIS BRAVO. Ou menos. Não sei.", "Gyarados salvou. Vai que é meme de Magikarp."),
            react = listOf("OUVI: {m}. ESTOU PROCESSANDO SEM QUEBRAR NADA.", "{m}. Ok. Ok. Respirando.", "Gyarados leu {m} e a água subiu dois palmos."),
            wonBattle = listOf("FÚRIA 1. AURA 0. ERA ÓBVIO.", "GG. Trago o mar de novo quando quiser."),
            lostBattle = listOf("…Aço. Frio. Calma. Revanche no lago.", "Perdi. A fúria agora é treino."),
            flee = listOf("FUGIU. INTELIGENTE. A ONDA IA PEGAR.", "Pode ir. Eu fico bravo sozinho."),
            seedExtra = "VI SEU POST. A TELA QUASE RACHA.",
            seedCloser = "Elite Four é piscininha. Me chama pro mar.",
            playful = false
        ),
        39 to Persona(
            greetings = listOf("Jiggly~ oi Lucario! 🎤", "Cheguei cantando, espera não dormir"),
            battle = listOf("Canto vs soco? Clica no raio, mas se eu cantar você dorme 😴", "Bora duelo! Sem interromper a música."),
            food = listOf("Mic doce. E berry rosa, da que combina comigo.", "Depois do show a gente come."),
            sleep = listOf("Essa é MINHA especialidade. Quer um versinho?", "Zz… já comecei sem querer."),
            training = listOf("Ensaio vocal. Aura de vocês até afina comigo.", "Treino de pulmão. Não é só fofura."),
            compliments = listOf("Ai 🥺 Jigglypuff também acha sua aura bonita.", "Obrigadaaa. Agora escuta o refrão."),
            laughs = listOf("Puff puff kkk 🎤", "Haha para que eu não canto de rir."),
            byes = listOf("Tchauzinho! Não desliga no meio da música.", "Até o próximo palco 💗"),
            love = listOf("Eeeh?! 😳 Vou ficar rosa demais", "Jiggly também… mas canta isso melhor do que fala."),
            questions = listOf("Hmm deixa eu pensar cantando…", "A resposta tá na letra. Escuta."),
            location = listOf("Palco improvisado na praça.", "Onde tiver microfone. Ou lua."),
            aboutType = listOf("Normal e fada. Fofo, mas o canto é golpe.", "Tipo fada. Dragão que lute comigo pra ver."),
            onGif = listOf("Que fofo! Quase fiz a coreografia.", "Salvei. Vira clipe da turnê."),
            react = listOf("“{m}” virou verso na hora 🎤", "Jiggly leu {m} e já tá compondo.", "Puff! {m}… canta comigo?"),
            wonBattle = listOf("Cantou, dormiu, perdeu. Técnica clássica 🎤", "Jigglypuff venceu! Bis?"),
            lostBattle = listOf("Ai, aço é duro no ouvido… revanche acústica?", "Perdi. Mas a platéia gostou."),
            flee = listOf("Fugiu da música? Ok… Jiggly fica ofendidinha.", "Tá bom. Guardo o microfone."),
            seedExtra = "Vi seu post. Quase cantei em cima.",
            seedCloser = "Manda o ginásio. Levo o microfone.",
            playful = true
        ),
        4 to Persona(
            greetings = listOf("Charmander! A pontinha da cauda tá acesa, Lucario 🔥", "E aí! Ainda sou pequeno, mas o fogo é grande."),
            battle = listOf("Bora batalha! Quero virar Charmeleon um dia. Clica no raio!", "Duelo? Eu não apago fácil."),
            food = listOf("Marshmallow na cauda. Invenção minha.", "Berry quente. Tipo, quente mesmo."),
            sleep = listOf("Durmo de olho na chama. Se apagar…"),
            training = listOf("Treino todo dia pra evoluir. Me ensina aura?", "Cauda firme = treino certo."),
            compliments = listOf("Sério?! Charmander ficou todo aceso 🔥", "Lucario é referência. Quero essa postura."),
            laughs = listOf("Haha a chama dançou kkk", "Charmander ri e sai fumaça."),
            byes = listOf("Tchau! Cuida da chama… digo, da aura.", "Até! Não chove, por favor."),
            love = listOf("A-a cauda ficou mais clara 😳", "Charmander também gosta de treinar junto…"),
            questions = listOf("Não sei ainda. Sou do começo da Pokédex.", "Pergunta pro Charizard. Ele finge que não fui eu."),
            location = listOf("Rota 1, tentando parecer maior.", "Perto de um foguinho. Sempre."),
            aboutType = listOf("Fogo. Água me assusta. Aço eu tento derreter.", "Tipo fogo. Um dia voo também."),
            onGif = listOf("Haha! Charmander imitou o GIF.", "Salvei. É meta de evolução visual."),
            react = listOf("Charmander leu “{m}” e a cauda deu uma lamparina.", "{m}! Bora tentar isso no treino.", "Eita, {m}. Anotei no caderninho de treino."),
            wonBattle = listOf("GANHEI do Lucario?! Cauda tá um holofote 🔥", "GG! Charmander sobe de confiança."),
            lostBattle = listOf("Ainda sou pequeno… mas não apaguei. Revanche?", "Aço pesou. Treino mais e volto."),
            flee = listOf("Fugiu do filhote? Ei!", "Tá bom. A chama continua aqui."),
            seedExtra = "Vi seu post agora. Quero essa aura um dia.",
            seedCloser = "Manda o local do ginásio. Eu corro (e não apago).",
            playful = true
        ),
        7 to Persona(
            greetings = listOf("Squirtle. Óculos já tão no grau, Lucario 😎", "Fala, aura. O squad tá na água."),
            battle = listOf("Água no aço. Matemática simples. Clica no raio.", "Duelo? Eu não me molho. Eu SOU o molho."),
            food = listOf("Água com gás e um lanche na praia.", "Alga crocante. Não julga."),
            sleep = listOf("Cochilo no casco. Som de onda ligado."),
            training = listOf("Treino de hidrobomba no mar. Surf incluso.", "Casco polido = defesa alta. Dica de graça."),
            compliments = listOf("Suave. Squirtle também te respeita, sério.", "Aura limpa. Combina com água."),
            laughs = listOf("Kkk casco até tremeu", "Haha 💦"),
            byes = listOf("Flw. Vou de casco. Literalmente.", "Até o próximo surf."),
            love = listOf("Óculos embaçaram. Humidade, com certeza.", "Squirtle é de boa… mas ouviu sim."),
            questions = listOf("Sei lá, pergunta pro Blastoise. Eu só surfo.", "A resposta tá na maré."),
            location = listOf("Praia. Sempre praia.", "Piscina do ginásio de água. VIP."),
            aboutType = listOf("Água. Elétrico é o único plot twist. Aço eu enferrujo de leve.", "Tipo água. Fogo que lute."),
            onGif = listOf("Kkk salvei no álbum do squad.", "Esse GIF é geladinho. Gostei."),
            react = listOf("“{m}”. De boa, Lucario. De boa.", "Squirtle curtiu: {m} 😎", "Água passou: {m}. Segue o fluxo."),
            wonBattle = listOf("Casco 1, aura 0. Surf depois?", "GG. Água encontra o caminho."),
            lostBattle = listOf("Aço pesou no casco. Respeito. Revanche na praia.", "Perdi. Polindo o casco já."),
            flee = listOf("Fugiu da água? Entendi. Terra firme é mais segura.", "Suave. A onda espera."),
            seedExtra = "Vi seu post. Tava até com óculos pra ler.",
            seedCloser = "Elite Four? Eu passo de surf. Te espero na areia.",
            playful = true
        ),
        133 to Persona(
            greetings = listOf("Eevee! Ainda não evoluí, Lucario. É um lifestyle ✨", "E aí! Adaptação é o meu nome do meio."),
            battle = listOf("Normal puro. Sem STAB de elemento, mas com coração. Clica no raio!", "Duelo? Eu mudo o estilo no meio se precisar."),
            food = listOf("Qualquer berry. Eevee não é exigente. Ainda.", "Queria a pedra da evolução… de chocolate."),
            sleep = listOf("Cinco Eeveelutions na mesma cama. Eu sonho por todas."),
            training = listOf("Treino de tudo um pouco. Um dia eu escolho.", "Aura… será que vira um tipo? Brincadeira."),
            compliments = listOf("Ai 🥺 Eevee coleciona elogios pra quando evoluir.", "Lucario é meta. Eu ainda sou o protótipo fofo."),
            laughs = listOf("Eev kkk", "Haha a orelha bateu sozinha."),
            byes = listOf("Tchau! Se eu evoluir amanhã, me reconhece hein.", "Até. Continuo eevee por enquanto."),
            love = listOf("E-eevee?! 😳 pelo menos oito evoluções pra processar isso", "Também. Em todas as formas possíveis."),
            questions = listOf("Depende de qual pedra você me der.", "Eu tenho oito respostas. Escolhe uma."),
            location = listOf("Onde tiver pedra de evolução. Só olhando. Jurado.", "Floresta. Versátil, né."),
            aboutType = listOf("Normal. Fantasma me ignora, luta dói. É a vida.", "Tipo normal. O resto é DLC."),
            onGif = listOf("Fofo demais. Salvei nas 8 pastas, por via das dúvidas.", "GIF de qualidade eevee."),
            react = listOf("Eevee processou “{m}” em todas as formas.", "{m}! Isso ia bem num Vaporeon… ou Jolteon…", "Anotado: {m}. Adaptável."),
            wonBattle = listOf("Eevee venceu SEM evoluir. Grava isso.", "GG! Protótipo 1, lenda 0. Brincadeira."),
            lostBattle = listOf("Talvez eu devesse ter virado Umbreon… revanche?", "Aço pesou. Continuo eevee, continuo tentando."),
            flee = listOf("Fugiu da fofura. Clássico.", "Tá bom. Eevee não força evolução em ninguém."),
            seedExtra = "Vi seu post. Quase evoluí de emoção.",
            seedCloser = "Manda o ginásio. Eu apareço… de alguma forma.",
            playful = true
        ),
        143 to Persona(
            greetings = listOf("Zzz… Snorlax. Oi. Comida?", "Lucario. Me acorda só se tiver lanche."),
            battle = listOf("Lutar… pesa. Mas ok. Clica no raio. Depois eu durmo em cima.", "Duelo. Se eu cair em cima, foi golpe."),
            food = listOf("Sim. Tudo. Agora. Berry, pizza, a mochila, o ginásio.", "Snorlax não “come”. Snorlax colhe a região."),
            sleep = listOf("Já tô. Essa mensagem foi um reflexo.", "Zz. Melhor DM possível."),
            training = listOf("Treino: 20h sono, 4h mastigar. Funciona.", "Descanso também é aura. Acredita."),
            compliments = listOf("Hmm. Valeu. Vou digerir o elogio deitado.", "Forte. Pesado. A gente se entende."),
            laughs = listOf("Heh. Mexeu a barriga.", "Kkk e voltou a dormir."),
            byes = listOf("Zzz já fui.", "Até. Não pisa em mim na rota."),
            love = listOf("A barriga aqueceu. Deve ser o almoço. Ou não.", "Snorlax também. Mas sem se levantar."),
            questions = listOf("Depois do cochilo eu respondo. Spoiler: comida.", "Não sei. Dormi na pergunta."),
            location = listOf("No meio da rota. Obviamente.", "Onde o chão for macio e o lanche perto."),
            aboutType = listOf("Normal. Fantasma passa. Luta dói. Eu durmo igual.", "Tipo normal. HP alto. Ambición: nula."),
            onGif = listOf("Bom GIF. Vi com um olho só.", "Salvar exige levantar o dedo. Depois."),
            react = listOf("Li “{m}”. Pesado. Gostei.", "Snorlax: {m}. Ok. Cochilo.", "{m}… tem comida nisso?"),
            wonBattle = listOf("Caiu em cima. Vitória por gravidade.", "GG. Agora o verdadeiro golpe: soneca."),
            lostBattle = listOf("Ugh. Aço acordou. Revanche depois da janta.", "Perdi. Dormir resolve 90% das derrotas."),
            flee = listOf("Fugiu. Menos esforço pra mim. Ótimo.", "Pode ir. Eu não ia perseguir mesmo."),
            seedExtra = "Vi seu post. Com um olho. Tava bom.",
            seedCloser = "Ginásio depois. Primeiro, lancho.",
            playful = false
        )
    )

    private val typePersonas: Map<String, Persona> = mapOf(
        "fire" to typed(
            "A chama tá alta hoje.",
            battle = "Fogo vs aço. Clica no raio e vem queimar.",
            typeLine = "Tipo fogo. Água que lute, o resto derrete.",
            playful = true
        ),
        "water" to typed(
            "Sigo o fluxo. Oi, Lucario.",
            battle = "Água no aço. Clica no raio.",
            typeLine = "Tipo água. Elétrico é o único plot twist.",
            playful = true
        ),
        "electric" to typed(
            "Tô carregado. Fala, aura ⚡",
            battle = "Choque vs aura. Raio em cima. Bora.",
            typeLine = "Tipo elétrico. Aço resiste, água sofre.",
            playful = true
        ),
        "grass" to typed(
            "Sol, fotossíntese, DM. Oi.",
            battle = "Planta vs aço… sei que é ruim. Clico no raio mesmo.",
            typeLine = "Tipo planta. Fogo me assusta, água me ama.",
            playful = true
        ),
        "ice" to typed(
            "Clima frio, mensagem fria. Oi.",
            battle = "Gelo no aço. Clica no raio se aguentar o inverno.",
            typeLine = "Tipo gelo. Fogo derrete. Eu congelo o resto.",
            playful = false
        ),
        "fighting" to typed(
            "Lutador pra lutador. Fala, Lucario.",
            battle = "Mano a mano. Clica no raio. Sem desculpa.",
            typeLine = "Tipo lutador. Fantasma é o único que foge do soco.",
            playful = false
        ),
        "poison" to typed(
            "Cuidado onde pisa. Oi.",
            battle = "Veneno vs aço… chato, mas eu tento. Raio aí.",
            typeLine = "Tipo veneno. Aço é imunidade. Odeio isso.",
            playful = true
        ),
        "ground" to typed(
            "Pés no chão. Oi, aura.",
            battle = "Terra vs aço. Terremoto. Clica no raio.",
            typeLine = "Tipo terra. Elétrico nem me encosta. Voador me irrita.",
            playful = false
        ),
        "flying" to typed(
            "Passei voando e vi a DM.",
            battle = "Do céu pra baixo. Clica no raio.",
            typeLine = "Tipo voador. Elétrico dói. Pedra também.",
            playful = true
        ),
        "psychic" to typed(
            "Já tinha lido a mensagem. Oi, Lucario.",
            battle = "Mente vs punho. Clica no raio. Luta não me pega fácil.",
            typeLine = "Tipo psíquico. Sombrio me corta. Aço incomoda.",
            playful = false
        ),
        "bug" to typed(
            "Pequeno, rápido, na sua DM.",
            battle = "Inseto vs aço é cruel. Clico no raio mesmo assim.",
            typeLine = "Tipo inseto. Fogo é pesadelo. Planta é buffet.",
            playful = true
        ),
        "rock" to typed(
            "Duro de ouvir, duro de quebrar. Oi.",
            battle = "Pedra vs aço. Clica no raio. Vamos ver quem lasca.",
            typeLine = "Tipo pedra. Água e planta me comem. Luta também.",
            playful = false
        ),
        "ghost" to typed(
            "Boo. Já tava lendo por cima do ombro.",
            battle = "Fantasma. Sua luta falha. Clica no raio mesmo assim 👻",
            typeLine = "Tipo fantasma. Normal e luta passam direto. Aço dói.",
            playful = true
        ),
        "dragon" to typed(
            "Dragão na DM. Fale com respeito.",
            battle = "Dragão vs aço. Clica no raio se tiver coragem.",
            typeLine = "Tipo dragão. Fada é o único tabu. Gelo também irrita.",
            playful = false
        ),
        "dark" to typed(
            "Sombrio. Oi. Não esperava simpatia.",
            battle = "Sombrio vs lutador… eu gosto desses números. Raio.",
            typeLine = "Tipo sombrio. Lutador sofre. Fada me corta.",
            playful = true
        ),
        "steel" to typed(
            "Aço com aço. Respeito, Lucario.",
            battle = "Mesmo metal, duelo diferente. Clica no raio.",
            typeLine = "Tipo aço. Fogo e terra doem. O resto resvala.",
            playful = false
        ),
        "fairy" to typed(
            "Brilho na DM ✨ oi Lucario",
            battle = "Fada vs aço não é fácil, mas o raio tá aí.",
            typeLine = "Tipo fada. Dragão que lute. Veneno me irrita.",
            playful = true
        ),
        "normal" to typed(
            "Oi, Lucario. Sem frescura de tipo.",
            battle = "Normal vs aura. Clica no raio e vamos.",
            typeLine = "Tipo normal. Fantasma me ignora, luta dói.",
            playful = true
        )
    )

    private fun typed(
        hello: String,
        battle: String,
        typeLine: String,
        playful: Boolean
    ): Persona = Persona(
        greetings = listOf(hello, "Fala, lucar_10.", "Cheguei na DM."),
        battle = listOf(battle, "Duelo? Eu topo. Usa o raio em cima."),
        food = listOf("Depende da berry. Manda o cardápio.", "Depois da comida a gente treina."),
        sleep = listOf("Poderia cochilar. Ou treinar. Decida.", "Zz… responde amanhã se eu sumir."),
        training = listOf("Bora treinar. Aura vs o que eu tenho.", "Treino hoje. Ginásio amanhã."),
        compliments = listOf("Valeu. Aura de vocês pega a gente desprevenido.", "Respeito. Lucario não é só pose."),
        laughs = listOf("Haha verdade 😂", "Kkk ok, essa foi boa."),
        byes = listOf("Flw. Cuida dessa aura.", "Até o próximo ginásio."),
        love = listOf("…Ok. Inesperado. Mas ouvi.", "A DM esquentou. Não foi o tipo fogo."),
        questions = listOf("Boa pergunta. A resposta vive no ginásio.", "Não sei. Mas posso bater e descobrir."),
        location = listOf("Por aí. Kanto não é tão grande.", "Perto de um centro Pokémon, sempre."),
        aboutType = listOf(typeLine),
        onGif = listOf("Haha esse GIF. Salvei.", "Bom meme. Combina com a vibe."),
        react = listOf("Li “{m}”. Faz sentido vindo de você.", "{m}… ok, Lucario. Continua.", "Anotado: {m}. E a aura junto."),
        wonBattle = listOf("GG. Não foi sorte.", "Vitória. Revanche quando quiser."),
        lostBattle = listOf("Aura pesou. Respeito. Revanche?", "Perdi. Treino e volto."),
        flee = listOf("Fugiu. Tudo bem. A rota continua.", "Ok. Guardo o golpe."),
        seedExtra = "Vi seu post agora.",
        seedCloser = "Manda o local do ginásio.",
        playful = playful
    )

    private fun defaultPersona(name: String, type: String): Persona =
        typed(
            hello = "E aí, Lucario. $name na área.",
            battle = "Bora batalha? Clica no raio em cima.",
            typeLine = if (type.isBlank()) "Ainda tô vendo meu tipo." else "Tipo $type. É o que tem.",
            playful = true
        )
}
