package notification_service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import notification_service.api.SendMailRequest;
import notification_service.kafka.UserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class NotificationIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", () -> StringSerializer.class.getName());
        registry.add("spring.kafka.producer.value-serializer", () -> JsonSerializer.class.getName());
    }

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"))
            .withPerMethodLifecycle(true);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Test
    void shouldSendEmailWhenKafkaEventReceived() {
        UserEvent event = new UserEvent(UserEvent.Operation.CREATE, "user@example.com");

        kafkaTemplate.send(topic, event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(1);
            assertThat(receivedMessages[0].getAllRecipients()[0].toString()).isEqualTo("user@example.com");
            assertThat(receivedMessages[0].getSubject()).isEqualTo("Уведомление");
            assertThat(receivedMessages[0].getContent().toString()).contains("Ваш аккаунт был успешно создан.");
        });
    }

    @Test
    void shouldSendEmailViaRestApi() {
        SendMailRequest request = new SendMailRequest("api-user@example.com", "Hello from API");

        restTemplate.postForEntity("/api/mail", request, Void.class);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(1);
            assertThat(receivedMessages[0].getAllRecipients()[0].toString()).isEqualTo("api-user@example.com");
            assertThat(receivedMessages[0].getContent().toString()).contains("Hello from API");
        });
    }
}
