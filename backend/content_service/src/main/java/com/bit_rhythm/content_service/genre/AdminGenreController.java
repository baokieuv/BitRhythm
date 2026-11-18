package com.bit_rhythm.content_service.genre;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.genre.dto.GenreRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ADMIN only (SecurityConfig). Not routed by the gateway: call the service directly. */
@RestController
@RequestMapping("/api/contents/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final GenreService genreService;
    private final ResponseFactory responseFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('write:admin:genres')")
    public ResponseEntity<ApiResponseFormat<Object>> create(@Valid @RequestBody GenreRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, genreService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('write:admin:genres')")
    public ResponseEntity<ApiResponseFormat<Object>> rename(@PathVariable Long id, @Valid @RequestBody GenreRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, genreService.rename(id, request));
    }
}
