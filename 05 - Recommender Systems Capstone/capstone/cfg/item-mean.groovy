import org.lenskit.api.ItemScorer
import org.lenskit.bias.BiasItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ItemBiasModel

// score items by their mean rating
bind ItemScorer to BiasItemScorer
bind BiasModel to ItemBiasModel