package com.dxunvrs.mailnotify_service.dto;

public record MailDto(
    String messageId,
    String subject,
    String from,
    int attachmentCount
) { }
