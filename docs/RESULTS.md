# Resultados — Comparativo de Heurísticas e Desempenho

## Comparação das heurísticas no estado inicial

Para cada caso, os valores abaixo são as estimativas h(n) calculadas **no estado inicial**.
h* é a profundidade ótima real (custo do caminho mais curto).
Quanto mais próximo de h* sem ultrapassar, mais precisa a heurística.

| Caso     | h* (ótimo real) | Misplaced (h1) | Manhattan (h2) | Manhattan+LC (h3) | 3×Manhattan (não adm.) |
|----------|-----------------|----------------|----------------|-------------------|------------------------|
| EASY_1   | 5               | 5              | 5              | 5                 | 15                     |
| MEDIUM_1 | 10              | 7              | 10             | 10                | 30                     |
| MEDIUM_2 | 14 (sol. real)  | 8              | 14             | 14                | 42                     |
| HARD_1   | 31              | 7              | 21             | 23                | 63                     |
| HARD_2   | 31              | 7              | 21             | 23                | 63                     |

### Observações sobre as heurísticas

**Misplaced (h1):** faixa de valores [0, 8]. Conta apenas presença/ausência da peça
no lugar correto, sem considerar distância. Para HARD_1 estima 7 quando o custo real
é 31 — subestima em 77%. Isso explica os 121.519 nós visitados: a heurística é tão
fraca que o algoritmo age quase como Custo Uniforme.

**Manhattan (h2):** faixa de valores [0, ~24]. Considera a distância real de cada peça
ao destino. Para HARD_1 estima 21 de 31 — subestima apenas 32%. Nos casos EASY e
MEDIUM coincide com o ótimo ou chega muito perto.

**Manhattan+LC (h3):** mesma faixa de Manhattan, mas acrescenta 2 por cada par de peças
em conflito linear. Para HARD_1 estima 23 de 31 — 74% do custo real, a mais precisa
das admissíveis. Domina todas as outras: h3(n) ≥ h2(n) ≥ h1(n) ≥ 0 para todo n.

**3×Manhattan (não admissível):** faixa [0, ~72]. Supera o custo real em todos os casos
difíceis (63 > 31 para HARD_1). Por isso o A* encontra soluções subótimas — veja tabela
de desempenho abaixo.

**Hierarquia de dominância (admissíveis):**
```
ManhattanLC  ≥  Manhattan  ≥  Misplaced  ≥  Zero(UCS)
    23              21             7            0        ← estimativas no HARD_1
```

---

## Tabela de desempenho — 4 variações

| Variação                         | Caso     | Nós visitados (a) | Tamanho do caminho (b) | Tempo (c)  | Fronteira máx. (d) |
|----------------------------------|----------|-------------------|------------------------|------------|--------------------|
| 1. Custo Uniforme (UCS)          | EASY_1   | 48                | 5                      | 0.007 s    | 34                 |
| 1. Custo Uniforme (UCS)          | MEDIUM_1 | 543               | 10                     | 0.008 s    | 329                |
| 2. A\* não admissível (3×Manh.)  | EASY_1   | 6                 | 5 ✓                    | 0.005 s    | 7                  |
| 2. A\* não admissível (3×Manh.)  | MEDIUM_1 | 11                | 10 ✓                   | 0.006 s    | 11                 |
| 3. A\* admissível simples (Mispl)| EASY_1   | 6                 | 5                      | 0.005 s    | 7                  |
| 3. A\* admissível simples (Mispl)| MEDIUM_1 | 23                | 10                     | 0.006 s    | 19                 |
| 3. A\* admissível simples (Mispl)| HARD_1   | 121.519           | 31                     | 0.170 s    | 25.913             |
| 4. A\* melhor admissível (M+LC)  | EASY_1   | 6                 | 5                      | 0.006 s    | 7                  |
| 4. A\* melhor admissível (M+LC)  | MEDIUM_1 | 11                | 10                     | 0.005 s    | 11                 |
| 4. A\* melhor admissível (M+LC)  | MEDIUM_2 | 19                | 14                     | 0.005 s    | 17                 |
| 4. A\* melhor admissível (M+LC)  | HARD_1   | 3.794             | 31                     | 0.015 s    | 2.097              |
| 4. A\* melhor admissível (M+LC)  | HARD_2   | 3.797             | 31                     | 0.016 s    | 2.100              |

> ✓ Para a variação não admissível, o caminho coincidiu com o ótimo nos casos fácil e médio
> por sorte estrutural — nos casos difíceis (HARD) encontra caminhos subótimos (35 passos vs 31).

### Análise do desempenho

**UCS vs A\* (efeito da heurística):**
No HARD_1, UCS visita 181.439 nós vs 3.794 do Manhattan+LC — **47× menos nós**.
Sem heurística, o algoritmo explora o espaço de estados em todas as direções.

**Misplaced vs Manhattan+LC (efeito da precisão):**
Ambos admissíveis, mas Misplaced visita 121.519 nós no HARD_1 enquanto Manhattan+LC
visita 3.794 — **32× menos**. Admissibilidade sozinha não garante eficiência; a
precisão da estimativa é o que determina quantos nós serão explorados.

**Não admissível (3×Manhattan):**
É o mais rápido em número de nós nos casos médios (11 nós vs 23 do Misplaced),
mas paga um preço: nos casos difíceis encontra caminhos subótimos. Não é adequado
quando a otimalidade da solução é exigida.

**Fronteira máxima:**
Diretamente proporcional ao número de nós visitados. UCS no HARD_1 mantém 25.140
nós na fila ao mesmo tempo; Manhattan+LC mantém no máximo 2.097 — 12× menor uso de memória.

---

## Limitações

Todos os objetivos do trabalho foram alcançados. Limitações observadas:

- Para puzzles de ordem superior (15-puzzle, 24-puzzle), esta implementação seria
  inviável sem heurísticas ainda mais precisas ou técnicas como IDA* (A* com
  aprofundamento iterativo), que usa O(d) de memória em vez de O(b^d).
- O 8-puzzle tem profundidade máxima de 31 movimentos e 181.440 estados alcançáveis.
  Todos os casos testados foram resolvidos em milissegundos mesmo com UCS.
