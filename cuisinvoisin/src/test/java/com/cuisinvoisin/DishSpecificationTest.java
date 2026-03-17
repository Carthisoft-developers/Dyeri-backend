// com/cuisinvoisin/DishSpecificationTest.java
package com.cuisinvoisin;

import com.cuisinvoisin.application.bean.request.DishFilterRequest;
import com.cuisinvoisin.domain.entities.*;
import com.cuisinvoisin.domain.repositories.*;
import com.cuisinvoisin.infrastructure.persistence.specifications.DishSpecification;
import com.cuisinvoisin.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DishSpecificationTest {

    @Autowired TestEntityManager em;
    @Autowired DishRepository dishRepository;

    Cook cook1, cook2;
    FoodCategory catTrad, catItalien;
    Dish couscous, pizza, pasta;

    @BeforeEach
    void setUp() {
        catTrad = em.persist(FoodCategory.builder().name("Traditionnel").icon("🍲").build());
        catItalien = em.persist(FoodCategory.builder().name("Italien").icon("🍕").build());

        cook1 = em.persist(Cook.builder()
                .name("Chef Mariem").email("mariem@test.com")
                .passwordHash("hash").phone("+216 1").role(UserRole.COOK)
                .isActive(true).isAvailable(true).build());

        cook2 = em.persist(Cook.builder()
                .name("Chef Karim").email("karim@test.com")
                .passwordHash("hash").phone("+216 2").role(UserRole.COOK)
                .isActive(true).isAvailable(true).build());

        couscous = em.persist(Dish.builder()
                .cook(cook1).category(catTrad).name("Couscous Royal")
                .description("Traditional couscous").price(new BigDecimal("15.000"))
                .available(true).portions(2).prepTimeMin(45).build());

        pizza = em.persist(Dish.builder()
                .cook(cook2).category(catItalien).name("Pizza Margherita")
                .description("Classic pizza").price(new BigDecimal("22.000"))
                .available(true).portions(1).prepTimeMin(20).build());

        pasta = em.persist(Dish.builder()
                .cook(cook2).category(catItalien).name("Pasta Carbonara")
                .description("Italian pasta").price(new BigDecimal("18.000"))
                .available(false).portions(1).prepTimeMin(15).build());

        em.flush();
    }

    @Test
    void filter_byCookId_shouldReturnOnlyThatCooksDishes() {
        DishFilterRequest filter = new DishFilterRequest(cook1.getId(), null, null, null, null, null);
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Couscous Royal");
    }

    @Test
    void filter_byCategoryId_shouldReturnItalianDishes() {
        DishFilterRequest filter = new DishFilterRequest(null, catItalien.getId(), null, null, null, null);
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        List<String> names = result.getContent().stream().map(Dish::getName).toList();
        assertThat(names).containsExactlyInAnyOrder("Pizza Margherita", "Pasta Carbonara");
    }

    @Test
    void filter_byPriceRange_shouldReturnMatchingDishes() {
        DishFilterRequest filter = new DishFilterRequest(
                null, null, new BigDecimal("14.000"), new BigDecimal("20.000"), null, null);
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        result.getContent().forEach(d ->
                assertThat(d.getPrice()).isBetween(new BigDecimal("14.000"), new BigDecimal("20.000")));
    }

    @Test
    void filter_byAvailableTrue_shouldExcludeUnavailableDishes() {
        DishFilterRequest filter = new DishFilterRequest(null, null, null, null, true, null);
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(Dish::isAvailable);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void filter_byCookAndAvailable_shouldReturnCookAvailableDishesOnly() {
        DishFilterRequest filter = new DishFilterRequest(cook2.getId(), null, null, null, true, null);
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Pizza Margherita");
    }

    @Test
    void filter_byQueryString_shouldReturnMatchingDishes() {
        DishFilterRequest filter = new DishFilterRequest(null, null, null, null, null, "pizza");
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Pizza Margherita");
    }

    @Test
    void filter_noFilters_shouldReturnAllDishes() {
        DishFilterRequest filter = new DishFilterRequest(null, null, null, null, null, null);
        Page<Dish> result = dishRepository.findAll(DishSpecification.from(filter), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}
