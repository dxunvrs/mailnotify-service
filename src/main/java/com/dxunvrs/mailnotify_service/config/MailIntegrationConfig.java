package com.dxunvrs.mailnotify_service.config;

import com.dxunvrs.mailnotify_service.service.MailTransformer;
import com.dxunvrs.mailnotify_service.service.TelegramService;
import jakarta.mail.Flags;
import jakarta.mail.search.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Configuration
@EnableIntegration
public class MailIntegrationConfig {
    @Value("${app.imap.user}")
    private String user;

    @Value("${app.imap.key}")
    private String key;

    @Value("${app.imap.host}")
    private String host;

    @Bean
    public IntegrationFlow mailIdlingFlow(MailTransformer transformer, TelegramService telegramService) {
        String imapUrl = String.format("imaps://%s:%s@%s:993/INBOX", user, key, host);

        return IntegrationFlow.from(Mail.imapIdleAdapter(imapUrl)
                        .autoStartup(true)
                        .shouldMarkMessagesAsRead(true)
                        .searchTermStrategy(((supportedFlags, folder) -> buildSearchTerm()))
                        .javaMailProperties(p -> p.put("mail.debug", "false")))
                .transform(transformer)
                .handle(telegramService, "processMail")
                .get();
    }

    private SearchTerm buildSearchTerm() {
        Flags seenFlag = new Flags(Flags.Flag.SEEN);
        SearchTerm unreadTerm = new FlagTerm(seenFlag, false);

        Date yesterday = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        SearchTerm recentTerm = new ReceivedDateTerm(ComparisonTerm.GE, yesterday);

        return new AndTerm(unreadTerm, recentTerm);
    }
}
