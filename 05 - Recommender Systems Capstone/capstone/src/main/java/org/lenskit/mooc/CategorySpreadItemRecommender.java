package org.lenskit.mooc;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.basic.AbstractItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.results.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Item recommender that only recommends one item per category.  Each category is a list representing the
 * category tree; we care about the last element (the leaf category).  An item will only be included in
 * the recommendations if it is in a category that is not yet represented in the previous recommendations.
 */
public class CategorySpreadItemRecommender extends AbstractItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(CategorySpreadItemRecommender.class);

    private final DataAccessObject dao;
    private final ItemRecommender delgate;

    @Inject
    public CategorySpreadItemRecommender(DataAccessObject dao, ItemRecommender base) {
        this.dao = dao;
        this.delgate = base;
    }

    @Override
    protected ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude) {
        ResultList recs = delgate.recommendWithDetails(user, -1, candidates, exclude);
        List<Result> results = new ArrayList<>();

        // what categories have we seen?
        Set<String> categories = new HashSet<>();

        for (Result r: recs) {
            Entity item = dao.lookupEntity(CommonTypes.ITEM, r.getId());
            if (item == null) {
                logger.debug("cannot find item {}", r.getId());
                continue;
            }

            Set<String> leafCategories = item.get(ItemData.CATEGORIES)
                                             .stream()
                                             .map(c -> c.get(c.size() - 1))
                                             .collect(Collectors.toSet());
            if (!categories.containsAll(leafCategories)) {
                // we have a fresh item!
                categories.addAll(leafCategories);
                results.add(r);
            }

            // are we done?
            if (n > 0 && results.size() >= n) {
                break;
            }
        }

        return Results.newResultList(results);
    }
}
