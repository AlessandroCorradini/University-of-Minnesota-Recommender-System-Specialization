package org.lenskit.mooc.ii;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.results.Results;
import org.lenskit.util.ScoredIdAccumulator;
import org.lenskit.util.TopNScoredIdAccumulator;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleItemItemScorer extends AbstractItemScorer {
    private final SimpleItemItemModel model;
    private final DataAccessObject dao;
    private final int neighborhoodSize;

    @Inject
    public SimpleItemItemScorer(SimpleItemItemModel m, DataAccessObject dao) {
        model = m;
        this.dao = dao;
        neighborhoodSize = 20;
    }

    /**
     * Score items for a user.
     * @param user The user ID.
     * @param items The score vector.  Its key domain is the items to score, and the scores
     *               (rating predictions) should be written back to this vector.
     */
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleMap itemMeans = model.getItemMeans();
        Long2DoubleMap ratings = getUserRatingVector(user);

        for(Map.Entry<Long,Double> rating: ratings.entrySet()) {
            rating.setValue(rating.getValue()-itemMeans.get(rating.getKey()));
        }

        // TODO Normalize the user's ratings by subtracting the item mean from each one.

        List<Result> results = new ArrayList<>();

        for (long item: items ) {
            double meanRating = itemMeans.get(item);
            Long2DoubleMap neighbors = model.getNeighbors(item);
            double weightedSum = 0.0;
            double weightedValue = 0.0;
            double resultRating = 0.0;

            Set<Map.Entry<Long,Double>> set = neighbors.entrySet();
            List<Map.Entry<Long,Double> > closeItems = new ArrayList<Map.Entry<Long, Double>>(set);

            Collections.sort(closeItems, new Comparator<Map.Entry<Long, Double>>() {
                @Override
                public int compare(Map.Entry<Long, Double> o1, Map.Entry<Long, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            int i = 0;
            for(Map.Entry<Long,Double> nearItem: closeItems) {

                if(i >= 20) {
                    break;
                }
                if(ratings.containsKey(nearItem.getKey())) {
                    i++;
                    weightedSum += nearItem.getValue()*(ratings.get(nearItem.getKey()));
                    weightedValue += nearItem.getValue();
                }
            }
            resultRating = meanRating + (weightedSum/weightedValue);
            results.add(Results.create(item,resultRating));

            // TODO Compute the user's score for each item, add it to results
        }

        return Results.newResultMap(results);

    }

    /**
     * Get a user's ratings.
     * @param user The user ID.
     * @return The ratings to retrieve.
     */
    private Long2DoubleOpenHashMap getUserRatingVector(long user) {
        List<Rating> history = dao.query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, user)
                                  .get();

        Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap();
        for (Rating r: history) {
            ratings.put(r.getItemId(), r.getValue());
        }

        return ratings;
    }


}
