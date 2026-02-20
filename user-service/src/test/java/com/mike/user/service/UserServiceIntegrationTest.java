package com.mike.user.service;

import com.mike.user.domain.OutboxEventTopic;
import com.mike.user.domain.User;
import com.mike.user.dto.UserRegisteredEvent;
import com.mike.user.exception.UserAlreadyExistsException;
import com.mike.user.outbox.OutboxEvent;
import com.mike.user.outbox.OutboxPublisher;
import com.mike.user.outbox.OutboxRepository;
import com.mike.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private OutboxPublisher publisher;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    void createUser_persistsUserInDatabase() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent request = new UserRegisteredEvent(userId, "mike", "mike@mail.com");

        userService.createUser(request);

        User saved = userRepository.findById(userId).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("mike@mail.com");
        assertThat(saved.getUsername()).isEqualTo("mike");
        assertThat(saved.getId()).isEqualTo(userId);
    }

    @Test
    void createUser_duplicateEmail_shouldFail() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UserRegisteredEvent request1 = new UserRegisteredEvent(userId1, "mike", "mike@mail.com");
        UserRegisteredEvent request2 = new UserRegisteredEvent(userId2, "mike2", "mike@mail.com");

        userService.createUser(request1);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request2)
        );
    }

    @Test
    void block_shouldBlockUser() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent request = new UserRegisteredEvent(userId, "mike", "mike@mail.com");

        userService.createUser(request);
        userService.block(userId);

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.isBlocked()).isTrue();
    }

    @Test
    void createUser_shouldInsertOutboxEvent() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent request = new UserRegisteredEvent(userId, "mike", "mike@mail.com");

        userService.createUser(request);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<OutboxEvent> events = outboxRepository.findAll();
                    assertThat(events)
                            .anyMatch(e ->
                                    e.getType().equals(OutboxEventTopic.USER_CREATED.toString())
                                            && !e.isPublished()
                                            && e.getAggregateId().equals(userId.toString())
                            );
                });
    }

    @Test
    void outboxPublisher_shouldSendToKafka_andMarkPublished() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent request = new UserRegisteredEvent(userId, "mike", "mike@mail.com");

        userService.createUser(request);
        publisher.publish();

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OutboxEvent event = outboxRepository.findAll().get(0);
                    assertThat(event.isPublished()).isTrue();
                    assertThat(event.getRetryCount()).isEqualTo(0);
                });
    }

    @Test
    void concurrentCreateUser_withSameEmail_onlyOneCreates() throws Exception {
        String email = "mike@mail.com";
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<Callable<Void>> tasks = IntStream.range(0, 5)
                .mapToObj(i -> (Callable<Void>) () -> {
                    try {
                        UUID userId = UUID.randomUUID();
                        UserRegisteredEvent request = new UserRegisteredEvent(userId, "user" + i, email);
                        userService.createUser(request);
                    } catch (UserAlreadyExistsException ignored) {
                    }
                    return null;
                })
                .toList();

        executor.invokeAll(tasks);

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(outboxRepository.count()).isEqualTo(1);
    }
}
