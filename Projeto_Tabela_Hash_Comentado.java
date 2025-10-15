import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
 * Projeto: Implementação e Análise de Tabelas Hash (Trabalho RA3)
 * Autor: [Seu Nome]
 * Descrição: Este código implementa e compara três estratégias de Tabela Hash em Java,
 * conforme as especificações do trabalho acadêmico RA3. O objetivo é analisar o desempenho
 * de cada abordagem em termos de tempo, colisões e distribuição de dados.
 */

/**
 * Classe simples (POJO) para encapsular cada registro de dado.
 * O 'codigo' é a chave que será usada para hashing.
 */
class Registro {
    public final String codigo;
    public Registro(String codigo) { this.codigo = codigo; }
}

/**
 * POJO para agrupar as estatísticas de gaps (espaços vazios) entre os elementos na tabela.
 * Facilita o retorno de múltiplos valores do método de cálculo.
 */
class GapStats {
    public final int menorGap;
    public final int maiorGap;
    public final double mediaGap;
    public GapStats(int menorGap, int maiorGap, double mediaGap) {
        this.menorGap = menorGap;
        this.maiorGap = maiorGap;
        this.mediaGap = mediaGap;
    }
}

/**
 * Interface que define o contrato base para todas as implementações de Tabela Hash.
 * Utilizar uma interface é uma boa prática de OO, pois permite que o nosso testador principal
 * trate todas as implementações de forma polimórfica, facilitando os testes.
 */
interface TabelaHash {
    boolean inserir(Registro r);
    boolean buscar(Registro r);
    long getColisoes();
    void resetarEstatisticas();
    int[] getTop3Listas();
    GapStats calcularGaps();
}

// =======================================================================================
// IMPLEMENTAÇÃO 1: ENCADEAMENTO SEPARADO (SEPARATE CHAINING)
// =======================================================================================
/**
 * Implementação de Tabela Hash usando Encadeamento Separado.
 * Cada índice da tabela é a cabeça de uma lista ligada. Colisões são resolvidas
 * adicionando o novo elemento a essa lista.
 */
class HashEncadeamento implements TabelaHash {
    // Nó padrão de uma lista ligada simples.
    static class No {
        Registro dado;
        No prox;
        No(Registro r) { this.dado = r; }
    }

    private No[] tabela;
    private long colisoes = 0;

    public HashEncadeamento(int capacidade) {
        tabela = new No[capacidade];
    }

    /**
     * Função Hash: Método da Multiplicação.
     * Escolhido por ser eficiente e oferecer uma ótima distribuição das chaves,
     * o que é crucial para manter as listas ligadas pequenas.
     * A constante 'A' é baseada na proporção áurea, recomendada por Knuth.
     * O '>>> 32' é um truque para usar os 32 bits mais significativos do produto,
     * que tendem a ser mais "aleatórios" e melhores para o hash.
     */
    private int hash(String codigo) {
        long chave = Long.parseLong(codigo);
        long A = 2654435769L;
        long produto = chave * A;
        int indice = (int)((produto >>> 32) % tabela.length);
        return Math.abs(indice); // Garante que não teremos índices negativos.
    }

    @Override
    public boolean inserir(Registro r) {
        int indice = hash(r.codigo);
        No bucket = tabela[indice];

        // Lógica de contagem de colisões conforme a especificação do trabalho:
        // cada nó já existente na lista conta como uma colisão para a nova inserção.
        No temp = bucket;
        int tamanhoAtualLista = 0;
        while (temp != null) {
            tamanhoAtualLista++;
            temp = temp.prox;
        }
        colisoes += tamanhoAtualLista;

        // A inserção é sempre no início da lista (cabeça). É uma operação O(1), muito rápida.
        No novoNo = new No(r);
        novoNo.prox = bucket;
        tabela[indice] = novoNo;
        return true;
    }

    @Override
    public boolean buscar(Registro r) {
        int indice = hash(r.codigo);
        No atual = tabela[indice];
        // Percorre a lista ligada daquele índice para encontrar o registro.
        while (atual != null) {
            if (atual.dado.codigo.equals(r.codigo)) {
                return true;
            }
            atual = atual.prox;
        }
        return false;
    }

    /**
     * Usa uma Fila de Prioridade (Min Heap) para encontrar os 3 maiores tamanhos de lista
     * de forma eficiente, sem precisar ordenar um array com todos os tamanhos.
     */
    @Override
    public int[] getTop3Listas() {
        PriorityQueue<Integer> topListas = new PriorityQueue<>(); // Min-heap por padrão
        for (No head : tabela) {
            int tamanho = 0;
            No atual = head;
            while (atual != null) {
                tamanho++;
                atual = atual.prox;
            }
            topListas.add(tamanho);
            // Se a fila tiver mais de 3 elementos, remove o menor.
            if (topListas.size() > 3) {
                topListas.poll();
            }
        }
        // Extrai e ordena os 3 resultados finais.
        int[] resultado = new int[3];
        for(int i = 2; i >= 0; i--) {
            if (!topListas.isEmpty()) {
                resultado[i] = topListas.poll();
            }
        }
        // Inverte para garantir a ordem decrescente (maior primeiro).
        int temp = resultado[0];
        resultado[0] = resultado[2];
        resultado[2] = temp;
        return resultado;
    }
    
    // Métodos de suporte da interface.
    @Override public long getColisoes() { return colisoes; }
    @Override public void resetarEstatisticas() { colisoes = 0; }
    @Override public GapStats calcularGaps() { return UtilitariosHash.calcularGapsGenerico(tabela); }
}

// =======================================================================================
// IMPLEMENTAÇÃO 2: SONDAGEM QUADRÁTICA (QUADRATIC PROBING)
// =======================================================================================
/**
 * Implementação usando Sondagem Quadrática (endereçamento aberto).
 * Escolhida como uma melhoria sobre a Sondagem Linear, pois evita o problema
 * de "agrupamento primário", resultando em melhor desempenho quando a tabela enche.
 */
class HashSondagemQuadratica implements TabelaHash {
    private String[] tabela;
    private long colisoes = 0;

    public HashSondagemQuadratica(int capacidade) {
        tabela = new String[capacidade];
    }

    /**
     * Função Hash: Método da Dobra (Folding).
     * Como a função 'resto da divisão' era proibida pela especificação, a dobra foi
     * uma alternativa. Ela soma diferentes partes da chave, fazendo com que todos os
     * dígitos contribuam para o resultado final, melhorando a distribuição.
     */
    private int hash(String codigo) {
        long p1 = Long.parseLong(codigo.substring(0, 3));
        long p2 = Long.parseLong(codigo.substring(3, 6));
        long p3 = Long.parseLong(codigo.substring(6, 9));
        return (int)((p1 + p2 + p3) % tabela.length);
    }
    
    @Override
    public boolean inserir(Registro r) {
        int indiceInicial = hash(r.codigo);
        int indiceAtual = indiceInicial;
        int tentativas = 0;
        
        while (tabela[indiceAtual] != null) {
            colisoes++;
            tentativas++;
            // A sondagem é quadrática: a próxima posição é (h(k) + i^2) % M.
            // Isso gera "pulos" cada vez maiores para fugir de zonas de colisão.
            indiceAtual = (indiceInicial + tentativas * tentativas) % tabela.length;
            
            // Salvaguarda para evitar loop infinito se a tabela encher ou se a sondagem
            // não conseguir encontrar um slot livre (uma limitação da quadrática).
            if (tentativas >= tabela.length) return false;
        }
        tabela[indiceAtual] = r.codigo;
        return true;
    }

    @Override
    public boolean buscar(Registro r) {
        int indiceInicial = hash(r.codigo);
        int indiceAtual = indiceInicial;
        int tentativas = 0;
        
        // A busca deve seguir exatamente a mesma sequência de saltos da inserção.
        while (tabela[indiceAtual] != null && tentativas < tabela.length) {
            if (tabela[indiceAtual].equals(r.codigo)) {
                return true;
            }
            tentativas++;
            indiceAtual = (indiceInicial + tentativas * tentativas) % tabela.length;
        }
        return false;
    }
    
    @Override public long getColisoes() { return colisoes; }
    @Override public void resetarEstatisticas() { colisoes = 0; }
    @Override public int[] getTop3Listas() { return new int[]{0,0,0}; } // Não se aplica a esta técnica.
    @Override public GapStats calcularGaps() { return UtilitariosHash.calcularGapsGenerico(tabela); }
}

// =======================================================================================
// IMPLEMENTAÇÃO 3: HASH DUPLO (DOUBLE HASHING)
// =======================================================================================
/**
 * Implementação usando Hash Duplo.
 * É uma das técnicas de endereçamento aberto mais eficientes, pois usa uma segunda
 * função hash para determinar o tamanho do passo, o que minimiza tanto o agrupamento
 * primário quanto o secundário.
 */
class HashDuplo implements TabelaHash {
    private String[] tabela;
    private long colisoes = 0;

    public HashDuplo(int capacidade) {
        tabela = new String[capacidade];
    }
    
    // h1 determina a posição inicial, usando o mesmo método da dobra.
    private int h1(String codigo) {
        long p1 = Long.parseLong(codigo.substring(0, 3));
        long p2 = Long.parseLong(codigo.substring(3, 6));
        long p3 = Long.parseLong(codigo.substring(6, 9));
        return (int)((p1 + p2 + p3) % tabela.length);
    }
    
    /**
     * h2 determina o tamanho do passo. É CRUCIAL que:
     * 1. O resultado nunca seja 0 (senão teríamos um loop infinito).
     * 2. O passo e o tamanho da tabela sejam relativamente primos para garantir que todos os slots sejam visitados.
     * A fórmula 1 + (chave % (M-1)) é um padrão para garantir o passo > 0.
     */
    private int h2(String codigo) {
        long chave = Long.parseLong(codigo);
        int divisor = tabela.length - 1;
        // Tenta encontrar um divisor primo para o passo, melhorando a cobertura.
        while (divisor > 1 && (tabela.length % divisor == 0)) {
            divisor--;
        }
        return 1 + (int)(chave % divisor);
    }
    
    @Override
    public boolean inserir(Registro r) {
        int indiceBase = h1(r.codigo);
        int passo = h2(r.codigo);
        int indiceAtual = indiceBase;
        int i = 0;
        
        while (tabela[indiceAtual] != null) {
            colisoes++;
            i++;
            // A fórmula do rehash: h(k, i) = (h1(k) + i * h2(k)) % M.
            // O passo é constante, mas diferente para cada chave.
            indiceAtual = (indiceBase + i * passo) % tabela.length;
            
            if (i >= tabela.length) return false; // Deu a volta na tabela, está cheia.
        }
        tabela[indiceAtual] = r.codigo;
        return true;
    }
    
    @Override
    public boolean buscar(Registro r) {
        int indiceBase = h1(r.codigo);
        int passo = h2(r.codigo);
        int indiceAtual = indiceBase;
        int i = 0;
        
        while (tabela[indiceAtual] != null && i < tabela.length) {
            if (tabela[indiceAtual].equals(r.codigo)) {
                return true;
            }
            i++;
            indiceAtual = (indiceBase + i * passo) % tabela.length;
        }
        return false;
    }
    
    @Override public long getColisoes() { return colisoes; }
    @Override public void resetarEstatisticas() { colisoes = 0; }
    @Override public int[] getTop3Listas() { return new int[]{0,0,0}; } // Não se aplica.
    @Override public GapStats calcularGaps() { return UtilitariosHash.calcularGapsGenerico(tabela); }
}

/**
 * Classe de utilitários para centralizar lógicas repetidas.
 * No caso, o cálculo de gaps é idêntico para todas as implementações
 * baseadas em array, então o isolamos aqui para evitar duplicação de código.
 */
class UtilitariosHash {
    public static GapStats calcularGapsGenerico(Object[] tabela) {
        int ultimoOcupado = -1;
        List<Integer> gaps = new ArrayList<>();
        for (int i = 0; i < tabela.length; i++) {
            if (tabela[i] != null) {
                if (ultimoOcupado != -1) {
                    gaps.add(i - ultimoOcupado - 1);
                }
                ultimoOcupado = i;
            }
        }
        if (gaps.isEmpty()) return new GapStats(0, 0, 0.0);
        
        int min = Collections.min(gaps);
        int max = Collections.max(gaps);
        double media = gaps.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        return new GapStats(min, max, media);
    }
}

/**
 * Classe responsável por gerar os conjuntos de dados para os testes.
 * O uso de uma 'seed' fixa é CRÍTICO para garantir que todos os algoritmos
 * sejam testados com exatamente os mesmos dados, tornando a comparação justa.
 */
class GeradorDeDados {
    public static Registro[] gerar(int n, long seed) {
        Registro[] registros = new Registro[n];
        Random rand = new Random(seed);
        for (int i = 0; i < n; i++) {
            int numero = rand.nextInt(1_000_000_000);
            registros[i] = new Registro(String.format("%09d", numero));
        }
        return registros;
    }
}


// =======================================================================================
// CLASSE PRINCIPAL: O ORQUESTRADOR DOS TESTES
// =======================================================================================
public class Projeto_Tabela_Hash {

    // Tamanhos de tabela alterados para números primos para otimizar o desempenho
    // das técnicas de rehashing. Isso é uma prática recomendada.
    static final int[] TAMANHOS_TABELA = {1009, 10007, 100003};
    
    static final int[] TAMANHOS_DADOS = {100_000, 1_000_000, 10_000_000};
    static final long[] SEEDS = {123456L, 234567L, 345678L};

    public static void main(String[] args) throws IOException {
        System.out.println("=============================");
        System.out.println("  Análise de Tabela Hash RA3  ");
        System.out.println("=============================");

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Implementacao,TamanhoTabela,TamanhoDados,Seed,FatorCarga,TempoInsercao(ms),TempoBusca(ms),Colisoes,Top1,Top2,Top3,MenorGap,MaiorGap,MediaGap,Status");
        
        String[] NOMES = {"Encadeamento", "Sondagem Quadrática", "Hash Duplo"};

        // Loop principal que itera sobre todas as combinações de parâmetros de teste.
        for (int tamTabela : TAMANHOS_TABELA) {
            for (int i = 0; i < TAMANHOS_DADOS.length; i++) {
                int tamDados = TAMANHOS_DADOS[i];
                long seed = SEEDS[i];

                double fatorCarga = (double) tamDados / tamTabela;
                System.out.printf("\nGerando %d registros (seed=%d) para tabela de %d (Fator de Carga: %.2f)\n", tamDados, seed, tamTabela, fatorCarga);
                Registro[] dadosOriginais = GeradorDeDados.gerar(tamDados, seed);

                TabelaHash[] tabelas = {
                    new HashEncadeamento(tamTabela),
                    new HashSondagemQuadratica(tamTabela),
                    new HashDuplo(tamTabela)
                };

                for (int j = 0; j < tabelas.length; j++) {
                    TabelaHash t = tabelas[j];
                    String nome = NOMES[j];
                    
                    Registro[] dadosParaTeste = dadosOriginais;
                    String status = "OK";

                    // Lógica crítica: Endereçamento aberto não suporta fator de carga >= 1.
                    // Em vez de pular o teste, ajustamos a carga de dados para 100% da capacidade
                    // da tabela, permitindo medir o comportamento em condição de saturação.
                    if (fatorCarga >= 1.0 && !(t instanceof HashEncadeamento)) {
                        System.out.println("-> AVISO para " + nome + ": Número de dados excede a capacidade. Testando com os primeiros " + tamTabela + " registros.");
                        dadosParaTeste = new Registro[tamTabela];
                        System.arraycopy(dadosOriginais, 0, dadosParaTeste, 0, tamTabela);
                        status = "CAPACIDADE_MAXIMA";
                    }
                    
                    System.out.println("-> Testando: " + nome);
                    System.gc(); // Sugestão ao Garbage Collector para rodar. Ajuda a obter tempos mais consistentes.
                    t.resetarEstatisticas();

                    // Medição do tempo de Inserção. Usamos nanoTime para maior precisão.
                    long inicio = System.nanoTime();
                    int inseridosComSucesso = 0;
                    for (Registro r : dadosParaTeste) {
                        if (t.inserir(r)) inseridosComSucesso++;
                    }
                    long tempoInsercao = (System.nanoTime() - inicio) / 1_000_000;

                    // Medição do tempo de Busca.
                    inicio = System.nanoTime();
                    int achados = 0;
                    for (Registro r : dadosParaTeste) {
                        if (t.buscar(r)) achados++;
                    }
                    long tempoBusca = (System.nanoTime() - inicio) / 1_000_000;

                    // Coleta de todas as métricas para o relatório.
                    long col = t.getColisoes();
                    int[] top = t.getTop3Listas();
                    GapStats g = t.calcularGaps();
                    
                    // Imprime um resumo no console para acompanhamento em tempo real.
                    System.out.printf(Locale.ROOT,"   > Inseridos: %d/%d | Colisões: %d | Tempo: %dms\n",
                        inseridosComSucesso, dadosParaTeste.length, col, tempoInsercao);
                    System.out.printf(Locale.ROOT,"   > Top 3 Listas: %d, %d, %d | Gaps (Min/Média/Max): %d / %.2f / %d\n",
                        top[0], top[1], top[2], g.menorGap, g.mediaGap, g.maiorGap);

                    // Formata a linha para o arquivo CSV.
                    String linhaCsv = String.format(Locale.ROOT, "%s,%d,%d,%d,%.2f,%d,%d,%d,%d,%d,%d,%d,%d,%.3f,%s",
                        nome, tamTabela, tamDados, seed, fatorCarga, tempoInsercao, tempoBusca, col,
                        top[0], top[1], top[2], g.menorGap, g.maiorGap, g.mediaGap, status);
                    csvLines.add(linhaCsv);
                }
            }
        }
        
        // Escreve a lista de resultados no arquivo CSV.
        // O bloco try-with-resources garante que o arquivo será fechado corretamente.
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("resultados_hash_RA3.csv"))) {
            for (String line : csvLines) {
                bw.write(line);
                bw.newLine();
            }
        }
        System.out.println("\n\nResultados salvos com sucesso no arquivo 'resultados_hash_RA3.csv'");
        System.out.println("Agora é só abrir no Excel ou Google Sheets e gerar os gráficos para o seu relatório!");
    }
}