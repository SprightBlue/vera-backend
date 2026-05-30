package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.UrlValidation;

public interface SafeBrowsingApiPort {
    UrlValidation checkUrlsInContent(String content);
}
