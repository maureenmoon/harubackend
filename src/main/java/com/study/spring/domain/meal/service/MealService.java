package com.study.spring.domain.meal.service;

import com.study.spring.domain.meal.dto.MealDto;
import com.study.spring.domain.meal.entity.Food;
import com.study.spring.domain.meal.entity.Meal;
import com.study.spring.domain.meal.entity.MealType;
import com.study.spring.domain.meal.repository.MealRepository;
import com.study.spring.domain.meal.repository.FoodRepository;
import com.study.spring.domain.member.entity.Member;
import com.study.spring.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealService {
    private final MealRepository mealRepository;
    private final MemberRepository memberRepository;
    private final FoodRepository foodRepository;  // 추가!

    @Transactional
    public MealDto.Response createMeal(Long memberId, MealDto.Request request) {
        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 1. Meal 저장
        Meal meal = Meal.builder()
                .member(member)
                .mealType(request.getMealType())
                .imageUrl(request.getImageUrl())
                .memo(request.getMemo())
                .build();

        Meal savedMeal = mealRepository.save(meal);

        // 2. Food들 생성 및 연관관계 설정
        if (request.getFoods() != null && !request.getFoods().isEmpty()) {
            for (MealDto.FoodRequest foodRequest : request.getFoods()) {
                Food food = Food.builder()
                        .foodName(foodRequest.getFoodName())
                        .calories(foodRequest.getCalories())
                        .carbohydrate(foodRequest.getCarbohydrate())
                        .protein(foodRequest.getProtein())
                        .fat(foodRequest.getFat())
                        .sodium(foodRequest.getSodium())
                        .fiber(foodRequest.getFiber())
                        .build();
                
                // 양방향 연관관계 설정
                food.setMeal(savedMeal);
                savedMeal.getFoods().add(food);
            }
            
            // 다시 저장 (CASCADE로 Food들도 저장)
            savedMeal = mealRepository.save(savedMeal);
        }

        return MealDto.Response.from(savedMeal);
    }

    public MealDto.Response getMeal(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "식사 기록을 찾을 수 없습니다."));
        
        return MealDto.Response.from(meal);
    }

    public List<MealDto.Response> getAllMeals() {
        return mealRepository.findAll().stream()
                .map(MealDto.Response::from)
                .collect(Collectors.toList());
    }

    public List<MealDto.Response> getMealsByMemberId(Long memberId) {
        // 회원 존재 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        return mealRepository.findByMemberId(memberId).stream()
                .map(MealDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MealDto.Response updateMeal(Long id, MealDto.Request request) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "식사 기록을 찾을 수 없습니다."));

        // 1. 기존 foods 모두 삭제
        meal.getFoods().clear();
        foodRepository.deleteAllByMealId(meal.getId()); // FoodRepository에 해당 메서드 필요

        // 2. 요청의 foods로 새 Food 리스트 생성 및 할당
        List<Food> newFoods = new ArrayList<>();
        if (request.getFoods() != null) {
            for (MealDto.FoodRequest foodReq : request.getFoods()) {
                Food food = Food.builder()
                        .foodName(foodReq.getFoodName())
                        .calories(foodReq.getCalories())
                        .carbohydrate(foodReq.getCarbohydrate())
                        .protein(foodReq.getProtein())
                        .fat(foodReq.getFat())
                        .sodium(foodReq.getSodium())
                        .fiber(foodReq.getFiber())
                        .meal(meal)
                        .build();
                newFoods.add(food);
            }
        }
        meal.getFoods().addAll(newFoods);

        // 3. Meal의 나머지 필드 업데이트
        meal.setMealType(request.getMealType());
        meal.setImageUrl(request.getImageUrl());
        meal.setMemo(request.getMemo());
        meal.setUpdatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt().atStartOfDay() : LocalDate.now().atStartOfDay());

        Meal savedMeal = mealRepository.save(meal);
        return MealDto.Response.from(savedMeal);
    }

    @Transactional
    public void deleteMeal(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "식사 기록을 찾을 수 없습니다."));

        mealRepository.delete(meal);
    }

    // 추가 편의 메서드들
    public List<MealDto.Response> getMealsByMemberIdAndMealType(Long memberId, MealType mealType) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // TODO: Repository에 findByMemberIdAndMealType 메서드 추가 필요
        return mealRepository.findByMemberId(memberId).stream()
                .filter(meal -> meal.getMealType() == mealType)
                .map(MealDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMealImage(Long id, String imageUrl) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "식사 기록을 찾을 수 없습니다."));

        Meal updatedMeal = Meal.builder()
                .id(meal.getId())
                .member(meal.getMember())
                .mealType(meal.getMealType())
                .imageUrl(imageUrl)  // 이미지만 업데이트
                .memo(meal.getMemo())
                .foods(meal.getFoods())
                .createdAt(meal.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        mealRepository.save(updatedMeal);
    }

    public List<MealDto.Response> getMealsByUpdatedDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        return mealRepository.findAll().stream()
                .filter(meal -> {
                    LocalDateTime updatedAt = meal.getUpdatedAt();
                    return updatedAt != null && !updatedAt.isBefore(start) && !updatedAt.isAfter(end);
                })
                .map(MealDto.Response::from)
                .collect(Collectors.toList());
    }
} 