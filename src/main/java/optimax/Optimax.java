package optimax;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Optimax {

    private static final Logger LOGGER = Logger.getLogger(Optimax.class.getName());

    static final String[] options = new String[]{"-mu", "-qu"};

    final Bidder bidder = new Bidder(new FuzzyAuctionModel());

    private Optimax configure(Map<String, Integer> config) {

        int MU = config.get(options[0]);
        int QU = config.get(options[1]);

        bidder.init(QU, MU);

        System.out.println(String.format("started with MU/QU : %d/%d", MU, QU));
        return this;
    }

    private void run() {
        
        Optional<Integer> placedBidOpt = Optional.empty();

        while (true) {
            
            if (! placedBidOpt.isPresent()) {
                placedBidOpt = Optional.of(bidder.placeBid());
            }

            System.out.print(String.format("my bid : %d MU, input 'quit' or your bid : ", placedBidOpt.get()));
            String line = System.console().readLine();
                if (line.equalsIgnoreCase("Quit")) {
                    printTradeResult();
                    break;
                }

                try {
                    Integer otherBid = Integer.parseInt(line);
                    bidder.bids(placedBidOpt.get(), otherBid);
                    
                    placedBidOpt = Optional.empty();
                } catch (NumberFormatException ne) {
                    LOGGER.log(Level.SEVERE, null, ne);
                }
        }
    }

    private void printTradeResult() {
        FuzzyAuctionModel m = bidder.getModel();
        System.out.println(String.format("I bought QU %d, MU left %d ", m.getOnHandsQU(), m.moneyInThePocket()));
    }
    
    private static Map parseOptions(String[] args) throws ParseException {
        if (args.length != 4) {
            throw new ParseException(Arrays.toString(args), 0);
        }

        Map<String, Integer> cmdLine = new HashMap();
        Iterator<String> it = Arrays.asList(args).iterator();
        cmdLine.put(it.next().toLowerCase().trim(), Integer.parseInt(it.next()));
        cmdLine.put(it.next().toLowerCase().trim(), Integer.parseInt(it.next()));
        return cmdLine;
    }

    private static void showHelp() {
        System.out.println(String.format("Usage:\n java -jar optimax.jar -mu 1000 -qu 100"
                + "\nwhere : mu - monetary units (int)\n qu - quantity units on store to trade"
                + "\ncommands : \n quit "));
    }

    public static void main(String[] args)  {
        try {
            new Optimax().configure(parseOptions(args)).run();
            System.exit(0);
        } catch (ParseException ex) {
            showHelp();
            System.exit(-1);
        }
    }
}
