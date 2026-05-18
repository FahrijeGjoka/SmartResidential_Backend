package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.ai.IssueCategoryMatch;
import com.smartresidential.backend.dto.ai.IssueCategoryMatchResponse;

import java.util.Optional;

public interface IssueCategoryMatcherService {

    Optional<IssueCategoryMatch> matchCategory(String title, String description);

    IssueCategoryMatchResponse matchCategoryForResponse(String title, String description);
}
