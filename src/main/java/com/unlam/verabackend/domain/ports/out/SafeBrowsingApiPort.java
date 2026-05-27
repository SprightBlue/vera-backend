package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.infrastructure.dto.SafeBrowsingDto;

import java.util.List;

public interface SafeBrowsingApiPort {
    SafeBrowsingDto checkUrls(List<String> urls);
}
