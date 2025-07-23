package com.study.spring.domain.meal.repository;

import com.study.spring.domain.meal.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByMemberId(Long memberId);
} 