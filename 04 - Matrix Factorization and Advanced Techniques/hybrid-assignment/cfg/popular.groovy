import org.lenskit.api.ItemScorer
import org.lenskit.basic.PopularityRankItemScorer

// score items by their popularity
bind ItemScorer to PopularityRankItemScorer
// rating prediction is meaningless for this algorithm
bind RatingPredictor to null