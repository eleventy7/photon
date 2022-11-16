package com.skyscraper.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VenueList {
    private List<VenueConfig> venueConfigs;
}
