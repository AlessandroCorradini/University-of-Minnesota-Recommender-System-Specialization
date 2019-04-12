package org.lenskit.mooc.cbf;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Basic tests for the TFIDF model builder.  These do not test value correctness, only that the
 * right tags are present.
 */
public class TFIDFModelProviderTest {
    private TFIDFModel model;

    static TFIDFModel createModel() {
        StaticDataSource data = new StaticDataSource("test");
        List<Entity> tags = new ArrayList<>();
        tags.add(Entities.newBuilder(TagData.ITEM_TAG_TYPE)
                         .setId(1)
                         .setAttribute(TagData.ITEM_ID, 1L)
                         .setAttribute(TagData.TAG, "walrus")
                         .build());
        tags.add(Entities.newBuilder(TagData.ITEM_TAG_TYPE)
                         .setId(2)
                         .setAttribute(TagData.ITEM_ID, 2L)
                         .setAttribute(TagData.TAG, "hamster")
                         .build());
        tags.add(Entities.newBuilder(TagData.ITEM_TAG_TYPE)
                         .setId(3)
                         .setAttribute(TagData.ITEM_ID, 2L)
                         .setAttribute(TagData.TAG, "walrus")
                         .build());
        tags.add(Entities.newBuilder(TagData.ITEM_TAG_TYPE)
                         .setId(4)
                         .setAttribute(TagData.ITEM_ID, 3L)
                         .setAttribute(TagData.TAG, "jubjub bird")
                         .build());

        data.addSource(tags);
        data.addDerivedEntity(CommonTypes.ITEM, TagData.ITEM_TAG_TYPE, TagData.ITEM_ID);

        TFIDFModelProvider mb = new TFIDFModelProvider(data.get());
        return mb.get();
    }

    @Before
    public void buildModel() {
        model = createModel();
    }

    @Test
    public void testItemOne() {
        // Vector for item 1 should just have 'walrus' tag
        Map<String, Double> v1 = model.getItemVector(1);
        assertThat(v1.keySet(), contains("walrus"));
    }

    @Test
    public void testItemTwo() {
        // Vector for item 2 should have 2 tags
        Map<String, Double> v2 = model.getItemVector(2);
        assertThat(v2.keySet(),
                   containsInAnyOrder("walrus", "hamster"));
    }

    @Test
    public void testItemThree() {
        // Vector for item 3 should have 1 tag again
        Map<String, Double> v3 = model.getItemVector(3);
        assertThat(v3.keySet(),
                   contains("jubjub bird"));
    }
}
