package com.smartresidential.backend.jobs;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.cache.TenantCacheEvictor;
import com.smartresidential.backend.entities.AIClassificationStatus;
import com.smartresidential.backend.entities.Apartment;
import com.smartresidential.backend.entities.Building;
import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.IssueCategory;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.IssueAssignmentRepository;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.IssueCategoryRepository;
import com.smartresidential.backend.repositories.MaintenanceRequestRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TechnicianProfileRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import com.smartresidential.backend.services.interfaces.IssueCategoryMatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueAutoClassificationJobTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueAssignmentRepository issueAssignmentRepository;

        @Mock
    private IssueCategoryRepository issueCategoryRepository;

@Mock
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Mock
    private TechnicianProfileRepository technicianProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueCategoryMatcherService issueCategoryMatcherService;

    @Mock
    private NotificationJob notificationJob;

    @Mock
    private TenantCacheEvictor tenantCacheEvictor;

    @Mock
    private AuditLogService auditLogService;

    private IssueAutoClassificationJob job;

    @BeforeEach
    void setUp() {
        job = new IssueAutoClassificationJob(
                issueRepository,
                issueAssignmentRepository,
                issueCategoryRepository,
                maintenanceRequestRepository,
                technicianProfileRepository,
                roleRepository,
                userRepository,
                issueCategoryMatcherService,
                notificationJob,
                tenantCacheEvictor,
                auditLogService
        );
    }

    @Test
    void successfulClassificationUpdatesStatusToCompleted() {
        Issue issue = issue(10L);
        IssueCategory category = category(20L, "Plumbing");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(issueCategoryMatcherService.matchCategory(issue.getTitle(), issue.getDescription()))
                .thenReturn(Optional.of(new IssueCategoryMatch(category, 0.91, "Matched plumbing.")));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        job.classifyAndAssign(issue.getId(), 1L);

        assertThat(issue.getCategory()).isSameAs(category);
        assertThat(issue.getAiClassificationStatus()).isEqualTo(AIClassificationStatus.COMPLETED);
        assertThat(issue.getAiCategoryConfidence()).isEqualTo(0.91);
        assertThat(issue.getAiCategoryReason()).isEqualTo("Matched plumbing.");
    }

    @Test
    void noUsableClassificationAssignsGeneralMaintenanceFallback() {
        Issue issue = issue(10L);
        IssueCategory fallback = category(99L, "General maintenance");
        fallback.setRequiredSpecialization("General maintenance");

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(issueCategoryMatcherService.matchCategory(issue.getTitle(), issue.getDescription()))
                .thenReturn(Optional.empty());
        when(issueCategoryRepository.findByName("General maintenance")).thenReturn(Optional.of(fallback));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        job.classifyAndAssign(issue.getId(), 1L);

        assertThat(issue.getCategory()).isSameAs(fallback);
        assertThat(issue.getAiClassificationStatus()).isEqualTo(AIClassificationStatus.COMPLETED);
        assertThat(issue.getAiCategoryConfidence()).isNull();
        assertThat(issue.getAiCategoryReason())
                .isEqualTo("No confident AI category matched; assigned fallback category: General maintenance.");
    }

    @Test
    void classificationFailureUpdatesStatusToFailedAndStoresReason() {
        Issue issue = issue(10L);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(issueCategoryMatcherService.matchCategory(issue.getTitle(), issue.getDescription()))
                .thenThrow(new RuntimeException("Classifier unavailable"));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        job.classifyAndAssign(issue.getId(), 1L);

        assertThat(issue.getCategory()).isNull();
        assertThat(issue.getAiClassificationStatus()).isEqualTo(AIClassificationStatus.FAILED);
        assertThat(issue.getAiCategoryReason()).isEqualTo("Classifier unavailable");
    }

    @Test
    void alreadyCategorizedIssueIsNotReclassified() {
        Issue issue = issue(10L);
        issue.setCategory(category(20L, "Plumbing"));

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

        job.classifyAndAssign(issue.getId(), 1L);

        verify(issueCategoryMatcherService, never()).matchCategory(any(), any());
    }

    private Issue issue(Long id) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Broken sink");
        issue.setDescription("Water is leaking.");
        issue.setStatus("OPEN");
        issue.setPriority("MEDIUM");
        issue.setApartment(apartment(30L));
        issue.setCreatedBy(user(40L));
        issue.setAiClassificationStatus(AIClassificationStatus.PENDING);
        return issue;
    }

    private IssueCategory category(Long id, String name) {
        IssueCategory category = new IssueCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(name + " issues");
        return category;
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

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@example.com");
        user.setIsActive(true);
        return user;
    }
}
