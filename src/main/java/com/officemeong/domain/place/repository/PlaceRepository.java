package com.officemeong.domain.place.repository;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.enums.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findBySourceTypeAndSourceId(SourceType sourceType, String sourceId);

    boolean existsBySourceTypeAndSourceId(SourceType sourceType, String sourceId);

    List<Place> findByRegionAndPlaceType(Region region, PlaceType placeType);

    List<Place> findByRegion(Region region);

    // KTO/GWTO 중복 장소 감지: 같은 지역에 다른 소스로 이미 수집된 동일 이름의 장소가 있는지 확인
    Optional<Place> findFirstByRegionAndNameIgnoreCaseAndSourceTypeNot(Region region, String name, SourceType excludedSourceType);

    // 코스 생성용: 지역+유형으로 장소를 펫워크지수 내림차순 조회 (petCondition fetch join)
    @Query("SELECT DISTINCT p FROM Place p LEFT JOIN FETCH p.score s LEFT JOIN FETCH p.petCondition " +
           "WHERE p.region = :region AND p.placeType = :placeType " +
           "ORDER BY COALESCE(s.totalScore, 0) DESC")
    List<Place> findByRegionAndPlaceTypeOrderByScore(@Param("region") Region region,
                                                     @Param("placeType") PlaceType placeType);

    // 목록 조회 - 점수 내림차순
    @Query(value = "SELECT p.id FROM Place p LEFT JOIN p.score s " +
                   "WHERE (:region IS NULL OR p.region = :region) " +
                   "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                   "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "ORDER BY COALESCE(s.totalScore, 0) DESC",
           countQuery = "SELECT COUNT(p) FROM Place p " +
                        "WHERE (:region IS NULL OR p.region = :region) " +
                        "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                        "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Long> findIdsByFilterOrderByScore(@Param("region") Region region,
                                           @Param("placeType") PlaceType placeType,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    // 목록 조회 - 최신 동기화 순
    @Query(value = "SELECT p.id FROM Place p " +
                   "WHERE (:region IS NULL OR p.region = :region) " +
                   "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                   "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "ORDER BY p.lastSyncedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Place p " +
                        "WHERE (:region IS NULL OR p.region = :region) " +
                        "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                        "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Long> findIdsByFilterOrderByLatest(@Param("region") Region region,
                                            @Param("placeType") PlaceType placeType,
                                            @Param("keyword") String keyword,
                                            Pageable pageable);

    // 목록 조회 - 점수 내림차순 + 혼잡도 필터
    @Query(value = "SELECT p.id FROM Place p LEFT JOIN p.score s " +
                   "WHERE (:region IS NULL OR p.region = :region) " +
                   "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                   "AND (:minCongestionScore IS NULL OR s.congestionScore >= :minCongestionScore) " +
                   "AND (:maxCongestionScore IS NULL OR s.congestionScore <= :maxCongestionScore) " +
                   "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "ORDER BY COALESCE(s.totalScore, 0) DESC",
           countQuery = "SELECT COUNT(p) FROM Place p LEFT JOIN p.score s " +
                        "WHERE (:region IS NULL OR p.region = :region) " +
                        "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                        "AND (:minCongestionScore IS NULL OR s.congestionScore >= :minCongestionScore) " +
                        "AND (:maxCongestionScore IS NULL OR s.congestionScore <= :maxCongestionScore) " +
                        "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Long> findIdsByFilterWithCongestionOrderByScore(@Param("region") Region region,
                                                         @Param("placeType") PlaceType placeType,
                                                         @Param("minCongestionScore") Integer minCongestionScore,
                                                         @Param("maxCongestionScore") Integer maxCongestionScore,
                                                         @Param("keyword") String keyword,
                                                         Pageable pageable);

    // 목록 조회 - 최신 동기화 순 + 혼잡도 필터
    @Query(value = "SELECT p.id FROM Place p LEFT JOIN p.score s " +
                   "WHERE (:region IS NULL OR p.region = :region) " +
                   "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                   "AND (:minCongestionScore IS NULL OR s.congestionScore >= :minCongestionScore) " +
                   "AND (:maxCongestionScore IS NULL OR s.congestionScore <= :maxCongestionScore) " +
                   "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "ORDER BY p.lastSyncedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Place p LEFT JOIN p.score s " +
                        "WHERE (:region IS NULL OR p.region = :region) " +
                        "AND (:placeType IS NULL OR p.placeType = :placeType) " +
                        "AND (:minCongestionScore IS NULL OR s.congestionScore >= :minCongestionScore) " +
                        "AND (:maxCongestionScore IS NULL OR s.congestionScore <= :maxCongestionScore) " +
                        "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "     OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Long> findIdsByFilterWithCongestionOrderByLatest(@Param("region") Region region,
                                                          @Param("placeType") PlaceType placeType,
                                                          @Param("minCongestionScore") Integer minCongestionScore,
                                                          @Param("maxCongestionScore") Integer maxCongestionScore,
                                                          @Param("keyword") String keyword,
                                                          Pageable pageable);

    // ID 목록으로 상세 fetch (score + petCondition 포함)
    @Query("SELECT DISTINCT p FROM Place p " +
           "LEFT JOIN FETCH p.score " +
           "LEFT JOIN FETCH p.petCondition " +
           "WHERE p.id IN :ids")
    List<Place> findByIdsWithSummaryDetails(@Param("ids") List<Long> ids);

    // 단건 상세 fetch (모든 연관관계 포함)
    @Query("SELECT DISTINCT p FROM Place p " +
           "LEFT JOIN FETCH p.score " +
           "LEFT JOIN FETCH p.petCondition " +
           "LEFT JOIN FETCH p.operation " +
           "LEFT JOIN FETCH p.accessibility " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.id = :id")
    Optional<Place> findByIdWithAllDetails(@Param("id") Long id);
}
