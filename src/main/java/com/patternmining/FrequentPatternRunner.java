package com.patternmining;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UPDATE: 11/7 - Bonus HW for sequential mining
 */
public class FrequentPatternRunner {
    public void solve(int testNumber, Scanner scanner, PrintWriter out) {

    }

    public static class FrequentSequentialPatternUtility {
        protected List<TransactionItem>  frequentOneItemSets = null;
        protected List<FrequentSequentialTransaction> transactions = new ArrayList<>();
        protected Integer minimumSupport;
        private Integer minLength;
        private Integer maxLength;

        public FrequentSequentialPatternUtility(List<FrequentSequentialTransaction> transactions, Integer minimumSupport, Integer minLength, Integer maxLength) {
            this.transactions = transactions;
            this.minimumSupport = minimumSupport;
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        public FrequentSequentialPatternUtility(Integer minimumSupport, Integer minLength, Integer maxLength) {
            this.minimumSupport = minimumSupport;
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        public void addTransaction(FrequentSequentialTransaction transcation) {
            transactions.add(transcation);
        }

        public void parseTransactions(){
            List<TransactionItem> oneItemSets = new ArrayList<>();
            Map<TransactionItem, Integer> hashed = new HashMap<>();
            for (FrequentSequentialTransaction aTransaction: transactions ) {
                TransactionParserThread thread = new TransactionParserThread(aTransaction,oneItemSets,hashed);
                thread.run();
            }

//                Map<TransactionItem, Integer> kOneFrequentItems = hashed.entrySet().stream()
//            .filter(x -> x.getValue() >= minimumSupport)
//            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
//
//
//            Map<TransactionItem, Integer> sortedDescending = kOneFrequentItems
//                    .entrySet()
//                    .stream()
//                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

//            this.frequentOneItemSets = new ArrayList(sortedDescending.keySet());

            this.frequentOneItemSets = oneItemSets.stream()
                    .filter(item -> item.getSupport() >= minimumSupport)
                    .collect(Collectors.toList());
        }

        public List<TransactionSuffix> getFrequentSequentialPatterns() {

            List<TransactionSuffix> results = new ArrayList<>();
            mineDB(new TransactionSuffix(null,0),1,new ProjectionDatabase(new ArrayList<>()),results);

            return results;
        }


        public static class TransactionParserThread extends Thread {
            private FrequentSequentialTransaction anItem;
            List<TransactionItem> oneItemSets;
            Map<TransactionItem, Integer> hashed;

            public TransactionParserThread(FrequentSequentialTransaction anItemToParse, List<TransactionItem> oneItemSets,Map<TransactionItem, Integer> hashed) {
                this.anItem = anItemToParse;
                this.oneItemSets = oneItemSets;
                this.hashed = hashed;
            }

            public void run() {
                FrequentSequentialTransaction sequentlaiTransaction = (FrequentSequentialTransaction)anItem;
                sequentlaiTransaction.parseItemsThreaded(oneItemSets);
            }
        }

        public class ProjectionDatabase {

            public ProjectionDatabase(List<TransactionSuffix> suffixes) {
                List<TransactionSuffix> newSuffixes = new ArrayList<>();
                for (TransactionSuffix aSuffix:suffixes) {
                    TransactionSuffix newSuffix = new TransactionSuffix(aSuffix);
                    newSuffixes.add(newSuffix);
                }

                this.suffixes = newSuffixes;
            }

            private List<TransactionSuffix> suffixes;

            public List<TransactionSuffix> getSuffixes() {
                return suffixes;
            }


            List<TransactionItem> getFrequenItems(){
                Map<TransactionItem, Integer> frequentItemCounts = new HashMap<>();

                for (TransactionSuffix suffix: suffixes) {
                    for(int i=0;i<suffix.getItems().size();i++){
                        if(i>suffix.getSuffixStart()){
                            TransactionItem itemAtI = suffix.getItems().get(i);
                            if(frequentItemCounts.containsKey(itemAtI)){
                                Integer currentCount = frequentItemCounts.get(itemAtI);
                                currentCount++;
                                frequentItemCounts.put(itemAtI,currentCount);
                            }else{
                                frequentItemCounts.put(itemAtI,1);
                            }
                        }
                    }
                }

                return new ArrayList<>(frequentItemCounts.keySet());

            }


        }


        public void mineDB(TransactionSuffix sequentialPattern,Integer length,ProjectionDatabase sequenceProjectionDB, List<TransactionSuffix> sequentialPatterns){
            if(length > maxLength) return;

            if(sequentialPattern.getItems().size()==0){
                Map<TransactionItem, List<TransactionSuffix>> oneLengthPrefixDBs = generateLengthOneProjectionDBs();

                for (TransactionItem projection : oneLengthPrefixDBs.keySet()) {
                    sequentialPattern.setItems(new ArrayList<>());
                    sequentialPattern.getItems().add(projection);
                    if (sequentialPattern.getItems().size() >= minLength) {
                        sequentialPatterns = addToSequencesFoundAndIncrementCount(sequentialPatterns,new TransactionSuffix(sequentialPattern));
                    }
                    List<TransactionSuffix> newProjections = oneLengthPrefixDBs.get(projection);
                    mineDB(sequentialPattern, length + 1, new ProjectionDatabase(newProjections), sequentialPatterns);
                }

            }else{
                List<TransactionItem> frequentItemsInDB = sequenceProjectionDB.getFrequenItems();

                for (TransactionItem freqItem: frequentItemsInDB) {
                    for (TransactionSuffix suffixToTestHasSequence: sequenceProjectionDB.getSuffixes()) {
                        TransactionSuffix originalSequenceComingIn = new TransactionSuffix(sequentialPattern); //clone
                        originalSequenceComingIn.getItems().add(freqItem);
                        List<TransactionSuffix> newDBtoMine = new ArrayList<>();
                        TransactionSuffix newTransactionSuffix = new TransactionSuffix(suffixToTestHasSequence);
                        Integer indexOfSequence = newTransactionSuffix.getSequence(freqItem);
                        if(indexOfSequence != -1) {
                            newTransactionSuffix.setSuffixStart(indexOfSequence);
                            newDBtoMine.add(newTransactionSuffix);
                        }
                        if(newDBtoMine.size()>0){
                            sequentialPatterns = addToSequencesFoundAndIncrementCount(sequentialPatterns,new TransactionSuffix(originalSequenceComingIn));
                            mineDB(originalSequenceComingIn,length + 1,new ProjectionDatabase(newDBtoMine),sequentialPatterns);
                        }
                    }
                }
            }

        }

        private List<TransactionSuffix> addToSequencesFoundAndIncrementCount(List<TransactionSuffix> sequentialPatterns, TransactionSuffix newPatternFound) {

            if(sequentialPatterns.indexOf(newPatternFound) > -1){
                TransactionSuffix existingPatternFound = sequentialPatterns.get(sequentialPatterns.indexOf(newPatternFound));
                existingPatternFound.setSupport(existingPatternFound.getSupport()+1);
            }else{
                newPatternFound.setSupport(1);
                sequentialPatterns.add(newPatternFound);
            }

            return sequentialPatterns;
        }

        public Map<TransactionItem, List<TransactionSuffix>> generateLengthOneProjectionDBs() {
            Map<TransactionItem, List<TransactionSuffix>> oneLengthPrefixDBs = new LinkedHashMap<>();

            for (TransactionItem oneItemProjectionName : this.frequentOneItemSets) {
                List<TransactionSuffix> transactionSuffixes = new ArrayList<>();
                for (FrequentSequentialTransaction transaction : transactions) {
                    List<TransactionSuffix> mySuffixes = transaction.getTransactionIdIndexes(oneItemProjectionName);
                    if(mySuffixes != null) {
                        transactionSuffixes.addAll(mySuffixes);
                    }
                }

                oneLengthPrefixDBs.put(oneItemProjectionName,transactionSuffixes);
            }
            return oneLengthPrefixDBs;
        }

    }

    public static class FrequentSequentialTransaction {
        Map<TransactionItem,ArrayList<TransactionSuffix>> transactionItemToSuffix = new HashMap<>();
        List<TransactionItem> items = new ArrayList<TransactionItem>();
        private String line;

        public FrequentSequentialTransaction(String line) {
            this.line = line;
        }


        public void parseItemsThreaded(List<TransactionItem> oneItemSets){
            Map<TransactionItem,Integer> itemToCount = new HashMap<>();
            String[] itemsStr = this.getLine().split(" ");

            int index=0;
            for (int i=0;i<itemsStr.length;i++) {
                TransactionItem transactionItem = new TransactionItem(itemsStr[i]);
                items.add(transactionItem);
                addTransactionIdIndex(transactionItem,index);
                index++;
                int indexOf = -1;
                if(((indexOf = oneItemSets.indexOf(transactionItem)) > -1)){
                    oneItemSets.get(indexOf).incrementSupport();
                }else{
                    transactionItem.setSupport(1);
                    oneItemSets.add(transactionItem);
                }

            }
        }

        public void parseItemsThreaded2(Map<TransactionItem, Integer> hashed) {

            String[] itemsStr = this.getLine().split(" ");

            int index=0;
            for (int i=0;i<itemsStr.length;i++) {
                TransactionItem transactionItem = new TransactionItem(itemsStr[i]);
                items.add(transactionItem);
                addTransactionIdIndex(transactionItem,index);
                index++;
                if (hashed.containsKey(transactionItem)) {
                    Integer count = hashed.get(transactionItem);
                    count = count + 1;
                    transactionItem.setSupport(count);
                    hashed.remove(transactionItem);
                    hashed.put(transactionItem, count);
                } else {
                    hashed.put(transactionItem, 1);
                    transactionItem.setSupport(1);
                }
            }
        }


        public List<TransactionItem> getItems() {
            return items;
        }

        public String getLine() {
            return line;
        }

        public List<TransactionSuffix> getTransactionIdIndexes(TransactionItem itemToGetIndexesOf){
            return transactionItemToSuffix.get(itemToGetIndexesOf);
        }

        public void addTransactionIdIndex(TransactionItem item,Integer index) {
            List<TransactionSuffix> suffixForItem;
            if((suffixForItem = transactionItemToSuffix.get(item)) != null){
                suffixForItem.add(new TransactionSuffix(this.getItems(),index));
            }else{
                transactionItemToSuffix.put(item,new ArrayList<>(Arrays.asList(new TransactionSuffix(this.getItems(),index))));
            }
        }
    }

    public static class TransactionItem {


        public TransactionItem(String name) {
            this.name = name;
        }

        String name;
        Integer support = 0;

        public String getName() {
            return name;
        }

        public Integer getSupport() {
            return support;
        }

        public void incrementSupport(){
            support++;
        }

        public void setSupport(Integer support) {
            this.support = support;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionItem that = (TransactionItem) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

    }

    public static class TransactionSuffix {
        private List<TransactionItem> items;
        Integer support = 0;
        private String line;

        private int suffixStart = 0;

        public TransactionSuffix(TransactionSuffix transactionSuffix){
            List<TransactionItem> newItemList = new ArrayList<>();
            for (TransactionItem item: transactionSuffix.getItems()) {
                TransactionItem newItem = new TransactionItem(item.getName());
                Integer existingSupport = item.getSupport();
                Integer newSupport = new Integer(existingSupport.intValue());
                newItem.setSupport(newSupport);
                newItemList.add(newItem);
            }
            this.setItems(newItemList);
            int suffixStart = transactionSuffix.getSuffixStart();
            this.setSuffixStart(suffixStart);
        }

        public TransactionSuffix(List<TransactionItem> items,int suffixStart) {
            if(items==null){
                items = new ArrayList<>();
            }
            this.setItems(items);
            this.suffixStart = suffixStart;
        }

        public TransactionSuffix(List<TransactionItem> items) {
            if(items==null){
                items = new ArrayList<>();
            }
            this.setItems(items);
        }


        public int getSuffixStart() {
            return suffixStart;
        }

        public void setSuffixStart(int suffixStart) {
            this.suffixStart = suffixStart;
        }

        public List<TransactionItem> getItems() {
            return items;
        }

        public void setItems(List<TransactionItem> items) {
            this.items = items;
        }

        public Integer getSupport() {
            return support;
        }

        public void setSupport(Integer support) {
            this.support = support;
        }


        public String getLine() {
            this.line = "";
            for (TransactionItem item: items) {
                this.line  = this.line  + item.getName();
            }

            return this.line;
        }

        public Integer getSequence(TransactionItem nextItemIs) {
            for(int i =0 ;i<getItems().size();i++) {
                if (i > suffixStart) {//Starting at the position+1 of this suffix in the transaction, is the sequence there?
                    TransactionItem thisSuffixItem = getItems().get(i);
                    if(nextItemIs.equals(thisSuffixItem)){
                        return i;
                    }else{
                        break;
                    }
                }
            }
            return -1;
        }

        @Override
        public String toString() {
            String finalOutput = "[" + support + ", ";

            StringJoiner joiner = new StringJoiner(" ", "'", "'");
            for (TransactionItem anItem: getItems()) {
                joiner.add(anItem.getName());
            }


            finalOutput = finalOutput + joiner.toString() + "]";
            return finalOutput;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionSuffix that = (TransactionSuffix) o;
            return Objects.equals(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hash(items);
        }
    }
}
