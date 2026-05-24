package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.attachment.AttachmentResponseDTO;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Attachment;
import com.smartresidential.backend.entities.AttachmentProcessingStatus;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.AttachmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttachmentProcessingService attachmentProcessingService;

    @Mock
    private AuditLogService auditLogService;

    @TempDir
    private Path tempDir;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TenantContext.clear();
    }

    @Test
    void uploadSchedulesProcessingAfterTransactionCommit() {
        AttachmentServiceImpl service = service();
        Issue issue = issue(10L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leak.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
        TenantContext.set(1L, "tenant_test", "tenant_test", null, null);
        TransactionSynchronizationManager.initSynchronization();

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment attachment = invocation.getArgument(0);
            if (attachment.getId() == null) {
                attachment.setId(3L);
            }
            return attachment;
        });

        AttachmentResponseDTO response = service.uploadAttachment(issue.getId(), file);

        assertThat(response.getProcessingStatus()).isEqualTo(AttachmentProcessingStatus.PROCESSING.name());
        verify(attachmentProcessingService, never()).processAttachmentAsync(any(), any());

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(attachmentProcessingService).processAttachmentAsync(
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.contains("pending")
        );
    }

    private AttachmentServiceImpl service() {
        return new AttachmentServiceImpl(
                attachmentRepository,
                issueRepository,
                userRepository,
                attachmentProcessingService,
                auditLogService,
                tempDir.toString()
        );
    }

    private Issue issue(Long id) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Broken sink");
        issue.setDescription("Water is leaking.");
        issue.setStatus("OPEN");
        issue.setPriority("MEDIUM");
        issue.setApartment(apartment(20L));
        return issue;
    }

    private Apartment apartment(Long id) {
        Apartment apartment = new Apartment();
        apartment.setId(id);
        apartment.setBuilding(building(1L));
        return apartment;
    }

    private Building building(Long id) {
        Building building = new Building();
        building.setId(id);
        building.setName("Building " + id);
        building.setAddress("Main Street " + id);
        return building;
    }
}
