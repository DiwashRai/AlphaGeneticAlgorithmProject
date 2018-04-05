import com.sun.org.apache.bcel.internal.generic.ALOAD;

import java.util.*;

public class Population {
    private List<Chromosome> chromosomePopulation = new ArrayList<Chromosome>();

    List<Chromosome> currentParentsList = new ArrayList<Chromosome>();
    List<Chromosome> offspringList = new ArrayList<Chromosome>();

    public static Population initialisePopulation(){
        Population population = new Population();

        for (int i = 0; i < AlphaGA.POPULATION_SIZE; ++i){
            Chromosome chromosome = new Chromosome();
            chromosome.initRandomFirstFit();
            chromosome.setFitness();
            population.addChromosome(chromosome);
        }

        return population;
    }

    public void sortPopulationByFittestFirst(){
        Collections.sort(chromosomePopulation, new MyChromosomeComparator());
    }

    public void mutation(){

        Random generator = new Random();
        for (int i = 0; i < chromosomePopulation.size(); ++i){ // iterates through the population of chromosomes

            if(generator.nextInt(100) < AlphaGA.MUTATION_PERCENTAGE_CHANCE){   // rolls to check if mutation should occur or not for this chromosome
                int totalBins = chromosomePopulation.get(i).getTotalBins();
                int genesPerMutation = AlphaGA.GENES_PER_MUTATED_GENE;

                // initialise and calculate how many bins to mutate in this chromosome
                int genesToMutate = (totalBins + genesPerMutation + 1) / genesPerMutation;
                chromosomePopulation.get(i).mutateAndReplace(genesToMutate);  //mutates the i'th chromosome of the population
            }
        }

    }

    public void selectSurvivors(){
        // Sort the population from highest to lowest fitness
/*        Collections.sort(chromosomePopulation, new MyChromosomeComparator());

        // Remove enough parents to add the offspring into the population and maintain a constant population size
        // parents will be removed starting from the worst
        for (int i = 0;i < (offspringList.size()); ++i){
            chromosomePopulation.remove(chromosomePopulation.size()-1);
        }

        // add offspring to the end of the chromosomePopulation list
        chromosomePopulation.addAll(offspringList);

        // remove all offspring objects from the offspringList
        System.out.println(offspringList.size());
        offspringList.clear();
        */

        int numberOfOffspring = offspringList.size();
        chromosomePopulation.addAll(offspringList);
        Collections.sort(chromosomePopulation, new MyChromosomeComparator());

        for (int i =0; i < numberOfOffspring; ++i){
            chromosomePopulation.remove(AlphaGA.POPULATION_SIZE);
        }

//        System.out.println(offspringList.size());
        offspringList.clear();
    }

    public void selectionAndCrossover(){
        // selectionType 0 = tournament. 1 = ranking. 2 = alphaMale
        switch (AlphaGA.PARENT_SELECTION_TYPE) {
            case 0:
                // The selection and crossover chooses TWO parents and creates a PAIR of offspring.
                // Therefore the loop needs to repeat half the offspring desired number of times
                for(int i = 0; i < (AlphaGA.OFFSPRING_PER_GENERATION); i++){
                    currentParentsList.clear();
                    tournamentSelectParents();  //produces and adds two parents to the currentParentsList
                    crossover();  //produces and adds two viable offspring to the offspring's list from the parents
                }

                break;

            case 1:
                // The selection and crossover chooses TWO parents and creates a PAIR of offspring.
                // Therefore the loop needs to repeat half the offspring desired number of times
                for(int i = 0; i < (AlphaGA.OFFSPRING_PER_GENERATION); i++){
                    currentParentsList.clear();
                    rankingSelectParents(); //produces and adds two parents to the currentParentsList
                    crossover(); //produces and adds two viable offspring to the offspring's list from the parents
                }

                break;

            case 2:
                originalAlphaMaleSelectionAndCrossover();
                break;

            // Test selection technique. Currently not functional
//            case 3:
//                myAlphaMaleSelectionAndCrossover();
//                break;


            default:
                System.out.println("ERROR!! Invalid parent selection type entered. Choose 0 for tournament, 1 for ranking and 2 for Alpha male.");
                System.exit(0);
                break;
        }


    }

    private void originalAlphaMaleSelectionAndCrossover(){

        // Population will be split into male and female.
        List<Chromosome> alphaMales = new ArrayList<Chromosome>();
        List<Chromosome> females = new ArrayList<Chromosome>();

        // Add all the population members into a list for females to begin with
        females.addAll(chromosomePopulation);

        // take K number of chromosomes to be the alpha males from the list of females
        Random generator = new Random();
        //int totalAlphaMales = generator.nextInt(6) + 20;
        int totalAlphaMales = AlphaGA.K_ALPHA_MALES;

        // Sort from fittest first if alpha males are to be chosen by fittest first
        if (AlphaGA.PICK_FITTEST_MALES){
            Collections.sort(females, new MyChromosomeComparator());
        } else {
            Collections.shuffle(females);
        }

        for (int i =0; i < totalAlphaMales; ++i){
            alphaMales.add(females.get(0));
            females.remove(i);
        }

        // iterate through all females crossing them over with their chosen alpha males
        for (int i = 0; i < females.size(); ++i){
            currentParentsList.clear();
            currentParentsList.add(females.get(i));
            currentParentsList.add(alphaMales.get(i % totalAlphaMales));

            crossover();
        }
    }

    private void myAlphaMaleSelectionAndCrossover(){

        // Population will be split randomly into male and female.
        List<Chromosome> allMale = new ArrayList<Chromosome>();
        List<Chromosome> allFemale = new ArrayList<Chromosome>();

        // Will use an intermediate list to ensure no chromosomes are duplicated when being split into male and female
        List<Chromosome> intermediate = new ArrayList<Chromosome>();
        intermediate.addAll(chromosomePopulation);

        int populationSize = intermediate.size();

        //
        for (int i = 0; i < populationSize/2; ++i) {
            Random generator = new Random();
            int randomIndex;
            // add one random male to the male list. And remove that male from the intermediate list
            randomIndex = generator.nextInt(intermediate.size());
            allMale.add(intermediate.get(randomIndex));
            intermediate.remove(randomIndex);

            // add one random female to the female list. And remove that female from the intermediate list
            randomIndex = generator.nextInt(intermediate.size());
            allFemale.add(intermediate.get(randomIndex));
            intermediate.remove(randomIndex);
        }

        // Pick the alpha males from the allMales list
        List<Chromosome> alphaMales = new ArrayList<Chromosome>();
        int totalAlphaMales = AlphaGA.K_ALPHA_MALES;

        // Sort from fittest first if alpha males are to be chosen by fittest first
        if (AlphaGA.PICK_FITTEST_MALES){Collections.sort(allMale, new MyChromosomeComparator());}

        for (int i =0; i < totalAlphaMales; ++i){
            alphaMales.add(allMale.get(0));
        }

        // iterate through all females crossing them over with their chosen alpha males
        for (int i = 0; i < allFemale.size(); ++i){
            currentParentsList.clear();
            currentParentsList.add(allFemale.get(i));
            currentParentsList.add(allFemale.get(i % totalAlphaMales));

            crossover();
        }
    }

    private void rankingSelectParents(){
        Collections.sort(chromosomePopulation, new MyChromosomeComparator());

        int populationSize = chromosomePopulation.size();
        int totalFitness = (populationSize + 1) * populationSize / 2;

        Random generator = new Random();
        int numberOfParentsToSelect = 2;  //Should always be 2 as there are usually two parents for each crossover

        // Loops to choose however many parents you want to generate per crossover
        for (int i = 0; i < numberOfParentsToSelect; ++i ){  // Loops twice to choose two parents
            int chosenValue = (generator.nextInt(totalFitness));  // generates a number between 0 and totalFitness.
            int currentValue = 0;

            for (int j = 0; j < populationSize; ++j){
                currentValue = currentValue + (populationSize - j);
                if (chosenValue < currentValue){
                    currentParentsList.add(chromosomePopulation.get(j));
                    break;
                }
            }
            //breaks to here
        }

    }

    // Places two tournament winners as winners in the the List provided
    private void tournamentSelectParents(){
        Random generator = new Random();

        // Array list to store contestants of the tournament
        List<Chromosome> tournamentGroup = new ArrayList<Chromosome>();

        // tournament settings
        int tournamentSize = AlphaGA.TOURNAMENT_SIZE;
        int populationSize = chromosomePopulation.size();
        int numberOfParentsToSelect = 2;  //Should always be 2 as there are usually two parents for each crossover

        // Loops for how many parents you want to generate per crossover
        for (int i = 0; i < numberOfParentsToSelect; ++i ){

            // Choose the tournament size number of chromosomes and place in a tournament list
            for (int j =0; j < tournamentSize; ++j){
                tournamentGroup.add(this.getChromosome(generator.nextInt(populationSize)));
            }
            // order the tournament contestants from best to worst fitness
            Collections.sort(tournamentGroup, new MyChromosomeComparator());
            // add the chromosome in position 0 (the best fitness) into the currentParents list
            currentParentsList.add(tournamentGroup.get(0));

            //Clear the tournament group for the next tournament
            tournamentGroup.clear();

        }
    }

    //
    private void crossover() {
        Random generator = new Random();

        // Create copies of the 2 selected parents for the offspring
        Chromosome parent1Copied = new Chromosome();
        Chromosome parent2Copied = new Chromosome();
        try {
            parent1Copied = new Chromosome(currentParentsList.get(0));
            parent2Copied = new Chromosome(currentParentsList.get(1));
        }
        catch (Exception e) {
            System.out.println("ERROR!! No offspring in the offspring list to crossover");
            System.exit(0);
        }


        // initialises the chromosome objects that will be the produced offspring
        Chromosome offspring1 = new Chromosome();
        Chromosome offspring2 = new Chromosome();

        // List to hold the bins
        List<Bin> holder1 = new ArrayList<Bin>();
        List<Bin> holder2 = new ArrayList<Bin>();

        // Initialise crossover point variables for parent 1 and parent 2
        int p1CrossPointX, p1CrossPointY;
        int p2CrossPointX, p2CrossPointY;

        // Generate random numbers between 0 and the size of the parents binList to be the cross points
        p1CrossPointX = generator.nextInt(parent1Copied.getTotalBins());
        p1CrossPointY = generator.nextInt(parent1Copied.getTotalBins());

        p2CrossPointX = generator.nextInt(parent2Copied.getTotalBins());
        p2CrossPointY = generator.nextInt(parent2Copied.getTotalBins());

        // Sort the crossover points so that the X value is the smaller one and the Y value is the bigger one
        if (p1CrossPointX > p1CrossPointY){
            int temp = p1CrossPointX;
            p1CrossPointX = p1CrossPointY;
            p1CrossPointY = temp;
        }

        if (p2CrossPointX > p2CrossPointY){
            int temp = p2CrossPointX;
            p2CrossPointX = p2CrossPointY;
            p2CrossPointY = temp;
        }

        // Number of genes being transmitted from the parent chromosomes
        int p1SegmentLength = (p1CrossPointY - p1CrossPointX) + 1;      // + 1 as if the same crosspoint is generated then the single gene will be transmitted
        int p2SegmentLength = (p2CrossPointY - p2CrossPointX) + 1;

//        System.out.println("Cutting from x = " + p1CrossPointX + " to y = " + p1CrossPointY);
//        printChromosome(parent1Copied);
//        System.out.println("Cutting from x = " + p2CrossPointX + " to y = " + p2CrossPointY);
//        printChromosome(parent2Copied);

        // copy the section of genes that are to be transmitted to the holder lists and remove them from the parent chromosomes
        for (int i = 0; i < p1SegmentLength; ++i){
            holder1.add(parent1Copied.getBin(p1CrossPointX));
            parent1Copied.removeBin(p1CrossPointX);
        }
        for (int i = 0; i < p2SegmentLength; ++i){
            holder2.add(parent2Copied.getBin(p2CrossPointX));
            parent2Copied.removeBin(p2CrossPointX);
        }

        // stitch segments back together for offspring 1.
        // Offspring 1 has parent1s original genes. and segment passed on from parent 2
        for (int i = 0; i < p1CrossPointX; ++i){
            offspring1.addBin(parent1Copied.getBin(0));
            parent1Copied.removeBin(0);
        }

        for (int i = 0; i < p2SegmentLength; ++i){
            offspring1.addBin(holder2.get(0));
            holder2.remove(0);
        }

        int p1TailLength = parent1Copied.getTotalBins();
        for (int i =0; i < p1TailLength; ++i){
            offspring1.addBin(parent1Copied.getBin(0));
            parent1Copied.removeBin(0);
        }

        // Offspring 2 has parent2s original genes. and segment passed on from parent 1
        for (int i = 0; i < p2CrossPointX; ++i){
            offspring2.addBin(parent2Copied.getBin(0));
            parent2Copied.removeBin(0);
        }

        for (int i = 0; i < p1SegmentLength; ++i){
            offspring2.addBin(holder1.get(0));
            holder1.remove(0);
        }

        int p2TailLength = parent2Copied.getTotalBins();
        for (int i =0; i < p2TailLength; ++i){
            offspring2.addBin(parent2Copied.getBin(0));
            parent2Copied.removeBin(0);
        }


        offspring1.makeValid(p1CrossPointX, (p1CrossPointX + p2SegmentLength));
        offspring2.makeValid(p2CrossPointX, (p2CrossPointX + p1SegmentLength));

//        printChromosome(offspring1);
//        printChromosome(offspring2);


//        offspringList.add(offspring1);
//        offspringList.add(offspring2);


//        if (generator.nextInt(2) == 1){
//                offspringList.add(offspring1);
//        } else {
//            offspringList.add(offspring2);
//        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (offspring1.getTotalBins() > offspring2.getFitness()){
            offspringList.add(offspring1);
        } else {
            offspringList.add(offspring2);
        }
    }


    private void addChromosome(Chromosome chromosome){
        chromosomePopulation.add(chromosome);
    }

    public Chromosome getChromosome(int i){
        return chromosomePopulation.get(i);
    }

    class MyChromosomeComparator implements Comparator<Chromosome>{
        // Custom comparator to compare chromosome fitness. Configured to sort in descending order by default instead of ascending.
        @Override
        public int compare(Chromosome o1, Chromosome o2) {
            if (o1.getFitness() < o2.getFitness()){
                return 1;
            }

            if (o1.getFitness() > o2.getFitness()){
                return -1;
            }
            return 0;
        }
    }

    public int getTotalChromosomes(){return chromosomePopulation.size();}

    public void printChromosomeFromPopulation(int position){


        System.out.println("=================");
        for (int i = 0; i < chromosomePopulation.get(position).getTotalBins();++i){
            System.out.print("[");
            for (int j = 0; j < chromosomePopulation.get(position).getBin(i).getTotalItems();++j){
                //System.out.print(chromosomePopulation.get(position).getBin(i).getItem(j).getIndex());
                System.out.print(AlphaGA.getItemVal(chromosomePopulation.get(position).getBin(i).getItem(j).getIndex()));
                if (j <  (chromosomePopulation.get(position).getBin(i).getTotalItems()-1)){
                    System.out.print(",");
                }
            }
            System.out.println("] - " + chromosomePopulation.get(position).getBin(i).getLoad());
        }
        System.out.println("=================");
        System.out.println("Fitness = " +  chromosomePopulation.get(position).getFitness());
        System.out.println("Total bins: " + chromosomePopulation.get(position).getTotalBins());
    }

    public static void printChromosome (Chromosome chromosome){
        System.out.println("=================");
        for (int i = 0; i < chromosome.getTotalBins();++i){
            System.out.print("[");
            for (int j = 0; j < chromosome.getBin(i).getTotalItems();++j){

                if (AlphaGA.asIndex){
                    System.out.print(chromosome.getBin(i).getItem(j).getIndex());            //This line shows index number of the items
                } else {
                    System.out.print(AlphaGA.getItemVal(chromosome.getBin(i).getItem(j).getIndex()));
                }    //This line shows the items weight/value

                if (j <  (chromosome.getBin(i).getTotalItems()-1)){
                    System.out.print(",");
                }
            }
            System.out.println("] - " + chromosome.getBin(i).getLoad());
        }
        System.out.println("=================");
        System.out.println("Fitness = " +  chromosome.getFitness());
        System.out.println("Total bins: " + chromosome.getTotalBins());
    }

    public static void printBinList (List<Bin> binList){
        System.out.println("=================");
        for (int i = 0; i < binList.size(); ++i){
            System.out.print("[");
            for (int j = 0; j < binList.get(i).getTotalItems(); ++j){

                if (AlphaGA.asIndex){
                    System.out.println(binList.get(i).getItem(j).getIndex());
                } else  {
                    System.out.print(AlphaGA.getItemVal(binList.get(i).getItem(j).getIndex()));
                }


                if (j < (binList.get(i).getTotalItems() -1)){
                    System.out.print(",");
                }
            }
            System.out.println("]");
        }
        System.out.println("=================");
    }


}
