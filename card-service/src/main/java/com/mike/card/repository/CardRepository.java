package com.mike.card.repository;

import com.mike.card.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findAllByUserId(String userId);

    boolean existsByUserIdAndCurrency(String userId, String currency);
}
