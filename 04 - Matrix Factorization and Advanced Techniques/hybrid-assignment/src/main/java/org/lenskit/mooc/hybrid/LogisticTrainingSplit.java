package org.lenskit.mooc.hybrid;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultDouble;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.Parameter;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages a train-tune split for training component recommenders and a logistic regression blend of them.
 */
public class LogisticTrainingSplit {
    private static final Logger logger = LoggerFactory.getLogger(LogisticTrainingSplit.class);

    private final DataAccessObject baseData;
    private final double blendTrainFraction;
    private final double trainBalance;
    private final Random random;
    private transient DataAccessObject trainData;
    private transient List<Rating> tuneRatings;

    @Inject
    public LogisticTrainingSplit(DataAccessObject dao, @BlendTrainFraction double frac, @TrainingBalance double bal,
                                 @Transient Random rng) {
        baseData = dao;
        blendTrainFraction = frac;
        trainBalance = bal;
        random = rng;
    }

    private synchronized void doSplit() {
        if (trainData != null) {
            return;
        }

        logger.info("separating input data into train and tune sets");

        EntityCollectionDAOBuilder trainBld = new EntityCollectionDAOBuilder();
        // add all non-rating entities
        for (EntityType type: baseData.getEntityTypes()) {
            if (!type.equals(CommonTypes.RATING)) {
                logger.debug("copying entities of type {}", type);
                try (ObjectStream<Entity> stream = baseData.streamEntities(type)) {
                    trainBld.addEntities(stream);
                }
            }
        }
        trainBld.addIndex(CommonTypes.RATING, CommonAttributes.ITEM_ID);
        trainBld.addIndex(CommonTypes.RATING, CommonAttributes.USER_ID);

        List<Rating> trs = new ArrayList<>();
        LongSet ratingPairs = new LongOpenHashSet();
        LongSet users = new LongOpenHashSet();
        LongSet items = new LongOpenHashSet();

        logger.debug("splitting ratings");
        try (ObjectStream<Rating> ratings = baseData.query(Rating.class).stream()) {
            for (Rating r: ratings) {
                long uid = r.getUserId();
                long iid = r.getItemId();
                if (uid <= 0 || uid > Integer.MAX_VALUE) {
                    logger.warn("bad user id {}", uid);
                }
                if (iid <= 0 || iid > Integer.MAX_VALUE) {
                    logger.warn("bad item id {}", iid);
                }
                users.add(uid);
                items.add(iid);
                ratingPairs.add((uid << 32) + iid);
                if (random.nextDouble() <= blendTrainFraction) {
                    logger.trace("sending entity {} to tuning", r);
                    trs.add(Rating.newBuilder()
                                  .setId(r.getId())
                                  .setUserId(r.getUserId())
                                  .setItemId(r.getItemId())
                                  .setRating(1)
                                  .build());
                } else {
                    logger.trace("sending entity {} to test", r);
                    trainBld.addEntity(r);
                }
            }
        }

        trainData = trainBld.build();

        // produce random negative samples
        LongList userList = new LongArrayList(users);
        LongList itemList = new LongArrayList(items);
        int target = trs.size() + (int) (trs.size() * trainBalance);
        logger.info("selected {} train ratings, building {} negative samples",
                    trs.size(), target - trs.size());
        while (trs.size() < target) {
            long user = userList.getLong(random.nextInt(userList.size()));
            long item = itemList.getLong(random.nextInt(itemList.size()));
            long pair = (user << 32) + item;
            if (!ratingPairs.contains(pair)) {
                trs.add(Rating.newBuilder()
                              .setId(-pair)
                              .setUserId(user)
                              .setItemId(item)
                              .setRating(-1)
                              .build());
            }
        }

        tuneRatings = trs;
    }

    public DataAccessObject getTrainData() {
        doSplit();
        return trainData;
    }

    public List<Rating> getTuneRatings() {
        doSplit();
        return tuneRatings;
    }

    /**
     * Approximate fraction of ratings to hold out for training the logistic regression.
     */
    @Documented
    @Qualifier
    @Parameter(Double.class)
    @DefaultDouble(0.1)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @interface BlendTrainFraction {}

    /**
     * DAO provider that returns the training data for the logistic training split.
     */
    public static class InnerTrainingDataProvider implements Provider<DataAccessObject> {
        private final LogisticTrainingSplit split;

        @Inject
        public InnerTrainingDataProvider(LogisticTrainingSplit lts) {
            split = lts;
        }

        @Override
        public DataAccessObject get() {
            return split.getTrainData();
        }
    }

    /**
     * The 'training balance' for the logistic regression.  For a balance {@code b}, and {@code n} atings, this
     * will ensure there are {@code b * n} unrated user-item pairs to train with.
     */
    @Qualifier
    @Parameter(Double.class)
    @DefaultDouble(1.0)
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    public static @interface TrainingBalance {
    }
}
