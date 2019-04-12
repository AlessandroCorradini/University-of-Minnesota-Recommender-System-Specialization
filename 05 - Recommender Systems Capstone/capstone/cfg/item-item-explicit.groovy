import org.lenskit.api.ItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ItemBiasModel
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.knn.item.ModelSize
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

// use the user-user rating predictor
bind ItemScorer to ItemItemScorer

set NeighborhoodSize to 20
set ModelSize to 5000

include 'fallback.groovy'

bind UserVectorNormalizer to BiasUserVectorNormalizer
within (UserVectorNormalizer) {
    bind BiasModel to ItemBiasModel
}