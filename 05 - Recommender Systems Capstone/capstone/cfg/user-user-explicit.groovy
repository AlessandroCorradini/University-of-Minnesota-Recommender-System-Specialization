import org.lenskit.api.RatingPredictor
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.data.ratings.RatingVectorPDAO
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.user.SimilaritySumUserNeighborhoodScorer
import org.lenskit.knn.user.UserNeighborhoodScorer
import org.lenskit.knn.user.UserUserItemScorer
import org.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer

// use the user-user rating predictor
bind ItemScorer to UserUserItemScorer

set NeighborhoodSize to 30

include 'fallback.groovy'

bind VectorNormalizer to MeanCenteringVectorNormalizer