package org.lenskit.mooc;

import com.google.common.reflect.TypeToken;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.TypedName;

import java.util.List;
import java.util.Map;

/**
 * Accessors for attributes.
 */
public final class ItemData {
    private ItemData() {}

    public static final TypedName<String> ASIN = TypedName.create("asin", String.class);
    public static final TypedName<Double> AVAILABILITY = TypedName.create("availability", Double.class);
    public static final TypedName<String> TITLE = CommonAttributes.NAME;
    public static final TypedName<String> DESCRIPTION = TypedName.create("description", String.class);
    public static final TypedName<Double> PRICE = TypedName.create("price", Double.class);
    public static final TypedName<Map<String,Number>> SALES_RANK =
            TypedName.create("description", new TypeToken<Map<String, Number>>() {});
    public static final TypedName<String> BRAND = TypedName.create("brand", String.class);
    public static final TypedName<List<List<String>>> CATEGORIES =
            TypedName.create("categories", new TypeToken<List<List<String>>>() {});
}
