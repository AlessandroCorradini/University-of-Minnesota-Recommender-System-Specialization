import org.lenskit.api.ItemScorer
import org.lenskit.bias.BiasItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.GlobalBiasModel

// score items by the global mean
bind ItemScorer to BiasItemScorer
bind BiasModel to GlobalBiasModel
// recommendation is meaningless for this algorithm
bind ItemRecommender to null