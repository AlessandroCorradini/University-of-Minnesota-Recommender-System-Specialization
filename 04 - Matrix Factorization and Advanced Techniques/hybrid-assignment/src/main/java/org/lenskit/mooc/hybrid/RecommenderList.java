package org.lenskit.mooc.hybrid;

import com.google.common.base.Throwables;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Recommender;
import org.lenskit.data.dao.DataAccessObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of recommenders.
 */
public class RecommenderList implements AutoCloseable {
    List<LenskitRecommender> recommenders;

    @Inject
    public RecommenderList(RecommenderEngineList engines, DataAccessObject dao) {
        recommenders = new ArrayList<>();
        for (LenskitRecommenderEngine engine: engines.getRecommenderEngines()) {
            recommenders.add(engine.createRecommender(dao));
        }
    }

    public List<ItemScorer> getItemScorers() {
        List<ItemScorer> scorers = new ArrayList<>(recommenders.size());
        for (LenskitRecommender rec : recommenders) {
            scorers.add(rec.getItemScorer());
        }
        return scorers;
    }

    public int getRecommenderCount() {
        return recommenders.size();
    }

    @Override
    public void close() {
        Throwable err = null;
        for (Recommender rec: recommenders) {
            try {
                rec.close();
            } catch (Error | RuntimeException ex) {
                if (err == null) {
                    err = ex;
                } else {
                    err.addSuppressed(ex);
                }
            }
        }
        if (err != null) {
            throw Throwables.propagate(err);
        }
    }
}