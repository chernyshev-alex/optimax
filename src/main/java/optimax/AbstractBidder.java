package optimax;

import java.util.logging.Logger;

public abstract class  AbstractBidder implements IBidder {

    protected static final Logger LOGGER = Logger.getLogger("bidder");

    protected final IBidder model;

    public AbstractBidder(IBidder model) {
       this.model = model;
    }
    
    @Override
    public void init(int quantity, int cash) {
        model.init(quantity, cash);
    }

    @Override
    public abstract int placeBid();

    @Override
    public void bids(int own, int other) {
        model.bids(own, other);
    }

}
