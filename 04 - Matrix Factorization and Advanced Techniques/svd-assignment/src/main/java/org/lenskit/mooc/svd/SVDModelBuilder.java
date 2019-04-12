package org.lenskit.mooc.svd;

import org.apache.commons.math3.linear.*;
import org.lenskit.bias.BiasModel;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.Transient;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.FrozenHashKeyIndex;
import org.lenskit.util.keys.KeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Model builder that computes the SVD model.
 */
public class SVDModelBuilder implements Provider<SVDModel> {
    private static final Logger logger = LoggerFactory.getLogger(SVDModelBuilder.class);

    private final DataAccessObject dao;
    private final BiasModel baseline;
    private final int featureCount;

    /**
     * Construct the model builder.
     * @param dao The data access object.
     * @param bias The bias model to use as a baseline.
     * @param nfeatures The number of latent features to train.
     */
    @Inject
    public SVDModelBuilder(@Transient DataAccessObject dao,
                           @Transient BiasModel bias,
                           @LatentFeatureCount int nfeatures) {
        this.dao = dao;
        baseline = bias;
        featureCount = nfeatures;
    }

    /**
     * Build the SVD model.
     *
     * @return A singular value decomposition recommender model.
     */
    @Override
    public SVDModel get() {
        // Create index mappings of user and item IDs.
        // You can use these to find row and columns in the matrix based on user/item IDs.
        KeyIndex userIndex = FrozenHashKeyIndex.create(dao.getEntityIds(CommonTypes.USER));
        KeyIndex itemIndex = FrozenHashKeyIndex.create(dao.getEntityIds(CommonTypes.ITEM));

        // We have to do 2 things:
        // First, prepare a matrix containing the rating data.
        // You will implement createRatingMatrix
        RealMatrix matrix = createRatingMatrix(userIndex, itemIndex);

        // Second, compute its factorization
        logger.info("factorizing matrix");
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        logger.info("decomposed matrix has rank {}", svd.getRank());

        // Third, truncate the decomposed matrix
        RealMatrix userMatrix = svd.getU();
        RealMatrix itemMatrix = svd.getV();
        RealVector weights = new ArrayRealVector(svd.getSingularValues());
        if (featureCount > 0) {
            logger.info("truncating matrix to {} features", featureCount);

            userMatrix = userMatrix.getSubMatrix(0, userIndex.size()-1,
                    0, featureCount-1);
            itemMatrix = itemMatrix.getSubMatrix(0, itemIndex.size()-1,
                    0, featureCount-1);
            weights = new ArrayRealVector(svd.getSingularValues(), 0, featureCount);
        }

        return new SVDModel(userIndex, itemIndex,
                userMatrix, itemMatrix,
                weights);
    }

    /**
     * Build a rating residual matrix from the rating data.  Each user's ratings are
     * normalized by subtracting a baseline score (usually a mean).
     *
     * @param userIndex The index mapping of user IDs to row numbers.
     * @param itemIndex The index mapping of item IDs to column numbers.
     * @return A matrix storing the <i>normalized</i> user ratings.
     */
    private RealMatrix createRatingMatrix(KeyIndex userIndex, KeyIndex itemIndex) {
        final int nusers = userIndex.size();
        final int nitems = itemIndex.size();

        // Create a matrix with users on rows and items on columns
        logger.info("creating {} by {} rating matrix", nusers, nitems);
        RealMatrix matrix = MatrixUtils.createRealMatrix(nusers, nitems);

        // populate it with data
        try (ObjectStream<Rating> ratings = dao.query(Rating.class)
                .stream()) {

            for (Rating r : ratings){
                double bias = baseline.getIntercept() + baseline.getUserBias(r.getUserId()) +
                        baseline.getItemBias(r.getItemId());
                matrix.setEntry(userIndex.getIndex(r.getUserId()),
                        itemIndex.getIndex(r.getItemId()),
                        r.getValue() - bias);
            }
        }

        return matrix;
    }
}