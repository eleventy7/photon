package com.skyscraper.config;

import com.skyscraper.enums.CcyPair;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CcyPairList {
    private List<CcyPair> ccyPairs;
}
