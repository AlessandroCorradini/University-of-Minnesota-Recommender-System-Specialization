package org.lenskit.mooc.cbf;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.inject.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for computing {@linkplain TFIDFModel TF-IDF models} from item tag data.  Each item is
 * represented by a normalized TF-IDF vector.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TFIDFModelProvider implements Provider<TFIDFModel> {
    private static final Logger logger = LoggerFactory.getLogger(TFIDFModelProvider.class);

    private final DataAccessObject dao;

    /**
     * Construct a model builder.  The {@link Inject} annotation on this constructor tells LensKit
     * that it can be used to build the model builder.
     *
     * @param dao The data access object.
     */
    @Inject
    public TFIDFModelProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * This method is where the model should actually be computed.
     * @return The TF-IDF model (a model of item tag vectors).
     */
    @Override
    public TFIDFModel get() {
        logger.info("Building TF-IDF model");

        // Create a map to accumulate document frequencies for the IDF computation
        Map<String, Double> docFreq = new HashMap<>();

        // We now proceed in 2 stages. First, we build a TF vector for each item.
        // While we do this, we also build the DF vector.
        // We will then apply the IDF to each TF vector and normalize it to a unit vector.

        // Create a map to store the item TF vectors.
        Map<Long, Map<String, Double>> itemVectors = new HashMap<>();

        // Iterate over the items to compute each item's vector.
        LongSet items = dao.getEntityIds(CommonTypes.ITEM);
        for (long item : items) {
            // Create a work vector to accumulate this item's tag vector.
            Map<String, Double> work = new HashMap<>();

            for (Entity tagApplication : dao.query(TagData.ITEM_TAG_TYPE)
                    .withAttribute(TagData.ITEM_ID, item)
                    .get()) {
                String tag = tagApplication.get(TagData.TAG);
                // TODO Count this tag application in the term frequency vector
                // TODO Also count it in the document frequencey vector when needed
                Double tagCount = work.get(tag);
                if (tagCount == null) {
                    work.put(tag, 1.0);
                    Double tagDF = docFreq.get(tag);
                    if (tagDF == null) {
                        docFreq.put(tag, 1.0);
                    } else {
                        docFreq.put(tag, tagDF + 1.0);
                    }
                } else {
                    work.put(tag, tagCount + 1.0);
                }
            }

            itemVectors.put(item, work);
        }

        logger.info("Computed TF vectors for {} items", itemVectors.size());

        // Now we've seen all the items, so we have each item's TF vector and a global vector
        // of document frequencies.
        // Invert and log the document frequency.  We can do this in-place.
        final double logN = Math.log(items.size());
        for (Map.Entry<String, Double> e : docFreq.entrySet()) {
            e.setValue(logN - Math.log(e.getValue()));
        }

        // Now docFreq is a log-IDF vector.  Its values can therefore be multiplied by TF values.
        // So we can use it to apply IDF to each item vector to put it in the final model.
        // Create a map to store the final model data.
        Map<Long, Map<String, Double>> modelData = new HashMap<>();
        for (Map.Entry<Long, Map<String, Double>> entry : itemVectors.entrySet()) {
            Map<String, Double> tv = new HashMap<>(entry.getValue());

            // TODO Convert this vector to a TF-IDF vector
            Double sqrSum = 0.0;
            for (Map.Entry<String, Double> e : tv.entrySet()) {
                Double newValue = e.getValue() * docFreq.get(e.getKey());
                sqrSum += newValue * newValue;
                e.setValue(newValue);
            }
            // TODO Normalize the TF-IDF vector to be a unit vector
            // Normalize it by dividing each element by its Euclidean norm, which is the
            // square root of the sum of the squares of the values.
            Double sqrt = Math.sqrt(sqrSum);
            for (Map.Entry<String, Double> e : tv.entrySet()) {
                e.setValue(e.getValue() / sqrt);
            }

            modelData.put(entry.getKey(), tv);
        }

        // We don't need the IDF vector anymore, as long as as we have no new tags
        return new TFIDFModel(modelData);
    }
}
