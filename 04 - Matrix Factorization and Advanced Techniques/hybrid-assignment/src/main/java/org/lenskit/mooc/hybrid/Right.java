package org.lenskit.mooc.hybrid;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Qualifier for the right item scorer.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface Right {
}
