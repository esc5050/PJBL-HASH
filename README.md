# PJBL-HASH
 Análise de Desempenho de Tabelas Hash em Java

 Objetivo

Este projeto tem como objetivo implementar e analisar o desempenho de diferentes tabelas hash em Java, utilizando tanto funções com rehashing quanto funções com encadeamento.

O trabalho avalia o tempo de inserção, tempo de busca, número de colisões, além de métricas complementares como o tamanho das listas encadeadas e o gap médio entre elementos.


 Implementação

O código foi implementado em Java, respeitando as restrições do trabalho:

* Não foram utilizadas funções prontas de estruturas de dados (`HashMap`, `Hashtable`, etc.).
* Apenas vetores, estruturas de nó, tipos primitivos e String foram usados.
* O tempo de execução foi medido em milissegundos, com opção de ajuste para microssegundos ou nanossegundos em testes de maior precisão.
* Os dados foram gerados com seeds fixas, garantindo a reprodutibilidade dos testes entre as funções hash.

A classe principal realiza a criação das tabelas, a inserção e a busca dos elementos, medindo o desempenho de cada função hash.



 Estruturas Implementadas

Foram implementadas três variações de tabela hash, uma com encadeamento e duas com rehashing:

1. Encadeamento Separado (Separate Chaining)
   Cada posição do vetor contém uma lista encadeada de nós. Em caso de colisão, o novo elemento é inserido ao final da lista.

2.  Rehashing Linear (Linear Probing)
   Em caso de colisão, procura-se a próxima posição livre linearmente até encontrar um espaço vazio.

3.  Rehashing Duplo (Double Hashing)
   Utiliza uma segunda função hash para determinar o salto (step size) em caso de colisão, reduzindo agrupamentos (clustering).



 Funções Hash Escolhidas

As funções hash foram definidas da seguinte forma:

* Hash 1 (Resto da Divisão):
  `hash = chave % tamanhoTabela`
  Simples e eficiente para tamanhos de tabela primos.

* Hash 2 (Multiplicação):
  `hash = (int) (tamanhoTabela * ((chave * A) % 1))`
  Onde `A` é uma constante fracionária (ex: 0.6180339887).
  Reduz colisões distribuindo melhor os valores.

* Hash 3 (Hash Duplo):
  `hash = (chave1 + i * (1 + chave2 % (tamanhoTabela - 1))) % tamanhoTabela`
  Garante melhor dispersão e evita agrupamentos consecutivos.


 Tamanhos das Tabelas

Três tamanhos de tabela foram utilizados, com variação de 10x entre eles:

* Tabela 1: 1.000 posições
* Tabela 2: 10.000 posições
* Tabela 3: 100.000 posições



 Conjuntos de Dados

Foram gerados três conjuntos de registros aleatórios (classe `Registro`), com seeds fixas para garantir a igualdade entre testes:

* Conjunto 1: 100.000 registros
* Conjunto 2: 1.000.000 registros
* Conjunto 3: 10.000.000 registros

Cada registro possui um código de 9 dígitos, por exemplo:


000001240
123456789
100000365



 Métricas Avaliadas

Durante os testes, foram coletadas as seguintes métricas:

* Tempo de inserção total
* Tempo de busca total
* Número total de colisões
* As três maiores listas encadeadas geradas
* Gap mínimo, máximo e médio entre elementos do vetor



 Resultados e Gráficos

Foram elaboradas tabelas e gráficos comparando o desempenho entre as funções hash e tamanhos de tabela.

 Principais Observações

* O encadeamento separado apresentou menos colisões, porém maior uso de memória.
* O rehashing linear foi rápido em inserções iniciais, mas apresentou degradação conforme a carga aumentou.
* O rehashing duplo teve o melhor equilíbrio entre tempo e dispersão, com poucas colisões e boa velocidade média.

Gráficos comparativos demonstram a diferença no tempo médio de inserção e busca** entre os três métodos, além da variação do número de colisões.



 Conclusão

Com base nos testes realizados:

* O rehashing duplo apresentou o melhor desempenho geral, equilibrando eficiência e baixa taxa de colisões.
* O encadeamento separado mostrou-se robusto para altas cargas, mas com maior consumo de memória.
* O rehashing linear teve bom desempenho apenas em tabelas menores.

Os resultados confirmam que a escolha da função hash e da estratégia de resolução de colisões influencia diretamente o desempenho** de uma tabela hash, tanto em termos de tempo quanto de uso de memória.


Autoria

Trabalho desenvolvido por  Antonio Bernardo Zilio Tomasi,Gustavo Lona Grespan e Julia Machado Kociolek
Disciplina: Resolução de Problemas Estruturados em Computação/ Ciência da Computação – PUCPR
Ano: 2025

