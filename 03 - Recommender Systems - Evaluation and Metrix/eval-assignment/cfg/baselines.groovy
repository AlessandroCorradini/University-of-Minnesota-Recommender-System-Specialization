import org.lenskit.api.ItemScorer
import org.lenskit.basic.PopularityRankItemScorer
import org.lenskit.bias.*

algorithm("GlobalMean") {
    // score items by the global mean
    bind ItemScorer to BiasItemScorer
    bind BiasModel to GlobalBiasModel
    // recommendation is meaningless for this algorithm
    bind ItemRecommender to null
}
algorithm("Popular") {
    // score items by their popularity
    bind ItemScorer to PopularityRankItemScorer
    // rating prediction is meaningless for this algorithm
    bind RatingPredictor to null
}
algorithm("ItemMean") {
    // score items by their mean rating
    bind ItemScorer to BiasItemScorer
    bind BiasModel to ItemBiasModel
}
algorithm("PersMean") {
    bind ItemScorer to BiasItemScorer
    bind BiasModel to UserItemBiasModel
}
