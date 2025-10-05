package com.ecommerce.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CsvExportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportService.class);

    @Autowired
    @Qualifier("supabaseWebClient")
    private WebClient supabaseWebClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<String> exportarPedidoParaCSV(String pedidoId) {
        return supabaseWebClient
                .get()
                .uri("/pedidos_detalhados?pedido_id=eq.{pedidoId}", pedidoId)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> pedidoDetalhado = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                        
                        if (pedidoDetalhado.isEmpty()) {
                            throw new RuntimeException("Pedido não encontrado: " + pedidoId);
                        }

                        return gerarCSVDoPedido(pedidoDetalhado);
                    } catch (Exception e) {
                        logger.error("Erro ao exportar pedido para CSV: {}", e.getMessage());
                        throw new RuntimeException("Erro ao gerar CSV do pedido", e);
                    }
                });
    }

    private String gerarCSVDoPedido(List<Map<String, Object>> pedidoDetalhado) throws Exception {
        StringWriter stringWriter = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("produto_nome", "quantidade", "preco_unitario", "subtotal")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
            for (Map<String, Object> item : pedidoDetalhado) {
                csvPrinter.printRecord(
                        item.get("produto_nome"),
                        item.get("quantidade"),
                        item.get("preco_unitario"),
                        item.get("subtotal")
                );
            }
        }

        return stringWriter.toString();
    }

    public Mono<String> exportarProdutosParaCSV() {
        return supabaseWebClient
                .get()
                .uri("/produtos")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> produtos = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                        return gerarCSVDosProdutos(produtos);
                    } catch (Exception e) {
                        logger.error("Erro ao exportar produtos para CSV: {}", e.getMessage());
                        throw new RuntimeException("Erro ao gerar CSV dos produtos", e);
                    }
                });
    }

    private String gerarCSVDosProdutos(List<Map<String, Object>> produtos) throws Exception {
        StringWriter stringWriter = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("id", "nome", "descricao", "preco", "estoque", "imagem_url")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
            for (Map<String, Object> produto : produtos) {
                csvPrinter.printRecord(
                        produto.get("id"),
                        produto.get("nome"),
                        produto.get("descricao"),
                        produto.get("preco"),
                        produto.get("estoque"),
                        produto.get("imagem_url")
                );
            }
        }

        return stringWriter.toString();
    }

    public Mono<String> exportarClientesParaCSV() {
        return supabaseWebClient
                .get()
                .uri("/clientes")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        List<Map<String, Object>> clientes = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                        return gerarCSVDosClientes(clientes);
                    } catch (Exception e) {
                        logger.error("Erro ao exportar clientes para CSV: {}", e.getMessage());
                        throw new RuntimeException("Erro ao gerar CSV dos clientes", e);
                    }
                });
    }

    private String gerarCSVDosClientes(List<Map<String, Object>> clientes) throws Exception {
        StringWriter stringWriter = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("id", "nome", "email", "endereco", "telefone", "created_at")
                .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
            for (Map<String, Object> cliente : clientes) {
                csvPrinter.printRecord(
                        cliente.get("id"),
                        cliente.get("nome"),
                        cliente.get("email"),
                        cliente.get("endereco"),
                        cliente.get("telefone"),
                        cliente.get("created_at")
                );
            }
        }

        return stringWriter.toString();
    }
}
