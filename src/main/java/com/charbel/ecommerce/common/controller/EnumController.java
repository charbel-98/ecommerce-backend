package com.charbel.ecommerce.common.controller;

import com.charbel.ecommerce.common.dto.EnumResponse;
import com.charbel.ecommerce.common.enums.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enums")
@Slf4j
@Tag(name = "Enums", description = "Enum values for frontend")
public class EnumController {

    @GetMapping("/genders")
    @Operation(summary = "Get all gender types", description = "Returns all available gender types")
    public ResponseEntity<List<EnumResponse>> getGenders() {
        List<EnumResponse> genders = Arrays.stream(GenderType.values())
                .map(g -> new EnumResponse(g.name(), g.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(genders);
    }

    @GetMapping("/sizes")
    @Operation(summary = "Get all size types", description = "Returns all available size types")
    public ResponseEntity<List<EnumResponse>> getSizes() {
        List<EnumResponse> sizes = Arrays.stream(SizeType.values())
                .map(s -> new EnumResponse(s.name(), s.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(sizes);
    }

    @GetMapping("/colors")
    @Operation(summary = "Get all color families", description = "Returns all available color families with hex codes")
    public ResponseEntity<List<Map<String, String>>> getColors() {
        List<Map<String, String>> colors = Arrays.stream(ColorFamily.values())
                .map(c -> Map.of(
                        "value", c.name(),
                        "displayName", c.getDisplayName(),
                        "hexCode", c.getHexCode()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(colors);
    }

    @GetMapping("/materials")
    @Operation(summary = "Get all material types", description = "Returns all available material types")
    public ResponseEntity<List<EnumResponse>> getMaterials() {
        List<EnumResponse> materials = Arrays.stream(MaterialType.values())
                .map(m -> new EnumResponse(m.name(), m.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/seasons")
    @Operation(summary = "Get all season types", description = "Returns all available season types")
    public ResponseEntity<List<EnumResponse>> getSeasons() {
        List<EnumResponse> seasons = Arrays.stream(SeasonType.values())
                .map(s -> new EnumResponse(s.name(), s.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(seasons);
    }

    @GetMapping("/occasions")
    @Operation(summary = "Get all occasion types", description = "Returns all available occasion types")
    public ResponseEntity<List<EnumResponse>> getOccasions() {
        List<EnumResponse> occasions = Arrays.stream(OccasionType.values())
                .map(o -> new EnumResponse(o.name(), o.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(occasions);
    }

    @GetMapping("/fits")
    @Operation(summary = "Get all fit types", description = "Returns all available fit types")
    public ResponseEntity<List<EnumResponse>> getFits() {
        List<EnumResponse> fits = Arrays.stream(FitType.values())
                .map(f -> new EnumResponse(f.name(), f.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(fits);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all enum values", description = "Returns all enum values in a single response")
    public ResponseEntity<Map<String, Object>> getAllEnums() {
        Map<String, Object> allEnums = Map.of(
                "genders", Arrays.stream(GenderType.values())
                        .map(g -> new EnumResponse(g.name(), g.getDisplayName()))
                        .collect(Collectors.toList()),
                "sizes", Arrays.stream(SizeType.values())
                        .map(s -> new EnumResponse(s.name(), s.getDisplayName()))
                        .collect(Collectors.toList()),
                "colors", Arrays.stream(ColorFamily.values())
                        .map(c -> Map.of(
                                "value", c.name(),
                                "displayName", c.getDisplayName(),
                                "hexCode", c.getHexCode()
                        ))
                        .collect(Collectors.toList()),
                "materials", Arrays.stream(MaterialType.values())
                        .map(m -> new EnumResponse(m.name(), m.getDisplayName()))
                        .collect(Collectors.toList()),
                "seasons", Arrays.stream(SeasonType.values())
                        .map(s -> new EnumResponse(s.name(), s.getDisplayName()))
                        .collect(Collectors.toList()),
                "occasions", Arrays.stream(OccasionType.values())
                        .map(o -> new EnumResponse(o.name(), o.getDisplayName()))
                        .collect(Collectors.toList()),
                "fits", Arrays.stream(FitType.values())
                        .map(f -> new EnumResponse(f.name(), f.getDisplayName()))
                        .collect(Collectors.toList())
        );
        return ResponseEntity.ok(allEnums);
    }
}