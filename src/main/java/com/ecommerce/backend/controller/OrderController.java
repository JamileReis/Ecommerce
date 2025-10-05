package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Pedido;
import com.ecommerce.backend.service.CsvExportService;
import com.ecommerce.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private CsvExportService csvExportService;

    @GetMapping("/pedidos/{pedidoId}")
    public Mono<ResponseEntity<?>> buscarPedido(@PathVariable String pedidoId) {
        return orderService.buscarPedidoPorId(pedidoId)
                .map(pedido -> {
                    if (pedido != null) {
                        return ResponseEntity.ok(pedido);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/clientes/{clienteId}/pedidos")
    public Mono<ResponseEntity<List<Pedido>>> listarPedidosDoCliente(@PathVariable String clienteId) {
        return orderService.listarPedidosPorCliente(clienteId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PostMapping("/pedidos/{pedidoId}/confirmar")
    public Mono<ResponseEntity<Map<String, String>>> confirmarPedido(@PathVariable String pedidoId) {
        return orderService.processarConfirmacaoPedido(pedidoId)
                .then(Mono.just(ResponseEntity.ok(Map.of("message", "E-mail de confirmação enviado com sucesso!"))))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erro ao processar confirmação do pedido")));
    }

    @GetMapping("/pedidos/{pedidoId}/export/csv")
    public Mono<ResponseEntity<String>> exportarPedidoCSV(@PathVariable String pedidoId) {
        return csvExportService.exportarPedidoParaCSV(pedidoId)
                .map(csvContent -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType("text/csv"));
                    headers.setContentDispositionFormData("attachment", "pedido_" + pedidoId + ".csv");
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(csvContent);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao gerar CSV do pedido"));
    }

    @GetMapping("/produtos/export/csv")
    public Mono<ResponseEntity<String>> exportarProdutosCSV() {
        return csvExportService.exportarProdutosParaCSV()
                .map(csvContent -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType("text/csv"));
                    headers.setContentDispositionFormData("attachment", "produtos.csv");
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(csvContent);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao gerar CSV dos produtos"));
    }

    @GetMapping("/clientes/export/csv")
    public Mono<ResponseEntity<String>> exportarClientesCSV() {
        return csvExportService.exportarClientesParaCSV()
                .map(csvContent -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType("text/csv"));
                    headers.setContentDispositionFormData("attachment", "clientes.csv");
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(csvContent);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao gerar CSV dos clientes"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ecommerce-backend"));
    }
}
