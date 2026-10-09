# Flipper

> Hypixel Skyblock Auction House Flipper: scanner que encontra, em tempo real, itens revendáveis com lucro na Auction House.

![License: MIT](https://img.shields.io/badge/license-MIT-blue)
![Java](https://img.shields.io/badge/Java-11-ED8B00)
![Build](https://img.shields.io/badge/build-Maven-C71A36)

Flipper é um scanner de linha de comando que monitora a Auction House do Hypixel Skyblock e
identifica oportunidades de **flip** (comprar barato, revender no preço de mercado) no instante
em que elas aparecem. Quando acha o item mais lucrativo do momento, ele já coloca o comando
`/viewauction <id>` na área de transferência e dá um beep, então basta colar no jogo e comprar
antes da concorrência.

## Sobre o projeto

Flipar na Auction House é uma corrida de milissegundos: milhares de leilões surgem o tempo
todo, e os itens realmente lucrativos somem em segundos porque dezenas de jogadores (e bots)
disputam os mesmos BINs. Fazer isso no olho é inviável. O Flipper resolve o problema
automatizando o ciclo inteiro de detecção: puxar os leilões, cruzar com o preço de mercado,
calcular o lucro real e entregar a jogada pronta para execução.

O coração do projeto é a comparação contínua entre dois fluxos de dados: os leilões ativos da
[API pública do Hypixel](https://api.hypixel.net/skyblock/auctions) e os preços de referência
*lowest BIN* do [Moulberry](https://moulberry.codes/lowestbin.json). Para cada leilão BIN, o
Flipper decodifica o NBT do item (os `item_bytes` vêm em Base64), extrai o identificador real
(inclusive tratando o caso especial de pets, que combinam tipo e tier) e calcula o lucro
**já descontando as taxas da Auction House**: taxa de criação escalonada por faixa de preço e
taxa de claim. Só entra na lista o que passa dos limites de preço e lucro definidos pelo usuário.

Tudo roda de forma concorrente sobre um `ScheduledExecutorService`, com atualização do preço de
mercado e varredura dos leilões em agendamentos independentes, além de um cache de leilões já
avaliados que é limpo periodicamente para não reprocessar o mesmo item.

## Tecnologias

- **Java 11** com a `HttpClient` nativa (`java.net.http`) para consumir as APIs
- **Maven** com `maven-shade-plugin` para gerar um JAR executável com dependências embutidas
- **[Gson](https://github.com/google/gson)**: parsing dos leilões e dos preços de mercado
- **[Nedit](https://github.com/Nullicorn/Nedit)**: leitura do NBT dos itens (Base64 para extrair atributos)
- **[Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)**: utilitários de String
- **Concorrência nativa**: `ScheduledExecutorService` e `parallelStream` para varredura rápida

## Funcionalidades

- **Detecção de flips em tempo real** cruzando leilões ativos com o lowest BIN de mercado
- **Dois modos de varredura**:
  - **Common**: acompanha o campo `lastUpdated` da Auction House e varre em paralelo a cada ciclo
  - **Cooldown**: foca nos leilões criados nos últimos 20 segundos, com polling a cada 250ms
- **Cálculo de lucro fiel ao jogo**, descontando taxa de criação (escalonada em 1%, 2% e 2.5% por faixa) e taxa de claim
- **Decodificação de NBT** dos itens, com tratamento dedicado para pets (tipo + tier)
- **Filtro de itens** por categoria permitida, itens removidos do jogo e descrições indesejadas
- **Ação pronta para o jogo**: o comando `/viewauction` do item mais lucrativo vai direto para a área de transferência
- **Parâmetros configuráveis** no início: modo de scan, preço máximo de compra e lucro mínimo
- **Cache de leilões avaliados** com limpeza periódica para evitar reprocessamento

## Como rodar

### Pré-requisitos

- [Java 11+](https://adoptium.net/)
- [Maven 3.6+](https://maven.apache.org/)

### Build

```bash
# Compila e gera o JAR executável (com dependências embutidas) em target/
mvn clean package
```

### Execução

```bash
# Rode o JAR gerado
java -jar target/Flipper-1.0.0-SNAPSHOT.jar
```

Ao iniciar, o programa pede três entradas no console:

1. **Modo de flip**: `1` para Common ou `2` para Cooldown (BED)
2. **Preço máximo do item**: teto de compra que você aceita pagar
3. **Lucro mínimo**: lucro líquido mínimo (já com taxas) para o item ser considerado

A partir daí o scanner roda continuamente. Quando encontra o melhor flip do momento, o comando
`/viewauction <id>` fica na sua área de transferência: cole no chat do jogo para abrir o leilão
e comprar.

## Estrutura do projeto

```
Flipper/
└── src/main/java/br/com/techsneeker/
    ├── Main.java                 # ponto de entrada: coleta preferências e inicia o scanner
    ├── client/
    │   └── ClientHttp.java       # chamadas HTTP à API do Hypixel e ao lowest BIN
    ├── scanner/
    │   ├── ScannerContract.java  # base abstrata: agendamento, cache e atualização de mercado
    │   ├── CommonScanner.java    # varredura paralela baseada em lastUpdated
    │   └── OnCooldownScanner.java# foco em leilões recém-criados (janela de 20s)
    ├── service/
    │   ├── ItemController.java   # resolve o id do item a partir do NBT (inclui pets)
    │   ├── ProfitCalculator.java # lucro líquido descontando taxas da Auction House
    │   └── Utils.java            # clipboard, conversão de datas e tabelas de apoio
    └── object/
        ├── Item.java             # modelo do item e decodificação de NBT (Base64)
        ├── Filter.java           # regras de itens ignoráveis
        ├── Config.java           # coleta interativa das preferências no console
        └── enums/                # endpoints, reforges e itens removidos
```

## Decisões técnicas

- **Dois scanners para dois objetivos.** O modo Common prioriza cobertura e paraleliza a
  varredura; o modo Cooldown prioriza velocidade em leilões recém-criados, onde os melhores
  flips costumam aparecer. Compartilham a mesma base (`ScannerContract`) para reaproveitar
  agendamento e cache.
- **Lucro descontando taxas reais.** Em vez de só subtrair preço de compra do lowest BIN, o
  `ProfitCalculator` modela as taxas da Auction House (criação escalonada por faixa de valor e
  claim), para o lucro exibido refletir o que de fato sobra.
- **NBT em vez de só o nome do item.** O identificador confiável do item está no NBT codificado
  em Base64, não no nome exibido. Decodificar isso evita falsos positivos e permite tratar casos
  como pets, cujo preço depende de tipo e tier.
- **Concorrência com agendamento independente.** Atualizar o preço de mercado e varrer os
  leilões em agendamentos separados mantém os dados frescos sem travar o loop de busca.

## Aviso

Projeto desenvolvido para fins educacionais e de estudo. Consulte os termos de uso do Hypixel
antes de utilizar qualquer automação no jogo.

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.

<div align="center">
  <br>
  <br>
  <br>
  <br>
  <img src="docs/screenshots/logo.png" alt="Logo TechSneeker" width="240">
  <br>
  <sub>Made by <strong>TechSneeker</strong></sub>
</div>
