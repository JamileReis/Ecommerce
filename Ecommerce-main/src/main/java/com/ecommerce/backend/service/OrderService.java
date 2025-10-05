package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Cliente;
import com.ecommerce.backend.model.Pedido;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    @Qualifier("supabaseWebClient")
    private WebClient supabaseWebClient;

    @Autowired
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Pedido> buscarPedidoPorId(String pedidoId) {
        return supabaseWebClient
                .get()
                .uri("/pedidos?id=eq.{id}", pedidoId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        List<Pedido> pedidos = objectMapper.readValue(response, new TypeReference<List<Pedido>>() {});
                        return pedidos.isEmpty() ? null : pedidos.get(0);
                    } catch (Exception e) {
                        logger.error("Erro ao deserializar pedido: {}", e.getMessage());
                        throw new RuntimeException("Erro ao processar dados do pedido", e);
                    }
                });
    }

    public Mono<Cliente> buscarClientePorId(String clienteId) {
        return supabaseWebClient
                .get()
                .uri("/clientes?id=eq.{id}", clienteId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        List<Cliente> clientes = objectMapper.readValue(response, new TypeReference<List<Cliente>>() {});
                        return clientes.isEmpty() ? null : clientes.get(0);
                    } catch (Exception e) {
                        logger.error("Erro ao deserializar cliente: {}", e.getMessage());
                        throw new RuntimeException("Erro ao processar dados do cliente", e);
                    }
                });
    }

    public Mono<Void> processarConfirmacaoPedido(String pedidoId) {
        return buscarPedidoPorId(pedidoId)
                .flatMap(pedido -> {
                    if (pedido == null) {
                        return Mono.error(new RuntimeException("Pedido não encontrado: " + pedidoId));
                    }
                    return buscarClientePorId(pedido.getClienteId())
                            .map(cliente -> {
                                if (cliente == null) {
                                    throw new RuntimeException("Cliente não encontrado: " + pedido.getClienteId());
                                }
                                emailService.enviarEmailConfirmacaoPedido(cliente, pedido);
                                logger.info("Confirmação de pedido processada para pedido: {}", pedidoId);
                                return cliente;
                            });
                })
                .then();
    }

    public Mono<List<Pedido>> listarPedidosPorCliente(String clienteId) {
        return supabaseWebClient
                .get()
                .uri("/pedidos?cliente_id=eq.{clienteId}", clienteId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        return objectMapper.readValue(response, new TypeReference<List<Pedido>>() {});
                    } catch (Exception e) {
                        logger.error("Erro ao deserializar lista de pedidos: {}", e.getMessage());
                        throw new RuntimeException("Erro ao processar lista de pedidos", e);
                    }
                });
    }
}
