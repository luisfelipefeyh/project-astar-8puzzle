# GUI Interativa — Design Spec

**Data:** 2026-05-02  
**Objetivo:** Interface web interativa para apresentação do trabalho A* 8-puzzle à professora, com animação passo a passo e log de decisões no terminal.

---

## Arquitetura

Servidor HTTP embutido no JDK (`com.sun.net.httpserver.HttpServer`) serve uma SPA (`web/index.html`). Nenhuma dependência externa. O backend Java roda o algoritmo existente e retorna todos os passos como JSON. `TracingSearch` também imprime um log detalhado no stdout do terminal.

```
java -cp out puzzle.ui.PuzzleServer [--port 8080]
→ http://localhost:8080
→ log de decisões no terminal ao vivo
```

---

## Novos arquivos

| Arquivo | Responsabilidade |
|---|---|
| `src/main/java/puzzle/ui/PuzzleServer.java` | Inicializa HttpServer, registra rotas, abre browser automaticamente |
| `src/main/java/puzzle/ui/SolveHandler.java` | `POST /solve` — parseia JSON, chama TracingSearch, serializa resposta |
| `src/main/java/puzzle/ui/CasesHandler.java` | `GET /cases` — retorna os 5 presets como JSON |
| `src/main/java/puzzle/search/StepSnapshot.java` | Imutável: board + frontierTop5 + visitedCount + g/h/f + move + logEntry |
| `src/main/java/puzzle/search/TracingSearch.java` | Wrapa BestFirstSearch; coleta StepSnapshot por expansão; imprime terminal log |
| `web/index.html` | SPA completa: HTML + CSS + JS vanilla, sem frameworks |

Arquivos existentes **não modificados** (exceto Main.java que ganha alias `gui`).

---

## API REST

### `GET /cases`
```json
{
  "cases": [
    { "name": "EASY_1",   "depth": 5,  "tiles": [1,0,3,5,2,6,4,7,8] },
    { "name": "MEDIUM_1", "depth": 10, "tiles": [5,1,3,2,6,8,4,7,0] },
    { "name": "MEDIUM_2", "depth": 20, "tiles": [0,1,8,5,3,2,4,6,7] },
    { "name": "HARD_1",   "depth": 27, "tiles": [8,6,7,2,5,4,3,0,1] },
    { "name": "HARD_2",   "depth": 29, "tiles": [6,4,7,8,5,0,3,2,1] }
  ]
}
```

### `POST /solve`
Request:
```json
{ "tiles": [1,0,3,5,2,6,4,7,8], "algo": "MANHATTAN_LC" }
```
`algo` aceita: `UCS` | `NON_ADMISSIBLE` | `MISPLACED` | `MANHATTAN_LC`

Response:
```json
{
  "solved": true,
  "pathLength": 5,
  "nodesVisited": 11,
  "maxFrontierSize": 11,
  "elapsedMs": 5,
  "steps": [
    {
      "stepIndex": 0,
      "board": [1,0,3,5,2,6,4,7,8],
      "move": null,
      "g": 0, "h": 5, "f": 5,
      "frontierTop": [
        { "board": [1,2,3,5,0,6,4,7,8], "g": 1, "h": 4, "f": 5 }
      ],
      "visitedCount": 0,
      "logEntry": "[0] Início — g=0 h=5 f=5"
    }
  ]
}
```

Cada `step` corresponde a uma expansão do algoritmo. O array `steps` começa no estado inicial e termina no estado objetivo.

---

## Terminal Log (Option B)

`TracingSearch.solve()` imprime no stdout a cada expansão:

```
[ManhattanLC] Passo 1: Expandindo g=0 h=5 f=5
              Estado: 1 0 3 / 5 2 6 / 4 7 8
  → DOWN  g=1 h=4 f=5  ★ entra na fronteira
  → LEFT  g=1 h=5 f=6    entra na fronteira

[ManhattanLC] Passo 2: Expandindo g=1 h=4 f=5
              Estado: 1 2 3 / 5 0 6 / 4 7 8
  → UP    já visitado — descartado
  → LEFT  g=2 h=3 f=5  ★ entra na fronteira
  → DOWN  g=2 h=5 f=7    entra na fronteira
  → RIGHT g=2 h=4 f=6    entra na fronteira

[ManhattanLC] ✓ SOLUÇÃO em 5 passos | 11 nós visitados | fronteira máx. 11 | 5ms
```

---

## Frontend — web/index.html

SPA de arquivo único, HTML + CSS + JS vanilla. Duas "telas" controladas por `display:none/block`.

### Tela 1 — Setup

- Aba **Presets**: 5 cards (EASY/MEDIUM/HARD) com mini-tabuleiro visual e profundidade. Clique seleciona.
- Aba **Interativo**: tabuleiro 3×3 grande e clicável. Clicar em peça adjacente ao espaço vazio executa o movimento. Botões Embaralhar e Resetar. Badge "Solucionável / Não solucionável" em tempo real (conta inversões no JS).
- Botão **Resolver** (desabilitado até tabuleiro selecionado) → chama `POST /solve` com o algoritmo padrão (MANHATTAN_LC) e navega para Tela 2.

### Tela 2 — Solver

**Coluna esquerda — Tabuleiro:**
- Grid 3×3, tiles com animação CSS `transition: all 0.2s` ao trocar posição.
- Barra de progresso: "Passo N de M".
- Linha g=X · h=Y · **f=Z** abaixo do tabuleiro, atualiza a cada passo.
- Controles: ⏮ (início) ⏪ (−1) ⏸/▶ (play/pause) ⏩ (+1) ⏭ (fim) + slider de velocidade (100ms–2000ms por passo).

**Coluna direita — Painel:**
- Seletor dos 4 algoritmos (botões com nome + descrição). Trocar algoritmo re-chama `/solve` com mesmo board.
- 4 métricas: Nós Visitados, Tamanho do Caminho, Fronteira Máx., Tempo.
- Tabela comparativa: ao clicar "Rodar todos os 4", dispara 4 chamadas paralelas a `/solve` e preenche a tabela. Linha do algoritmo mais eficiente em nós fica destacada em verde.

**Gaveta inferior — Internals (Option C):**
- Botão "🔍 Ver internals do algoritmo" na base da tela.
- Ao clicar, expande painel dividido em dois:
  - **Fronteira (top 5):** lista os 5 melhores nós da priority queue no passo atual, com board em notação curta + g/h/f. O primeiro (menor f) fica destacado em azul.
  - **Log de decisões:** lista rolável de `logEntry` de todos os passos até o atual. Rola automaticamente para o último. Entrada do passo atual destacada em amarelo.
- Sincronizado com a animação: avançar/voltar passo atualiza a gaveta.

---

## Comportamentos importantes

- **Tabuleiro insolucionável** no modo interativo: badge vermelho "Não solucionável" + botão Resolver desabilitado.
- **Erro do servidor** (ex: board inválido): alerta visual inline, não quebra a UI.
- **CORS**: servidor adiciona header `Access-Control-Allow-Origin: *` para facilitar dev local.
- **Arquivo estático**: `GET /` serve `web/index.html` diretamente do filesystem.
- **Porta configurável**: `java -cp out puzzle.ui.PuzzleServer --port 9090`.

---

## Como rodar para apresentação

```bash
export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
javac -d out $(find src -name "*.java")
java -cp out puzzle.ui.PuzzleServer
# Abre http://localhost:8080 automaticamente
# Terminal exibe log de decisões ao vivo
```
