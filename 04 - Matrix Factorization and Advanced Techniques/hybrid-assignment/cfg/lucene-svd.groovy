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
import org.lenskit.mooc.hybrid.BlendWeight
import org.lenskit.mooc.hybrid.Left
import org.lenskit.mooc.hybrid.LinearBlendItemScorer
import org.lenskit.mooc.hybrid.Right
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

bind ItemScorer to LinearBlendItemScorer
bind (BaselineScorer, ItemScorer) to BiasItemScorer
bind BiasModel to UserItemBiasModel

bind (Left, ItemScorer) to ItemItemScorer
set NeighborhoodSize to 20
bind UserVectorNormalizer to BiasUserVectorNormalizer
bind ItemItemModel to LuceneItemItemModel
within (UserVectorNormalizer) {
    bind BiasModel to ItemBiasModel
}

bind (Right, ItemScorer) to FunkSVDItemScorer
set FeatureCount to 40
set IterationCount to 125
set LearningRate to 0.0015

def bw = System.getProperty("blendWeight")
set BlendWeight to (bw ? bw.toDouble() : 0.5)