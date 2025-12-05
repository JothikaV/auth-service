package com.demo.authservice.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
public class AdminStatsResponse {
    private long totalUsers;
    private Map<String, ZonedDateTime> lastLoginTimes;
}
