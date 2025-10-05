package com.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoItem {
    private String id;
    
    @JsonProperty("pedido_id")
    private String pedidoId;
    
    @JsonProperty("produto_id")
    private String produtoId;
    
    private Integer quantidade;
    
    @JsonProperty("preco_unitario")
    private BigDecimal precoUnitario;
    
    private BigDecimal subtotal;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Construtores
    public PedidoItem() {}

    public PedidoItem(String id, String pedidoId, String produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal, LocalDateTime createdAt) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(String produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
