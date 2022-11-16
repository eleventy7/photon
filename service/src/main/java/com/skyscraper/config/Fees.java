package com.skyscraper.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Fees {
    private double makerFeeBp;
    private double takerFeeBp;
}
