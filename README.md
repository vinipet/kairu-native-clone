# 🚀 KAIRu — Core Architecture & Implementation

O KAIRu evoluiu de um conceito orientado a eventos para uma ferramenta de produtividade robusta via CLI. Ele combina a leveza do Java puro com uma interface dinâmica capaz de gerenciar ciclos de foco, pausas e persistência de dados.

Este documento detalha a arquitetura atual e as decisões técnicas que sustentam o sistema.

---

## 🎯 Estado Atual do Projeto

O KAIRu hoje é capaz de:
- **Gerenciar Sessões de Foco:** Fluxo completo de Início -> Pausa -> Retomada -> Finalização.
- **Persistência em Tempo Real:** Registro de sessões no histórico (InMemory ou Persistente).
- **UI Dinâmica (Rich CLI):** Uso de sequências de escape ANSI para manter um cronômetro vivo sem bloquear o input do usuário.
- **Validação de Regras de Negócio:** Proteção contra sessões curtas demais e estados que poderiam quebrar a lógica de tempo.

---

## 🏗️ Arquitetura Implementada

A estrutura atual segue o padrão de **Desacoplamento por Camadas**:

### 1. Camada de Aplicação (`com.kairu.Application`)
Atua como o **Orquestrador de UI**.
- Gerencia o loop de entrada do usuário via `Scanner`.
- Controla o `ScheduledExecutorService` para o cronômetro visual em background.
- **Diferencial:** Implementa uma "Status Line" manual via ANSI (`MOVE_UP`, `SAVE_CURSOR`), permitindo que o cronômetro e o prompt de comando coexistam sem conflitos visuais.

### 2. Camada de Serviço (`SessionManager`)
O "Cérebro" do sistema.
- Detém a máquina de estados da sessão (RUNNING, PAUSED, STOPPED).
- Coordena o uso do `Clock` para garantir a precisão temporal.
- Aplica a regra de "Sessão Curta": impede o fechamento de sessões com menos de 5 minutos, mantendo o foco ativo.

### 3. Camada de Eventos (`EventBus`)
O sistema de comunicação desacoplada.
- Quando uma sessão é finalizada com sucesso, o `SessionManager` emite um `SessionCompletedEvent`.
- O `PersistenceListener` escuta esse evento e salva os dados no `Repository`, garantindo que a lógica de negócio não dependa da lógica de armazenamento.

---

## 🧠 Decisões Arquiteturais Chave

### ⏱️ Gestão de Tempo e Pausa (Pause-Aware)
Para que o cronômetro visual e o tempo salvo sejam idênticos, o sistema utiliza um cálculo de **Duração Acumulada**:
- O tempo não é um simples `agora - início`.
- Ao pausar, o tempo decorrido é somado a um acumulador (`accumulatedTime`).
- Ao retomar, um novo ponto de referência (`lastStartTime`) é definido.
- Isso garante que o tempo parado no café ou descanso não "suje" suas métricas de produtividade.

### 🎨 UI Não-Bloqueante com Notificações
Implementamos um sistema de **Buffer de Status**:
- Mensagens de erro ou avisos (ex: "Sessão muito curta") ganham prioridade visual por 5 segundos.
- O Timer detecta a expiração dessas mensagens e retoma a exibição do relógio automaticamente, sem intervenção do usuário.

### 🛡️ Trava de Segurança (Exit Guard)
O comando `exit` verifica se existe uma sessão em progresso. Caso positivo, interrompe o timer visual e exige uma confirmação `[s/n]`. Se a saída for negada, o ambiente de trabalho é restaurado imediatamente.

---

## 🧱 Diagrama de Fluxo (Mental Model)

1. **Input:** Usuário digita `start`.
2. **Manager:** Muda estado para `RUNNING` e notifica o `Clock`.
3. **Application:** Inicia Thread de background para desenhar o Timer na linha acima do prompt.
4. **Pause/Resume:** Ajustam o acumulador de tempo para manter a precisão.
5. **Stop:** Manager valida a duração. Se válida, emite evento -> Listener salva -> Thread do timer morre.

---

## 🧩 Modelo de Dados

- **Eventos:** Imutáveis, contendo o timestamp exato do ocorrido.
- **Sessions:** Objetos de valor que representam o esforço concluído, prontos para análise de dados.

---

## 📥 Instalação e Execução

Como o KAIRu é uma aplicação Java leve, você pode compilá-lo e executá-lo em qualquer máquina com o **JDK 17** ou superior instalado.

### 1. Pré-requisitos
- **Java JDK 17+**
- **Maven**

### 2. Compilação
No diretório raiz do projeto, execute:
```bash
mvn clean package
```
Isso criará uma pasta target/ contendo o arquivo executável (ex: kairu-1.0.0.jar).

### 3. Execução
Para rodar a aplicação:
```Bash
java -jar target/kairu-1.0.0.jar
```
### 4. Dica: Atalho no Terminal (Linux/macOS)

Adicione um alias no seu .bashrc ou .zshrc para acesso rápido:
```Bash
alias kairu='java -jar /caminho/completo/para/target/kairu-1.0.0.jar'
```

## 📌 Resumo Técnico

- **Linguagem:** Java 17+ (Puro, focado em portabilidade).
- **Concorrência:** Uso estratégico de `ScheduledExecutorService`.
- **Comunicação:** Arquitetura Orientada a Eventos (EDA).
- **Interface:** Terminal Interativo (ANSI Escape Codes).
