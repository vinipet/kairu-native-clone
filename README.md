# KAIRu — Core Architecture Overview

KAIRu é uma aplicação de organização de estudos baseada em tempo (pomodoro-like), projetada para rodar de forma leve em segundo plano, com foco em extensibilidade, baixo consumo de recursos e arquitetura sólida.

Este documento descreve **as decisões arquiteturais do core**, explicando **o porquê** de cada escolha.

---

## 🎯 Objetivo do Projeto

O KAIRu tem como objetivo:

- Organizar sessões de estudo baseadas em tempo
- Rodar em segundo plano com consumo mínimo de recursos
- Ser altamente extensível via mods
- Manter um core simples, previsível e testável
- Manter UI totalmente desacoplada da lógica principal

O projeto **não é centrado em interface**, mas em **regras, eventos e tempo**.

---

## 🧠 Princípio Fundamental

> O KAIRu é um sistema **orientado a eventos**, não um loop que fica rodando constantemente.

Isso significa:

- Nenhum `while(true)` consumindo CPU
- O sistema só “acorda” quando:
  - o usuário interage
  - um evento de tempo ocorre
- Quando não há cronômetro ativo, o sistema fica efetivamente “congelado”

---

## ❌ Por que não usar Spring no core

O projeto pode ter sido inicializado via Spring Boot, mas:

- Spring é ideal para:
  - aplicações web
  - APIs
  - serviços HTTP
- Ele adiciona:
  - lifecycle próprio
  - dependências implícitas
  - acoplamento via annotations

Para um app:
- leve
- orientado a eventos
- em background
- com sistema de mods

👉 **Java puro é mais simples, mais leve e mais controlável**.

Nada impede usar Spring futuramente em:
- UI
- serviços externos

Mas o **core não depende de Spring**.

---

## 🧱 Arquitetura em Alto Nível

┌─────────────┐
│ UI │ (tray, GUI, CLI, etc)
└──────┬──────┘
│ escuta eventos
┌──────▼──────┐
│ Core │ ← Java puro
│ (Event-Driven)
└──────┬──────┘
│ emite eventos
┌──────▼──────┐
│ Mods │ (Lua no futuro)
└─────────────┘


### Regra de Ouro

- O core **não conhece a UI**
- O core **não conhece mods**
- O core apenas **emite eventos**

---

## ⚙️ Modelo Mental do Core

O core é estruturado em **três pilares principais**.

---

### 1️⃣ Eventos

Eventos representam **fatos que aconteceram no sistema**.

Exemplos:
- Cronômetro iniciado
- Cronômetro finalizado
- Sessão interrompida
- Tag adicionada

Características dos eventos:

- São **imutáveis**
- Não contêm lógica
- Não sabem quem os consome
- Representam apenas *o que aconteceu*

---

### 2️⃣ EventBus

O EventBus é o barramento central de eventos.

Responsabilidades:
- Receber eventos
- Distribuir eventos para todos os interessados
- Não conhecer regras de negócio
- Não conhecer UI nem mods

Ele funciona como:
> “Alguém emite um evento; quem quiser escutar, escuta.”

Decisão inicial:
- EventBus **síncrono**
- Execução imediata
- Ordem previsível
- Simples de testar

---

### 3️⃣ Tempo (Clock)

O tempo nunca é acessado diretamente via `System.currentTimeMillis`.

Motivos:
- Facilitar testes
- Garantir previsibilidade
- Permitir análise de dados no futuro

O tempo é abstraído por um `Clock`:

- Em produção → relógio real
- Em testes → tempo controlado

---

## ⏱️ Eventos e Tempo

### Estrutura escolhida

- Existe uma **classe base de evento**
- Ela contém:
  - data
  - hora
- Todos os eventos concretos **herdam** essas informações

Motivos:
- Padronização
- Menos repetição
- Nenhum evento sem timestamp
- Melhor suporte a métricas, gráficos e análises

---

## 🙈 Eventos são cegos

Eventos:
- Não sabem quem os consome
- Não sabem se alguém os consome
- Não têm efeitos colaterais

Isso garante:
- Baixo acoplamento
- UI intercambiável
- Facilidade para mods
- Testes simples

---

## 🧩 Sistema de Mods (Visão Futura)

Mods:
- Não alteram regras do core
- Não modificam comportamento existente
- Apenas:
  - escutam eventos
  - reagem a eles
  - adicionam funcionalidades paralelas

A ideia é:
- Core sólido e fechado
- Extensão via eventos
- Criatividade sem risco de quebrar o sistema

Lua será usada futuramente como:
- linguagem de extensão
- não como base do sistema

---

## 🧪 Testes como Prioridade

Desde o início:

- O core é projetado para ser testável
- Decisões arquiteturais levam testes em conta
- Tempo e eventos são controláveis

Não é obrigatório TDD estrito, mas:
> **Testabilidade influencia o design**

---

## 📌 Resumo (TL;DR)

- Core em Java puro
- Arquitetura orientada a eventos
- EventBus síncrono
- Tempo abstraído via `Clock`
- Eventos imutáveis com timestamp
- Core independente de UI e mods
- Extensão via mods, não via hack
- Foco em clareza, previsibilidade e aprendizado

---

## ▶️ Próximo Passo

Com essa base definida, o próximo passo é criar as primeiras interfaces do core:

- `Event`
- `BaseEvent`
- `Clock`
- `EventBus`

Esses contratos formam a fundação do KAIRu.

