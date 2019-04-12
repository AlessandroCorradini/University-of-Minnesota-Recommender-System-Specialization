package org.lenskit.mooc.cbf;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.history.History;
import org.lenskit.data.history.UserHistory;
import org.lenskit.data.ratings.Rating;

import java.util.Map;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ThresholdUserProfileBuilderTest {
    private EntityFactory factory = new EntityFactory();
    private TFIDFModel model;
    private ThresholdUserProfileBuilder profileBuilder;

    @Before
    public void buildModel() {
        model = TFIDFModelProviderTest.createModel();
        profileBuilder = new ThresholdUserProfileBuilder(model);
    }

    @Test
    public void testEmptyModel() throws Exception {
        UserHistory<Rating> empty = History.forUser(42);
        Map<String, Double> vector = profileBuilder.makeUserProfile(empty);
        assertThat(vector.size(), equalTo(0));
    }

    @Test
    public void testSingleItemVector() throws Exception {
        UserHistory<Rating> empty = History.forUser(42, factory.rating(42, 1, 4.0));
        Map<String, Double> vector = profileBuilder.makeUserProfile(empty);
        // item 1 only has 1 tag
        assertThat(vector.size(), equalTo(1));
        // are the vectors equal?
        assertThat(vector, equalTo(model.getItemVector(1)));
    }

    @Test
    public void testTwoItemVector() throws Exception {
        UserHistory<Rating> empty = History.forUser(42, factory.rating(42, 1, 4.0),
                                                    factory.rating(42, 2, 4.0));
        Map<String, Double> vector = profileBuilder.makeUserProfile(empty);
        // two tags!
        assertThat(vector.size(), equalTo(2));
        // check some sums
        assertThat(vector.get("walrus"),
                   closeTo(model.getItemVector(1).get("walrus") + model.getItemVector(2).get("walrus"),
                           1.0e-6));
    }

    @Test
    public void testOneItemVectorBecauseThreshold() throws Exception {
        UserHistory<Rating> empty = History.forUser(42, factory.rating(42, 1, 4.0),
                                                    factory.rating(42, 2, 2.0));
        Map<String, Double> vector = profileBuilder.makeUserProfile(empty);
        // item 1 only has 1 tag
        assertThat(vector.size(), equalTo(1));
        // are the vectors equal?
        assertThat(vector, equalTo(model.getItemVector(1)));
    }
}
