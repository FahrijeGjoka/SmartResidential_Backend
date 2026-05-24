package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.residentProfile.CreateResidentProfileRequest;
import com.smartresidential.backend.dto.residentProfile.ResidentProfileResponseDTO;
import com.smartresidential.backend.dto.residentProfile.UpdateResidentProfileRequest;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.ResidentProfile;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.repositories.ApartmentRepository;
import com.smartresidential.backend.repositories.ResidentProfileRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResidentProfileServiceImplTest {

    @Mock
    private ResidentProfileRepository residentProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogService auditLogService;

    private ResidentProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ResidentProfileServiceImpl(
                residentProfileRepository,
                userRepository,
                apartmentRepository,
                roleRepository,
                auditLogService
        );
    }

    @Test
    void creatingResidentProfileWithResidentRoleSucceeds() {
        User resident = user(10L, 4L, true);
        Apartment apartment = apartment(20L);
        LocalDateTime movedInAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        CreateResidentProfileRequest request = createRequest(resident.getId(), apartment.getId(), movedInAt);

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(roleRepository.findById(resident.getRoleId())).thenReturn(Optional.of(role(4L, "ROLE_RESIDENT")));
        when(residentProfileRepository.existsByUserId(resident.getId())).thenReturn(false);
        when(apartmentRepository.findById(apartment.getId())).thenReturn(Optional.of(apartment));
        when(residentProfileRepository.save(any(ResidentProfile.class))).thenAnswer(invocation -> {
            ResidentProfile profile = invocation.getArgument(0);
            profile.setId(30L);
            return profile;
        });

        ResidentProfileResponseDTO response = service.createResidentProfile(request);

        ArgumentCaptor<ResidentProfile> captor = ArgumentCaptor.forClass(ResidentProfile.class);
        verify(residentProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(resident);
        assertThat(captor.getValue().getApartment()).isSameAs(apartment);
        assertThat(response.getUserId()).isEqualTo(resident.getId());
        assertThat(response.getApartmentId()).isEqualTo(apartment.getId());
    }

    @Test
    void creatingResidentProfileWithStaffRoleFails() {
        assertWrongRoleCreateFails("ROLE_STAFF");
    }

    @Test
    void creatingResidentProfileWithTechnicianRoleFails() {
        assertWrongRoleCreateFails("ROLE_TECHNICIAN");
    }

    @Test
    void creatingResidentProfileWithInactiveResidentFails() {
        User resident = user(10L, 4L, false);
        CreateResidentProfileRequest request = createRequest(resident.getId(), 20L, null);

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));

        assertThatThrownBy(() -> service.createResidentProfile(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resident user must be active.");

        verify(residentProfileRepository, never()).save(any());
    }

    @Test
    void updatingResidentProfileToNonResidentUserFails() {
        ResidentProfile existing = residentProfile(100L, user(10L, 4L, true), apartment(20L));
        User staff = user(11L, 2L, true);
        UpdateResidentProfileRequest request = updateRequest(staff.getId(), 20L, null);

        when(residentProfileRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(roleRepository.findById(staff.getRoleId())).thenReturn(Optional.of(role(2L, "ROLE_STAFF")));

        assertThatThrownBy(() -> service.updateResidentProfile(existing.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only users with ROLE_RESIDENT can be linked as residents.");

        verify(residentProfileRepository, never()).save(any());
    }

    @Test
    void duplicateResidentProfileForSameUserFailsOnCreate() {
        User resident = user(10L, 4L, true);
        CreateResidentProfileRequest request = createRequest(resident.getId(), 20L, null);

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(roleRepository.findById(resident.getRoleId())).thenReturn(Optional.of(role(4L, "ROLE_RESIDENT")));
        when(residentProfileRepository.existsByUserId(resident.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.createResidentProfile(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("This user is already linked as a resident.");

        verify(residentProfileRepository, never()).save(any());
    }

    @Test
    void duplicateResidentProfileForSameUserFailsOnUpdate() {
        ResidentProfile existing = residentProfile(100L, user(10L, 4L, true), apartment(20L));
        User otherResident = user(11L, 4L, true);
        UpdateResidentProfileRequest request = updateRequest(otherResident.getId(), 20L, null);

        when(residentProfileRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(userRepository.findById(otherResident.getId())).thenReturn(Optional.of(otherResident));
        when(roleRepository.findById(otherResident.getRoleId())).thenReturn(Optional.of(role(4L, "ROLE_RESIDENT")));
        when(residentProfileRepository.existsByUserIdAndIdNot(otherResident.getId(), existing.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.updateResidentProfile(existing.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("This user is already linked as a resident.");

        verify(residentProfileRepository, never()).save(any());
    }

    @Test
    void duplicateResidentProfileConstraintViolationDoesNotReturnInternalError() {
        User resident = user(10L, 4L, true);
        Apartment apartment = apartment(20L);
        CreateResidentProfileRequest request = createRequest(resident.getId(), apartment.getId(), null);

        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        when(roleRepository.findById(resident.getRoleId())).thenReturn(Optional.of(role(4L, "ROLE_RESIDENT")));
        when(residentProfileRepository.existsByUserId(resident.getId())).thenReturn(false);
        when(apartmentRepository.findById(apartment.getId())).thenReturn(Optional.of(apartment));
        when(residentProfileRepository.save(any(ResidentProfile.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate resident user"));

        assertThatThrownBy(() -> service.createResidentProfile(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("This user is already linked as a resident.");
    }

    private void assertWrongRoleCreateFails(String roleName) {
        User user = user(10L, 2L, true);
        CreateResidentProfileRequest request = createRequest(user.getId(), 20L, null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findById(user.getRoleId())).thenReturn(Optional.of(role(2L, roleName)));

        assertThatThrownBy(() -> service.createResidentProfile(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only users with ROLE_RESIDENT can be linked as residents.");

        verify(residentProfileRepository, never()).save(any());
    }

    private CreateResidentProfileRequest createRequest(Long userId, Long apartmentId, LocalDateTime movedInAt) {
        CreateResidentProfileRequest request = new CreateResidentProfileRequest();
        request.setUserId(userId);
        request.setApartmentId(apartmentId);
        request.setMovedInAt(movedInAt);
        return request;
    }

    private UpdateResidentProfileRequest updateRequest(Long userId, Long apartmentId, LocalDateTime movedInAt) {
        UpdateResidentProfileRequest request = new UpdateResidentProfileRequest();
        request.setUserId(userId);
        request.setApartmentId(apartmentId);
        request.setMovedInAt(movedInAt);
        return request;
    }

    private ResidentProfile residentProfile(Long id, User user, Apartment apartment) {
        ResidentProfile profile = new ResidentProfile();
        profile.setId(id);
        profile.setUser(user);
        profile.setApartment(apartment);
        return profile;
    }

    private User user(Long id, Long roleId, Boolean active) {
        User user = new User();
        user.setId(id);
        user.setRoleId(roleId);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("password");
        user.setIsActive(active);
        return user;
    }

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private Apartment apartment(Long id) {
        Apartment apartment = new Apartment();
        apartment.setId(id);
        return apartment;
    }
}
