package org.lenskit.mooc.hybrid;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.bias.BiasModel;
import org.lenskit.bias.UserBiasModel;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Item scorer that does a logistic blend of a subsidiary item scorer and popularity.  It tries to predict
 * whether a user has rated a particular item.
 */
public class LogisticItemScorer extends AbstractItemScorer {
    private final LogisticModel logisticModel;
    private final BiasModel biasModel;
    private final RecommenderList recommenders;
    private final RatingSummary ratingSummary;

    @Inject
    public LogisticItemScorer(LogisticModel model, UserBiasModel bias, RecommenderList recs, RatingSummary rs) {
        logisticModel = model;
        biasModel = bias;
        recommenders = recs;
        ratingSummary = rs;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        System.gc();
        List<Result> results = new ArrayList<>();
        List<ItemScorer> scorers = recommenders.getItemScorers();
        double[] scores = new double[recommenders.getRecommenderCount()+2];
        double biasInterceptAndUser = biasModel.getIntercept() + biasModel.getUserBias(user);
        for(Long item: items) {
            double bias = biasInterceptAndUser + biasModel.getItemBias(item);
            scores[0] = bias;
            scores[1] = Math.log10(ratingSummary.getItemRatingCount(item));
            int i = 0;
            for(ItemScorer is: scorers) {
                Result score = is.score(user, item);
                scores[i+2] = score == null ? 0.0 : score.getScore() - bias;
                i++;
            }
            results.add(Results.create(item, logisticModel.evaluate(1.0, scores)));
        }
        return Results.newResultMap(results);
    }
}
