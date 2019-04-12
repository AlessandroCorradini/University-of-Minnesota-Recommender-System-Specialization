import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.iterative.LearningRate
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.basic.FallbackItemScorer
import org.lenskit.basic.PrimaryScorer
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
import org.lenskit.mooc.hybrid.BlendWeight
import org.lenskit.mooc.hybrid.Left
import org.lenskit.mooc.hybrid.LinearBlendItemScorer
import org.lenskit.mooc.hybrid.Right
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

bind ItemScorer to LinearBlendItemScorer
bind (BaselineScorer, ItemScorer) to BiasItemScorer
bind BiasModel to UserItemBiasModel

for (blend in [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]) {
    algorithm("ii-svd-blend") {
        set BlendWeight to blend
        attributes["BlendWeight"] = blend

        bind (Left, ItemScorer) to ItemItemScorer
        set NeighborhoodSize to 20
        bind UserVectorNormalizer to BiasUserVectorNormalizer
        within (UserVectorNormalizer) {
            bind BiasModel to ItemBiasModel
        }

        bind (Right, ItemScorer) to FunkSVDItemScorer
        set FeatureCount to 40
        set IterationCount to 125
        set LearningRate to 0.0015
    }

    algorithm("lucene-svd-blend") {
        set BlendWeight to blend
        attributes["BlendWeight"] = blend

        bind (Left, ItemScorer) to ItemItemScorer
        set NeighborhoodSize to 20
        bind ItemItemModel to LuceneItemItemModel
        bind UserVectorNormalizer to BiasUserVectorNormalizer
        within (UserVectorNormalizer) {
            bind BiasModel to ItemBiasModel
        }

        bind (Right, ItemScorer) to FallbackItemScorer
        within (Right, ItemScorer) {
            bind (PrimaryScorer, ItemScorer) to FunkSVDItemScorer
            set FeatureCount to 40
            set IterationCount to 125
            set LearningRate to 0.0015
        }
    }
}