package com.mike.transfer.repository;

import com.mike.transfer.domain.Transfer;
import com.mike.transfer.dto.AccountTurnover;
import com.mike.transfer.dto.TransferDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<Transfer, UUID> {

    @Query(value = """
            SELECT
                a.id AS accountId,
                a.currency AS currency,
                COUNT(t.id) AS operationsCount,
                COALESCE(SUM(t.amount), 0) AS turnover
            FROM accounts a
            LEFT JOIN transfers t ON t.from_account = a.id
                AND t.transaction_at BETWEEN :from AND :to
            WHERE a.id = :accountId
            GROUP BY a.id, a.currency
            """,
            nativeQuery = true)
    AccountTurnover turnover(
            @Param("accountId") UUID accountId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query(value = """
            SELECT
                t.id AS id,
                t.from_account AS fromAccount,
                t.to_account AS toAccount,
                t.amount AS amount,
                t.transaction_at AS transactionAt
            FROM transfers t
            WHERE t.from_account = :accountId
              AND t.transaction_at >= :from
            ORDER BY t.amount DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<TransferDto> topTransfers(
            @Param("accountId") UUID accountId,
            @Param("from") Instant from,
            @Param("limit") int limit
    );
}
