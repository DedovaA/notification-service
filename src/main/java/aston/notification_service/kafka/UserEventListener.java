package aston.notification_service.kafka;

import lombok.RequiredArgsConstructor;
import aston.notification_service.mail.EmailSender;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final EmailSender emailSender;

    @KafkaListener(topics = "${app.kafka.topic}")
    public void onMessage(UserEvent event) {

        String text = switch (event.operation()) {
            case DELETE -> "Ваш аккаунт " + event.email() + " был удалён.";
            case CREATE -> "Ваш аккаунт " + event.email() + " был успешно создан.";
        };

        emailSender.send(
                event.email(),
                "Уведомление",
                text
        );
    }
}
