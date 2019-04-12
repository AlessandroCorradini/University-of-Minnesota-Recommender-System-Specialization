import org.lenskit.api.RatingPredictor
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.data.ratings.RatingVectorPDAO
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.user.SimilaritySumUserNeighborhoodScorer
import org.lenskit.knn.user.UserNeighborhoodScorer
import org.lenskit.knn.user.UserUserItemScorer

// use the user-user rating predictor
bind ItemScorer to UserUserItemScorer

set NeighborhoodSize to 30

bind RatingPredictor to null
bind UserNeighborhoodScorer to SimilaritySumUserNeighborhoodScorer

// Use 0-1 rating vectors for implicit data
bind RatingVectorPDAO to EntityCountRatingVectorPDAO
set InteractionEntityType to CommonTypes.RATING