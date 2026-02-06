package aston.notification_service.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record SendMailRequest(
        @Schema(description = "Email получателя", example = "user@example.com")
        String email,
        @Schema(description = "Текст сообщения", example = "Это тестовое письмо.")
        String text
) {}
