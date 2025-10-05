# Backend de E-commerce com Supabase e Spring Boot (Java)

Este projeto implementa um backend para um sistema de e-commerce utilizando Supabase como banco de dados e autenticação, e Spring Boot (Java) para a lógica de negócios e automação de tarefas.

## Sumário

1.  [Visão Geral](#1-visão-geral)
2.  [Estrutura do Projeto](#2-estrutura-do-projeto)
3.  [Configuração do Supabase](#3-configuração-do-supabase)
    *   [Criação das Tabelas](#31-criação-das-tabelas)
    *   [Row-Level Security (RLS)](#32-row-level-security-rls)
    *   [Funções e Triggers do Banco de Dados](#33-funções-e-triggers-do-banco-de-dados)
    *   [Views Otimizadas](#34-views-otimizadas)
4.  [Configuração do Projeto Java](#4-configuração-do-projeto-java)
    *   [Pré-requisitos](#41-pré-requisitos)
    *   [Configuração das Variáveis de Ambiente](#42-configuração-das-variáveis-de-ambiente)
    *   [Executando a Aplicação](#43-executando-a-aplicação)
5.  [Endpoints da API](#5-endpoints-da-api)
6.  [Considerações de Segurança](#6-considerações-de-segurança)

## 1. Visão Geral

O projeto é dividido em duas partes principais:

*   **Supabase**: Gerencia o banco de dados PostgreSQL, autenticação, RLS, funções de banco de dados e views.
*   **Aplicação Spring Boot (Java)**: Responsável pela lógica de negócios, interação com a API REST do Supabase, envio de e-mails de confirmação e exportação de dados para CSV.

## 2. Estrutura do Projeto

```
. (raiz do projeto)
├── ecommerce-backend
│   ├── pom.xml
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── ecommerce
│   │   │   │           └── backend
│   │   │   │               ├── EcommerceBackendApplication.java
│   │   │   │               ├── config
│   │   │   │               │   └── SupabaseConfig.java
│   │   │   │               ├── controller
│   │   │   │               │   └── OrderController.java
│   │   │   │               ├── model
│   │   │   │               │   ├── Cliente.java
│   │   │   │               │   ├── Pedido.java
│   │   │   │               │   ├── PedidoItem.java
│   │   │   │               │   └── Produto.java
│   │   │   │               └── service
│   │   │   │                   ├── CsvExportService.java
│   │   │   │                   ├── EmailService.java
│   │   │   │                   └── OrderService.java
│   │   │   └── resources
│   │   │       └── application.yml
│   │   └── test
│   │       └── java
│   │           └── com
│   │               └── ecommerce
│   │                   └── backend
│   │                       └── ... (testes)
│   └── README.md
└── supabase_scripts
    ├── schema.sql
    ├── rls_policies.sql
    ├── db_functions.sql
    └── db_views.sql
```

## 3. Configuração do Supabase

Para configurar o Supabase, você precisará de uma conta Supabase e um projeto criado. Uma vez que o projeto esteja ativo, você pode aplicar os scripts SQL fornecidos na pasta `supabase_scripts`.

### 3.1. Criação das Tabelas

O arquivo `supabase_scripts/schema.sql` contém as definições para as tabelas `clientes`, `produtos`, `pedidos` e `pedido_itens`. Para aplicá-lo:

1.  Acesse o painel do seu projeto Supabase.
2.  Vá para a seção **SQL Editor**.
3.  Cole o conteúdo de `schema.sql` e execute-o.

```sql
-- Conteúdo de supabase_scripts/schema.sql
-- Tabela de Clientes
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL, -- Em um ambiente real, a senha seria hash
    endereco TEXT,
    telefone VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabela de Produtos
CREATE TABLE produtos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco NUMERIC(10, 2) NOT NULL CHECK (preco >= 0),
    estoque INT NOT NULL CHECK (estoque >= 0),
    imagem_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabela de Pedidos
CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cliente_id UUID REFERENCES clientes(id) ON DELETE CASCADE,
    data_pedido TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'pendente' NOT NULL,
    total NUMERIC(10, 2) NOT NULL CHECK (total >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabela de Itens do Pedido
CREATE TABLE pedido_itens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pedido_id UUID REFERENCES pedidos(id) ON DELETE CASCADE,
    produto_id UUID REFERENCES produtos(id) ON DELETE CASCADE,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(10, 2) NOT NULL CHECK (preco_unitario >= 0),
    subtotal NUMERIC(10, 2) NOT NULL CHECK (subtotal >= 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices para otimização de consultas
CREATE INDEX idx_clientes_email ON clientes(email);
CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);
CREATE INDEX idx_pedido_itens_pedido_id ON pedido_itens(pedido_id);
CREATE INDEX idx_pedido_itens_produto_id ON pedido_itens(produto_id);
```

### 3.2. Row-Level Security (RLS)

O arquivo `supabase_scripts/rls_policies.sql` contém as políticas de RLS para garantir que os usuários só possam acessar seus próprios dados. Aplique este script da mesma forma que o `schema.sql`.

**Importante**: Certifique-se de que a autenticação do Supabase esteja configurada e que os usuários estejam autenticados para que as políticas de RLS funcionem corretamente.

```sql
-- Conteúdo de supabase_scripts/rls_policies.sql
-- Habilitar RLS para todas as tabelas
ALTER TABLE clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE produtos ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedidos ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedido_itens ENABLE ROW LEVEL SECURITY;

-- Políticas para a tabela 'clientes'
CREATE POLICY "Clientes podem ver seus próprios dados" ON clientes FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Clientes podem inserir seus próprios dados" ON clientes FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Clientes podem atualizar seus próprios dados" ON clientes FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);
CREATE POLICY "Clientes podem deletar seus próprios dados" ON clientes FOR DELETE USING (auth.uid() = id);

-- Políticas para a tabela 'produtos'
CREATE POLICY "Todos podem ver produtos" ON produtos FOR SELECT USING (TRUE);
CREATE POLICY "Usuários autenticados podem gerenciar produtos" ON produtos FOR ALL USING (auth.role() = 'authenticated');

-- Políticas para a tabela 'pedidos'
CREATE POLICY "Clientes podem ver seus próprios pedidos" ON pedidos FOR SELECT USING (auth.uid() = cliente_id);
CREATE POLICY "Clientes podem inserir seus próprios pedidos" ON pedidos FOR INSERT WITH CHECK (auth.uid() = cliente_id);
CREATE POLICY "Clientes podem atualizar seus próprios pedidos" ON pedidos FOR UPDATE USING (auth.uid() = cliente_id) WITH CHECK (auth.uid() = cliente_id);
CREATE POLICY "Clientes podem deletar seus próprios pedidos" ON pedidos FOR DELETE USING (auth.uid() = cliente_id);

-- Políticas para a tabela 'pedido_itens'
CREATE POLICY "Clientes podem ver itens de seus próprios pedidos" ON pedido_itens FOR SELECT USING (EXISTS (SELECT 1 FROM pedidos WHERE id = pedido_id AND cliente_id = auth.uid()));
CREATE POLICY "Clientes podem inserir itens em seus próprios pedidos" ON pedido_itens FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM pedidos WHERE id = pedido_id AND cliente_id = auth.uid()));
CREATE POLICY "Clientes podem atualizar itens em seus próprios pedidos" ON pedido_itens FOR UPDATE USING (EXISTS (SELECT 1 FROM pedidos WHERE id = pedido_id AND cliente_id = auth.uid())) WITH CHECK (EXISTS (SELECT 1 FROM pedidos WHERE id = pedido_id AND cliente_id = auth.uid()));
CREATE POLICY "Clientes podem deletar itens de seus próprios pedidos" ON pedido_itens FOR DELETE USING (EXISTS (SELECT 1 FROM pedidos WHERE id = pedido_id AND cliente_id = auth.uid()));
```

### 3.3. Funções e Triggers do Banco de Dados

O arquivo `supabase_scripts/db_functions.sql` contém funções e triggers para automatizar o cálculo do total do pedido e a atualização do estoque. Aplique este script da mesma forma.

```sql
-- Conteúdo de supabase_scripts/db_functions.sql
-- Função para calcular o total de um pedido
CREATE OR REPLACE FUNCTION calcular_total_pedido(p_pedido_id UUID)
RETURNS NUMERIC(10, 2) AS $$
DECLARE
    total_calculado NUMERIC(10, 2);
BEGIN
    SELECT SUM(quantidade * preco_unitario) INTO total_calculado
    FROM pedido_itens
    WHERE pedido_id = p_pedido_id;

    RETURN COALESCE(total_calculado, 0);
END;
$$ LANGUAGE plpgsql;

-- Trigger para atualizar o total do pedido após inserção/atualização/deleção de itens
CREATE OR REPLACE FUNCTION atualizar_total_pedido_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        UPDATE pedidos
        SET total = calcular_total_pedido(OLD.pedido_id)
        WHERE id = OLD.pedido_id;
        RETURN OLD;
    ELSE
        UPDATE pedidos
        SET total = calcular_total_pedido(NEW.pedido_id)
        WHERE id = NEW.pedido_id;
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_atualizar_total_pedido
AFTER INSERT OR UPDATE OR DELETE ON pedido_itens
FOR EACH ROW EXECUTE FUNCTION atualizar_total_pedido_trigger();

-- Função para atualizar o estoque de produtos após a criação de um item de pedido
CREATE OR REPLACE FUNCTION atualizar_estoque_apos_pedido()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE produtos
        SET estoque = estoque - NEW.quantidade
        WHERE id = NEW.produto_id;
    ELSIF TG_OP = 'UPDATE' THEN
        UPDATE produtos
        SET estoque = estoque - (NEW.quantidade - OLD.quantidade)
        WHERE id = NEW.produto_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE produtos
        SET estoque = estoque + OLD.quantidade
        WHERE id = OLD.produto_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_atualizar_estoque
AFTER INSERT OR UPDATE OR DELETE ON pedido_itens
FOR EACH ROW EXECUTE FUNCTION atualizar_estoque_apos_pedido();

-- Função para atualizar o status do pedido (exemplo simples)
CREATE OR REPLACE FUNCTION atualizar_status_pedido(p_pedido_id UUID, p_novo_status VARCHAR(50))
RETURNS VOID AS $$
BEGIN
    UPDATE pedidos
    SET status = p_novo_status
    WHERE id = p_pedido_id;
END;
$$ LANGUAGE plpgsql;
```

### 3.4. Views Otimizadas

O arquivo `supabase_scripts/db_views.sql` define views para facilitar consultas complexas, como `pedidos_detalhados` e `produtos_em_estoque`. Aplique este script da mesma forma.

```sql
-- Conteúdo de supabase_scripts/db_views.sql
-- View para Pedidos Detalhados
CREATE OR REPLACE VIEW pedidos_detalhados AS
SELECT
    p.id AS pedido_id,
    c.id AS cliente_id,
    c.nome AS cliente_nome,
    c.email AS cliente_email,
    p.data_pedido,
    p.status AS status_pedido,
    p.total AS total_pedido,
    pi.produto_id,
    pr.nome AS produto_nome,
    pr.descricao AS produto_descricao,
    pi.quantidade,
    pi.preco_unitario,
    pi.subtotal
FROM
    pedidos p
JOIN
    clientes c ON p.cliente_id = c.id
JOIN
    pedido_itens pi ON p.id = pi.pedido_id
JOIN
    produtos pr ON pi.produto_id = pr.id;

-- View para Produtos em Estoque
CREATE OR REPLACE VIEW produtos_em_estoque AS
SELECT
    id AS produto_id,
    nome AS produto_nome,
    descricao AS produto_descricao,
    preco,
    estoque
FROM
    produtos
WHERE
    estoque > 0;
```

## 4. Configuração do Projeto Java

### 4.1. Pré-requisitos

*   Java Development Kit (JDK) 17 ou superior
*   Apache Maven 3.6+ (para gerenciar dependências e build)
*   Acesso a um servidor SMTP (para envio de e-mails, como Gmail)

### 4.2. Configuração das Variáveis de Ambiente

Edite o arquivo `ecommerce-backend/src/main/resources/application.yml` ou defina as seguintes variáveis de ambiente:

*   `SUPABASE_URL`: URL do seu projeto Supabase (ex: `https://your-project.supabase.co`)
*   `SUPABASE_ANON_KEY`: Chave `anon` (publica) do seu projeto Supabase
*   `SUPABASE_SERVICE_ROLE_KEY`: Chave `service_role` (secreta) do seu projeto Supabase. **Cuidado ao expor esta chave em ambientes de produção.**
*   `EMAIL_USERNAME`: Seu e-mail para envio (ex: `your-email@gmail.com`)
*   `EMAIL_PASSWORD`: Senha de aplicativo gerada para o seu e-mail (para Gmail, veja [Gerar uma senha de app](https://support.google.com/accounts/answer/185833?hl=pt-BR)).

Exemplo de `application.yml` com placeholders:

```yaml
# Conteúdo de ecommerce-backend/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: ecommerce-backend
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME:your-email@gmail.com}
    password: ${EMAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# Configurações do Supabase
supabase:
  url: ${SUPABASE_URL:https://your-project.supabase.co}
  anon-key: ${SUPABASE_ANON_KEY:your-anon-key}
  service-role-key: ${SUPABASE_SERVICE_ROLE_KEY:your-service-role-key}

logging:
  level:
    com.ecommerce.backend: DEBUG
    org.springframework.web: DEBUG
```

### 4.3. Executando a Aplicação

1.  Navegue até o diretório `ecommerce-backend`.
2.  Compile o projeto usando Maven:
    ```bash
    mvn clean install
    ```
3.  Execute a aplicação Spring Boot:
    ```bash
    mvn spring-boot:run
    ```
    Ou, se preferir executar o JAR gerado:
    ```bash
    java -jar target/ecommerce-backend-1.0.0.jar
    ```

A aplicação estará disponível em `http://localhost:8080`.

## 5. Endpoints da API

Os seguintes endpoints estão disponíveis na aplicação Spring Boot:

*   **`GET /api/health`**
    *   Verifica a saúde da aplicação.
    *   Resposta: `{"status": "UP", "service": "ecommerce-backend"}`

*   **`GET /api/pedidos/{pedidoId}`**
    *   Busca um pedido específico pelo ID.
    *   Exemplo: `GET /api/pedidos/a1b2c3d4-e5f6-7890-1234-567890abcdef`

*   **`GET /api/clientes/{clienteId}/pedidos`**
    *   Lista todos os pedidos de um cliente específico.
    *   Exemplo: `GET /api/clientes/a1b2c3d4-e5f6-7890-1234-567890abcdef/pedidos`

*   **`POST /api/pedidos/{pedidoId}/confirmar`**
    *   Processa a confirmação de um pedido e envia um e-mail de confirmação ao cliente.
    *   Exemplo: `POST /api/pedidos/a1b2c3d4-e5f6-7890-1234-567890abcdef/confirmar`

*   **`GET /api/pedidos/{pedidoId}/export/csv`**
    *   Exporta os detalhes de um pedido específico para um arquivo CSV.
    *   Exemplo: `GET /api/pedidos/a1b2c3d4-e5f6-7890-1234-567890abcdef/export/csv`

*   **`GET /api/produtos/export/csv`**
    *   Exporta todos os produtos para um arquivo CSV.
    *   Exemplo: `GET /api/produtos/export/csv`

*   **`GET /api/clientes/export/csv`**
    *   Exporta todos os clientes para um arquivo CSV.
    *   Exemplo: `GET /api/clientes/export/csv`

## 6. Considerações de Segurança

*   **Chaves Supabase**: A `SUPABASE_SERVICE_ROLE_KEY` deve ser tratada com extrema cautela, pois concede acesso total ao seu banco de dados. Em produção, considere usar uma abordagem mais segura para credenciais, como variáveis de ambiente gerenciadas por um orquestrador ou um serviço de segredos.
*   **Autenticação**: A aplicação Java interage com o Supabase usando as chaves fornecidas. Para endpoints que exigem autenticação de usuário final, a aplicação Java precisaria implementar um fluxo de autenticação (ex: JWT) e passar o token do usuário para o Supabase nas requisições.
*   **Validação de Entrada**: Embora o Spring Boot Starter Validation esteja incluído, é crucial implementar validação de entrada robusta em todos os endpoints da API para prevenir ataques e garantir a integridade dos dados.
*   **HTTPS**: Sempre use HTTPS em ambientes de produção para proteger a comunicação entre clientes e o backend, e entre o backend e o Supabase.
