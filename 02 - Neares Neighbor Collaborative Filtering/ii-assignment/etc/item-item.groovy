import org.lenskit.api.ItemBasedItemScorer
import org.lenskit.api.ItemScorer
import org.lenskit.mooc.ii.SimpleItemBasedItemScorer
import org.lenskit.mooc.ii.SimpleItemItemScorer

// use our item scorer
bind ItemScorer to SimpleItemItemScorer
bind ItemBasedItemScorer to SimpleItemBasedItemScorer