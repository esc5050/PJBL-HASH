import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


class Registro {
    public final String codigo;
    public Registro(String codigo) { this.codigo = codigo; }
}

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

interface TabelaHash {
    boolean inserir(Registro r);
    boolean buscar(Registro r);
    long getColisoes();
    void resetarEstatisticas();
    int[] getTop3Listas();
    GapStats calcularGaps();
}

class HashEncadeamento implements TabelaHash {
    static class No {
        Registro dado;
        No prox;
        No(Registro r) { this.dado = r; }
    }
    private No[] tabela;
    private long colisoes = 0;
    public HashEncadeamento(int capacidade) { tabela = new No[capacidade]; }
    private int hash(String codigo) {
        long chave = Long.parseLong(codigo);
        long A = 2654435769L;
        long produto = chave * A;
        int indice = (int)((produto >>> 32) % tabela.length);
        return Math.abs(indice);
    }
    @Override
    public boolean inserir(Registro r) {
        int indice = hash(r.codigo);
        No bucket = tabela[indice];
        No temp = bucket;
        int tamanhoAtualLista = 0;
        while (temp != null) {
            tamanhoAtualLista++;
            temp = temp.prox;
        }
        colisoes += tamanhoAtualLista;
        No novoNo = new No(r);
        novoNo.prox = bucket;
        tabela[indice] = novoNo;
        return true;
    }
    @Override
    public boolean buscar(Registro r) {
        int indice = hash(r.codigo);
        No atual = tabela[indice];
        while (atual != null) {
            if (atual.dado.codigo.equals(r.codigo)) {
                return true;
            }
            atual = atual.prox;
        }
        return false;
    }
    @Override
    public int[] getTop3Listas() {
        PriorityQueue<Integer> topListas = new PriorityQueue<>();
        for (No head : tabela) {
            int tamanho = 0;
            No atual = head;
            while (atual != null) {
                tamanho++;
                atual = atual.prox;
            }
            topListas.add(tamanho);
            if (topListas.size() > 3) {
                topListas.poll();
            }
        }
        int[] resultado = new int[3];
        for(int i = 2; i >= 0; i--) {
            if (!topListas.isEmpty()) {
                resultado[i] = topListas.poll();
            }
        }
        int temp = resultado[0];
        resultado[0] = resultado[2];
        resultado[2] = temp;
        return resultado;
    }
    @Override public long getColisoes() { return colisoes; }
    @Override public void resetarEstatisticas() { colisoes = 0; }
    @Override public GapStats calcularGaps() { return UtilitariosHash.calcularGapsGenerico(tabela); }
}

class HashSondagemQuadratica implements TabelaHash {
    private String[] tabela;
    private long colisoes = 0;
    public HashSondagemQuadratica(int capacidade) { tabela = new String[capacidade]; }
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
            indiceAtual = (indiceInicial + tentativas * tentativas) % tabela.length;
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
    @Override public int[] getTop3Listas() { return new int[]{0,0,0}; }
    @Override public GapStats calcularGaps() { return UtilitariosHash.calcularGapsGenerico(tabela); }
}

class HashDuplo implements TabelaHash {
    private String[] tabela;
    private long colisoes = 0;
    public HashDuplo(int capacidade) { tabela = new String[capacidade]; }
    private int h1(String codigo) {
        long p1 = Long.parseLong(codigo.substring(0, 3));
        long p2 = Long.parseLong(codigo.substring(3, 6));
        long p3 = Long.parseLong(codigo.substring(6, 9));
        return (int)((p1 + p2 + p3) % tabela.length);
    }
    private int h2(String codigo) {
        long chave = Long.parseLong(codigo);
        int divisor = tabela.length - 1;
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
            indiceAtual = (indiceBase + i * passo) % tabela.length;
            if (i >= tabela.length) return false;
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
    @Override public int[] getTop3Listas() { return new int[]{0,0,0}; }
    @Override public GapStats calcularGaps() { return UtilitariosHash.calcularGapsGenerico(tabela); }
}

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

public class Projeto_Tabela_Hash {

    static final int[] TAMANHOS_TABELA = {1009, 10007, 100003};
    
    static final int[] TAMANHOS_DADOS = {100_000, 1_000_000, 10_000_000};
    static final long[] SEEDS = {123456L, 234567L, 345678L};

    public static void main(String[] args) throws IOException {
        System.out.println("  Análise de Tabela Hash RA3  ");

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Implementacao,TamanhoTabela,TamanhoDados,Seed,FatorCarga,TempoInsercao(ms),TempoBusca(ms),Colisoes,Top1,Top2,Top3,MenorGap,MaiorGap,MediaGap,Status");
        
        String[] NOMES = {"Encadeamento", "Sondagem Quadrática", "Hash Duplo"};

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

                    if (fatorCarga >= 1.0 && !(t instanceof HashEncadeamento)) {
                        System.out.println("-> AVISO para " + nome + ": Número de dados excede a capacidade. Testando com os primeiros " + tamTabela + " registros.");
                        dadosParaTeste = new Registro[tamTabela];
                        System.arraycopy(dadosOriginais, 0, dadosParaTeste, 0, tamTabela);
                        status = "CAPACIDADE_MAXIMA";
                    }
                    
                    System.out.println("-> Testando: " + nome);
                    System.gc();
                    t.resetarEstatisticas();

                    long inicio = System.nanoTime();
                    int inseridosComSucesso = 0;
                    for (Registro r : dadosParaTeste) {
                        if (t.inserir(r)) inseridosComSucesso++;
                    }
                    long tempoInsercao = (System.nanoTime() - inicio) / 1_000_000;

                    inicio = System.nanoTime();
                    int achados = 0;
                    for (Registro r : dadosParaTeste) {
                        if (t.buscar(r)) achados++;
                    }
                    long tempoBusca = (System.nanoTime() - inicio) / 1_000_000;

                    long col = t.getColisoes();
                    int[] top = t.getTop3Listas();
                    GapStats g = t.calcularGaps();
                    
                    System.out.printf(Locale.ROOT,"   > Inseridos: %d/%d | Colisões: %d | Tempo: %dms\n",
                        inseridosComSucesso, dadosParaTeste.length, col, tempoInsercao);
                    System.out.printf(Locale.ROOT,"   > Top 3 Listas: %d, %d, %d | Gaps (Min/Média/Max): %d / %.2f / %d\n",
                        top[0], top[1], top[2], g.menorGap, g.mediaGap, g.maiorGap);

                    String linhaCsv = String.format(Locale.ROOT, "%s,%d,%d,%d,%.2f,%d,%d,%d,%d,%d,%d,%d,%d,%.3f,%s",
                        nome, tamTabela, tamDados, seed, fatorCarga, tempoInsercao, tempoBusca, col,
                        top[0], top[1], top[2], g.menorGap, g.maiorGap, g.mediaGap, status);
                    csvLines.add(linhaCsv);
                }
            }
        }
        
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