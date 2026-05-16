package com.smartresidential.backend.jobs;

import com.smartresidential.backend.entities.Issue;
import com.smartresidential.backend.entities.Notification;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.IssueRepository;
import com.smartresidential.backend.repositories.NotificationRepository;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationJob {

    private final NotificationRepository notificationRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Async
    @Transactional
    public void notifyIssueCreated(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        Long adminRoleId = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_ADMIN not found"))
                .getId();

        Long staffRoleId = roleRepository.findByName("ROLE_STAFF")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_STAFF not found"))
                .getId();

        List<User> admins = userRepository.findAllByRoleId(adminRoleId);
        List<User> staff = userRepository.findAllByRoleId(staffRoleId);

        String message = "New issue created: " + issue.getTitle();

        createNotifications(admins, message, "ISSUE_CREATED");
        createNotifications(staff, message, "ISSUE_CREATED");

        log.info("Background job completed: issue created notifications for issue {}", issueId);
    }

    @Async
    @Transactional
    public void notifyTechnicianAssigned(Long issueId, Long technicianId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        String message = "You have been assigned to issue: " + issue.getTitle();

        Notification notification = new Notification();
        notification.setUser(technician);
        notification.setMessage(message);
        notification.setType("TECHNICIAN_ASSIGNED");
        notification.setIsRead(false);

        notificationRepository.save(notification);

        log.info("Background job completed: technician assignment notification for issue {}", issueId);
    }

    @Async
    @Transactional
    public void notifyIssueStatusChanged(Long issueId, String newStatus) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        User resident = issue.getCreatedBy();

        String message = "Status for your issue '" + issue.getTitle() + "' changed to: " + newStatus;

        Notification notification = new Notification();
        notification.setUser(resident);
        notification.setMessage(message);
        notification.setType("ISSUE_STATUS_CHANGED");
        notification.setIsRead(false);

        notificationRepository.save(notification);

        log.info("Background job completed: issue status changed notification for issue {}", issueId);
    }

    private void createNotifications(List<User> users, String message, String type) {
        for (User user : users) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setType(type);
            notification.setIsRead(false);

            notificationRepository.save(notification);
        }
    }

    @Async
    @Transactional
    public void notifyMaintenanceRequestEscalation(Long maintenanceRequestId, Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        Long adminRoleId = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_ADMIN not found"))
                .getId();

        Long staffRoleId = roleRepository.findByName("ROLE_STAFF")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_STAFF not found"))
                .getId();

        List<User> admins = userRepository.findAllByRoleId(adminRoleId);
        List<User> staff = userRepository.findAllByRoleId(staffRoleId);

        String message = "Maintenance request needs escalation. Issue: " + issue.getTitle();

        createNotifications(admins, message, "MAINTENANCE_ESCALATION");
        createNotifications(staff, message, "MAINTENANCE_ESCALATION");

        log.info("Background job completed: maintenance escalation notification. Request ID: {}", maintenanceRequestId);
    }
}
