package com.officemeong.api.dog;

import com.officemeong.common.dto.ApiResponse;
import com.officemeong.domain.dog.dto.DogRequest;
import com.officemeong.domain.dog.dto.DogResponse;
import com.officemeong.domain.dog.service.DogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dogs")
@RequiredArgsConstructor
public class DogController {

    private final DogService dogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DogResponse>>> getMyDogs(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(dogService.getMyDogs(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DogResponse>> addDog(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DogRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dogService.addDog(userId, request)));
    }

    @PutMapping("/{dogId}")
    public ResponseEntity<ApiResponse<DogResponse>> updateDog(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dogId,
            @Valid @RequestBody DogRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dogService.updateDog(userId, dogId, request)));
    }

    @DeleteMapping("/{dogId}")
    public ResponseEntity<ApiResponse<Void>> deleteDog(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long dogId) {
        dogService.deleteDog(userId, dogId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
