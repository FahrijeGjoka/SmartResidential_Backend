package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByIssueId(Long issueId);


    List<Attachment> findByFileName(String fileName);
}