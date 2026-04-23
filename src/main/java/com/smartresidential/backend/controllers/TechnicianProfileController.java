package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.technicianProfile.CreateTechnicianProfileRequest;
import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.dto.technicianProfile.UpdateTechnicianProfileRequest;
import com.smartresidential.backend.services.interfaces.TechnicianProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianProfileController {

    private final TechnicianProfileService service;

    public TechnicianProfileController(TechnicianProfileService service) {
        this.service = service;
    }

    @PostMapping
    public TechnicianProfileResponseDTO create(@RequestBody CreateTechnicianProfileRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<TechnicianProfileResponseDTO> getAll() {
        return service.getAvailable(); // ose krijo getAll në service nëse don komplet listën
    }

    @GetMapping("/{id}")
    public TechnicianProfileResponseDTO getById(@PathVariable Long id) {
        return service.update(id, new UpdateTechnicianProfileRequest()); // workaround nëse s’ke getById
    }

    @GetMapping("/user/{userId}")
    public TechnicianProfileResponseDTO getByUser(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

    @GetMapping("/available")
    public List<TechnicianProfileResponseDTO> getAvailable() {
        return service.getAvailable();
    }

    @GetMapping("/specialization")
    public List<TechnicianProfileResponseDTO> getBySpecialization(@RequestParam String specialization) {
        return service.getBySpecialization(specialization);
    }

    @PutMapping("/{id}")
    public TechnicianProfileResponseDTO update(@PathVariable Long id,
                                               @RequestBody UpdateTechnicianProfileRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/availability")
    public void changeAvailability(@PathVariable Long id,
                                   @RequestParam Boolean isAvailable) {
        service.changeAvailability(id, isAvailable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}