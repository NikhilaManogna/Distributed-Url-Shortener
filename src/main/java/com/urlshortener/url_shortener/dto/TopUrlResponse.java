package com.urlshortener.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopUrlResponse {
    private String shortCode;
    private long clicks;
}