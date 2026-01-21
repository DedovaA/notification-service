package notification_service.kafka;

import lombok.RequiredArgsConstructor;
import notification_service.mail.EmailSender;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailSender emailSender;

    @KafkaListener(topics = "${app.kafka.topic}")
    public void onMessage(UserEvent event) {

        String text = switch (event.operation()) {
            case DELETE -> "Ваш аккаунт был удалён.";
            case CREATE -> "Ваш аккаунт был успешно создан.";
        };

        emailSender.send(
                event.email(),
                "Уведомление",
                text
        );
    }
}
