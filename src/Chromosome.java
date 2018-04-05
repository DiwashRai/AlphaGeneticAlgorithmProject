import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Chromosome {

    private List<Bin> binsList = new ArrayList<Bin>();
    private double fitness;

    public Chromosome (){}

    public Chromosome (Chromosome copy) {
        //copy instance values

        this.fitness = copy.fitness;
        for(int i=0; i < copy.getTotalBins(); ++i){
            this.binsList.add(new Bin(copy.getBin(i)));
        }
    }

    public void initRandomFirstFit(){

        // Create an arraylist of the integers from 1 to the total number of objects
        List<Item> itemsList = new ArrayList<>();
        for (int i = 0; i < AlphaGA.TOTAL_OBJECTS; ++i){
            itemsList.add(new Item(i));
        }

        // Shuffle the list to create a random list of numbers
        Collections.shuffle(itemsList);

        // Place items from the random list into a chromosome using the first fit algorithm
        firstFitListItems(itemsList);
    }

    public void setFitness(){
        double fill, capacity;
        double sigma = 0;

        for (int i =0; i < binsList.size(); ++i){
            fill = binsList.get(i).getLoad();
            capacity = binsList.get(i).getCapacity();
            sigma = sigma + Math.pow( (fill / capacity),AlphaGA.FITNESS_K_CONSTANT);
        }
        fitness = sigma / binsList.size();
    }

    public double getFitness() {return fitness;}

    private void firstFitListItems(List<Item> itemsList){
        Boolean itemAdded;
        for (int i = 0; i < itemsList.size(); ++i){ // iterate through each item in the item list in sequential order
            Item currentItem = itemsList.get(i);
            itemAdded = false;

            for (int j = 0; j < binsList.size(); ++j){  //Checks the list of bins from the top
                if (binsList.get(j).additem(currentItem)) {
                   itemAdded = true;
                   break;
                }
            }
            // Breaks to here
            if (!itemAdded){
                Bin bin = new Bin();
                bin.additem(currentItem);
                binsList.add(bin);
            }
        }
    }

    public Bin getBin (int i){return binsList.get(i);}

    public void mutateAndReplace(int numberOfMutations){
        List<Bin> binsToMutate = new ArrayList<Bin>();

        List<Item> replacementList = new ArrayList<Item>();
        List<Item> fillList = new ArrayList<Item>();

        // This loop extracts the bins from the bins list that are to be mutated and places them in the binsToMutate list
        for (int i = 0; i < numberOfMutations; ++i){
            Random generator = new Random();
            int binPos = generator.nextInt(binsList.size()); //Chooses the bin to mutate randomly and stores its position/index value.
            binsToMutate.add(binsList.get(binPos));
            binsList.remove(binPos);
        }

        //Now need to extract the individual items from all the bins in the binsToMutateList

        for (int i = 0; i < binsToMutate.size(); ++i) { //iterates through each bin in the binsList
            for (int j  = 0; j < binsToMutate.get(i).getTotalItems(); ++j){ //iterates through every item in the chosen bin
                replacementList.add(binsToMutate.get(i).getItem(j));
            }
        }

        // binsToMutate no longer required. Will clear to avoid accessing items that it refers to that are still necessary elsewhere
        binsToMutate.clear();

        // replacement step using the items from the replacementList
        for (int i = 0; i < replacementList.size(); ++i) {  // iterates through all of the items that are on the replacement list
            replaceWithItem(replacementList.get(i), fillList);
        }

        //replacementList no longer needed. will clear to avoid accidentally accessing objects that it refers to that are still needed
        replacementList.clear();

        // Will first fit remaining items after replacement step
        firstFitListItems(fillList);

        //fillList no longer needed. will clear to avoid accidentally accessing objects that it refers to that are still needed
        fillList.clear();

        // final touches to making the chromosome fully valid
        setFitness();
    }

    // The crossover process creates invalid chromosomes. This method rectifies that whilst preserving genes that are passed on.
    public void makeValid(int x, int y){
        List <Item> duplicateItemCheckList = new ArrayList<Item>();

        //Population.printChromosome(this);

        // Create the list of items that have to be checked for duplicates
        for (int i = x; i < y; ++i){  //iterates through the bins that were passed through crossover
            for (int j = 0; j < binsList.get(i).getTotalItems(); ++j){   //iterates through the contents of the bin
                duplicateItemCheckList.add(binsList.get(i).getItem(j));
            }
        }
        //printItemList(duplicateItemCheckList);

        // Remove all bins that contains any item in the duplicateItemCheckList
        int [] headTail = {x,y};
        for (int i = 0; i < duplicateItemCheckList.size();++i){   // iterates through the items that could have duplicates
            removeDuplicateBin(duplicateItemCheckList.get(i), headTail);
        }

        // create replacement list and fillList
        List<Item> replacementList = new ArrayList<Item>();
        List<Item> fillList = new ArrayList<Item>();

        // populate replacement list with all the items required to make the chromosome valid again
        for (int i = 0; i < AlphaGA.TOTAL_OBJECTS; ++i){
            if(itemAbsent(i)){
                replacementList.add(new Item(i));
            }
        }

        // implement my own version of replacement step mentioned in BPRX (bin packing crossover with replacement) as mentioned by Emanuel Falkenauer
        // in A hybrid grouping genetic algorithm for bin packing, Journal of Heuristics, 1996, Volume 2, Issue 1


        for (int i = 0; i < replacementList.size(); ++i) {  // iterates through all of the items that are on the replacement list
            replaceWithItem(replacementList.get(i), fillList);
        }

        //replacementList no longer needed. will clear to avoid accidentally accessing objects that it refers to that are still needed
        replacementList.clear();

        // Will first fit remaining items after replacement step
        firstFitListItems(fillList);

        //fillList no longer needed. will clear to avoid accidentally accessing objects that it refers to that are still needed
        fillList.clear();

        // final touches to making the chromosome fully valid
        setFitness();
    }

    private void replaceWithItem(Item item, List<Item> fillList){
        int itemValue = AlphaGA.getItemVal(item.getIndex());

        int replacementBinIndex;
        int itemCountToReplace;
        int valueOfItemsSubList;

        for (int i = 0; i < binsList.size(); ++i){      //iterates through the bins in the chromosome
            // Next loop iterates through considering replacing all the items in the bin. Then reducing the items considered by 1 each time
            replacementBinIndex = i;
            for (itemCountToReplace = binsList.get(i).getTotalItems(); itemCountToReplace > 0; --itemCountToReplace){
                valueOfItemsSubList = valueOfFirstNItemsInBin(replacementBinIndex, itemCountToReplace);

                if ( ((itemValue) > (valueOfItemsSubList))   &&   ((binsList.get(replacementBinIndex).getLoad() - valueOfItemsSubList + itemValue) <= AlphaGA.BIN_CAPACITY)   ){
                    for (int k = 0; k < itemCountToReplace; ++k){  // iterates through the number of items to replace
                        fillList.add(binsList.get(replacementBinIndex).getItem(0));
                        binsList.get(replacementBinIndex).removeItem(0);
                    }
                    binsList.get(replacementBinIndex).additem(item);
                    return;
                }
            }
        }

        fillList.add(item);
    }

    private int valueOfFirstNItemsInBin(int binIndex, int itemsToTotal){
        int total = 0;
        for (int i =0; i < itemsToTotal; ++i){
            total = total + AlphaGA.getItemVal(binsList.get(binIndex).getItem(i).getIndex());
        }
        return total;
    }

    private boolean itemAbsent(int itemID){
        for (int i = 0; i < binsList.size(); ++i){  // iterates through the bins in binList
           for (int j = 0; j < binsList.get(i).getTotalItems();++j){   //iterates through the items in the bin
               if(itemID == binsList.get(i).getItem(j).getIndex()){
                   return false;
               }
           }
        }

        return true;
    }


    // Checks the head and the tail part of chromosome for duplicates of given item and deletes the containing bin
    private void removeDuplicateBin(Item item, int [] headTail){
        //Check the head part
        for (int i = 0; i < headTail[0]; ++i){   //Iterates through the bins on the head of the chromosome
            for (int j = 0; j < binsList.get(i).getTotalItems(); ++j){ //Iterates through the items in the bin
                if(item.getIndex() == binsList.get(i).getItem(j).getIndex()){
                    binsList.remove(i);
                    headTail[0] = headTail[0] - 1;
                    headTail[1] = headTail[1] - 1;
                    return;
                }
            }
        }

        //Check the tail part
        for (int i = headTail[1]; i < binsList.size(); ++i){
            for (int j = 0; j < binsList.get(i).getTotalItems(); ++j){ //Iterates through the items in the bin
                if(item.getIndex() == binsList.get(i).getItem(j).getIndex()){
                    binsList.remove(i);
                    return;
                }
            }
        }
    }

    public List<Bin> getSubList (int a, int b){
        return binsList.subList(a , b);
    }

    public void clearSubListFromBinsList (int a, int b) {
        binsList.subList(a, b).clear();
    }

    public void addBin(Bin bin){ binsList.add(bin);}

    public void addBin(int i, Bin bin){
        binsList.add(i, bin);
    }

    public void removeBin (int i){
        binsList.remove(i);
    }

    public int getTotalBins (){
        return binsList.size();
    }

    public static void printItemList(List<Item> itemsList){
        System.out.println("=================");
        System.out.print("[");
        for (int i = 0; i < itemsList.size(); ++i) {
            if (AlphaGA.asIndex){
                System.out.print(itemsList.get(i).getIndex());
            } else {
                System.out.print(AlphaGA.getItemVal(itemsList.get(i).getIndex()));
            }

            if ( i < (itemsList.size()-1)){
                System.out.print(",");
            }
        }
        System.out.println("]");
    }
}
