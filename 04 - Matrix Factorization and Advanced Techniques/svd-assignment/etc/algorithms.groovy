import org.lenskit.api.ItemScorer
import org.lenskit.bias.*
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.mooc.svd.LatentFeatureCount
import org.lenskit.mooc.svd.SVDItemScorer
import org.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer

algorithm("PersMean") {
    bind ItemScorer to BiasItemScorer
    bind BiasModel to UserItemBiasModel
}

algorithm("ItemItem") {
    bind ItemScorer to ItemItemScorer
    bind VectorNormalizer to MeanCenteringVectorNormalizer
    set NeighborhoodSize to 20
}

// test different SVD sizes
for (size in [0, 1, 5, 10, 15, 20, 25, 30, 40, 50]) {
    algorithm("SVD") {
        attributes["FeatureCount"] = size
        attributes["Bias"] = "global"
        bind ItemScorer to SVDItemScorer
        set LatentFeatureCount to size
        // compute SVD of offsets from global mean
        bind BiasModel to GlobalBiasModel
    }

    algorithm("SVD") {
        attributes["FeatureCount"] = size
        attributes["Bias"] = "user"
        bind ItemScorer to SVDItemScorer
        set LatentFeatureCount to size
        // compute SVD of offsets from user mean
        bind BiasModel to UserBiasModel
    }

    algorithm("SVD") {
        attributes["FeatureCount"] = size
        attributes["Bias"] = "item"
        bind ItemScorer to SVDItemScorer
        set LatentFeatureCount to size
        // compute SVD of offsets from item mean
        bind BiasModel to ItemBiasModel
    }

    algorithm("SVD") {
        attributes["FeatureCount"] = size
        attributes["Bias"] = "user-item"
        bind ItemScorer to SVDItemScorer
        set LatentFeatureCount to size
        // compute SVD of offsets from item-user mean
        bind BiasModel to UserItemBiasModel
    }
}

