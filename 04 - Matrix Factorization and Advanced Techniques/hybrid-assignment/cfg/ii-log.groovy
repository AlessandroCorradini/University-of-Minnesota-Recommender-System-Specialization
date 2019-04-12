import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.iterative.LearningRate
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.bias.BiasItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ItemBiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.knn.item.model.ItemItemModel
import org.lenskit.mf.funksvd.FeatureCount
import org.lenskit.mf.funksvd.FunkSVDItemScorer
import org.lenskit.mooc.cbf.LuceneItemItemModel
import org.lenskit.mooc.hybrid.LogisticItemScorer
import org.lenskit.mooc.hybrid.LogisticTrainingSplit
import org.lenskit.mooc.hybrid.RecommenderConfigurationList
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

def ii = {
    bind ItemScorer to ItemItemScorer
    set NeighborhoodSize to 20
    bind UserVectorNormalizer to BiasUserVectorNormalizer
    within (UserVectorNormalizer) {
        bind BiasModel to ItemBiasModel
    }

    bind (BaselineScorer, ItemScorer) to BiasItemScorer
    bind BiasModel to UserItemBiasModel
}

addComponent RecommenderConfigurationList.create(ii)

bind ItemScorer to LogisticItemScorer
set LogisticTrainingSplit.TrainingBalance to 2.0

bind (BaselineScorer, ItemScorer) to BiasItemScorer
bind BiasModel to UserItemBiasModel