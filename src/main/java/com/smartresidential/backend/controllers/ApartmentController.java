package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.apartment.ApartmentResponseDTO;
import com.smartresidential.backend.dto.apartment.CreateApartmentRequest;
import com.smartresidential.backend.dto.apartment.UpdateApartmentRequest;
import com.smartresidential.backend.services.interfaces.ApartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @PostMapping
    public ResponseEntity<ApartmentResponseDTO> createApartment(
            @RequestBody CreateApartmentRequest request
    ) {
        ApartmentResponseDTO response = apartmentService.createApartment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApartmentResponseDTO> getApartmentById(
            @PathVariable Long id
    ) {
        ApartmentResponseDTO response = apartmentService.getApartmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ApartmentResponseDTO>> getAllApartments() {
        List<ApartmentResponseDTO> response = apartmentService.getAllApartments();
        return ResponseEntity.ok(response);
    }

    // ekstra endpoint sipas service-it
    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<ApartmentResponseDTO>> getApartmentsByBuildingId(
            @PathVariable Long buildingId
    ) {
        List<ApartmentResponseDTO> response =
                apartmentService.getApartmentsByBuildingId(buildingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApartmentResponseDTO> updateApartment(
            @PathVariable Long id,
            @RequestBody UpdateApartmentRequest request
    ) {
        ApartmentResponseDTO response =
                apartmentService.updateApartment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(
            @PathVariable Long id
    ) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.noContent().build();
    }
}