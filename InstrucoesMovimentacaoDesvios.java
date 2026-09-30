import java.util.Objects;

/**
 * Parte da Pessoa 4: movimentação de dados e controle de fluxo do SIC/XE.
 *
 * Esta classe não implementa CPU, memória, decodificação nem interface gráfica.
 * Ela recebe esses componentes por meio de contratos mínimos para que o grupo
 * possa conectá-la ao projeto final posteriormente.
 */
public final class InstrucoesMovimentacaoDesvios {
    private static final int MASCARA_24_BITS = 0xFF_FFFF;

    private final Cpu cpu;
    private final Memoria memoria;

    public InstrucoesMovimentacaoDesvios(Cpu cpu, Memoria memoria) {
        this.cpu = Objects.requireNonNull(cpu, "cpu");
        this.memoria = Objects.requireNonNull(memoria, "memoria");
    }

    /** Executa apenas as instruções que pertencem à Pessoa 4. */
    public void executar(Instrucao instrucao) {
        Objects.requireNonNull(instrucao, "instrucao");

        switch (instrucao.mnemonico()) {
            // Carga da memória para os registradores
            case LDA -> carregarPalavra(Registrador.A, instrucao);
            case LDB -> carregarPalavra(Registrador.B, instrucao);
            case LDL -> carregarPalavra(Registrador.L, instrucao);
            case LDS -> carregarPalavra(Registrador.S, instrucao);
            case LDT -> carregarPalavra(Registrador.T, instrucao);
            case LDX -> carregarPalavra(Registrador.X, instrucao);
            case LDCH -> carregarByteEmA(instrucao);

            // Armazenamento dos registradores na memória
            case STA -> armazenarPalavra(Registrador.A, instrucao);
            case STB -> armazenarPalavra(Registrador.B, instrucao);
            case STL -> armazenarPalavra(Registrador.L, instrucao);
            case STS -> armazenarPalavra(Registrador.S, instrucao);
            case STT -> armazenarPalavra(Registrador.T, instrucao);
            case STX -> armazenarPalavra(Registrador.X, instrucao);
            case STCH -> armazenarByteDeA(instrucao);

            // Movimentação entre registradores
            case CLEAR -> cpu.gravar(instrucao.exigirR1(), 0);
            case RMO -> cpu.gravar(
                    instrucao.exigirR2(),
                    cpu.ler(instrucao.exigirR1()));

            // Controle de fluxo
            case J -> saltar(instrucao);
            case JEQ -> saltarSe(instrucao, CodigoCondicional.IGUAL);
            case JGT -> saltarSe(instrucao, CodigoCondicional.MAIOR);
            case JLT -> saltarSe(instrucao, CodigoCondicional.MENOR);
            case JSUB -> chamarSubrotina(instrucao);
            case RSUB -> retornarDaSubrotina();
        }
    }

    private void carregarPalavra(Registrador destino, Instrucao instrucao) {
        int valor = instrucao.imediata()
                ? instrucao.valorOuEndereco()
                : memoria.lerPalavra24(instrucao.valorOuEndereco());

        cpu.gravar(destino, valor & MASCARA_24_BITS);
    }

    private void carregarByteEmA(Instrucao instrucao) {
        int byteLido = instrucao.imediata()
                ? instrucao.valorOuEndereco() & 0xFF
                : memoria.lerByte(instrucao.valorOuEndereco());

        // LDCH altera somente os 8 bits da direita do acumulador A.
        int acumulador = cpu.ler(Registrador.A);
        int resultado = (acumulador & 0xFF_FF00) | (byteLido & 0xFF);
        cpu.gravar(Registrador.A, resultado);
    }

    private void armazenarPalavra(Registrador origem, Instrucao instrucao) {
        exigirEnderecoDeMemoria(instrucao);
        memoria.gravarPalavra24(
                instrucao.valorOuEndereco(),
                cpu.ler(origem) & MASCARA_24_BITS);
    }

    private void armazenarByteDeA(Instrucao instrucao) {
        exigirEnderecoDeMemoria(instrucao);
        memoria.gravarByte(
                instrucao.valorOuEndereco(),
                cpu.ler(Registrador.A) & 0xFF);
    }

    private void saltar(Instrucao instrucao) {
        cpu.gravar(Registrador.PC, instrucao.valorOuEndereco());
    }

    private void saltarSe(
            Instrucao instrucao,
            CodigoCondicional condicaoNecessaria) {
        if (cpu.codigoCondicional() == condicaoNecessaria) {
            saltar(instrucao);
        }
    }

    private void chamarSubrotina(Instrucao instrucao) {
        // O ciclo de busca deve ter avançado PC antes de chamar este método.
        int enderecoDeRetorno = cpu.ler(Registrador.PC);
        cpu.gravar(Registrador.L, enderecoDeRetorno);
        saltar(instrucao);
    }

    private void retornarDaSubrotina() {
        cpu.gravar(Registrador.PC, cpu.ler(Registrador.L));
    }

    private static void exigirEnderecoDeMemoria(Instrucao instrucao) {
        if (instrucao.imediata()) {
            throw new IllegalArgumentException(
                    instrucao.mnemonico()
                            + " não pode armazenar usando modo imediato");
        }
    }

    /**
     * Contrato esperado da CPU feita pelos outros integrantes.
     * Na integração, esta interface pode ser substituída pela classe real.
     */
    public interface Cpu {
        int ler(Registrador registrador);

        void gravar(Registrador registrador, int valor);

        CodigoCondicional codigoCondicional();
    }

    /**
     * Contrato esperado da memória feita pelos outros integrantes.
     * Palavras possuem 24 bits e ocupam três bytes consecutivos.
     */
    public interface Memoria {
        int lerByte(int endereco);

        void gravarByte(int endereco, int valor);

        int lerPalavra24(int endereco);

        void gravarPalavra24(int endereco, int valor);
    }

    public enum Registrador {
        A, X, L, B, S, T, PC, SW
    }

    public enum CodigoCondicional {
        MENOR, IGUAL, MAIOR
    }

    /** As 22 instruções atribuídas à Pessoa 4. */
    public enum Mnemonico {
        LDA, LDB, LDCH, LDL, LDS, LDT, LDX,
        STA, STB, STCH, STL, STS, STT, STX,
        CLEAR, RMO,
        J, JEQ, JGT, JLT, JSUB, RSUB
    }

    /**
     * Resultado mínimo esperado do futuro decodificador.
     *
     * valorOuEndereco contém o literal no modo imediato ou o endereço final
     * já calculado nos demais modos. r1 e r2 são usados por CLEAR e RMO.
     */
    public record Instrucao(
            Mnemonico mnemonico,
            int valorOuEndereco,
            boolean imediata,
            Registrador r1,
            Registrador r2) {

        public Instrucao {
            Objects.requireNonNull(mnemonico, "mnemonico");
        }

        public static Instrucao memoria(Mnemonico mnemonico, int endereco) {
            return new Instrucao(mnemonico, endereco, false, null, null);
        }

        public static Instrucao imediata(Mnemonico mnemonico, int valor) {
            return new Instrucao(mnemonico, valor, true, null, null);
        }

        public static Instrucao umRegistrador(
                Mnemonico mnemonico,
                Registrador r1) {
            return new Instrucao(mnemonico, 0, false, r1, null);
        }

        public static Instrucao doisRegistradores(
                Mnemonico mnemonico,
                Registrador r1,
                Registrador r2) {
            return new Instrucao(mnemonico, 0, false, r1, r2);
        }

        public static Instrucao semOperando(Mnemonico mnemonico) {
            return new Instrucao(mnemonico, 0, false, null, null);
        }

        private Registrador exigirR1() {
            return Objects.requireNonNull(r1, mnemonico + " exige r1");
        }

        private Registrador exigirR2() {
            return Objects.requireNonNull(r2, mnemonico + " exige r2");
        }
    }
}
