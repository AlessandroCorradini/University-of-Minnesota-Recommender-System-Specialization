import org.grouplens.lenskit.iterative.IterationCount
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ZeroBiasModel
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.data.ratings.RatingVectorPDAO
import org.lenskit.mf.funksvd.FeatureCount
import org.lenskit.mf.funksvd.FunkSVDItemScorer

bind ItemScorer to FunkSVDItemScorer
set FeatureCount to 50
set IterationCount to 125

bind RatingPredictor to null
// Create a 0-1 vector based on whether the user rated the item
bind RatingVectorPDAO to EntityCountRatingVectorPDAO
set InteractionEntityType to CommonTypes.RATING
// MF on implicit data uses no bias
bind BiasModel to ZeroBiasModel