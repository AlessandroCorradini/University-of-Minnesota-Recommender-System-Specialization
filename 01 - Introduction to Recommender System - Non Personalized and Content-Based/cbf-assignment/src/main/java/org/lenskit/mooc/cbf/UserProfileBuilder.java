package org.lenskit.mooc.cbf;

import org.lenskit.data.ratings.Rating;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Builds a user profile from the user's ratings and the content model.  This is split
 * into a separate class so that we can have 2 different ones &mdash; weighted and
 * unweighted &mdash; and use the same code for the rest of the process.
 */
public interface UserProfileBuilder {
    /**
     * Create a user profile (weights over tags).
     *
     * @param ratings The user's history (their ratings).
     * @return A vector of tag weights describing the user's preferences.
     */
    Map<String,Double> makeUserProfile(@Nonnull List<Rating> ratings);
}
