package com.phenikaa.security.impossibletravel.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtpMailServiceTest {
    @Test @SuppressWarnings("unchecked")
    void sendsWarningAndCodeToTheRegisteredMailbox() {
        var provider = (ObjectProvider<JavaMailSender>) mock(ObjectProvider.class);
        var sender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        var service = new OtpMailService(provider, "sender@example.test");
        service.send("recipient@example.test", "012345", false, "128.122.1.1");
        var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertArrayEquals(new String[]{"recipient@example.test"}, message.getValue().getTo());
        assertEquals("sender@example.test", message.getValue().getFrom());
        assertTrue(message.getValue().getText().contains("012345"));
        assertTrue(message.getValue().getText().contains("128.122.1.1"));
        assertTrue(message.getValue().getSubject().contains("bất thường"));
    }
}
