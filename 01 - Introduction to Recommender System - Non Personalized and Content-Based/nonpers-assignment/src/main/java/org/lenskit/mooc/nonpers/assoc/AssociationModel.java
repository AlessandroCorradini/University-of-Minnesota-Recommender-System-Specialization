package org.lenskit.mooc.nonpers.assoc;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.SortedKeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * An association rule model, storing item-item association scores.
 *
 * <p>You <strong>should note</strong> need to change this class.  It has some internal optimizations to reduce
 * the memory requirements after the model is built.</p>
 */
@Shareable
public class AssociationModel implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(AssociationModel.class);
    private static final long serialVersionUID = 1L;

    private final SortedKeyIndex index;
    private final double[][] scores;

    /**
     * Construct a new association model.
     * @param assocScores The association scores.  The outer map's keys are the X items, and the inner map's keys are
     *                    the Y items.  So {@code assocScores.get(x).get(y)} should return the score for {@code y}
     *                    with respect to {@code x}.
     */
    public AssociationModel(Map<Long, ? extends Map<Long,Double>> assocScores) {
        index = SortedKeyIndex.fromCollection(assocScores.keySet());
        int n = index.size();
        logger.debug("transforming input map for {} items into log data", n);
        scores = new double[n][n];
        for (int i = 0; i < n; i++) {
            long itemX = index.getKey(i);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue; // skip self-similarities
                }

                long itemY = index.getKey(j);
                Double score = assocScores.get(itemX).get(itemY);
                if (score == null) {
                    logger.error("no score found for items {} and {}", itemX, itemY);
                    String msg = String.format("no score found for x=%d, y=%d", itemX, itemY);
                    throw new IllegalArgumentException(msg);
                }
                scores[i][j] = score;
            }
        }
    }

    /**
     * Get the set of known items.
     * @return The set of known item IDs.
     */
    public LongSet getKnownItems() {
        return index.keySet();
    }

    /**
     * Query whether the model knows about an item.
     * @param item The item ID.
     * @return {@code true} if the model knows about the item {@code item}, {@code false} otherwise.
     */
    public boolean hasItem(long item) {
        return index.containsKey(item);
    }

    /**
     * Get the association between two items.
     * @param ref The reference item (X).
     * @param item The item to score (Y).
     * @return The score between X and Y.
     * @throws IllegalArgumentException if either item is invalid.
     */
    public double getItemAssociation(long ref, long item) {
        // look up item positions
        int refIndex = index.tryGetIndex(ref);
        Preconditions.checkArgument(refIndex >= 0, "unknown reference item %d", ref);
        int itemIndex = index.tryGetIndex(item);
        Preconditions.checkArgument(itemIndex >= 0, "unknown target item %d", item);

        return scores[refIndex][itemIndex];
    }
}
