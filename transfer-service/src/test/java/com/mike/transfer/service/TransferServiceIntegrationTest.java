package com.mike.transfer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.transfer.domain.Account;
import com.mike.transfer.domain.Currency;
import com.mike.transfer.domain.IdempotentRequest;
import com.mike.transfer.dto.TransferRequest;
import com.mike.transfer.exception.IdempotencyConflictException;
import com.mike.transfer.repository.AccountRepository;
import com.mike.transfer.repository.IdempotentRepository;
import com.mike.transfer.repository.TransferRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
public class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private IdempotentRepository idempotentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardResolverService cardResolverService;

    private UUID accountFromId;
    private UUID accountToId;
    private UUID cardFromUuid;
    private UUID cardToUuid;
    private String cardFromId;
    private String cardToId;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();
        idempotentRepository.deleteAll();

        accountFromId = UUID.randomUUID();
        accountToId = UUID.randomUUID();
        Account from = new Account(accountFromId, UUID.randomUUID(), Currency.USD);
        from.credit(new BigDecimal("1000"));
        Account to = new Account(accountToId, UUID.randomUUID(), Currency.USD);
        to.credit(new BigDecimal("500"));
        accountRepository.save(from);
        accountRepository.save(to);

        cardFromUuid = UUID.randomUUID();
        cardToUuid = UUID.randomUUID();
        cardFromId = cardFromUuid.toString();
        cardToId = cardToUuid.toString();

        when(cardResolverService.getAccountId(cardFromUuid)).thenReturn(accountFromId);
        when(cardResolverService.getAccountId(cardToUuid)).thenReturn(accountToId);
    }

    @Test
    void transfer_withIdempotencyKey_shouldBeIdempotent() {
        // given
        String idempotencyKey = "unique-key-123";
        TransferRequest request = new TransferRequest(cardFromId, cardToId, new BigDecimal("100"));

        // when
        UUID firstTransferId = transferService.transfer(request, idempotencyKey);

        // then
        Account fromAfterFirst = accountRepository.findById(accountFromId).orElseThrow();
        Account toAfterFirst = accountRepository.findById(accountToId).orElseThrow();
        assertThat(fromAfterFirst.getBalance()).isEqualByComparingTo("900");
        assertThat(toAfterFirst.getBalance()).isEqualByComparingTo("600");

        Optional<IdempotentRequest> saved = idempotentRepository.findById(idempotencyKey);
        assertThat(saved).isPresent();
        assertThat(saved.get().getEntityId()).isEqualTo(firstTransferId);
        assertThat(saved.get().getRequestHash()).isNotBlank();

        // when
        UUID secondTransferId = transferService.transfer(request, idempotencyKey);

        // then
        assertThat(secondTransferId).isEqualTo(firstTransferId);

        Account fromAfterSecond = accountRepository.findById(accountFromId).orElseThrow();
        Account toAfterSecond = accountRepository.findById(accountToId).orElseThrow();
        assertThat(fromAfterSecond.getBalance()).isEqualByComparingTo("900");
        assertThat(toAfterSecond.getBalance()).isEqualByComparingTo("600");
    }

    @Test
    void transfer_withSameKeyButDifferentRequest_shouldThrowConflict() {
        // given
        String idempotencyKey = "conflict-key";
        TransferRequest firstRequest = new TransferRequest(cardFromId, cardToId, new BigDecimal("100"));

        // when
        transferService.transfer(firstRequest, idempotencyKey);

        // then
        TransferRequest secondRequest = new TransferRequest(cardFromId, cardToId, new BigDecimal("200"));
        assertThrows(IdempotencyConflictException.class, () ->
                transferService.transfer(secondRequest, idempotencyKey)
        );
    }

    @Test
    void transfer_withoutIdempotencyKey_shouldExecuteEachTime() {
        // given
        TransferRequest request = new TransferRequest(cardFromId, cardToId, new BigDecimal("50"));

        // when
        UUID firstId = transferService.transfer(request, null);

        // then
        Account fromAfterFirst = accountRepository.findById(accountFromId).orElseThrow();
        Account toAfterFirst = accountRepository.findById(accountToId).orElseThrow();
        assertThat(fromAfterFirst.getBalance()).isEqualByComparingTo("950");
        assertThat(toAfterFirst.getBalance()).isEqualByComparingTo("550");

        // when
        UUID secondId = transferService.transfer(request, null);

        // then
        Account fromAfterSecond = accountRepository.findById(accountFromId).orElseThrow();
        Account toAfterSecond = accountRepository.findById(accountToId).orElseThrow();
        assertThat(fromAfterSecond.getBalance()).isEqualByComparingTo("900");
        assertThat(toAfterSecond.getBalance()).isEqualByComparingTo("600");
        assertThat(secondId).isNotEqualTo(firstId);
    }

    @Test
    void transfer_withIdempotencyKey_shouldStoreResultCorrectly() {
        // given
        String key = "store-result-key";
        TransferRequest request = new TransferRequest(cardFromId, cardToId, new BigDecimal("75"));

        // when
        UUID transferId = transferService.transfer(request, key);

        // then
        IdempotentRequest ir = idempotentRepository.findById(key).orElseThrow();
        String expectedHash = computeHash(request, "TRANSFER");
        assertThat(ir.getRequestHash()).isEqualTo(expectedHash);
    }

    private String computeHash(Object request, String operationType) {
        try {
            Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<>() {
            });
            map.put("operationType", operationType);
            String json = objectMapper.writeValueAsString(map);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}