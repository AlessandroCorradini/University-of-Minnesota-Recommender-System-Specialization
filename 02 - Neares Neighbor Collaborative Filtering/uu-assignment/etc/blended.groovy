import org.lenskit.mooc.uu.SimpleUserUserItemScorer
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.basic.PopularityRankItemScorer
import org.lenskit.hybrid.BlendWeight
import org.lenskit.hybrid.RankBlendingItemRecommender

// use our item scorer
bind ItemScorer to SimpleUserUserItemScorer

// set up the blending
bind ItemRecommender to RankBlendingItemRecommender
within (RankBlendingItemRecommender.Right, ItemRecommender) {
    bind ItemScorer to PopularityRankItemScorer
}
set BlendWeight to 0.9
