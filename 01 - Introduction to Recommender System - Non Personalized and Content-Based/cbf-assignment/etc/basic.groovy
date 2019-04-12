import org.lenskit.mooc.cbf.TFIDFItemScorer
import org.lenskit.mooc.cbf.ThresholdUserProfileBuilder
import org.lenskit.mooc.cbf.UserProfileBuilder
import org.lenskit.api.ItemScorer

// the core: use our item scorer
bind ItemScorer to TFIDFItemScorer
// with the basic profile builder
bind UserProfileBuilder to ThresholdUserProfileBuilder
