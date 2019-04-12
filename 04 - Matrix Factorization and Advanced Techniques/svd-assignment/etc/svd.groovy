import org.lenskit.api.ItemScorer
import org.lenskit.mooc.svd.LatentFeatureCount
import org.lenskit.mooc.svd.SVDItemScorer

// Set up item scorer
bind ItemScorer to SVDItemScorer
set LatentFeatureCount to 25