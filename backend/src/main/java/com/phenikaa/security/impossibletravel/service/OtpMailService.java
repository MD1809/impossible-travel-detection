package com.phenikaa.security.impossibletravel.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpMailService {
    private final ObjectProvider<JavaMailSender> sender;
    private final String from;
    public OtpMailService(ObjectProvider<JavaMailSender> sender, @Value("${app.mail.from:}") String from) {
        this.sender = sender; this.from = from;
    }
    public void send(String email, String code, boolean registration, String ip) {
        JavaMailSender mailSender = sender.getIfAvailable();
        if (mailSender == null || from.isBlank()) throw new IllegalStateException("SMTP chưa được cấu hình");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(registration ? "Xác minh email đăng ký tài khoản" : "Cảnh báo đăng nhập bất thường - Mã xác minh");
        message.setText((registration ? "Xác minh địa chỉ email đăng ký của bạn." : "Phát hiện đăng nhập bất thường từ IP: " + ip + ".")
            + "\nMã xác minh: " + code + "\nMã có hiệu lực 5 phút và chỉ dùng một lần. Không chia sẻ mã với người khác."
            + "\nNếu không phải bạn, hãy bỏ qua mã này. Yêu cầu chưa được cấp quyền đăng nhập.");
        mailSender.send(message);
    }
}
