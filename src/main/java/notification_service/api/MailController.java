package notification_service.api;

import lombok.RequiredArgsConstructor;
import notification_service.mail.EmailSender;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final EmailSender emailSender;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@RequestBody SendMailRequest request) {
        emailSender.send(request.email(), "Уведомление", request.text());
    }
}
