package com.unlam.verabackend.domain.port.out;

import java.util.List;

public interface SafeBrowsingProvider {
    List<String> checkUrls(List<String> urls);
}