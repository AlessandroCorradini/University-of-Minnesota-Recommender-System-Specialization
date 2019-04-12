package org.lenskit.mooc.nonpers.assoc;

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.Transient;
import org.lenskit.util.IdBox;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Build an association rule model using a lift metric.
 */
public class LiftAssociationModelProvider implements Provider<AssociationModel> {
    private static final Logger logger = LoggerFactory.getLogger(LiftAssociationModelProvider.class);
    private final DataAccessObject dao;

    @Inject
    public LiftAssociationModelProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    @Override
    public AssociationModel get() {
        // First step: map each item to the set of users who have rated it.
        // While we're at it, compute the set of all users.

        // This set contains all users.
        LongSet allUsers = new LongOpenHashSet();

        // This map will map each item ID to the set of users who have rated it.
        Long2ObjectMap<LongSortedSet> itemUsers = new Long2ObjectOpenHashMap<>();

        // Open a stream, grouping ratings by item ID
        try (ObjectStream<IdBox<List<Rating>>> ratingStream = dao.query(Rating.class)
                                                                 .groupBy(CommonAttributes.ITEM_ID)
                                                                 .stream()) {
            // Process each item's ratings
            for (IdBox<List<Rating>> item: ratingStream) {
                // Build a set of users.  We build an array first, then convert to a set.
                LongList users = new LongArrayList();
                // Add each rating's user ID to the user sets
                for (Rating r: item.getValue()) {
                    long user = r.getUserId();
                    users.add(user);
                    allUsers.add(user);
                }
                // put this item's user set into the item user map
                // a frozen set will be very efficient later
                itemUsers.put(item.getId(), LongUtils.frozenSet(users));
            }
        }

        // Second step: compute all association rules

        // We need a map to store them
        Long2ObjectMap<Long2DoubleMap> assocMatrix = new Long2ObjectOpenHashMap<>();


        // then loop over 'x' items
        for (Long2ObjectMap.Entry<LongSortedSet> xEntry: itemUsers.long2ObjectEntrySet()) {
            long xId = xEntry.getLongKey();
            LongSortedSet xUsers = xEntry.getValue();

            // set up a map to hold the scores for each 'y' item
            Long2DoubleMap itemScores = new Long2DoubleOpenHashMap();

            // TODO Compute lift association formulas for all other 'Y' items with respect to this 'X'

            // save the score map to the main map
            assocMatrix.put(xId, itemScores);
        }

        return new AssociationModel(assocMatrix);
    }
}
