package org.lenskit.mooc.cbf;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.inject.Transient;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LuceneModelBuilder implements Provider<LuceneItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(LuceneModelBuilder.class);
    private final DataAccessObject dao;

    @Inject
    public LuceneModelBuilder(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    @Override
    public LuceneItemItemModel get() {
        Directory dir = new RAMDirectory();

        try {
            writeMovies(dir);
        } catch (IOException e) {
            throw new RuntimeException("I/O error writing movie model", e);
        }
        return new LuceneItemItemModel(dir, LongUtils.packedSet(dao.getEntityIds(CommonTypes.ITEM)));
    }

    private void writeMovies(Directory dir) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_35);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(dir, config);
        try {
            logger.info("Building Lucene movie model");
            for (long movie: dao.getEntityIds(CommonTypes.ITEM)) {
                logger.debug("building model for {}", movie);
                Document doc = makeMovieDocument(movie);
                writer.addDocument(doc);
            }
        } finally {
            writer.close();
        }
    }

    private Document makeMovieDocument(long movieId) {
        Document doc = new Document();
        Entity movie = dao.lookupEntity(CommonTypes.ITEM, movieId);
        List<Entity> tagApps = dao.query(TagData.ITEM_TAG_TYPE)
                                  .withAttribute(TagData.ITEM_ID, movieId)
                                  .get();
        StringBuilder tagDoc = new StringBuilder();
        for (Entity te: tagApps) {
            tagDoc.append(te.get(TagData.TAG))
                  .append('\n');
        }

        doc.add(new Field("movie", Long.toString(movieId),
                                Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        doc.add(new Field("title", movie.get(CommonAttributes.NAME),
                          Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        doc.add(new Field("tags", tagDoc.toString(),
                          Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        return doc;
    }
}
