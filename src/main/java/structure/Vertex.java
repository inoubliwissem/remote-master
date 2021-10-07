package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Vertex implements Serializable {

    private int id;
    private String type;
    private List<Integer> neigbords;
    private Set<Integer> strongNeigbords;


    public Vertex(int id) {
        this.id = id;
        neigbords = new ArrayList<Integer>();
        strongNeigbords=new HashSet<Integer>();
    }

    public Vertex(int id, Set<Integer> neighbors) {
        this.id = id;
        neigbords = new ArrayList<Integer>();
        this.neigbords.addAll(neighbors);
        strongNeigbords=new HashSet<Integer>();
    }

    public Vertex() {
        neigbords = new ArrayList<Integer>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getNeigbords() {
        return neigbords;
    }

    public void setNeigbords(List<Integer> neigbords) {
        this.neigbords = neigbords;
    }

    public void addNeigbor(int v) {
        if (!this.neigbords.contains(v)) {
            this.neigbords.add(v);
        }
    }
    public boolean toBeRemoved(Set<Integer> internalV)
    {

        for(Integer nei : this.getNeigbords())
        {
           if(internalV.contains(nei) )
           {
               return false;
           }
        }
        return true;
    }
    public boolean secondToBeRemoved(Set<Integer> internalV)
    {

        for(Integer nei : this.getNeigbords())
        {
            if(internalV.contains(nei) )
            {
                return false;
            }
        }
        return true;
    }

    public void removeNeighbor(int v)
    {
        this.getNeigbords().removeIf(n->n==v);
        this.getStrongNeigbords().removeIf(n->n==v);
    }

    public void addNeigbors(List<Integer> newNeighbors) {
        for(Integer nei: newNeighbors) {
            if (!this.neigbords.contains(nei)) {
                this.neigbords.add(nei);
            }
        }
    }


    public void printVertex()
    {

      String ng="[";
      for ( Integer neig : this.neigbords){
         ng+=","+neig;
      }
        ng+="]";
        System.out.println("vertice :"+this.id+" neighbors :"+ng);
    }
/*
    public  List<Integer>getStrongNei(double sigma)
    {
        List<Integer> list_strong_neigh=new ArrayList<Integer>();

        for( int vi : this.getNeigbords())
        {
            for(Edge e: edges)
            {
                if(this.id==e.getFromNode() && vi==e.getEndNode() && e.getSimilarity()>=sigma)
                {
                    list_strong_neigh.add(e.getEndNode());
                }
                if(this.id==e.getEndNode() && vi==e.getFromNode() && e.getSimilarity()>=sigma)
                {
                    list_strong_neigh.add(e.getFromNode());
                }

            }
        }

        return list_strong_neigh;
    }*/

    public void addStrongNeighbors(int neighbors)
    {
        this.strongNeigbords.add(neighbors);
    }
    public Set<Integer> getStrongNeigbords()
    {
        return strongNeigbords;
    }

    public boolean isExternal(Set<Integer> frontier)
    {
        for(Integer vf : frontier)
        {
            if(this.neigbords.contains(vf))
            {
                return true;
            }
        }
        return false;
    }

    public void removeAllStrongNeigbords()
    {
        this.strongNeigbords.removeAll(strongNeigbords);
    }

    public void removeStrongneighord(Integer nei)
    {
        this.strongNeigbords.remove(nei);
    }
}
