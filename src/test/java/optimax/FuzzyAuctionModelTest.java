package optimax;

import java.util.Random;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class FuzzyAuctionModelTest {
    
    FuzzyAuctionModel model;
    
    @Before
    public void setUp()  {
        model = new FuzzyAuctionModel();
        model.init(10, 100);
    }
    
    @Test
    public void testInit()  {
        Assert.assertEquals(0, model.getOnHandsQU());
        Assert.assertEquals(100, model.moneyInThePocket());
    }

    @Test
    public void testPlaceBid()  {
        Assert.assertEquals(10, model.placeBid());
    }

    @Test
    public void testRules()  {
        Random r = new Random();
        while (model.moneyInThePocket() >= 0) {
            int value = model.placeBid();
            model.bids(value, r.nextInt(50));
        }
        Assert.assertTrue(model.getOnHandsQU() >=0);
    }
    
}
