package org.lenskit.mooc.hybrid;

import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.bias.BiasModel;
import org.lenskit.bias.UserBiasModel;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.inject.Transient;
import org.lenskit.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Trainer that builds logistic models.
 */
public class LogisticModelProvider implements Provider<LogisticModel> {
    private static final Logger logger = LoggerFactory.getLogger(LogisticModelProvider.class);
    private static final double LEARNING_RATE = 0.00005;
    private static final int ITERATION_COUNT = 100;

    private final LogisticTrainingSplit dataSplit;
    private final BiasModel baseline;
    private final RecommenderList recommenders;
    private final RatingSummary ratingSummary;
    private final int parameterCount;
    private final Random random;

    @Inject
    public LogisticModelProvider(@Transient LogisticTrainingSplit split,
                                 @Transient UserBiasModel bias,
                                 @Transient RecommenderList recs,
                                 @Transient RatingSummary rs,
                                 @Transient Random rng) {
        dataSplit = split;
        baseline = bias;
        recommenders = recs;
        ratingSummary = rs;
        parameterCount = 1 + recommenders.getRecommenderCount() + 1;
        random = rng;
    }

    @Override
    public LogisticModel get() {
        List<ItemScorer> scorers = recommenders.getItemScorers();
        double intercept = 0;
        double[] params = new double[parameterCount];
        LogisticModel current = LogisticModel.create(intercept, params);

        // TODO Implement model training
        List<Rating> tuneRatings = dataSplit.getTuneRatings();
        for(int itera=0; itera<ITERATION_COUNT; itera++){
            Collections.shuffle(tuneRatings);
            logger.info("{} th iteration", itera);

            for (Rating r: tuneRatings){
                long itemId = r.getItemId();
                long userId = r.getUserId();
                double b_ui = baseline.getIntercept() + baseline.getItemBias(itemId) + baseline.getUserBias(userId);
                double lg_popularity = Math.log(ratingSummary.getItemRatingCount(itemId));

                RealVector x_array = new ArrayRealVector(parameterCount);

                double y =r.getValue();

                x_array.setEntry(0, b_ui);
                x_array.setEntry(1, lg_popularity);
                int i=2;
                for (ItemScorer scorer: scorers){
                    Result score_result = scorer.score(userId, itemId);
                    if (score_result == null) {
                        x_array.setEntry(i,0.);
                        i+=1;
                        continue;
                    }
                    double x_value = score_result.getScore() - b_ui;
                    x_array.setEntry(i, x_value);
                    i+=1;
                }
                //take care, put -y below based on evaluate declaration
                double sigmoid = current.evaluate(-y, x_array);

                intercept += LEARNING_RATE*y*sigmoid;
                for (int j=0; j<parameterCount;j++){
                    params[j] += LEARNING_RATE*y*x_array.getEntry(j)*sigmoid;
                }

                current = LogisticModel.create(intercept, params);

            }
            //logger.info("{} intercept", intercept);

        }

        return current;
    }

}
