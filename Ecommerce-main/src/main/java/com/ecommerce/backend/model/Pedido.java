package com.ecommerce.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {
    private String id;
    
    @JsonProperty("cliente_id")
    private String clienteId;
    
    @JsonProperty("data_pedido")
    private LocalDateTime dataPedido;
    
    private String status;
    private BigDecimal total;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public Pedido() {}

    public Pedido(String id, String clienteId, LocalDateTime dataPedido, String status, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.clienteId = clienteId;
        this.dataPedido = dataPedido;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
