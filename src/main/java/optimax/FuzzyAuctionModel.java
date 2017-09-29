package optimax;

import com.fuzzylite.Engine;
import com.fuzzylite.activation.General;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.AlgebraicSum;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Gaussian;
import com.fuzzylite.term.Trapezoid;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;

/**
 * Fuzzy logic auction model
 */
public final class FuzzyAuctionModel implements IBidder {

    private int initialMU, initialQU;
    private int optimalBuyPricePerUnit;
    private int lastWinBid;
    private int onHandsQU;
    private int expenses;

    private Engine engine;

    /**
     * @return buyed units
     */
    public int getOnHandsQU() {
        return onHandsQU;
    }

    /**
     * @return current money units in the pocket
     */
    public int moneyInThePocket() {
        return initialMU - expenses;
    }

    /**
     * Initialize fuzzy engine
     * 
     * Fuzzy input  variable : 
     *   Divergence - last execution price - offer
     *   Budget - current moneys in the pocket
     * 
     * Fuzzy outpur variable : 
     *   Buy - buy price
     * 
     * @param quantity - need to buy
     * @param cash - initial cache
     */
    @Override
    public void init(int quantity, int cash) {

        optimalBuyPricePerUnit = cash / quantity;
        initialMU = cash;
        initialQU = quantity;
        this.onHandsQU = 0;
        

        engine = new Engine();
        engine.setName("bot");

        engine.addInputVariable(configureVarDivergence(optimalBuyPricePerUnit));
        engine.addInputVariable(configureVarBudget(initialMU));
        engine.addOutputVariable(configureVarBuy(optimalBuyPricePerUnit));
        engine.addRuleBlock(createRules());
    }

    /**
     * Trigger rules and calculate bid depended from input fuzzy vars
     * 
     * @return bid price
     */
    @Override
    public int placeBid() {

        InputVariable budget = engine.getInputVariable("BUDGET");
        InputVariable div = engine.getInputVariable("DIVERGENCE");
        budget.setValue(moneyInThePocket());
        div.setValue(Math.abs(lastWinBid - optimalBuyPricePerUnit));

        // trigger fuzzy rules
        engine.process();

        double buy = engine.getOutputValue("BUY");
        
        /*
        System.out.println(String.format("lastWinBid=%d, optimalBuyPricePerUnit=%d, moneyInThePocket=%d, onhands=%d, budget =%f; div =%f, buy=%f", 
                lastWinBid, optimalBuyPricePerUnit, moneyInThePocket(), this.onHandsQU,
                budget.getValue(), div.getValue(), buy));
        */
        
        return (int) Math.round(buy);
    }

    
    /*
    * Presents outcome i-th currrent trade
    */
    @Override
    public void bids(int own, int other) {
        if (own == other) {
            updateWith(new Trade(1, own, other, Trade.EOUTCOME.PARTIAL));
        } else if (own > other) {
            updateWith(new Trade(2, own, other, Trade.EOUTCOME.WIN));
        } else {
            updateWith(new Trade(0, own, other, Trade.EOUTCOME.LOSS));
        }
    }

    /*
    * Update valuable parameters
    */
    private void updateWith(Trade trade) {
        switch (trade.outcome) {
            case LOSS:
                lastWinBid = trade.otherBid;
                break;
            case PARTIAL:
                lastWinBid = trade.bid;
                break;
            case WIN:
               lastWinBid = trade.bid;
        }

        this.expenses += trade.bid;
        this.onHandsQU += trade.quantity;
    }

    private static class Trade {
        public enum EOUTCOME {
            LOSS, PARTIAL, WIN
        }
        int quantity, bid, otherBid;
        EOUTCOME outcome;   //  loss/1:1 win/full win

        public Trade(int quantity, int bid, int otherBid, EOUTCOME outcome) {
            this.quantity = quantity;
            this.bid = bid;
            this.otherBid = otherBid;
            this.outcome = outcome;
        }
    }
    
    // COnfigure fuzzy inference engine

    private InputVariable configureVarDivergence(double optimalPrice) {
        InputVariable divergenceVar = new InputVariable();
        divergenceVar.setName("DIVERGENCE");
        divergenceVar.setDescription("divergence from optimal price");
        divergenceVar.setEnabled(true);
        divergenceVar.setRange(0.000, optimalPrice * 3);
        divergenceVar.setLockValueInRange(true);
        divergenceVar.addTerm(new Trapezoid("LOWER", 0.000, 0.000, optimalPrice / 2, optimalPrice));
        divergenceVar.addTerm(new Gaussian("NORMAL", optimalPrice, optimalPrice / 10));
        divergenceVar.addTerm(new Trapezoid("ABOVE", optimalPrice, optimalPrice + optimalPrice / 2, optimalPrice * 3, optimalPrice * 3));
        return divergenceVar;
    }

    private InputVariable configureVarBudget(double budget) {
        InputVariable budgetVar = new InputVariable();
        budgetVar.setName("BUDGET");
        budgetVar.setEnabled(true);
        budgetVar.setRange(0.000, budget);
        budgetVar.setLockValueInRange(true);
        budgetVar.addTerm(new Trapezoid("EXHAUSTED", 0.000, 0.0, budget / 3, budget / 1.5));
        budgetVar.addTerm(new Trapezoid("NORMAL", budget / 4, budget / 1.5, budget, budget));
        return budgetVar;
    }

    private OutputVariable configureVarBuy(double optimalPrice) {
        OutputVariable buyVar = new OutputVariable();
        buyVar.setDescription("generate bid");
        buyVar.setName("BUY");
        buyVar.setDescription("Mamdani inference");
        buyVar.setEnabled(true);
        buyVar.setRange(0.000, optimalPrice * 3);
        buyVar.setLockValueInRange(false);
        buyVar.setAggregation(new Maximum());
        buyVar.setDefuzzifier(new Centroid(100));
        buyVar.setDefaultValue(optimalBuyPricePerUnit);
        buyVar.setLockPreviousValue(false);

        buyVar.addTerm(new Trapezoid("SKIP", optimalPrice, optimalPrice + optimalPrice / 2, optimalPrice * 3, optimalPrice * 3));
        buyVar.addTerm(new Gaussian("NORMAL", optimalPrice, optimalPrice / 10));
        buyVar.addTerm(new Trapezoid("LOWER", 0.000, 0.000, optimalPrice / 2, optimalPrice));
        return buyVar;
    }

    private RuleBlock createRules() {
        RuleBlock mamdani = new RuleBlock();
        mamdani.setName("mamdani");
        mamdani.setDescription("Mamdani inference");
        mamdani.setEnabled(true);
        mamdani.setConjunction(new AlgebraicProduct());
        mamdani.setDisjunction(new AlgebraicSum());
        mamdani.setImplication(new Minimum());
        mamdani.setActivation(new General());
        mamdani.addRule(Rule.parse("if BUDGET is NORMAL and DIVERGENCE is LOWER then BUY is LOWER", engine));
        mamdani.addRule(Rule.parse("if BUDGET is NORMAL and DIVERGENCE is NORMAL then BUY is NORMAL", engine));
        mamdani.addRule(Rule.parse("if BUDGET is NORMAL and DIVERGENCE is ABOVE then BUY is NORMAL", engine));
        mamdani.addRule(Rule.parse("if BUDGET is EXHAUSTED and DIVERGENCE is LOWER then BUY is LOWER", engine));
        mamdani.addRule(Rule.parse("if BUDGET is EXHAUSTED and DIVERGENCE is NORMAL then BUY is SKIP", engine));
        mamdani.addRule(Rule.parse("if BUDGET is EXHAUSTED and DIVERGENCE is ABOVE then BUY is SKIP", engine));
        return mamdani;
    }


}
