package aston.notification_service.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import aston.notification_service.mail.EmailSender;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final EmailSender emailSender;

    @Operation(summary = "Отправить email пользователю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Письмо успешно отправлено"),
            @ApiResponse(responseCode = "400", description = "Ошибка в параметрах запроса")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void send(@RequestBody SendMailRequest request) {
        emailSender.send(request.email(), "Уведомление", request.text());
    }
}
