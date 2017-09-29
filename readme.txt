Fuzzy Bot

Bot uses fuzzy logic for trading

@see references about fuzzy logic 
http://ffll.sourceforge.net/inside_the_code.htm
https://www.mathworks.com/help/fuzzy/

I used https://www.fuzzylite.com/qt/

Many trading strategies could be developed and tested in a short time
using FCL language and implemented later in java code.
example : scripts/bot1.fcl

Trading strategy

Rules are very naive and just only for demo purposes.
Rules : 
  Class FuzzyAuctionModel#createRules() presents trading rules in a human readable format

** Warning ** Don't use this bot in a real trading

How to build and test

mvn clean package

# execute tests
mvn test

How to run 

mvn exec:java -Dexec.mainClass=optimax.Optimax -Dexec.args="-mu 100 -qu 10"













