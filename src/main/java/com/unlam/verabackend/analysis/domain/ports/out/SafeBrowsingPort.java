package com.unlam.verabackend.analysis.domain.ports.out;

public interface SafeBrowsingPort {
    boolean checkUrl(String url);
}
