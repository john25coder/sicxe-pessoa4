# SIC/XE - Pessoa 4 em C

Implementacao isolada das instrucoes de movimentacao de dados e controle de
fluxo atribuidas a Pessoa 4.

## Instrucoes implementadas

- Carga: `LDA`, `LDB`, `LDCH`, `LDL`, `LDS`, `LDT`, `LDX`
- Armazenamento: `STA`, `STB`, `STCH`, `STL`, `STS`, `STT`, `STX`
- Registradores: `CLEAR`, `RMO`
- Desvios: `J`, `JEQ`, `JGT`, `JLT`, `JSUB`, `RSUB`

Total: 22 instrucoes.

## Arquivos

- `instrucoes_movimentacao_desvios.h`: tipos, contratos e funcoes publicas.
- `instrucoes_movimentacao_desvios.c`: logica das 22 instrucoes.

O modulo nao implementa CPU, memoria, decodificador ou interface grafica. Ele
recebe ponteiros de funcao para acessar esses componentes quando forem criados
pelos outros integrantes.

## Compilacao isolada

```sh
gcc -std=c11 -Wall -Wextra -Werror -c instrucoes_movimentacao_desvios.c
```

## Decisoes temporarias de integracao

1. A memoria e enderecada por byte, pois as instrucoes usam palavras nos
   enderecos `m`, `m+1` e `m+2`, enquanto `LDCH` e `STCH` usam apenas um byte.
2. O decodificador futuro deve fornecer o endereco efetivo final. No modo
   imediato, `valor_ou_endereco` contem o proprio literal.
3. Antes de executar `JSUB`, o ciclo de busca deve deixar `PC` apontando para a
   proxima instrucao. Esse valor sera salvo em `L`.
4. Um desvio condicional nao tomado mantem o valor atual de `PC`.
5. Os valores dos registradores sao limitados a 24 bits dentro deste modulo.
