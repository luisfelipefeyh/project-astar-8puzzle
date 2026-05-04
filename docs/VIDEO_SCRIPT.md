# Roteiro — Trabalho Prático 1: Métodos de Busca

**Duração estimada:** ~9 minutos  
**Apresentadores:** Luis Felipe · João Pedro

---

## ABERTURA — 30 segundos

> "Olá, professora Nathalia. Somos Luis Felipe e João Pedro, da disciplina de Sistemas Inteligentes. Neste vídeo apresentamos nossa implementação do algoritmo A* aplicado ao 8-puzzle, em Java puro, sem nenhuma biblioteca externa."

**[Mostrar na tela: terminal rodando `java -cp out puzzle.Main --algo MANHATTAN_LC --case EASY_1`]**

---

## PARTE 1 — Métodos principais e relação com A* (~2 min)

> "Vamos começar pela estrutura do projeto e como as classes se conectam ao algoritmo."

**[Abrir IDE com árvore de arquivos visível]**

**`Board.java` — o estado**
> "O estado do tabuleiro é um `int[9]`, índice 0 a 8 da esquerda para a direita, cima para baixo. O valor 0 é o espaço vazio. O método `hashKey()` empacota os 9 valores em nibbles de 4 bits num `long` — chave única para cada permutação, usada no conjunto de visitados."

**`Node.java` — o nó da busca**
> "Cada nó carrega: estado, pai, o movimento que o gerou, `g` (custo do caminho percorrido) e `h` (estimativa da heurística). O `f()` é `g + h`. O flag `stale` serve para lazy deletion — explicamos a seguir."

**`BestFirstSearch.java` — o motor**
> "Este é o coração. O algoritmo é genérico: recebe qualquer `Heuristic` por injeção de dependência. Para UCS injetamos `ZeroHeuristic`; para A* injetamos qualquer outra implementação. O algoritmo em si não muda — só a heurística."

**`Heuristic.java` — a interface**
> "Interface com um único método: `estimate(Board b)`. Cada uma das 4 variantes implementa esse contrato."

**`reconstructPath()` — o caminho**
> "Quando o objetivo é encontrado, percorremos a cadeia de ponteiros `parent` do nó final até a raiz, coletando os movimentos em ordem reversa. Esse é o menor caminho retornado."

---

## PARTE 2 — Gerenciamento da fronteira (~2 min)

**[Mostrar BestFirstSearch.java — linhas da frontier]**

> "Agora a parte mais importante do trabalho: como gerenciamos a fronteira."

> "Usamos três estruturas de dados juntas:"

```java
PriorityQueue<Node> frontier  // sempre retira o nó com menor f
HashMap<Long, Node> openMap   // acesso O(1) à versão mais barata na fronteira
HashSet<Long>       closed    // estados definitivamente expandidos
```

> "A `PriorityQueue` garante que expandimos sempre o menor `f`. O Java não permite decrementar prioridade numa heap, então usamos `lazy deletion` — marcamos versões antigas como `stale` em vez de reorganizar."

**[Destacar o bloco de verificação no código]**

> "Antes de adicionar um sucessor à fronteira, fazemos três verificações:"

```java
// 1. Já foi definitivamente expandido? Descarta.
if (closed.contains(succKey)) continue;

// 2. Já está na fronteira com custo menor ou igual? Descarta.
Node existing = openMap.get(succKey);
if (existing != null && succ.g() >= existing.g()) continue;

// 3. Versão mais cara na fronteira: marca como stale e substitui.
if (existing != null) existing.stale = true;

frontier.add(succ);
openMap.put(succKey, succ);
```

> "Quando retiramos da fila: `if (current.stale) continue` — descartamos versões antigas sem tocar na heap."

> "O desempate na fila é por `h` — entre dois nós com mesmo `f`, preferimos o que está mais perto do objetivo."

---

## PARTE 3 — Heurísticas (~2 min 30 seg)

**[Abrir cada arquivo de heurística]**

**1. UCS — `ZeroHeuristic`**
> "`h = 0` sempre. Sem estimativa. Explora em ondas de custo crescente — equivalente a busca em largura ponderada. Garante solução ótima mas é cego ao objetivo."

**2. Não admissível — `OverestimateHeuristic`**
> "`h = 3 × distância Manhattan`. Superestima propositalmente. Expande muito menos nós por ser mais 'agressiva', mas perde a garantia de otimalidade. Nos nossos testes com HARD_1, encontrou caminho de 35 passos em vez do ótimo de 31."

**3. Peças Fora do Lugar — `MisplacedTilesHeuristic`**
> "Conta quantas peças não estão na posição correta. Admissível — cada peça fora precisa de pelo menos 1 movimento. Fácil de implementar mas fraca: não considera distância, só presença. Range: 0 a 8."

**4. Manhattan + Conflito Linear — `ManhattanLinearConflictHeuristic`**
> "Soma das distâncias de cada peça à sua posição objetivo, em linhas e colunas. Para a peça no índice `i` com destino `goal`: `|i/3 − goal/3| + |i%3 − goal%3|`. Range: 0 a ~24."

> "Somamos ainda `2 × conflitos lineares`. **O que é um conflito linear?** Dois tiles estão na sua linha-objetivo (ou coluna-objetivo), mas na ordem invertida. Para se cruzarem, um precisa sair da linha e voltar — pelo menos 2 movimentos extras por par conflitante."

**[Mostrar `countRowConflicts` no código]**

> "Implementado matematicamente: para cada linha, verificamos pares de tiles cujo `goal_row == linha_atual` e que estejam com colunas trocadas. Nenhuma tabela codificada."

> "Essa heurística **domina** Manhattan pura: sempre retorna valor ≥ Manhattan, nunca superestima. Portanto é admissível e mais precisa — leva o A* a expandir muito menos nós."

**Comparação de estimativas para HARD_1:**
> "Para o mesmo estado inicial: UCS estima 0; Misplaced estima ~8; Manhattan estima ~20; Manhattan+LC estima ~24. O custo real ótimo é 31. Quanto mais próxima, melhor."

---

## PARTE 4 — Tabela comparativa de desempenho (~2 min)

**[Mostrar tabela no terminal ou na GUI — "Rodar todos os 4"]**

| Algoritmo      | Caso     | Nós visitados | Path | Front. máx. | Tempo   |
|----------------|----------|--------------|------|-------------|---------|
| UCS            | EASY_1   | 48           | 5    | 34          | 0.007s  |
| UCS            | MEDIUM_1 | 543          | 10   | 329         | 0.008s  |
| UCS            | HARD_1   | **181.439**  | 31   | 25.140      | 0.223s  |
| 3×Manhattan    | EASY_1   | 6            | 5    | 7           | 0.005s  |
| 3×Manhattan    | MEDIUM_1 | 11           | 10   | 11          | 0.006s  |
| Misplaced      | EASY_1   | 6            | 5    | 7           | 0.005s  |
| Misplaced      | MEDIUM_1 | 23           | 10   | 19          | 0.006s  |
| Misplaced      | HARD_1   | **121.519**  | 31   | 25.913      | 0.170s  |
| Manhattan LC   | EASY_1   | 6            | 5    | 7           | 0.006s  |
| Manhattan LC   | MEDIUM_1 | 11           | 10   | 11          | 0.005s  |
| Manhattan LC   | MEDIUM_2 | 19           | 14   | 17          | 0.005s  |
| Manhattan LC   | HARD_1   | **3.794**    | 31   | 2.097       | 0.015s  |
| Manhattan LC   | HARD_2   | **3.797**    | 31   | 2.100       | 0.016s  |

> "Os números falam por si. UCS no HARD_1 visita 181 mil nós em 0.22s. Manhattan+LC visita 3.794 nós em 15ms — **47 vezes menos nós, 15 vezes mais rápido**, com o mesmo caminho ótimo de 31 passos."

> "Misplaced, apesar de admissível, visita 121 mil nós — quase tão ruim quanto UCS. Isso mostra que **admissibilidade sozinha não basta** — precisamos de precisão."

> "A 3×Manhattan é rápida — 11 nós no MEDIUM — mas no HARD encontrou 35 passos em vez de 31. Sacrificou otimalidade por velocidade."

> "**Conclusão:** Manhattan+LC é a combinação ideal: ótima e eficiente."

---

## PARTE 5 — O que foi alcançado e limitações (~30 seg)

> "Todos os 4 objetivos foram alcançados. As heurísticas são 100% matemáticas, sem tabelas codificadas. A estrutura usa `PriorityQueue` com lazy deletion e `HashMap` para verificação O(1) na fronteira."

> "Uma limitação: para puzzles maiores como o 15-puzzle, essa abordagem ficaria inviável sem heurísticas ainda mais precisas ou técnicas como IDA*. No 8-puzzle, o máximo de profundidade é 31 — nossos casos difíceis estão nessa faixa e são resolvidos em milissegundos."

---

## FECHAMENTO — 20 segundos

> "Referências: Russell & Norvig — *Artificial Intelligence: A Modern Approach*, capítulo 3, para A* e as heurísticas; Luger — *AI: Structures and Strategies*. Utilizamos Claude da Anthropic como assistente de pesquisa e para revisar trechos de código durante o desenvolvimento."

> "Obrigado, professora!"

---

## GUIA DE GRAVAÇÃO

| Parte | O que mostrar na tela | Tempo |
|-------|-----------------------|-------|
| Abertura | Terminal com saída do Main | 0:30 |
| Parte 1 | IDE — árvore de arquivos + cada classe aberta | 2:00 |
| Parte 2 | BestFirstSearch.java com linhas frontier destacadas | 2:00 |
| Parte 3 | Arquivos de heurística + terminal com números | 2:30 |
| Parte 4 | Tabela (terminal ou GUI "Rodar todos os 4") | 2:00 |
| Parte 5 + Fechamento | Tela neutra ou logo do projeto | 0:50 |
| **Total** | | **~9:50** |

**Dicas:**
- Fonte do IDE: ≥ 16px para ficar legível no vídeo
- Grave em 1080p
- A GUI pode aparecer na abertura como visual de impacto, mas o foco é o código
- Se quiser dividir: Luis Felipe faz Partes 1 e 2; João Pedro faz Partes 3 e 4
