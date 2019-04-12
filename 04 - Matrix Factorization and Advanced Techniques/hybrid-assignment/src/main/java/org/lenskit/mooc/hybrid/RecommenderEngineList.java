package org.lenskit.mooc.hybrid;

import com.google.common.collect.ImmutableList;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.Shareable;
import org.lenskit.inject.Transient;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * A list of recommenders.
 */
@Shareable
public class RecommenderEngineList implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<LenskitRecommenderEngine> engines;

    @Inject
    public RecommenderEngineList(@Transient LogisticTrainingSplit split, RecommenderConfigurationList configs) {
        ImmutableList.Builder<LenskitRecommenderEngine> bld = ImmutableList.builder();
        for (LenskitConfiguration config: configs.getConfigurations()) {
            bld.add(LenskitRecommenderEngine.build(config, split.getTrainData()));
        }
        engines = bld.build();
    }

    public List<LenskitRecommenderEngine> getRecommenderEngines() {
        return engines;
    }
}