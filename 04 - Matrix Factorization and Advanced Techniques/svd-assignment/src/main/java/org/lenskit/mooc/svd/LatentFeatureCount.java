package org.lenskit.mooc.svd;

import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Parameter controlling the number of latent features to retain.
 */
@Documented
@Qualifier
@Parameter(Integer.class)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LatentFeatureCount {
}
