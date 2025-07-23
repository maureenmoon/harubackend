package com.study.spring.domain.meal.controller;

import com.study.spring.domain.meal.dto.MealDto;
import com.study.spring.domain.meal.entity.Meal;
import com.study.spring.domain.meal.entity.MealType;
import com.study.spring.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {
    private final MealService mealService;

    // 식사 기록 생성
    @PostMapping
    public ResponseEntity<MealDto.Response> createMeal(
            @RequestParam("memberId") Long memberId,  // 이름 명시
            @RequestBody MealDto.Request request) {
        return ResponseEntity.ok(mealService.createMeal(memberId, request));
    }

    // 특정 식사 기록 조회
    @GetMapping("/{id}")
    public ResponseEntity<MealDto.Response> getMeal(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mealService.getMeal(id));
    }

    // 전체 식사 기록 조회
    @GetMapping
    public ResponseEntity<List<MealDto.Response>> getAllMeals() {
        return ResponseEntity.ok(mealService.getAllMeals());
    }

    // 회원별 식사 기록 조회
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MealDto.Response>> getMealsByMemberId(
            @PathVariable("memberId") Long memberId) {  // 이름 명시
        return ResponseEntity.ok(mealService.getMealsByMemberId(memberId));
    }

    // 회원별 + 식사타입별 조회
    @GetMapping("/member/{memberId}/type/{mealType}")
    public ResponseEntity<List<MealDto.Response>> getMealsByMemberIdAndMealType(
            @PathVariable("memberId") Long memberId,
            @PathVariable("mealType") MealType mealType) {
        return ResponseEntity.ok(mealService.getMealsByMemberIdAndMealType(memberId, mealType));
    }

    // updatedAt 날짜로 식사 기록 조회
    @GetMapping("/updated-date")
    public ResponseEntity<List<MealDto.Response>> getMealsByUpdatedDate(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return ResponseEntity.ok(mealService.getMealsByUpdatedDate(date));
    }

    // 식사 기록 수정
    @PutMapping("/{id}")
    public ResponseEntity<MealDto.Response> updateMeal(
            @PathVariable("id") Long id,
            @RequestBody MealDto.Request request) {
        return ResponseEntity.ok(mealService.updateMeal(id, request));
    }

    // 식사 이미지만 수정
    @PatchMapping("/{id}/image")
    public ResponseEntity<Void> updateMealImage(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        mealService.updateMealImage(id, imageUrl);
        return ResponseEntity.noContent().build();
    }

    // 식사 기록 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable("id") Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }

    // 식사 이미지 저장
    // @PostMapping("/{id}/")
    // public void testCreate(@ModelAttribute MealDto.Request request) {
    //     mealService.uploadMealImage(request);
    // }
} 