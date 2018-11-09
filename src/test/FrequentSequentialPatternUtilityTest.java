import com.patternmining.FrequentPatternRunner;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class FrequentSequentialPatternUtilityTest {


    @Test
    public void getFrequentOneLengthPrefixDBs_test_Suffix_Are_Correct() {
        FrequentPatternRunner.FrequentSequentialTransaction transaction1 = new FrequentPatternRunner.FrequentSequentialTransaction("good grilled fish sandwich and french fries , but the service is bad");
        FrequentPatternRunner.FrequentSequentialTransaction transaction2 = new FrequentPatternRunner.FrequentSequentialTransaction("disgusting fish sandwich , but good french fries");
        FrequentPatternRunner.FrequentSequentialTransaction transaction3 = new FrequentPatternRunner.FrequentSequentialTransaction("their grilled fish sandwich is the best fish sandwich , but pricy");
        FrequentPatternRunner.FrequentSequentialTransaction transaction4 = new FrequentPatternRunner.FrequentSequentialTransaction("A B A B A B A");
        List<FrequentPatternRunner.FrequentSequentialTransaction>transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);


        FrequentPatternRunner.FrequentSequentialPatternUtility fpUtility = new FrequentPatternRunner.FrequentSequentialPatternUtility(transactions,2,2,5);
        fpUtility.parseTransactions();
        List<FrequentPatternRunner.TransactionSuffix> results = fpUtility.getFrequentSequentialPatterns();

        formatOutput(results);
    }

    private void formatOutput(List<FrequentPatternRunner.TransactionSuffix> results) {

        List<FrequentPatternRunner.TransactionSuffix> countsMinSupport = results.stream()
                .filter(line -> line.getSupport() >= 2).collect(Collectors.toList());

        countsMinSupport.sort(Comparator.comparing(FrequentPatternRunner.TransactionSuffix::getSupport).reversed()
                .thenComparing(Comparator.comparing(a -> a.getLine())));


        String frequentItemsOutput = "";
        for (FrequentPatternRunner.TransactionSuffix itemExt: countsMinSupport) {
            frequentItemsOutput = frequentItemsOutput + "\n" + itemExt;
        }

        System.out.println(frequentItemsOutput.trim());
    }

}


