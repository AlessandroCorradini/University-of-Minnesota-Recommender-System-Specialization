package org.lenskit.mooc.svd;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * SVD model for collaborative filtering.
 */
@Shareable
@DefaultProvider(SVDModelBuilder.class)
public class SVDModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final KeyIndex userMapping;
    private final KeyIndex itemMapping;
    private final RealMatrix userFeatureMatrix;
    private final RealMatrix itemFeatureMatrix;
    private final RealVector featureWeights;

    /**
     * Construct an SVD model.  The matrices represent the decomposition, such that the predictions
     * are equal to {@code umat * weights * imat.transpose()}.
     *  @param umap The mapping between user IDs and row numbers.
     * @param imap The mapping between item IDs and row numbers.
     * @param umat The user feature matrix (users x features).
     * @param imat The item feature matrix (items x features).
     * @param weights The singular values.
     */
    SVDModel(KeyIndex umap, KeyIndex imap, RealMatrix umat, RealMatrix imat, RealVector weights) {
        Preconditions.checkArgument(umat.getColumnDimension() == weights.getDimension(),
                                    "user matrix has incorrect column dimension (%s != %s)",
                                    umat.getColumnDimension(), weights.getDimension());
        Preconditions.checkArgument(imat.getColumnDimension() == weights.getDimension(),
                                    "item matrix has incorrect column dimension (%s != %s)",
                                    imat.getColumnDimension(), weights.getDimension());
        userMapping = umap;
        itemMapping = imap;
        userFeatureMatrix = umat;
        itemFeatureMatrix = imat;
        featureWeights = weights;
    }

    /**
     * Get the feature weights.  This is a diagonal matrix.
     * @return The diagonal matrix of feature weights.
     */
    public RealVector getFeatureWeights() {
        return featureWeights;
    }

    /**
     * Get a user feature vector. This is a row vector whose values (columns) are the feature
     * values for a particular user.
     *
     * @param user The user ID.
     * @return The feature vector for user {@code user}, or {@code null} if the user is unkonwn.
     */
    @Nullable
    public RealVector getUserVector(long user) {
        long row = userMapping.tryGetIndex(user);
        if (row >= 0) {
            return userFeatureMatrix.getRowVector((int) row);
        } else {
            return null;
        }
    }

    /**
     * Get a item feature vector. This is a row vector whose values (columns) are the feature
     * values for a particular item.
     *
     *
     * @param item The item ID.
     * @return The feature vector for item {@code item}.
     */
    public RealVector getItemVector(long item) {
        long row = itemMapping.tryGetIndex(item);
        if (row >= 0) {
            return itemFeatureMatrix.getRowVector((int) row);
        } else {
            return null;
        }
    }

    /**
     * Get a item feature vector matrix.  Its rows are items and its columns are latent features.
     *
     * @return The item-feature matrix (this must not be modified).
     */
    public RealMatrix getItemFeatureMatrix() {
        return itemFeatureMatrix;
    }

    /**
     * Get a user feature vector matrix.  Its rows are users and its columns are latent features.
     *
     * @return The user-feature matrix (this must not be modified).
     */
    public RealMatrix getUserFeatureMatrix() {
        return userFeatureMatrix;
    }

    /**
     * Get the user index mapping.
     * @return The mapping between user IDs and matrix row numbers.
     */
    public KeyIndex getUserIndexMapping() {
        return userMapping;
    }

    /**
     * Get the item index mapping.
     * @return The mapping between item IDs and matrix row numbers.
     */
    public KeyIndex getItemIndexMapping() {
        return itemMapping;
    }

    /**
     * Get the row number for an item in the item-feature matrix.
     * @param item The item ID.
     * @return The row number for the item.
     * @throws IllegalArgumentException if the item is unknown.
     */
    public int getItemRow(long item) {
        return itemMapping.getIndex(item);
    }
}
