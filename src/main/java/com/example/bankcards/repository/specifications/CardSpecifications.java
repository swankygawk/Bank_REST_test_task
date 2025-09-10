package com.example.bankcards.repository.specifications;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CardSpecifications {
    public static Specification<Card> hasHolderId(UUID holderId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("holder").get("id"), holderId);
    }

    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Card> numberEndsWith(String lastFourDigits) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("number"), "%" + lastFourDigits);
    }
}
