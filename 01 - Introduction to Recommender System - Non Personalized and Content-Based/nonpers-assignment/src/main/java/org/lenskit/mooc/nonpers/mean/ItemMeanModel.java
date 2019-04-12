package org.lenskit.mooc.nonpers.mean;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Map;

/**
 * A <em>model</em> class that stores item mean ratings.
 *
 * <p>The {@link Shareable} annotation is common for model objects, and tells LensKit that the class can be shared
 * between multiple recommender instances.</p>
 *
 * <p>The {@link DefaultProvider} annotation tells LensKit to use a <em>provider class</em> &mdash; the mean item scorer
 * provider &mdash; to create instances of this class.</p>
 *
 * <p>You <strong>should not</strong> need to make any changes to this class.</p>
 */
@Shareable
@Immutable
@DefaultProvider(ItemMeanModelProvider.class)
public class ItemMeanModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2DoubleMap itemMeans;

    /**
     * Construct a new item mean model.
     * @param means A map of item IDs to their mean ratings.
     */
    public ItemMeanModel(Map<Long, Double> means) {
        itemMeans = LongUtils.frozenMap(means);
    }

    /**
     * Get the set of items known by the model.
     * @return The set of items known by the model.
     */
    public LongSet getKnownItems() {
        return itemMeans.keySet();
    }

    /**
     * Query whether this model knows about an item.
     * @param item The item ID.
     * @return {@code true} if the item is known by the model, {@code false} otherwise.
     */
    public boolean hasItem(long item) {
        return itemMeans.containsKey(item);
    }

    /**
     * Get the mean rating for an item.
     * @param item The item ID.
     * @return The mean rating.
     * @throws IllegalArgumentException if the item is not a known itemm.
     */
    public double getMeanRating(long item) {
        Preconditions.checkArgument(hasItem(item), "unknown item " + item);
        return itemMeans.get(item);
    }
}
