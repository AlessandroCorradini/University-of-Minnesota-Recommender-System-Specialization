import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.iterative.LearningRate
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.basic.TopNItemRecommender
import org.lenskit.bias.BiasItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ItemBiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.data.dao.DataAccessObject
import org.lenskit.data.ratings.RatingSummary
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.mf.funksvd.FeatureCount
import org.lenskit.mf.funksvd.FunkSVDItemScorer
import org.lenskit.mooc.hybrid.LogisticItemScorer
import org.lenskit.mooc.hybrid.LogisticModel
import org.lenskit.mooc.hybrid.LogisticTrainingSplit
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

bind ItemScorer to ItemItemScorer
set NeighborhoodSize to 20
bind UserVectorNormalizer to BiasUserVectorNormalizer
within (UserVectorNormalizer) {
    bind BiasModel to ItemBiasModel
}

bind (BaselineScorer, ItemScorer) to BiasItemScorer
bind BiasModel to UserItemBiasModel