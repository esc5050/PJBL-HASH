PJBL - HASH

 Objetivo

Este projeto tem como finalidade implementar e analisar o desempenho de diferentes tabelas hash em Java, comparando estratégias de encadeamento e rehashing (sondagem quadrática e hash duplo).

O trabalho mede tempo de inserção, tempo de busca, número de colisões, tamanho das listas encadeadas e gaps (espaços vazios), conforme especificado no enunciado.


 Implementação

O código foi totalmente desenvolvido em Java, utilizando apenas vetores, tipos primitivos, classes simples e controle manual de colisões.
Nenhuma biblioteca de estrutura de dados pronta foi usada (`HashMap`, `ArrayList`, `Hashtable`, etc.).

A execução gera automaticamente um arquivo chamado `resultados_hash_RA3.csv`, com todos os resultados medidos, que podem ser abertos no Excel ou Google Sheets para geração dos gráficos e tabelas solicitados no relatório.


 Estruturas Implementadas

O código define a interface `TabelaHash` e implementa três variações distintas:

 1.  `HashEncadeamento`

* Implementa encadeamento separado, onde cada posição da tabela armazena uma lista ligada (`No`).
* Em caso de colisão, o novo elemento é inserido no início da lista.
* Utiliza uma função hash baseada na multiplicação de Knuth com uma constante inteira (`A = 2654435769`).
* Permite alto fator de carga e é mais tolerante a colisões.

 2.  `HashSondagemQuadratica`

* Implementa rehashing com sondagem quadrática.
* Em caso de colisão, procura o próximo índice usando a função `(i²)`.
* É eficiente para fatores de carga moderados, mas sofre degradação conforme a tabela enche.
* Mede todas as colisões geradas durante as tentativas.

 3. `HashDuplo`

* Implementa rehashing com hash duplo.
* Usa duas funções hash (`h1` e `h2`) para calcular saltos independentes, minimizando agrupamentos (clustering).
* É eficiente para fatores de carga abaixo de 0.8.
* Oferece melhor dispersão dos elementos entre as posições da tabela.



  Funções Hash Utilizadas

| Tipo                | Estratégia                 | Fórmula / Descrição                                      |
| ------------------- | -------------------------- | -------------------------------------------------------- |
| Encadeamento        | Multiplicação (Knuth)      | `(chave * A >>> 32) % tamanho`                           |
| Sondagem Quadrática | Soma Modular               | `(p1 + p2 + p3) % tamanho`                               |
| Hash Duplo          | Duas funções independentes | `h1 = (p1 + p2 + p3) % n`  e  `h2 = 1 + (chave % primo)` |

As três estratégias garantem diversidade de comportamento e distribuição, permitindo uma análise comparativa detalhada.



 Tamanhos das Tabelas

Foram definidos três tamanhos diferentes, com aumento de aproximadamente 10x entre eles:

| Tabela  | Tamanho | Tipo                     |
| ------- | ------- | ------------------------ |
| Pequena | 1.009   | Primo próximo de 1.000   |
| Média   | 10.007  | Primo próximo de 10.000  |
| Grande  | 100.003 | Primo próximo de 100.000 |



 Conjuntos de Dados

Foram gerados três conjuntos de registros aleatórios utilizando seeds fixas para garantir igualdade entre testes:

| Conjunto | Quantidade de Registros | Seed   | Observação |
| -------- | ----------------------- | ------ | ---------- |
| 1        | 100.000                 | 123456 | Pequeno    |
| 2        | 1.000.000               | 234567 | Médio      |
| 3        | 10.000.000              | 345678 | Grande     |

Cada registro é um objeto da classe `Registro`, contendo um código numérico de 9 dígitos (ex: `000001240`, `123456789`).

Os dados são gerados pela classe `GeradorDeDados`, utilizando `Random(seed)` para reprodutibilidade.


 Métricas Avaliadas

Durante a execução, o código mede e salva no CSV as seguintes informações:

| Métrica              | Descrição                                                           |
| -------------------- | ------------------------------------------------------------------- |
| ⏱️ Tempo de inserção | Tempo total para inserir todos os registros                         |
| 🔍 Tempo de busca    | Tempo total para buscar todos os registros inseridos                |
| ⚠️ Colisões          | Número total de colisões ocorridas durante as inserções             |
| 🧾 Top 3 listas      | Tamanho das três maiores listas encadeadas (apenas no encadeamento) |
| 🧮 Gaps              | Menor, maior e média de distância entre elementos ocupados          |
| 📊 Fator de carga    | Razão entre número de registros e tamanho da tabela                 |



 Resultados Gerados

Ao final da execução, é criado o arquivo `resultados_hash_RA3.csv`, contendo colunas como:


Implementacao,TamanhoTabela,TamanhoDados,Seed,FatorCarga,TempoInsercao(ms),
TempoBusca(ms),Colisoes,Top1,Top2,Top3,MenorGap,MaiorGap,MediaGap,Status


Esses dados podem ser usados para gerar gráficos de comparação de desempenho.


 Análise dos Resultados

Com base nos testes e nos resultados obtidos:

* Encadeamento apresentou maior tolerância a altas cargas e menor impacto no desempenho em cenários de colisão, porém consome mais memória.
* Sondagem Quadrática teve bom desempenho inicial, mas degrada quando o fator de carga se aproxima de 1.
* Hash Duplo apresentou o melhor equilíbrio entre tempo e dispersão, com menor número de colisões e melhor tempo médio de busca.

O desempenho geral confirma que o hash duplo é a opção mais eficiente em termos de custo temporal e distribuição.



 Execução do Programa

 Compilar:


javac Projeto_Tabela_Hash.java


### Executar:

java Projeto_Tabela_Hash

O resultado será salvo automaticamente em:


resultados_hash_RA3.csv


Abra o arquivo no Excel ou Google Sheets para gerar os gráficos de comparação.



 Conclusão

O estudo mostra que a escolha da função hash e da estratégia de colisão influencia diretamente o desempenho de inserções e buscas.

* O encadeamento é mais robusto para tabelas cheias.
* O rehashing quadrático é eficiente em cargas médias.
* O rehashing duplo é o mais equilibrado e apresentou o melhor desempenho global.

Esses resultados corroboram a teoria sobre dispersão uniforme e eficiência de rehashing duplo em tabelas grandes.



 Autores

Trabalho desenvolvido por Antonio Bernardo Zilio Tomasi,Gustavo Lona Grespan e Julia Machado Kociolek
Disciplina: Resolução de Problemas Estruturados em Computação  — PUCPR
Ano: 2025
Professor: Andrey Cabral Meira
