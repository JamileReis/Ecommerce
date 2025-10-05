CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS clientes (
                                        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome text NOT NULL,
    email text NOT NULL UNIQUE,
    criado_em timestamptz DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS produtos (
                                        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nome text NOT NULL,
    descricao text,
    preco numeric(12,2) NOT NULL CHECK (preco >= 0),
    estoque integer NOT NULL DEFAULT 0,
    criado_em timestamptz DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS pedidos (
                                       id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id uuid NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    status text NOT NULL DEFAULT 'PENDENTE',
    total numeric(12,2) NOT NULL DEFAULT 0,
    criado_em timestamptz DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS itens_pedido (
                                            id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id uuid NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    produto_id uuid NOT NULL REFERENCES produtos(id),
    quantidade integer NOT NULL CHECK (quantidade > 0),
    preco_unitario numeric(12,2) NOT NULL,
    criado_em timestamptz DEFAULT now()
    );
