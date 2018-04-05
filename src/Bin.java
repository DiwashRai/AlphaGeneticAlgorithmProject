import com.sun.org.apache.bcel.internal.generic.ALOAD;

import java.util.ArrayList;
import java.util.List;

public class Bin {
    private int capacity = AlphaGA.BIN_CAPACITY;
    private int load = 0;
    private List<Item> itemsList = new ArrayList<Item>();

    public Bin(){}

    //Copy constructor
    public Bin (Bin copy){
        //copy instance values
        this.capacity = copy.getCapacity();
        this.load = copy.getLoad();
        for (int i=0; i < copy.getTotalItems();++i){
            this.itemsList.add(new Item(copy.getItem(i)));
        }
    }

    public Bin(int loadValue, List<Item> List){
        capacity = AlphaGA.TOTAL_OBJECTS;
        load = loadValue;
        itemsList = List;
    }

    //attempts to add the passed item into the bin. Returns true if there is enough capacity and the items has been added.
    //Returns false if there is not enough capacity and does not add the item.
    public boolean additem(Item item){
        if (load + AlphaGA.getItemVal(item.getIndex()) <= capacity){
            load = load + AlphaGA.getItemVal(item.getIndex());

            // inserts items into the itemsList in ascending order
            for (int i = 0; i < itemsList.size(); i++){
                if((AlphaGA.getItemVal(item.getIndex()) <= AlphaGA.getItemVal(itemsList.get(i).getIndex())))
                {
                    itemsList.add(i, item);
                    return true;
                }
            }

            // inserts item at the end of the list if the item being inserted is larger than all of the items
            itemsList.add(item);
            //System.out.println(AlphaGA.getItemVal(item.getIndex()));
            return true;
        }
        return false;
    }

    public void removeItem(int itemIndex){
        load = load - AlphaGA.getItemVal(itemsList.get(itemIndex).getIndex());
        itemsList.remove(itemIndex);
    }

    public int getLoad(){return load;}

    public int getCapacity() {return capacity;}

    public Item getItem(int i){return itemsList.get(i);}

    public int getTotalItems (){return itemsList.size();}
}
