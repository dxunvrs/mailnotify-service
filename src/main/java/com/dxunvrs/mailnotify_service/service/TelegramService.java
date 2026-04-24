package com.dxunvrs.mailnotify_service.service;

import com.dxunvrs.mailnotify_service.dto.MailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TelegramService {
    private final RestClient restClient;
    private final DeduplicationService deduplicationService;
    private final String chatId;

    public TelegramService(DeduplicationService deduplicationService,
                           @Value("${app.tg.token}") String token,
                           @Value("${app.tg.chat-id}") String chatId) {
        this.deduplicationService = deduplicationService;
        this.chatId = chatId;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + token)
                .build();
    }

    public void processMail(MailDto mailDto) {
        if (deduplicationService.isNewMessage(mailDto.messageId())) {
            String text = String.format("📧 *Новое письмо!*\n*От:* %s\n*Тема:* %s\n%s",
                    mailDto.from(),
                    mailDto.subject(),
                    mailDto.attachmentCount() > 0 ? "📎 Вложений: " + mailDto.attachmentCount() : "📎 Без вложений");

            sendToTelegram(text);
        }
    }

    private void sendToTelegram(String text) {
        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text,
                "parse_mode", "Markdown"
        );

        restClient.post()
                .uri("/sendMessage")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
