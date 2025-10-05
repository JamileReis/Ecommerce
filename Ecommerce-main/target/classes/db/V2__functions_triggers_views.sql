CREATE OR REPLACE FUNCTION calcular_total_pedido(pedido_uuid uuid) RETURNS numeric AS $$
DECLARE
v_total numeric := 0;
BEGIN
SELECT COALESCE(SUM(quantidade * preco_unitario), 0) INTO v_total
FROM itens_pedido
WHERE pedido_id = pedido_uuid;

UPDATE pedidos SET total = v_total WHERE id = pedido_uuid;

RETURN v_total;
END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION trg_itens_pedido_after_change() RETURNS trigger AS $$
BEGIN
  PERFORM calcular_total_pedido(NEW.pedido_id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS itens_pedido_after_insert ON itens_pedido;
CREATE TRIGGER itens_pedido_after_insert
    AFTER INSERT ON itens_pedido
    FOR EACH ROW EXECUTE PROCEDURE trg_itens_pedido_after_change();

DROP TRIGGER IF EXISTS itens_pedido_after_update ON itens_pedido;
CREATE TRIGGER itens_pedido_after_update
    AFTER UPDATE ON itens_pedido
    FOR EACH ROW EXECUTE PROCEDURE trg_itens_pedido_after_change();

DROP TRIGGER IF EXISTS itens_pedido_after_delete ON itens_pedido;
CREATE TRIGGER itens_pedido_after_delete
    AFTER DELETE ON itens_pedido
    FOR EACH ROW EXECUTE PROCEDURE trg_itens_pedido_after_change();

CREATE OR REPLACE VIEW vw_pedidos_completos AS
SELECT p.id AS pedido_id,
       p.cliente_id,
       p.status,
       p.total,
       p.criado_em,
       json_agg(json_build_object(
               'item_id', ip.id,
               'produto_id', ip.produto_id,
               'quantidade', ip.quantidade,
               'preco_unitario', ip.preco_unitario
                )) FILTER (WHERE ip.id IS NOT NULL) AS itens
FROM pedidos p
         LEFT JOIN itens_pedido ip ON ip.pedido_id = p.id
GROUP BY p.id;
