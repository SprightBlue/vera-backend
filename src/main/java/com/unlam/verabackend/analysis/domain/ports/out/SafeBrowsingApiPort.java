package com.unlam.verabackend.analysis.domain.ports.out;

import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingDto;

import java.util.List;

public interface SafeBrowsingApiPort {
    SafeBrowsingDto checkUrls(List<String> urls);
}
