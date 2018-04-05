import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class AlphaGA {

    public static boolean asIndex = true;  //Printing out the objects being packed as index unique values or their weights
    // Initialise the data set that the algorithm will try to find a solution for.
    // Set how many of the list of objects to include. Starting from first to last.
//    static File dataTxt = new File("dataSets/2000 sets/2000(20).txt");
    static File dataTxt = new File("");
    static int TOTAL_OBJECTS = 0;

    // Initialise the settings for the GA algorithm
    static int BIN_CAPACITY = 150;
    final static int POPULATION_SIZE = 100;
    final static int OFFSPRING_PER_GENERATION = 75; // Controls how many offspring are made.
    final static int MUTATION_PERCENTAGE_CHANCE = 66;
    final static int GENES_PER_MUTATED_GENE = 1000;  // controls how many genes/bins are mutated in each chromosome. A value of 10 mutates 1 gene every 10 genes.
    static int PARENT_SELECTION_TYPE = 0; // 0 = tournament.1 = ranking. 2 = Original Alpha male. 3 = New Alpha male

    // Initialise fitness function K constant
    final static int FITNESS_K_CONSTANT = 2;

    // Initialise tournament selection variables
    final static int TOURNAMENT_SIZE = 10;    //Lower value preserves diversity but could increase convergence speed. Default value of 2 is a good starting place.

    // Initialise Alpha male selection variables
    final static int K_ALPHA_MALES = 25;  // controls how many alpha males are chosen for each generation
    final static boolean PICK_FITTEST_MALES = true;    //controls if the alpha males picked are random or the fittest males

    // Initialise the "Master" array containing the objects from the txt file.
//    private static int [] catalogue = txtToArr(dataTxt);
        private static int [] catalogue = new int[TOTAL_OBJECTS];

    // tracks if the algorithm is stagnating for not
    static int stagnationTillTermination = 10;
    static int stagnation = 0;
    static Chromosome bestSolution;

    static double startTime;
    static double endTime;
    static double timeTaken;

    static double highestStagnation;

    public static void main (String [] args)
    {
        System.out.println("RESULTS");
        for(int run = 1; run < 2; ++run){ // loop for when the experiments were set to automate through the dataSets one at a time automatically
            //dataTxt = new File("dataSets/"+ TOTAL_OBJECTS +" sets/"+ TOTAL_OBJECTS + "(" + run +").txt");
            // Error check to see if there is any txt file for the program to even read.

            ///////////////////////////// Code to create demo .jar where user can input their own settings////////////////////////////////////////
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.println("Enter name of data txt file e.g. 'data.txt'");
            dataTxt = new File(reader.next());
            System.out.println("Enter number of items in data file: ");
            TOTAL_OBJECTS = reader.nextInt();
            System.out.println("Set bin capacity: ");
            BIN_CAPACITY = reader.nextInt();
            System.out.println("Set parent selection type.");
            System.out.println("0 = tournament. 1 = rank based roulette. 2 = alpha male.");
            PARENT_SELECTION_TYPE = reader.nextInt();
            reader.close();




            if (!dataTxt.canRead()){
                System.out.println("ERROR!! Cannot read data file. Program terminated.");
                return;
            }
            catalogue = txtToArr(dataTxt);

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////

            startTime = System.nanoTime();

            Population population = new Population();
            population = Population.initialisePopulation();
            int generationNumber = 0;

            population.sortPopulationByFittestFirst();
            bestSolution = new Chromosome(population.getChromosome(0));
//            System.out.println("Generation " + generationNumber + ". Initial generation. Best chromosome fitness = " + bestSolution.getFitness());
//            System.out.println("Number of bins used = " + population.getChromosome(0).getTotalBins());

            for (int i = 0; i < 1000; ++i){
                population.selectionAndCrossover();
                population.selectSurvivors();
                population.mutation();
                population.sortPopulationByFittestFirst();
                if (isStagnant(population)){break;}
                ++generationNumber;


                System.out.println();
                System.out.println("Generation " + generationNumber + " best chromosome fitness = " + bestSolution.getFitness());
                System.out.println("Number of bins used = " + bestSolution.getTotalBins());
            }

            endTime = System.nanoTime();
            timeTaken = (endTime - startTime)/1000000;

             population.printChromosomeFromPopulation(0);    // Prints the best solution evolved at the end of the whole GA algorithm
//        System.out.println("Generations taken = " + (generationNumber -stagnationTillTermination));
//        System.out.println("Time taken = "+ timeTaken);

//            System.out.println("highest stagnation = " + highestStagnation);

//            System.out.println();
//            System.out.println("RESULTS");
//            System.out.println(bestSolution.getTotalBins() + "\t" + generationNumber + "\t" + bestSolution.getFitness() + "\t" + timeTaken);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            stagnation =0;
            startTime = 0;
            endTime = 0;
            timeTaken = 0;
        }
}

    private static boolean isStagnant(Population population){
        if (bestSolution.getFitness() >= population.getChromosome(0).getFitness()){
            stagnation = stagnation +1;
            System.out.println("Stagnation = " + stagnation);
        } else {
            if (highestStagnation < stagnation){
                highestStagnation = stagnation;
            }
            stagnation = 0;
            bestSolution = new Chromosome(population.getChromosome(0));
            System.out.println("New BEST solution!!");
        }

        if (stagnation >= stagnationTillTermination){return true;}

        return false;
    }

    private static  int [] txtToArr (File txt){
        int[] arr = new int[TOTAL_OBJECTS];
        try {
            Scanner scanner = new Scanner(dataTxt);
            int i = 0;
            while (scanner.hasNextInt()) {
                arr[i] = scanner.nextInt();
                i++;
            }
        }
        catch (FileNotFoundException e ) {
            System.out.println("File not found");
        }
        return arr;
    }

    public static int getItemVal(int index){
        return catalogue[index];
    }

    public static void printItemList(List<Item> itemList){
        for (int i = 0; i < itemList.size();++i){
            System.out.print("[" + getItemVal(itemList.get(i).getIndex()));
            System.out.print("]");
        }
        System.out.println();
//        for (int i = 0; i < itemList.size();++i){
//            System.out.print("[" + itemList.get(i).getIndex());
//            System.out.print("]");
//        }
//        System.out.println();
    }
}

