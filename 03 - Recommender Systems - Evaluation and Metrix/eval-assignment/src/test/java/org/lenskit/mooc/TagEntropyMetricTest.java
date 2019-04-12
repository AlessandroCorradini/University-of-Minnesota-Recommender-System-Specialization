package org.lenskit.mooc;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.results.Results;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

public class TagEntropyMetricTest {
    private File dataDir = new File(System.getProperty("data.dir", "data"));
    private File tagFile = new File(dataDir, "tags.csv");
    private File ratingsFile = new File(dataDir, "ratings.csv");
    private TagEntropyMetric metric;
    private LenskitRecommender recommender;

    @Before
    public void createMetric() throws RecommenderBuildException, IOException {
        assumeTrue("movie tag file available", tagFile.exists());
        assumeTrue("ratings file available", ratingsFile.exists());
        metric = new TagEntropyMetric();

        StaticDataSource source = StaticDataSource.load(dataDir.toPath().resolve("movielens.yml"));

        // build a recommender
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(DataAccessObject.class);
        config.bind(ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        recommender = LenskitRecommender.build(config, source.get());
    }

    @Test
    public void testLifecycle() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));
        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(result, not(nullValue()));
    }

    /**
     * Test the measurement of a single user with a single movie.
     */
    @Test
    public void testSimpleUser() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));

        TestUser tu = createUser(42);
        ResultList recs = createRecommendations(903);

        MetricResult mr = metric.measureUser(tu, 1, recs, ctx);
        assertThat(mr, instanceOf(TagEntropyMetric.TagEntropyResult.class));
        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) mr;

        double expected = 5.278729;

        assertThat(result.entropy, closeTo(expected, 0.0001));

        TagEntropyMetric.TagEntropyResult aggResult = (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(aggResult, not(nullValue()));
        // average of 1 user's entropy is that entropy
        assertThat(aggResult.entropy, closeTo(expected, 0.0001));
    }

    /**
     * Test the aggregation of two users, each with a single movie.
     */
    @Test
    public void testTwoUsers() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));

        TestUser tu = createUser(42);
        ResultList recs = createRecommendations(903);

        MetricResult mr = metric.measureUser(tu, 1, recs, ctx);
        assertThat(mr, instanceOf(TagEntropyMetric.TagEntropyResult.class));
        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) mr;

        assertThat(result.entropy, closeTo(5.27872957, 0.0001));

        tu = createUser(39);
        recs = createRecommendations(3993);

        result =  (TagEntropyMetric.TagEntropyResult) metric.measureUser(tu, 1, recs, ctx);
        assertThat(result, not(nullValue()));
        assertThat(result.entropy, closeTo(5.2696308, 0.0001));

        TagEntropyMetric.TagEntropyResult aggResult = (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(aggResult, not(nullValue()));
        // average of 1 user's entropy is that entropy
        assertThat(aggResult.entropy,
                   closeTo((5.27872957 + 5.2696308) / 2, 0.0001));
    }

    /**
     * Test a user with two movies.
     */
    @Test
    public void testTwoMovies() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));

        TestUser tu = createUser(42);
        ResultList recs = createRecommendations(903, 3993);

        double expected = 6.2741802;
        MetricResult mr = metric.measureUser(tu, 1, recs, ctx);
        assertThat(mr, instanceOf(TagEntropyMetric.TagEntropyResult.class));
        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) mr;

        assertThat(result.entropy, closeTo(expected, 0.0001));

        TagEntropyMetric.TagEntropyResult aggResult =  (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(aggResult, not(nullValue()));
        // average of 1 user's entropy is that entropy
        assertThat(aggResult.entropy,
                   closeTo(expected, 0.0001));
    }

    private double movieTagEntropy(int ntags) {
        // probability of 1 tag
        double prob = 1.0 / ntags;
        // entropy of ntags such tags, 1 movie
        return -ntags * prob * (Math.log(prob) / Math.log(2));
    }

    private ResultList createRecommendations(long... items) {
        List<Result> recs = Lists.newArrayList();
        for (long item: items) {
            recs.add(Results.create(item, 0));
        }
        return Results.newResultList(recs);

    }
    private TestUser createUser(long uid) {
        return new TestUser(Entities.create(CommonTypes.USER, uid),
                            Collections.emptyList(),
                            Collections.emptyList());
    }
}
