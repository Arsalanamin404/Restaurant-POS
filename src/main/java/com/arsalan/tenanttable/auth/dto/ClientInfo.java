package com.arsalan.tenanttable.auth.dto;

public record ClientInfo(
        String ipAddress,
        String userAgent
) {}