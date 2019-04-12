import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.data.ratings.RatingVectorPDAO
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.knn.item.ModelSize
import org.lenskit.knn.item.NeighborhoodScorer
import org.lenskit.knn.item.SimilaritySumNeighborhoodScorer
import org.lenskit.transform.normalize.UnitVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer

// use the user-user rating predictor
bind ItemScorer to ItemItemScorer

set NeighborhoodSize to 20
set ModelSize to 5000

bind RatingPredictor to null
bind NeighborhoodScorer to SimilaritySumNeighborhoodScorer

// Rating-based normalization is meaning
within (UserVectorNormalizer) {
    bind VectorNormalizer to UnitVectorNormalizer
}

// Create a 0-1 vector based on whether the user rated the item
bind RatingVectorPDAO to EntityCountRatingVectorPDAO
set InteractionEntityType to CommonTypes.RATING