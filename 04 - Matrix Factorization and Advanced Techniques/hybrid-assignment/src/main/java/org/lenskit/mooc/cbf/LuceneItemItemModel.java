package org.lenskit.mooc.cbf;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.Directory;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * The Lucene-backed CBF model.
 * @author Michael Ekstrand
 */
@DefaultProvider(LuceneModelBuilder.class)
public class LuceneItemItemModel implements ItemItemModel {
    private static Logger logger = LoggerFactory.getLogger(LuceneItemItemModel.class);

    private final Directory luceneDir;
    private final LongSortedSet itemSet;
    private final LoadingCache<Long,Long2DoubleMap> cache;

    LuceneItemItemModel(Directory dir, LongSortedSet items) {
        luceneDir = dir;
        itemSet = items;
        cache = CacheBuilder.newBuilder()
                            .build(new LuceneCacheLoader());
    }

    @Override
    public LongSortedSet getItemUniverse() {
        return itemSet;
    }

    @Nonnull
    @Override
    public Long2DoubleMap getNeighbors(long item) {
        try {
            return cache.get(item);
        } catch (ExecutionException e) {
            logger.error("error fetching neighborhood", e.getCause());
            throw Throwables.propagate(e.getCause());
        }
    }

    public Long2DoubleMap getNeighborsImpl(long item) {
        try {
            Closer closer = Closer.create();
            try {
                IndexReader reader = closer.register(IndexReader.open(luceneDir));
                IndexSearcher idx = closer.register(new IndexSearcher(reader));;

                Term term = new Term("movie", Long.toString(item));
                Query tq = new TermQuery(term);
                TopDocs docs = idx.search(tq, 1);
                if (docs.totalHits > 1) {
                    logger.warn("found multiple matches for {}", item);
                } else if (docs.totalHits == 0) {
                    logger.debug("could not find movie {}", item);
                    return Long2DoubleMaps.EMPTY_MAP;
                }

                int docid = docs.scoreDocs[0].doc;
                Document doc = idx.doc(docid);
                Long mid = Long.parseLong(doc.get("movie"));
                if (mid != item) {
                    logger.error("retrieved document doesn't match ({} != {})", mid, item);
                    return Long2DoubleMaps.EMPTY_MAP;
                }
                logger.trace("movie {} has index {}", item, docid);
                logger.trace("finding neighbors for movie {} ({})", item, doc.get("title"));

                MoreLikeThis mlt = new MoreLikeThis(idx.getIndexReader());
                mlt.setFieldNames(new String[]{"title", "genres", "tags"});
                Query q = mlt.like(docid);
                TopDocs results = idx.search(q, itemSet.size() + 1);

                logger.trace("index returned {} of {} similar movies",
                             results.scoreDocs.length, results.totalHits);
                Long2DoubleMap scores = new Long2DoubleOpenHashMap(results.totalHits);
                for (ScoreDoc sd: results.scoreDocs) {
                    Document nbrdoc = idx.doc(sd.doc);
                    long id = Long.parseLong(nbrdoc.get("movie"));
                    if (id != item) {
                        scores.put(id, sd.score);
                    }
                }
                logger.trace("returning {} neighbors", scores.size());
                return LongUtils.frozenMap(scores);
            } catch (Throwable th) {
                throw closer.rethrow(th);
            } finally {
                closer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("I/O error fetching neighbors", e);
        }
    }

    private class LuceneCacheLoader extends CacheLoader<Long,Long2DoubleMap> {
        @Override
        public Long2DoubleMap load(Long key) throws Exception {
            return getNeighborsImpl(key);
        }
    }
}
