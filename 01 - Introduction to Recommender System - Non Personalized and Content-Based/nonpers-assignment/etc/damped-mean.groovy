import org.lenskit.api.ItemBasedItemRecommender
import org.lenskit.baseline.MeanDamping
import org.lenskit.mooc.nonpers.mean.DampedItemMeanModelProvider
import org.lenskit.mooc.nonpers.mean.ItemMeanModel
import org.lenskit.mooc.nonpers.mean.MeanItemBasedItemRecommender

// set up the recommender
bind ItemBasedItemRecommender to MeanItemBasedItemRecommender

// this time, we will use the damped mean model
bind ItemMeanModel toProvider DampedItemMeanModelProvider

// use a mean damping of 5
set MeanDamping to 5
