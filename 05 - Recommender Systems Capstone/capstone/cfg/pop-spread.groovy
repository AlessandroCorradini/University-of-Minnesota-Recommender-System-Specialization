import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.basic.PopularityRankItemScorer
import org.lenskit.basic.TopNItemRecommender
import org.lenskit.mooc.CategorySpreadItemRecommender

// score items by their popularity
bind ItemScorer to PopularityRankItemScorer
// rating prediction is meaningless for this algorithm
bind RatingPredictor to null

// Use the category-spread recommender
bind ItemRecommender to CategorySpreadItemRecommender
within (CategorySpreadItemRecommender) {
    bind ItemRecommender to TopNItemRecommender
}