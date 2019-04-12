package org.lenskit.mooc.cbf;

import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;

/**
 * Class containing entity type and attribute definitions for accessing tag data.
 */
public final class TagData {
    /**
     * The entity type for item tag applications.
     */
    public static final EntityType ITEM_TAG_TYPE = EntityType.forName("item-tag");

    public static final TypedName<Long> USER_ID = CommonAttributes.USER_ID;
    public static final TypedName<Long> ITEM_ID = CommonAttributes.ITEM_ID;
    public static final TypedName<Long> TIMESTAMP = CommonAttributes.TIMESTAMP;

    /**
     * The attribute for item tags.
     */
    public static final TypedName<String> TAG = TypedName.create("tag", String.class);
}
