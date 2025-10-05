package com.ecommerce.backend.service;

import com.ecommerce.backend.model.Cliente;
import com.ecommerce.backend.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmailConfirmacaoPedido(Cliente cliente, Pedido pedido) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(cliente.getEmail());
            message.setSubject("Confirmação do Pedido #" + pedido.getId());
            message.setText(construirMensagemConfirmacao(cliente, pedido));
            message.setFrom("no-reply@seuecommerce.com");

            mailSender.send(message);
            logger.info("E-mail de confirmação enviado para: {}", cliente.getEmail());
        } catch (Exception e) {
            logger.error("Erro ao enviar e-mail de confirmação para: {}", cliente.getEmail(), e);
            throw new RuntimeException("Falha ao enviar e-mail de confirmação", e);
        }
    }

    private String construirMensagemConfirmacao(Cliente cliente, Pedido pedido) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Olá ").append(cliente.getNome()).append(",\n\n");
        mensagem.append("Seu pedido #").append(pedido.getId()).append(" foi confirmado com sucesso!\n\n");
        mensagem.append("Detalhes do pedido:\n");
        mensagem.append("- ID do Pedido: ").append(pedido.getId()).append("\n");
        mensagem.append("- Data do Pedido: ").append(pedido.getDataPedido()).append("\n");
        mensagem.append("- Status: ").append(pedido.getStatus()).append("\n");
        mensagem.append("- Total: R$ ").append(pedido.getTotal()).append("\n\n");
        mensagem.append("Agradecemos a sua compra!\n\n");
        mensagem.append("Atenciosamente,\n");
        mensagem.append("Sua Loja E-commerce");
        
        return mensagem.toString();
    }

    public void enviarEmailSimples(String destinatario, String assunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(texto);
            message.setFrom("no-reply@seuecommerce.com");

            mailSender.send(message);
            logger.info("E-mail enviado para: {}", destinatario);
        } catch (Exception e) {
            logger.error("Erro ao enviar e-mail para: {}", destinatario, e);
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }
}
