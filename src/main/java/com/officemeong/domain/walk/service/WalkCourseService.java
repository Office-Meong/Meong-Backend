package com.officemeong.domain.walk.service;

import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.walk.dto.CourseRecommendResponse;
import com.officemeong.domain.walk.entity.WalkCourse;
import com.officemeong.domain.walk.repository.WalkCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseService {

    private static final double SEARCH_RADIUS_KM = 30.0;
    private static final int MAX_RESULTS = 10;

    private final WalkCourseRepository walkCourseRepository;
    private final DogRepository dogRepository;

    public List<CourseRecommendResponse> recommend(Double lat, Double lng, Long userId, Long dogId) {
        Double maxCourseKm = resolveMaxCourseKm(userId, dogId);

        return walkCourseRepository.findAll().stream()
                .filter(c -> c.getStartLatitude() != null && c.getStartLongitude() != null)
                .map(c -> {
                    double dist = haversineKm(lat, lng,
                            c.getStartLatitude().doubleValue(),
                            c.getStartLongitude().doubleValue());
                    return new CourseWithDistance(c, dist);
                })
                .filter(cd -> cd.distance <= SEARCH_RADIUS_KM)
                .filter(cd -> maxCourseKm == null
                        || cd.course.getDistanceKm() == null
                        || cd.course.getDistanceKm().doubleValue() <= maxCourseKm)
                .sorted(Comparator.comparingDouble(cd -> cd.distance))
                .limit(MAX_RESULTS)
                .map(cd -> CourseRecommendResponse.from(cd.course,
                        BigDecimal.valueOf(cd.distance).setScale(2, RoundingMode.HALF_UP).doubleValue()))
                .toList();
    }

    private Double resolveMaxCourseKm(Long userId, Long dogId) {
        if (dogId == null) return null;
        Dog dog = dogRepository.findByIdAndUserId(dogId, userId)
                .orElseThrow(() -> new NoSuchElementException("반려견을 찾을 수 없습니다. id=" + dogId));
        BigDecimal weight = dog.getWeightKg();
        if (weight == null) return null;
        double kg = weight.doubleValue();
        if (kg < 10.0) return 3.0;
        if (kg < 25.0) return 6.0;
        return null;
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private record CourseWithDistance(WalkCourse course, double distance) {}
}
