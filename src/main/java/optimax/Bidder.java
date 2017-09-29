package optimax;

/**
 * Trading bot
 */
public class Bidder extends AbstractBidder {

    public Bidder(FuzzyAuctionModel model) {
        super(model);
    }
    
   public FuzzyAuctionModel getModel() {
        return  (FuzzyAuctionModel) model;
    }

    /**
     * @return next bid
     */
   @Override
    public int placeBid() {
        return getModel().placeBid();
    }
   
}
