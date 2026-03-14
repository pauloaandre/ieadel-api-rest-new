package ex.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void enviarEmailVerificacao(String para, String token) {
        String link = baseUrl + "/auth/verify?token=" + token;
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                "<h2 style='color: #2c3e50; text-align: center;'>Verificação de E-mail</h2>" +
                "<p>Olá,</p>" +
                "<p>Obrigado por se cadastrar! Para concluir seu registro, clique no botão abaixo para verificar seu e-mail:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + link + "' style='background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Verificar Minha Conta</a>" +
                "</div>" +
                "<p>Se o botão acima não funcionar, copie e cole o seguinte link no seu navegador:</p>" +
                "<p style='word-break: break-all; color: #3498db;'>" + link + "</p>" +
                "<p>Este link é válido por 24 horas.</p>" +
                "<br>" +
                "<p>Atenciosamente,<br>Equipe IEADEL</p>" +
                "</div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(para);
            helper.setSubject("Verificação de E-mail - IADEL");
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail de verificação", e);
        }
    }
}
