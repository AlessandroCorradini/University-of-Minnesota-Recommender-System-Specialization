package org.lenskit.mooc.uu;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.results.Results;
import org.lenskit.util.ScoredIdAccumulator;
import org.lenskit.util.TopNScoredIdAccumulator;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Scalars;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User-user item scorer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleUserUserItemScorer extends AbstractItemScorer {
    private final DataAccessObject dao;
    private final int neighborhoodSize;

    /**
     * Instantiate a new user-user item scorer.
     * @param dao The data access object.
     */
    @Inject
    public SimpleUserUserItemScorer(DataAccessObject dao) {
        this.dao = dao;
        neighborhoodSize = 30;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        // TODO Score the items for the user with user-user CF

        // Create a place to store the results of our score computations
        List<Result> results = new ArrayList<>();

        //Retrieve users list
        LongSet users = dao.getEntityIds(CommonTypes.USER);

        Map<Long, Long2DoubleOpenHashMap> userRatings = new HashMap<>();

        //Calculate weighted rating of users
        for(long userID: users) {
            Long2DoubleOpenHashMap rat = getUserRatingVector(userID);
            double meanRating = Vectors.mean(rat);

            for(Map.Entry<Long,Double> map : rat.entrySet()) {
                map.setValue(map.getValue() - meanRating);
            }
            userRatings.put(userID, rat);
        }

        //Find the initial ratings of target user
        Long2DoubleOpenHashMap targetUserRatings = getUserRatingVector(user);

        //Calculate Cosine Similarity to find nearest neighbors
        Map<Long,Double> cosineVector = new HashMap<>();
        for(Long x: users) {
            if(x != user) {
                Long2DoubleOpenHashMap userRatingVector = userRatings.get(x);
                double cosineValue = (Vectors.dotProduct(userRatings.get(user),userRatingVector))/(Vectors.euclideanNorm(userRatingVector)*
                        Vectors.euclideanNorm(userRatings.get(user)));

                if (cosineValue > 0.0) {
                    cosineVector.put(x,cosineValue);
                }
            }
        }

        //Sort the cosineVector in decreasing order of their values
        Set<Map.Entry<Long,Double>> set = cosineVector.entrySet();
        List<Map.Entry<Long,Double> > closeUsers = new ArrayList<Map.Entry<Long, Double>>(set);

        Collections.sort(closeUsers, new Comparator<Map.Entry<Long, Double>>() {
            @Override
            public int compare(Map.Entry<Long, Double> o1, Map.Entry<Long, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Iterate over the items to compute each item's vector.
        for(Long item: items) {

            double weightedSum = 0.0;
            double weightValue = 0.0;
            int i = 0;
            for(Map.Entry<Long,Double> entry : closeUsers) {
                if(i >= 30) {
                    break;
                }
                long userID = entry.getKey();
                if(userRatings.get(userID).containsKey(item)) {
                    i++;
                    weightedSum += entry.getValue()*(userRatings.get(userID).get(item));
                    weightValue += entry.getValue();
                }
            }
            if(weightValue == 0 || i < 2) {
                results.add(Results.create(item,0.0));
            }else {
                results.add(Results.create(item, (Vectors.mean(targetUserRatings) + (weightedSum/weightValue))));
            }
        }
        return Results.newResultMap(results);

    }

    /**
     * Get a user's rating vector.
     * @param user The user ID.
     * @return The rating vector, mapping item IDs to the user's rating
     *         for that item.
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
