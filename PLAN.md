# PLAN.md — BeeBetter

App Android nativo, offline-first, de checklist de metas com recorrência diária,
semanal e mensal. Visual neobrutalista domado, com identidade de colmeia:
cera, mel e tinta, e a célula hexagonal como forma-assinatura. Sem backend,
sem login, sem rede.

---

## Decisões tomadas

Respostas às perguntas em aberto, que fixam a lógica de domínio e os testes:

| Tema | Decisão |
| --- | --- |
| **Streak** | O período atual em aberto é **neutro**. O streak conta os períodos fechados consecutivos e soma +1 se o período atual já estiver marcado. Não marcar hoje só quebra a sequência quando o dia virar. |
| **Marcação retroativa** | **Permitida pela tela de Histórico**, sem limite de janela. O repositório aceita `periodKey` arbitrária; o heatmap é tocável. |
| **Heatmap** | **Os dois escopos**: tela global de Histórico (por dia, quantas metas de N foram concluídas) + heatmap por meta na tela de detalhe. |
| **Taxa de conclusão** | **Últimos N períodos da recorrência**: 30 dias para diárias, 4 semanas para semanais, 3 meses para mensais. O rótulo na UI muda junto com a recorrência. |

Defaults assumidos (diga se quiser diferente):

- `applicationId` / namespace: `com.checklisted.app`
- Nome do app: **BeeBetter** (o `applicationId` segue `com.checklisted.app` —
  renomear o pacote é uma decisão à parte, ver abaixo)
- Lembrete diário **global** (um só, não por meta), conforme o escopo da v1
- Excluir meta faz **cascade** nas `Completion` (o histórico se preserva via
  *arquivar*, que é a ação não destrutiva)
- Linter: **ktlint** via `org.jlleitschuh.gradle.ktlint`, rodando no CI local
  desde a Fase 1

---

## Arquitetura

```
app/src/main/java/com/checklisted/app/
├── data/
│   ├── local/          Room: entities, DAOs, database, converters
│   ├── prefs/          DataStore: início da semana, tema, lembrete
│   └── repository/     implementações dos contratos de domain
├── domain/
│   ├── model/          Goal, Completion, Recurrence, GoalWithStatus, Streak
│   ├── period/         PeriodCalculator — periodKey, virada, iteração
│   ├── repository/     interfaces
│   └── usecase/        toggle, streak, taxa de conclusão, heatmap
├── ui/
│   ├── theme/          Color.kt, Type.kt, Shape.kt, NeoTheme.kt
│   ├── components/     NeoButton, NeoCard, NeoCheckbox, NeoTextField,
│   │                   NeoChip, NeoDialog, NeoProgressBar (+ @Preview)
│   ├── today/          tela Hoje
│   ├── goal/           criar/editar meta, detalhe da meta
│   ├── history/        heatmap global
│   └── settings/       preferências e lembrete
├── work/               WorkManager: lembrete diário
└── di/                 módulos Hilt
```

`domain/period` é **Kotlin puro** (sem dependência de Android) para rodar em
testes JVM rápidos. Usa `java.time` com desugaring habilitado (minSdk 26 já tem
`java.time`, mas o desugaring garante a API completa).

### Regras de período

- `periodKey` derivada do fuso local via um `Clock` injetável (testes usam
  `Clock.fixed`, nunca `LocalDate.now()` solto).
  - `DAILY` → `2026-08-16`
  - `WEEKLY` → `2026-W33` (semana ISO, primeiro dia configurável)
  - `MONTHLY` → `2026-08`
- Marcar/desmarcar = inserir/remover uma `Completion`. **Virada de período nunca
  apaga histórico.**
- Reatividade da virada: um `Flow` de `periodKey` alimentado por um
  `BroadcastReceiver` de `ACTION_DATE_CHANGED` / `ACTION_TIME_CHANGED` /
  `ACTION_TIMEZONE_CHANGED`, mais revalidação em `onResume` (o receiver não é
  garantido em Doze).

---

## Fases

Cada fase termina com `./gradlew assembleDebug` e `./gradlew test` passando, um
commit e um resumo para você antes de eu seguir.

### Fase 1 — Projeto + design system ✅

- Scaffold Gradle KTS com `libs.versions.toml`, minSdk 26 / targetSdk 35
- Hilt, Room, DataStore, Navigation, WorkManager declarados
- ktlint configurado
- `ui/theme/`: paleta clara e escura, tipografia condensada uppercase, shapes 0–4.dp
- Modificadores `neoBorder` / `neoShadow` (offset sólido 4.dp, zero blur) e uma
  `Indication` própria que desloca +4.dp e some com a sombra ao pressionar
- Os 7 componentes `Neo*`, cada um com `@Preview` claro e escuro
- **Entregável:** app roda mostrando uma galeria dos componentes

### Fase 2 — Persistência + lógica de período (testada) ✅

- Entities `GoalEntity` / `CompletionEntity`, índice único `(goalId, periodKey)`
- DAOs com `Flow`, repositórios, módulos Hilt
- `PeriodCalculator` completo: chave por recorrência, limites do período,
  iteração para trás, virada de ano em semana ISO
- `StreakCalculator`: streak atual (período aberto neutro), recorde, taxa dos
  últimos N períodos
- **Testes unitários** de período e streak — casos de borda: 29–31 dez em semana
  ISO, ano bissexto, mudança de fuso, semana começando no domingo, buraco no meio
  do histórico, marcação retroativa
- **Entregável:** `./gradlew test` verde, sem UI nova

> **Nota sobre o início da semana.** A chave semanal usa regras ISO generalizadas
> (`WeekFields.of(primeiroDia, 4)`), então funciona para segunda **e** domingo.
> Trocar a configuração re-agrupa o histórico semanal: uma conclusão gravada sob
> semana-começa-na-segunda pode cair numa semana de número diferente depois da
> troca. Nada é apagado — o histórico é relido através da nova fronteira.

### Fase 3 — Tela Hoje + CRUD ✅

- Hoje: três seções (Diárias / Semanais / Mensais), contador `3/5`,
  `NeoProgressBar` chunky por seção
- `NeoCheckbox` com haptic e animação de afundar na sombra
- Criar / editar / arquivar / excluir (com `NeoDialog` de confirmação)
- Reordenar por drag, persistindo `position`
- Estados vazios ilustrados com texto com personalidade
- Virada de período reativa ligada de ponta a ponta
- **Entregável:** app usável

> A galeria de componentes da Fase 1 foi removida: ela era o entry point
> provisório e agora está substituída pela tela Hoje. Os `@Preview` de cada
> componente continuam de pé.

### Fase 4 — Streaks + histórico ✅

- Tela de detalhe da meta: streak atual, recorde, taxa da janela correta,
  heatmap dos últimos 3 meses **daquela meta**
- Tela de Histórico global: grade por dia com intensidade proporcional
- Toque na célula marca/desmarca o período retroativamente
- **Entregável:** as duas telas navegáveis a partir de Hoje

> Uma meta semanal ou mensal acende **todos** os dias do período em que foi
> concluída. O grid mostra quando o usuário estava em dia, e uma meta mensal
> marcada no dia 3 estava em dia o mês inteiro, não só naquele dia.
>
> Tocar numa célula do Histórico global abre o dia com as metas daquela data,
> já que um dia agrega várias metas e um toque só seria ambíguo. No heatmap do
> detalhe, o toque marca direto — ali só existe uma meta.

### Fase 5 — Lembretes + polimento ✅

- `DataStore`: horário do lembrete, início da semana, modo de tema
- Tela de Configurações
- WorkManager com `PeriodicWorkRequest` diária, notificação neobrutalista,
  permissão `POST_NOTIFICATIONS` no Android 13+, reagendamento no boot
- Passada final: contraste, acessibilidade (`contentDescription`, alvos de 48.dp),
  zero warning novo de compilação
- **Entregável:** definição de pronto cumprida

> O lembrete é um one-shot que se reagenda, **não** um `PeriodicWorkRequest`:
> um periódico repete a cada 24 horas fixas, o que desloca o horário escolhido
> em uma hora sempre que o relógio muda. Recalcular a próxima ocorrência local
> após cada disparo o mantém preso ao relógio de parede.
>
> Ele só notifica se ainda houver meta em aberto — a contagem existe para poder
> ficar calado.
>
> Reordenar por arraste é inalcançável com leitor de tela, então a mesma
> reordenação está exposta como ações customizadas de acessibilidade.

---

## Definição de pronto

Estado final:

- [x] `./gradlew assembleDebug` passando
- [x] `./gradlew test` passando — 138 testes
- [x] Nenhum warning novo de compilação
- [x] `@Preview` para cada componente do design system
- [x] ktlint sem violações

- [x] `./gradlew verifyPaparazziDebug` passando — 43 screenshots

`./gradlew assembleRelease` também passa, com R8 e shrink de recursos ligados.

### Screenshots

Paparazzi renderiza Compose na JVM via layoutlib, sem emulador. As imagens douradas
ficam em `app/src/test/snapshots/` e cobrem cada componente em tema claro e escuro,
mais as superfícies reais: lista de metas, estado vazio, estatísticas e heatmap.

- `./gradlew recordPaparazziDebug` regrava as imagens
- `./gradlew verifyPaparazziDebug` falha se algo mudou de aparência

Olhar as primeiras renderizações encontrou três defeitos que nenhum teste de
comportamento pegaria: o estado desativado virava cinza de baixo contraste, a
ilustração do estado vazio tinha tracinhos fora da trajetória do check, e o tile de
estatística acentuado ficava com texto bone sobre rosa.

Imagem de componente não bastou. Dois defeitos passaram por ela e só apareceram no
aparelho — o grid do histórico mais alto que a tela e um chip de horário virado de
lado. A resposta foi renderizar **telas inteiras, em tamanho de aparelho, sem
`SHRINK`**, com a quantidade de dados que um app em uso tem
(`FullScreenScreenshotTest`). Isso achou de imediato mais três: o diálogo pedindo
428.dp numa tela de 411.dp, o botão flutuante cobrindo a última meta da lista, e o
grid do histórico sem eixo de tempo nenhum.

Duas armadilhas da ferramenta, documentadas para não serem redescobertas:

- O Paparazzi prende a janela de diálogo numa largura fixa e corta o que passa dela.
  Um diálogo é renderizado pelo corpo (`NeoDialogContent`), não pela janela.
- `DeviceConfig.locale` resolve recurso, não `Locale.getDefault()`. Datas e dias da
  semana saem do `AppLocale` do app, que é o idioma em que a interface está escrita.

Continua **não verificado em dispositivo**: o gesto de arraste (incluindo a rolagem
automática), o disparo real do WorkManager e a notificação. Screenshot test cobre
pixels parados, não gesto nem agendamento.

---

## Identidade — BeeBetter

A marca é a colmeia, e o que a carrega é a paleta antes de qualquer desenho: a
página é **cera** (`#FBF3E2`), a cor de ação é **mel** (`#C77F00`) e o rótulo
sobre ela é **tinta**, não branco — preto sobre âmbar é o único par de cores que
todo mundo já lê como abelha.

O mel é fundo em vez do amarelo vivo que a abelha sugere porque essa cor também
precisa funcionar como *traço* sobre um card branco: no brilho máximo media 2:1
ali, invisível como o tique do estado vazio ou o preenchimento da barra de
progresso. O amarelo vivo sobrou para o tema escuro e para o ícone, onde não há
texto a contrastar.

A rampa do heatmap passou a compartilhar o matiz do mel — uma célula que se
preenche *é* uma célula de favo enchendo — mas é mantida um degrau de valor mais
escura que a ação (2,2:1 de distância), porque pintar o grid exatamente na cor
dos botões já fez "isto é tocável" e "esta semana foi cheia" parecerem a mesma
coisa.

O carmim da exclusão é a única cor que não deve nada à colmeia. Todo o resto é
cera, mel ou tinta — vizinhos na roda —, então um vermelho tirado dessa família
leria como mais um tom de mel em vez de exceção.

A célula hexagonal (`NeoCombCell`) é a forma-assinatura: caixa de seleção, estado
vazio e ícone do launcher. **Não** o heatmap — favo de verdade precisa de linhas
alternadas defasadas para fechar, e defasar aquele grid custaria as linhas retas
de dia da semana e as colunas de semana alinhadas que levaram dois defeitos de
aparelho para acertar.

### Pendente

O `applicationId` e o pacote Kotlin continuam `com.checklisted.app`. Renomear
alcança todos os arquivos do módulo, o nome do banco Room e os 40 nomes de
arquivo de screenshot; é uma decisão à parte, sem efeito visível para quem usa o
app enquanto ele não for publicado.

---

## Fora do escopo (v1)

Contas, sync, widgets, subtarefas, gamificação.
