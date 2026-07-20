package com.dxunvrs.mailnotify_service.service;

import com.dxunvrs.mailnotify_service.dto.MailDto;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.jspecify.annotations.NullMarked;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MailTransformer extends AbstractTransformer {
    @Override
    @NullMarked
    protected Object doTransform(Message<?> message) {
        MimeMessage mimeMessage = (MimeMessage) message.getPayload();
        try {
            String messageId = mimeMessage.getMessageID();
            String subject = "Без темы";
            if (mimeMessage.getSubject() != null) subject = mimeMessage.getSubject();
            String from = MimeUtility.decodeText(mimeMessage.getFrom()[0].toString());
            int attachments = countAttachments(mimeMessage);

            return new MailDto(messageId, subject, from, attachments);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при парсинге письма");
        }
    }

    private int countAttachments(MimeMessage message) throws Exception {
        if (!message.isMimeType("multipart/*")) return 0;

        Multipart multipart = (Multipart) message.getContent();
        int count = 0;
        for (int i = 0; i < multipart.getCount(); i++) {
            Part part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                count++;
            }
        }
        return count;
    }

    @Override
    @NullMarked
    public String getComponentType() {
        return MailDto.class.getTypeName();
    }
}
