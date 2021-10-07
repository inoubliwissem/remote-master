package structure;

import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable {

    int node;
    int partition;

    HashMap< Integer, Node> listOfNeighbors;

    public Node(int node) {
        this.node = node;
        //this.partition = partition;
        listOfNeighbors = new HashMap<Integer, Node>();
    }

    public HashMap<Integer, Node> getListOfNeighbors() {
        return listOfNeighbors;
    }

    public void addNeighbor(int neighbor, Node node) {
        listOfNeighbors.put(neighbor, node);
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

}
