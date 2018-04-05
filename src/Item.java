public class Item {
    private int index;

    public Item (int i) {index = i;}
    public Item (){}


    // Copy constructor
    public Item (Item copy){
        //Copy instance values
        this.index = copy.getIndex();
    }

    public int getIndex(){return index;}
    public void setIndex(int i){index = i;}
}
