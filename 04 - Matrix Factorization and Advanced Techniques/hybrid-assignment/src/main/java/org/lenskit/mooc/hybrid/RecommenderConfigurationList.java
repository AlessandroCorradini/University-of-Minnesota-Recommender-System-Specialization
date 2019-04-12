package org.lenskit.mooc.hybrid;

import com.google.common.collect.ImmutableList;
import groovy.lang.Closure;
import org.lenskit.LenskitConfiguration;
import org.lenskit.config.ConfigHelpers;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of recommender configurations.
 */
public class RecommenderConfigurationList {
    private final List<LenskitConfiguration> configurations;

    public RecommenderConfigurationList(List<LenskitConfiguration> configs) {
        configurations = configs;
    }

    public static RecommenderConfigurationList create(LenskitConfiguration... configs) {
        return new RecommenderConfigurationList(ImmutableList.copyOf(configs));
    }

    public static RecommenderConfigurationList create(Closure<?>... configs) {
        List<LenskitConfiguration> configurations = new ArrayList<>();
        for (Closure<?> config: configs) {
            configurations.add(ConfigHelpers.load(config));
        }
        return new RecommenderConfigurationList(configurations);
    }

    public List<LenskitConfiguration> getConfigurations() {
        return configurations;
    }
}
