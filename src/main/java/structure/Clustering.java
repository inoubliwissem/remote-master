
package structure;
import scala.Int;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.Inet4Address;
import java.util.*;
import java.util.stream.Collectors;

public class Clustering {

  //  private List<Vertex> vertices;
    private HashMap<Integer,Vertex> dic_vertices;
    private List<Edge> edges;
    private Set<Vertex> cores_vertices;
    private Set<Integer> idCoreVertices;
    private Set<Integer> border_vertices;
    private Set<Integer> outiler_vertices;
    private Set<Integer> bridge_vertices;
    private List<Set<Integer>> clusters;
    private Set<Integer> real_outiler_vertices;
    // for memorize the affected vertices and clusters
    private Set<Integer> global_affected_vertices;
    private Set<Integer> global_remaining_vertices;
    private List<Set<Integer>> global_affected_clusters;

    public Set<Integer> getReal_outiler_vertices() {
        return real_outiler_vertices;
    }

    public void setReal_outiler_vertices(Set<Integer> real_outiler_vertices) {
        this.real_outiler_vertices = real_outiler_vertices;
    }

    public Set<Integer> getReal_bridge_vertices() {
        return real_bridge_vertices;
    }

    public void setReal_bridge_vertices(Set<Integer> real_bridge_vertices) {
        this.real_bridge_vertices = real_bridge_vertices;
    }

    private Set<Integer> real_bridge_vertices;
    private int mu;
    private double sigma;

    public Clustering(Partition p) {
    //    vertices = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();
        cores_vertices = new HashSet<Vertex>();
        border_vertices = new HashSet<Integer>();
        outiler_vertices = new HashSet<Integer>();
        bridge_vertices = new HashSet<Integer>();
        real_outiler_vertices = new HashSet<Integer>();
        real_bridge_vertices = new HashSet<Integer>();
        clusters = new ArrayList<Set<Integer>>();
        idCoreVertices=new HashSet<>();
        //for memorize the new change in each window
        this.global_affected_clusters=new ArrayList<>();
        this.global_affected_vertices=new HashSet<>();
        this.global_remaining_vertices=new HashSet<>();

        // add vertices to Hashmap
        dic_vertices =new HashMap<>();
        for(Vertex v : p.getVertices())
        {
            dic_vertices.put(v.getId(),v);
        }
       // vertices = p.getVertices();
        edges = p.getEdges();
    }


    public void removeOutliers(Set<Integer> tbd) {
        if (tbd.size() > 0) {

            this.real_outiler_vertices.removeAll(tbd);
        }
    }

    //  function to add a vertex V to list of vertices "vertices".
    public void addVertex(Edge e) {
        boolean tv = false;
        boolean tv2 = false;
        for (Integer idv : dic_vertices.keySet()) {

            if (dic_vertices.get(idv).getId() == e.getFromNode()) {
                dic_vertices.get(idv).addNeigbor(e.getFromNode());
                dic_vertices.get(idv).addNeigbor(e.getEndNode());
                tv = true;
            }
            if (dic_vertices.get(idv).getId() == e.getEndNode()) {
                dic_vertices.get(idv).addNeigbor(e.getFromNode());
                dic_vertices.get(idv).addNeigbor(e.getEndNode());
                tv2 = true;
            }
            // if a two nodes of the edge exist into our vertices list we exit block "for"
            if (tv && tv2) {
                break;
            }
        }
        if (tv == false) {
            Vertex vi = new Vertex(e.getFromNode());
            vi.addNeigbor(e.getFromNode());
            vi.addNeigbor(e.getEndNode());
           // vertices.add(vi);
            dic_vertices.put(vi.getId(),vi);
        }
        if (tv2 == false) {
            Vertex vi = new Vertex(e.getEndNode());
            vi.addNeigbor(e.getFromNode());
            vi.addNeigbor(e.getEndNode());
           // vertices.add(vi);
            dic_vertices.put(vi.getId(),vi);
        }
    }

    // public function to get a specific vertex with his Id from a shared list of vertices "vertices"
    public Vertex getVertex(int id) {
       if(dic_vertices.keySet().contains(id))
       {
            return dic_vertices.get(id);
       }
        return null;
    }

    public boolean isexiste(Edge e) {

        for (Edge ed : edges) {
            if ((ed.getFromNode() == e.getEndNode() && ed.getEndNode() == e.getFromNode()) || (ed.getFromNode() == e.getFromNode() && ed.getEndNode() == e.getEndNode()))
                return true;

        }
        return false;
    }

    // public function to determine that a given vertex v is a core or not
    public boolean isCore(Vertex v, int mu, double sigma) {
        boolean isC = false;
        int nb_strongConnections = 0;
        // get all edges from/to vertex V where their similarity better than sigma
        for (Edge e : edges) {
            if ((e.getFromNode() == v.getId() || e.getEndNode() == v.getId()) && e.getSimilarity() >= sigma) {
                nb_strongConnections += 1;
                if (nb_strongConnections >= mu) {
                    return true;
                }
            }
        }
        return isC;
    }

    // function to get an intersection between two lists
    private int getListintersection(List<Integer> lst1, Set<Integer> lst2) {
        int rst = 0;
        if(lst1.size()>0 && lst2.size()>0)
        if (lst1.size() > lst2.size()) {
            for (Integer i : lst2) {
                if (lst1.contains(i)) rst++;
            }


        } else {
            for (Integer i : lst1) {
                if (lst2.contains(i)) rst++;
            }

        }
        return rst;

    }

    // calulate a similarity for all edges in current partition
    public void calculateSimilarity() {
        //add a neighbors list to each bord of an edge
        for (Edge e : edges) {
            e.setfNode(getVertex(e.getFromNode()));
            e.seteNode(getVertex(e.getEndNode()));
        }
        // calculate a similarity for each edge
       /* for (Edge e : edges) {
            e.similarityCalculation();
        }*/
        edges.stream().forEach(e -> e.similarityCalculation());
    }

    public void upDatesimilarity(int from, int end) {
        int i = 0;
        boolean tv = false;
        while (i < edges.size() && tv != true) {
            Edge e = new Edge();
            if (edges.get(i).getFromNode() == from && edges.get(i).getFromNode() == end) {
                e = edges.get(i);
                e.setfNode(getVertex(e.getFromNode()));
                e.seteNode(getVertex(e.getEndNode()));
                e.similarityCalculation();
                tv = true;
            }
            i++;

        }
    }

    // get a list of a cores vertices in current sub graph
    public void filterCorevertices(int mu) {
     /*   for (Integer vid : dic_vertices.keySet()) {
            if (dic_vertices.get(vid).getStrongNeigbords().size() >= mu) {
                cores_vertices.add(dic_vertices.get(vid));
                dic_vertices.get(vid).setType("core");
                idCoreVertices.add(vid);
            }
        }*/
      dic_vertices.entrySet().stream().forEach(v -> {
            if (v.getValue().getStrongNeigbords().size() >= mu) {
                cores_vertices.add(v.getValue());
                dic_vertices.get(v.getKey()).setType("core");
                idCoreVertices.add(v.getKey());
            }
        });
    }

    // function to get foreach vertex its list of strong neighbors according to "sigma" threshold
    public void computeStrongNeighbors(double sigma) {
        dic_vertices.entrySet().stream().forEach(v -> {

            for (Edge e : edges) {
                if (e.getFromNode() == v.getKey() && e.getSimilarity() >= sigma) {
                    v.getValue().addStrongNeighbors(e.getEndNode());
                    // getVertex(e.getEndNode()).setType("border");
                }
                if (e.getEndNode() == v.getKey() && e.getSimilarity() >= sigma) {
                    v.getValue().addStrongNeighbors(e.getFromNode());
                }
            }
        });
    }

    // function to add each border vertex to a global list of all border vertices
    public void addBorderVertcies(List<Integer> listTBP) {
        for (Integer idv : listTBP) {
            boolean tv = false;
            for (Vertex v : cores_vertices) {
                if (v.getId() == idv) {
                    tv = true;
                }
            }
            if (!tv) {
                border_vertices.add(idv);
                // set a border type to the vertex idv
               // getVertex(idv).setType("border");
                dic_vertices.get(idv).setType("border");
            }
        }
    }

    // function to show more details about this partition
    public void printDetails() {   // print a sub-graph details
        System.out.println("in the graph we have " + dic_vertices.size() + " vertices and " + edges.size() + " edges");
        // print a cores vertices in the sub-graph
        System.out.println("print a cores vertices");
        for (Vertex v : cores_vertices)
            System.out.println(v.getId() + " its neighbors " + v.getNeigbords());
        // print a border vertices
        System.out.println("print a border vertices");
        for (Integer v : border_vertices)
            System.out.println(v);

        System.out.println("we fined a " + clusters.size() + " clusters");

        for (Set<Integer> c : clusters) {
            System.out.println(" cluster : ");
            for (int v : c) {
                System.out.print(" " + v);
            }
            System.out.println(" \n ****************** : ");

        }
        // print a bridges vertices
        System.out.println("print a bridges vertices");
        for (Integer v : this.bridge_vertices)
            System.out.println(v);

        // print a outliers vertices
        System.out.println("print a outliers vertices");
        for (Integer v : this.outiler_vertices)
            System.out.println(v);


    }

    public String checkVertex(int v) {
        Vertex node = dic_vertices.get(v);
        if (node == null)
            return "outlier";
        if (node.getNeigbords().size() == 0) {
            return "outlier";
        } else {


        }


        return "outlier";
    }

    // list of vertices in this partition
   // public List<Vertex> getVertices() {
        //return vertices;
   // }
    public HashMap<Integer, Vertex> getVertices() {
        return dic_vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Vertex> getCores_vertices() {
        return cores_vertices;
    }

    public Set<Integer> getBorder_vertices() {
        return border_vertices;
    }

    public Set<Integer> getOutiler_vertices() {
        return outiler_vertices;
    }

    public List<Vertex> getVerticeOutlier() {
        List<Vertex> outliers_vertices = new ArrayList<Vertex>();
        for (Integer i : outiler_vertices) {
            outliers_vertices.add(getVertex(i));
        }
        return outliers_vertices;

    }

    public Set<Integer> getBridge_vertices() {
        return bridge_vertices;
    }

    public List<Vertex> getBridges_vertices() {
        List<Vertex> bridge_vertices = new ArrayList<Vertex>();
        for (Integer i : getBridge_vertices()) {
            bridge_vertices.add(getVertex(i));
        }
        return bridge_vertices;
    }

    public List<Set<Integer>> getClusters() {
        return clusters;
    }

    public void mergeCluster(Set<Integer> cluster, Integer core) {

        for (Set<Integer> c : this.getClusters()) {
            if (c.contains(core)) {
                c.addAll(cluster);
            }
        }
    }

    public void mergeClusterDynamic(Set<Integer> cluster, Integer core) {

        int id=-1;
        for(int i=0;i<this.clusters.size();i++)
        {
         if(this.clusters.get(i).contains(core))
         {
             clusters.get(i).addAll(cluster);
             System.out.println(" we merge "+clusters.get(i)+" with "+cluster +" in same cluster");

             id=i;
             break;
         }
        }
        this.global_affected_clusters.add(clusters.get(id));
        clusters.remove(id);
    }
    public void deleteCluster(Integer core) {
        for (Set<Integer> c : this.clusters) {
            if (c.contains(core)) {
                this.clusters.remove(c);
            }
            return;
        }
    }

    public Set<Integer> getIdCores() {
        Set<Integer> liste = new HashSet<Integer>();
        /*
        for (Vertex v : cores_vertices) {
            liste.add(v.getId());
        }*/
        return this.idCoreVertices;
    }

    public void reComputeBridgeOutliers() {
        this.bridge_vertices.clear();
        this.outiler_vertices.clear();
        // get last vertices (outliers and bridges)
        Set<Integer> all_precessed_vertices = new HashSet<Integer>();
        for (Integer v : idCoreVertices) {
            all_precessed_vertices.add(v);
        }
        all_precessed_vertices.addAll(border_vertices);
        Set<Integer> unprocessed_vertices = new HashSet<Integer>();
        for (Integer vid : dic_vertices.keySet()) {
            if (!all_precessed_vertices.contains(vid))
                unprocessed_vertices.add(vid);
        }
        // get a stats of each unprocessed vertices, we well annotated each one as an outliers or bridges
        // according their connections
        for (Integer i : unprocessed_vertices) {
            int nb_connections = 0;
            for (Set<Integer> c : clusters) {
                // if we have a least one connection between a current unprocessed vertex I and a current cluster C
                // so we increment a nb_connections value, to be used after that to decide that a I is a bridge or an outlier
                if (getListintersection(getVertex(i).getNeigbords(), c) > 0) {
                    nb_connections += 1;
                    if (nb_connections >= 2) {
                        bridge_vertices.add(i);
                        break;
                    }
                }
            }
            if (nb_connections < 2)
                outiler_vertices.add(i);

        }
        this.real_bridge_vertices.addAll(this.getBridge_vertices());
        this.real_outiler_vertices.addAll(this.getOutiler_vertices());
    }

    public boolean isBridge(Integer outlier, Set<Integer> cluster) {
        List<Set<Integer>> local_clusters = this.getClusters();
        if (local_clusters == null)
            return false;
        for (Set<Integer> c : local_clusters) {
            c.removeAll(cluster);
            if (c.size() > 0) {
                for (Integer elm : c) {
                    Vertex v = getVertex(elm);
                    if (v != null) {
                        if (v.getNeigbords().contains(outlier)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public Set<Integer> reComputeDynamicBridgeOutliers(Set<Integer> affectedVertices) {

       //check the affected bridges if they changed to outlier we delete them
        Set<Integer> bridgesTBD=new HashSet<>();
        for(Integer vx: this.outiler_vertices){
         if(this.bridge_vertices.contains(vx))
         {
             bridgesTBD.add(vx);
         }
        }
        this.bridge_vertices.removeAll(bridgesTBD);

        // get almost vertices (outliers and bridges)
        Set<Integer> remaindvertices = new HashSet<Integer>();
        Set<Integer> newBridges=new HashSet<>();

        for(Integer v : affectedVertices){
            boolean assigned=false;
            for(Set<Integer> c : clusters){
                if(c.contains(v)){
                  assigned=true;
                  break;
                }
            }
            if(!assigned){
                remaindvertices.add(v);
            }
        }
        // get a stats of each unprocessed vertices, we well annotated each one as an outliers or bridges
        // according their connections
        for (Integer i : remaindvertices) {
            int nb_connections = 0;
            for (Set<Integer> c : clusters) {
                // if we have a least one connection between a current unprocessed vertex I and a current cluster C
                // so we increment a nb_connections value, to be used after that to decide that a I is a bridge or an outlier
               Vertex node=getVertex(i);
               if(node!=null) {
                   if (getListintersection(getVertex(i).getNeigbords(), c) > 0) {
                       nb_connections += 1;
                       if (nb_connections >= 2) {
                           bridge_vertices.add(i);
                           newBridges.add(i);
                           break;
                       }
                   }
               }
            }
            if (nb_connections < 2)
                outiler_vertices.add(i);

        }

        this.real_bridge_vertices.addAll(this.getBridge_vertices());
        this.real_outiler_vertices.addAll(this.getOutiler_vertices());
        return newBridges;
    }


    public  boolean isMembreOfAffectedClusters(Integer vertex)
    {
        for(Set<Integer> cluster: this.clusters){
            if(cluster.contains(vertex)){
                return true;
            }
        }
       return false;
    }


    // SCAN function
    public void SCAN(double sigma, int mu) {
        if (dic_vertices.size() > 0 && edges.size() > 0) {

            this.mu = mu;
            this.sigma = sigma;
            // after a loading a given sub graph we calculate all similarity
            // this.calculateSimilarity();
            // we find a strong neighbors foreach vertex according a sigma value
            this.computeStrongNeighbors(sigma);
            // filter a cores vertices in the sub graph
            this.filterCorevertices(mu);
            // start SCAN algorithm
            // varibale to save a list of clusters
            //   List<Set<Integer>> clusters=new ArrayList<Set<Integer>>();
          //  idCoreVertices.stream().forEach(idv -> {
                  for (Vertex v : cores_vertices) {
                // if we on first step, when we haven't any cluster
                // add a current core vertex and his strong neighbors
              //  Vertex v=dic_vertices.get(idv);
                if (clusters.size() == 0) {
                    Set<Integer> cluster = new HashSet<Integer>();
                    cluster.add(v.getId());
                    cluster.addAll(v.getStrongNeigbords());
                    // add border vertices to global list of borders
                    addBorderVertcies(new ArrayList<Integer>(v.getStrongNeigbords()));
                    clusters.add(cluster);
                } else {
                    //variable to memorise when we have merge the current vertex into one existing cluster
                    // when we fid  at least one shared neighbor between a current core vertex and a some cluster
                    // we merge them into same cluster
                    //else
                    //  we must create a new cluster which group  the current vertex ant his strong
                    // neighbors into same cluster
                    boolean added = false;
                    for (Set<Integer> c : clusters) {   // if a difference between current cluster and a neighbors of a v core vertex not null
                        // push a v and their strong neighbors into current cluster

                        if (getListintersection(v.getNeigbords(), c) > 0) {
                            c.addAll(v.getStrongNeigbords());
                            c.add(v.getId());
                            // add border vertices
                            addBorderVertcies(new ArrayList<Integer>(v.getStrongNeigbords()));
                            added = true;
                            break;
                        }

                    }
                    // if we parse all cluster and we cannot get added the current  vertex v
                    // we must create a new cluster and push into this a current core and their strong neighbors
                    // the add the new cluster to our set of clusters
                    //
                    if (!added) {
                        Set<Integer> newcluster = new HashSet<Integer>();
                        newcluster.add(v.getId());
                        newcluster.addAll(v.getStrongNeigbords());
                        //add border vertices
                        addBorderVertcies(new ArrayList<Integer>(v.getStrongNeigbords()));
                        clusters.add(newcluster);

                    }

                }
            }
            //);
            // get last vertices (outliers and bridges)
            Set<Integer> all_precessed_vertices = new HashSet<Integer>();
            all_precessed_vertices.addAll(idCoreVertices);
           /* idCoreVertices.stream().forEach(v -> {
                //  for (Vertex v : cores_vertices) {
                all_precessed_vertices.add(v);

            });*/

            all_precessed_vertices.addAll(border_vertices);
            Set<Integer> unprocessed_vertices = new HashSet<Integer>();
            //  for (Vertex v : vertices) {
            dic_vertices.entrySet().stream().forEach(v -> {
                if (!all_precessed_vertices.contains(v.getKey()))
                    unprocessed_vertices.add(v.getKey());
            });
            // get a stats of each unprocessed vertices, we well annotated each one as an outliers or bridges
            // according their connections
            unprocessed_vertices.stream().forEach(i -> {
                //  for (Integer i : unprocessed_vertices) {
                int nb_connections = 0;
                for (Set<Integer> c : clusters) {  // if we have a least one connection between a current unprocessed vertex I and a current cluster C
                    // so we increment a nb_connections value, to be used after that to decide that a I is a bridge or an outlier
                    if (getListintersection(getVertex(i).getNeigbords(), c) > 0) {
                        nb_connections += 1;
                        if (nb_connections >= 2) {
                            bridge_vertices.add(i);
                            break;
                        }
                    }
                }
                if (nb_connections < 2)
                    outiler_vertices.add(i);

            });

        } else {
            System.out.print("we don't have a graph to compute it");
        }

    }

    public Set<Integer> getCoreOfCluster(Set<Integer> cluster, Set<Integer> externalVertex) {
        Set<Integer> cores = new HashSet<Integer>();
        for (Integer v : cluster) {
            if (this.getCores_vertices().contains(v) & externalVertex.contains(v)) {
                cores.add(v);
            }
        }
        return cores;

    }

    public Set<Integer> getCluster(Integer outlier) {
        for (Set<Integer> cluster : getClusters()) {
            for (Integer v : cluster) {
                Vertex node = getVertex(v);
                if (node != null) {
                    if (getVertex(v).getNeigbords().contains(outlier)) {
                        return cluster;
                    }
                } else {
                    return null;
                }

            }
        }
        return null;
    }

    public String getVerticeType(Integer id) {

        String rst = "outlier";

        if (this.border_vertices.contains(id))
            // rst="border";
            return "border";
        else if (this.bridge_vertices.contains(id))
            return "bridge";//  rst="bridge";
        else if (this.getIdCores().contains(id))
            return "core";//rst="core";

        return rst;
    }

    public void reclustering(Set<Integer> affected) {
        Set<Edge> affected_edges = this.getEdges().stream().filter(e -> (affected.contains(e.getFromNode()) || affected.contains(e.getEndNode()))).collect(Collectors.toSet());
        // according to affected edge we check all strong connections
        for(Edge e : affected_edges){
            if(e.getfNode().getStrongNeigbords().contains(e.getEndNode()) && e.getSimilarity()< sigma){

            }
        }
        //  System.out.println("Print all affected edge in worker "+this.workerID);
        //  affected_edges.forEach(e->e.printEdge());
        HashMap<Integer, String> changes = new HashMap<Integer, String>();
        //checking core vertices

        for (Integer v : affected) {
            getVertex(v).removeAllStrongNeigbords();
            String old_status = getVerticeType(v);
            affected_edges.stream().forEach(e -> {
                if (e.getFromNode() == v && e.getSimilarity() >= sigma) {
                    getVertex(v).addStrongNeighbors(e.getEndNode());
                }
                if (e.getEndNode() == v && e.getSimilarity() >= sigma) {
                    getVertex(v).addStrongNeighbors(e.getFromNode());
                }
            });

            if (getVertex(v).getStrongNeigbords().size() >= mu) {
                cores_vertices.add(getVertex(v));
                getVertex(v).setType("core");
                //add the new status to changes list
                // if v is a core
                changes.put(v, old_status + ":core");
            } else {
                if (old_status.equals("core")) {
                    changes.put(v, old_status + ":Ncore");
                } else if (isBorder(getVertex(v))) {
                    changes.put(v, old_status + ":border");
                } else {
                    changes.put(v, old_status + ":-");
                }
            }

        }

        // process all vertices accroding to their change
        for (Integer v : changes.keySet()) {
            String parts[] = changes.get(v).split(":");
            System.out.println(v + " From " + changes.get(v));
             // case of : core to Ncore
            if (parts[0].equals("core") && parts[1].equals("Ncore")) {
                Vertex core = this.getVertex(v);
                Set<Integer> verticesTBRFromCluster = new HashSet<>();
                // borders of the changed core
                Set<Integer> borders = core.getStrongNeigbords();

                System.out.println("border of "+core.getId()+" "+borders);
                // check core border if they remaind core
                // if one border do not a border we remove it
                for(Integer b : borders){
                    for(Vertex c : cores_vertices)
                    {
                        if(!c.getStrongNeigbords().contains(b))
                        {
                          borders.remove(b);
                        }
                    }
                }
                getIdCores().remove(core.getId());
                cores_vertices.remove(core);
                System.out.println("remaind core "+cores_vertices);

                //if the old core does not a border according to an other core
                // we remove it and their border from the cluster
                if (!isBorder(core)) {
                    verticesTBRFromCluster.addAll(core.getNeigbords());
                }
                // else it is a border vertex
                // we keep it in the cluster and  we remove their neighbors (old border)
                else {
                    verticesTBRFromCluster.addAll(core.getNeigbords());
                    verticesTBRFromCluster.remove(core.getId());
                }
                System.out.println("before checking "+verticesTBRFromCluster);
                // test if their old borders have any connection with other core
                // then keep them into the cluster
                for (Integer bor : borders) {
                    if (this.isBorder(bor)) {
                        verticesTBRFromCluster.remove(bor);
                    }
                }
                System.out.println("after checking "+verticesTBRFromCluster);
                // remove border vertices
               // border_vertices.remove(verticesTBRFromCluster);
                System.out.println("remain borders  "+border_vertices);
              //  System.out.println("the next vertices should be removed from their clusters "+verticesTBRFromCluster);
                this.checkClusters(core.getId(), verticesTBRFromCluster);
              //  System.out.println("core to non core" + core.getId() + " it losts " + verticesTBRFromCluster);
            } else if ((parts[0].equals("outlier") || parts[0].equals("bridge") || parts[0].equals("border")) && parts[1].equals("core")) {
                System.out.println("non core to core "+v);
                Vertex core = this.getVertex(v);
                cores_vertices.add(core);
                System.out.println("we will drove "+v+" from outliers, bridges and borders");
                outiler_vertices.remove(v);
                bridge_vertices.remove(v);
                border_vertices.remove(v);
                // test if the new core vertex do not be belong into any cluster
                // then we create a new cluster with the core and its borders
                // else we push all its border into the same cluster like the current core
                boolean inCluster = false;
                int idCluster = 0;
                for (int i = 0; i < clusters.size(); i++) {
                    if (clusters.get(i).contains(v)) {
                        inCluster = true;
                        idCluster = i;
                        break;
                    }
                }
                if (!inCluster) {
                    Set<Integer> newCluster = new HashSet<>();
                    newCluster.addAll(core.getStrongNeigbords());
                    newCluster.add(v);
                    this.clusters.add(newCluster);
                    outiler_vertices.removeAll(core.getStrongNeigbords());
                    bridge_vertices.removeAll(core.getStrongNeigbords());
                    border_vertices.addAll(core.getStrongNeigbords());

                } else {
                    clusters.get(idCluster).addAll(core.getStrongNeigbords());
                    outiler_vertices.removeAll(core.getStrongNeigbords());
                    bridge_vertices.removeAll(core.getStrongNeigbords());
                    border_vertices.addAll(core.getStrongNeigbords());
                }
            }
            // from any weak connection to border to border status
            else if ((parts[0].equals("outlier") || parts[0].equals("bridge") ) && parts[1].equals("border")) {
                System.out.println("other to border "+v);
                // 1 get the core and it cluster
                // change the vertex to border and remove it from the list
               // we use the affected edge for the core associated with new border
                int  conncernedCore=-1;
                for(Edge e : affected_edges)
                 {
                   if(e.getEndNode()==v && e.getSimilarity()>=this.sigma)
                    { conncernedCore=e.getFromNode();
                     }else if(e.getFromNode()==v && e.getSimilarity()>=this.sigma ){
                     conncernedCore=e.getEndNode();
                     }
                 }
                 // find the affected cluster and add the new border into the cluster
                for(Set<Integer> c : clusters){
                    // if the cluster contains the fetched core
                    if(c.contains(conncernedCore))
                    {
                        c.add(v);
                    }
                }
                // update the news changes like V be a border an it be removed from its old status
                border_vertices.add(v);
                outiler_vertices.remove(v);
                bridge_vertices.remove(v);
                // add V as an border to the concerned core
                //TBA
            }
            else if (parts[0].equals("core")  && parts[1].equals("core")) {
                System.out.println("Core tow Core : but  loses strong connection with an other core");
                for(Edge e : affected_edges)
                {
                    // if two core vertices have a weak connection and they belong same cluster
                    // we split the cluster
                    if((e.getEndNode()==v && getIdCores().contains(e.getFromNode()) || e.getFromNode()==v && getIdCores().contains(e.getEndNode()) )&& e.getSimilarity()<sigma)
                    {
                        int id1=getIdCluster(e.getFromNode());
                        int id2=getIdCluster(e.getEndNode());
                        System.out.println("split cluster");
                        // if its exist the two clusters and the both clusters are different
                        if(id1>-1 && id2>-1 && id1==id2)
                        {
                            // get the common cluster
                            Set<Integer> cluster=clusters.get(id1);
                            // remove the common cluster from global clusters
                            cluster.remove(clusters.get(id1));
                            // split the common cluster into two subClusters
                            Set<Set<Integer>>new_clusters=new HashSet<>();
                           // TBA
                        }

                    }

                    // if two core vertices have a strong connection and they belong different clusters
                    // we merge the clusters
                    else if(e.getEndNode()==v && getIdCores().contains(e.getFromNode()) && e.getSimilarity()<sigma)
                    {
                        System.out.println("merge");
                        int id1=getIdCluster(e.getFromNode());
                        int id2=getIdCluster(e.getEndNode());
                        if(id1>-1 && id2>-1 && id1!=id2){
                            // merge second with the first them
                              clusters.get(id1).addAll(clusters.get(id2));
                            // delete the second
                              clusters.remove(id2);
                        }
                    }

                }

            }
        }
    }

    // these methods for the incremental compution
    // isBorder tests if a given vertex V is a border
    // it takes Vertex v
    // returns boolean value : true if the passed vertex is a border else return false
    public boolean isBorder(Vertex v) {
        for (Integer node : v.getStrongNeigbords()) {
            if (this.getIdCores().contains(node))
                return true;
        }
        return false;
    }
    public boolean isBorder(Integer v)
    {
        for(Integer vid : this.idCoreVertices)
        {   Vertex vx=dic_vertices.get(vid);
            if(vx.getStrongNeigbords().contains(v))
            {
                return true;
            }
        }
        return false;
    }

    public void deleteVertex(Integer vertex) {
        for (int i = 0; i < this.clusters.size(); i++) {
            if (this.clusters.get(i).contains(vertex)) {
                this.clusters.get(i).remove(vertex);
                break;
            }
        }
        this.idCoreVertices.remove(vertex);
        this.outiler_vertices.remove(vertex);
        this.bridge_vertices.remove(vertex);
        this.border_vertices.remove(vertex);
    }

    // check clusters check affected clusters accordding to one core and set of vertices must be removed
    public void checkClusters(Integer core, Set<Integer> verticesTBR) {
        System.out.println("all clusters before checking  are " + this.clusters);
        Set<Integer> affectedCluster = null;
        int id_clusters = -1;
        // find the affected cluster
        for (int i = 0; i < this.clusters.size(); i++) {
            if (clusters.get(i).contains(core)) {
                affectedCluster = clusters.get(i);
                id_clusters = i;
                break;
            }
        }
        System.out.println("affected cluster is " + affectedCluster);
        // we will verifie that all vertices to be removed do not belong the list of borders
        for(Integer elm : affectedCluster)
        {
           if(border_vertices.contains(elm))
           {
               verticesTBR.remove(elm);
           }
        }
        if (affectedCluster != null) {
            affectedCluster.removeAll(verticesTBR);

            border_vertices.removeAll(verticesTBR);
            cores_vertices.remove(core);
            getIdCores().removeAll(verticesTBR);

            this.outiler_vertices.addAll(verticesTBR);
            if (affectedCluster.size() > 0) {
                this.clusters.set(id_clusters, affectedCluster);
            } else {
                this.clusters.remove(id_clusters);
            }
        }
        System.out.println("all clusters after checking  are " + this.clusters);
    }

    public void printlocalclustering(int id) {

    try

    {
        BufferedWriter writer = new BufferedWriter(new FileWriter("clusteringSchema"+id ));
        writer.write("================================================================= \n");
        writer.write("=======================Clusters list=============================== \n");
        writer.write(""+this.clusters);
        writer.write("\n=======================end list of clusters=============================== \n");
        writer.write("=======================Core vertices=============================== \n");
        writer.write(""+this.idCoreVertices);
        writer.write("\n=======================end core vertices=============================== \n");
        writer.write("=======================border vertices=============================== \n");
        writer.write(""+this.border_vertices);
        writer.write("\n=======================end borders=============================== \n");
        writer.write("=======================bridge vertices=============================== \n");
        writer.write(""+this.bridge_vertices);
        writer.write("\n=======================end bridges=============================== \n");
        writer.write("=======================outliers vertices=============================== \n");
        writer.write(""+this.outiler_vertices);
        writer.write("\n=======================end outliers=============================== \n");
        writer.close();



    }catch(
    Exception e )

    {
        System.out.println("exception from clustering class generated by "+e.getMessage());
    }
}


    public int getIdCluster(Integer core){
        for(int i=0; i<clusters.size();i++)
        {
            if(clusters.get(i).contains(core))
            {
                return i;
            }
        }
        return -1;
        }


   public void checkStrongConnections(Set<Edge> affectedEdge)
     {
    for(Edge e : affectedEdge)
    {

     if(e.getSimilarity()>=sigma)
     {
         Vertex from,end;
     from=dic_vertices.get(e.getFromNode());
     end=dic_vertices.get(e.getFromNode());
     if(from!=null & end !=null ) {
         if(from.getNeigbords().size()>0 & end.getNeigbords().size()>0) {
             dic_vertices.get(e.getFromNode()).addStrongNeighbors(e.getEndNode());
               dic_vertices.get(e.getEndNode()).addStrongNeighbors(e.getFromNode());
         }
     }
     }
     else
     {
         Vertex from,end;
         from=dic_vertices.get(e.getFromNode());
         end=dic_vertices.get(e.getFromNode());
         if(from!=null & end !=null){
             if(from.getNeigbords().size()>0 & end.getNeigbords().size()>0) {
                 dic_vertices.get(e.getFromNode()).removeStrongneighord(e.getEndNode());
                 dic_vertices.get(e.getEndNode()).removeStrongneighord(e.getFromNode());
             }
         }
     }
    }
}
   public void checkCores(Set<Edge> affectedEdge){
       // we memorise the core vertices should be deleted in order to delete the concerned borders
       Set<Integer> newcores =new HashSet<>();
       Set<Integer> newBorders =new HashSet<>();
     //   affectedEdge.stream().forEach(e->{
            for(Edge e: affectedEdge){
            // senarios to remove affected cores
            if(idCoreVertices.contains(e.getFromNode())) {
                Vertex vx = dic_vertices.get(e.getFromNode());
                if (vx!=null) {
                    if (dic_vertices.get(e.getFromNode()).getStrongNeigbords().size() < mu) {
                        // remove core
                        idCoreVertices.remove(e.getFromNode());
                        // if we have one cluster depends only on the core we remove it
                        int id_clusters = -1;
                        for (int i = 0; i < this.clusters.size(); i++) {
                            boolean delete = true;
                            Set<Integer> c = this.getClusters().get(i);
                            for (Integer elm : c) {
                                if (this.idCoreVertices.contains(elm)) {
                                    delete = false;
                                }
                            }
                            if (delete) {
                                id_clusters = i;
                                break;
                            }
                        }
                        if (id_clusters > -1) {
                          //  Set<Integer> cluster=this.clusters.get(id_clusters);
                           // Set<Integer> bridgesTBR=new HashSet<>();
                            this.clusters.remove(id_clusters);
                            // we remove all bridges depend on the cluster
                          //  for(Integer elm: cluster){
                            //    Vertex node=getVertex(elm);
                              //  bridgesTBR.addAll(node.getNeigbords());
                           // }
                           // System.out.println("aaaaaaaaaaaaaaaaaaaaaaaa"+bridgesTBR);
                           // this.bridge_vertices.removeAll(bridgesTBR);

                        }
                        // change the core to outlier
                        outiler_vertices.add(e.getFromNode());
                    }
            }
            }
            if(idCoreVertices.contains(e.getEndNode())) {
                Vertex vx = dic_vertices.get(e.getEndNode());
                if (vx!=null) {
                if (dic_vertices.get(e.getEndNode()).getStrongNeigbords().size() < mu) {
                    idCoreVertices.remove(e.getEndNode());
                    // if we have one cluster depends only on the core we remove it
                    int id_clusters = -1;
                    for (int i = 0; i < this.clusters.size(); i++) {
                        boolean delete = true;
                        Set<Integer> c = this.getClusters().get(i);
                        for (Integer elm : c) {
                            if (this.idCoreVertices.contains(elm)) {
                                delete = false;
                            }
                        }
                        if (delete) {
                            id_clusters = i;
                            break;
                        }
                    }
                    if (id_clusters > -1) {
                       //  Set<Integer> cluster=this.clusters.get(id_clusters);
                        // Set<Integer> bridgesTBR=new HashSet<>();
                        this.clusters.remove(id_clusters);
                        // we remove all bridges depend on the cluster
                       //   for(Integer elm: cluster){
                       //  Vertex node=getVertex(elm);
                         // bridgesTBR.addAll(node.getNeigbords());
                        // }
                      ///  this.bridge_vertices.removeAll(bridgesTBR);
                    }
                    // change the core to outlier
                    outiler_vertices.add(e.getEndNode());
                }
            }
            }
            // if we have a new border we add it into global list of cores and add its strong connected neighords
                Vertex v1=dic_vertices.get(e.getFromNode());
            if(v1!=null) {
                if (dic_vertices.get(e.getFromNode()).getStrongNeigbords().size() >= mu) {
                    idCoreVertices.add(e.getFromNode());
                    //  border_vertices.addAll(dic_vertices.get(e.getFromNode()).getStrongNeigbords());
                    newBorders.addAll(dic_vertices.get(e.getFromNode()).getStrongNeigbords());
                    newcores.add(e.getFromNode());
                }
            }
            // if we have a new border we add it into global list of cores and add its strong connected neighords
            // remove it from it lod list like outliers, bridges or borders
                Vertex v2=dic_vertices.get(e.getEndNode());
            if(v2!=null) {
                if (dic_vertices.get(e.getEndNode()).getStrongNeigbords().size() >= mu) {
                    idCoreVertices.add(e.getEndNode());
                    // border_vertices.addAll(dic_vertices.get(e.getEndNode()).getStrongNeigbords());
                    newBorders.addAll(dic_vertices.get(e.getEndNode()).getStrongNeigbords());
                    newcores.add(e.getEndNode());
                }
            }
        }
        if(newBorders.size()>0)
        {
            border_vertices.addAll(newBorders);
            outiler_vertices.removeAll(newBorders);
            bridge_vertices.removeAll(newBorders);
        }

        if(newcores.size()>0) {
            border_vertices.removeAll(newcores);
            outiler_vertices.removeAll(newcores);
            bridge_vertices.removeAll(newcores);
        }

   }

   public void checkBorders(Set<Edge> affectedEdge)
   {
       Set<Integer> oldBorders=new HashSet<>();
       affectedEdge.stream().forEach(e->{
           if(idCoreVertices.contains(e.getFromNode()))
           {
                   oldBorders.add(e.getEndNode());
           }
           if(idCoreVertices.contains(e.getEndNode()))
           {
                   oldBorders.add(e.getFromNode());
           }
       });
       Set<Integer> borderToRemoved=new HashSet<>();
      for (Integer border : border_vertices) {
          int nbCoreConnections = 0;
          for (Integer nei : dic_vertices.get(border).getStrongNeigbords()) {
              // we compare only with the affected cores
              if (idCoreVertices.contains(nei)) {
                  nbCoreConnections++;
              }
          }
          if (nbCoreConnections == 0 && this.border_vertices.contains(border) && border!=null) {
            // this.border_vertices.remove(border);
              borderToRemoved.add(border);
         }

      }
       if(borderToRemoved.size()>0) {
           this.border_vertices.removeAll(borderToRemoved);
           outiler_vertices.addAll(borderToRemoved);
       }

   }


   public void checkbridges(Set<Vertex> affected)
   {
       Set<Integer> bridgesTBR=new HashSet<>();
       for(Vertex vx: affected){
           List<Integer> neighbors=vx.getNeigbords();
         //  for()
       }
   }
   public void clusteringMainenance(Set<Integer> affected) {
      if(affected.size()>0) {
            // add the affected vertices to be shared between workers after that
            this.global_affected_vertices.clear();
            //this.global_affected_clusters.clear();
            this.global_affected_vertices.addAll(affected);
            // get affected clusters according to the affected vertices
           HashMap<Integer, Set<Integer>> affectedClusters = new HashMap<>();
            // all vertices of the affected clusters
           // Set<Integer> all_vertices = new HashSet<>();
            // new clusters
            List<Set<Integer>> new_clusters = new ArrayList<>();
           // get affected clusters from all local clusters
            for (Integer v : affected) {
                int i = 0;
                for (Set<Integer> c : this.clusters) {
                    if (c.contains(v)) {
                        affectedClusters.put(i, c);
                    }
                    i++;
                }
            }
          // update the locals clusters
          // get all core vertices from all affected vertices  in order to build new clusters
            Set<Integer> cores = new HashSet<>();
            // new cores that do not belong any cluster
           cores.addAll(getCores(affected,affectedClusters));
          // cores.addAll(getCores0(affected));
       //   System.out.println("core 0 "+cores+" whereas core are "+getCores(affected,affectedClusters)+" all cores are "+this.idCoreVertices+" affected clusters"+affectedClusters);
            //  System.out.println("cores will be tested "+cores);
            // distribute all cores to build the first clusters (clusters of core only)
            // create first cluster with first core and remove the core from cores list
            Set<Integer> c0 = new HashSet<>();
            if (cores.size() > 0) {
               // c0.add(cores.iterator().next());
                Integer cr=cores.iterator().next();
                c0.add(cr);
                new_clusters.add(c0);
                cores.removeAll(c0);
                // assign remaining cores
                // for each core, we check if it represent a border of an assigned core then we
                // or create a new cluster otherwise
             Set<Integer> addedCore=new HashSet<>();
               for (Integer c : cores) {
                    // check C core with all new clusters
                   boolean added = false;
                    for (int i = 0; i < new_clusters.size(); i++) {
                        // parse all cores of the cluster
                        added=false;
                        Set<Integer> cluster = new_clusters.get(i);
                      //  System.out.println("All clusters "+new_clusters+" corrently we have "+cluster);
                        for (Integer v : cluster) {
                            Vertex vx=dic_vertices.get(v);
                            //System.out.println(vx.getStrongNeigbords());
                          if (dic_vertices.get(v).getStrongNeigbords().contains(c)) {
                                new_clusters.get(i).add(c);
                                added = true;
                            }
                        }
                        // if we do not added the current core in any cluster
                        // then  we create a new cluster with "C" and add it into the newclusters list
                        if (!added && !addedCore.contains(c)) {
                            Set<Integer> newCluster = new HashSet<>();
                            newCluster.add(c);
                            new_clusters.add(newCluster);
                            addedCore.add(c);
                        }
                    }
                }
           }
           // we add all border core for each clusters
            for (int i = 0; i < new_clusters.size(); i++) {
                // updatedClusters.add(new_clusters.get(i));
                Set<Integer> cl = new HashSet<>();
                for (Integer c : new_clusters.get(i)) {
                    Vertex v = dic_vertices.get(c);
                    if (v != null && v.getStrongNeigbords().size() > 0) {
                        cl.addAll(new_clusters.get(i));
                        cl.addAll(v.getStrongNeigbords());
                    }
                }
                new_clusters.set(i, cl);
            }
            // insert the new clusters
            for (Set<Integer> c_new : new_clusters) {
                this.clusters.add(c_new);
            }
            /// check affected cluster
            // we remove the clusters that do not have any core vertices

           this.global_affected_clusters.addAll(new_clusters);
            // check remaining vertices = ( affected vertices - new-clusters)
            // we check them if they can be changed to bridge or we change  them as outliers
          //  for (Set<Integer> c : new_clusters) {
              // affected.removeAll(c);
           // }
            this.global_remaining_vertices.clear();
            this.global_remaining_vertices.addAll(affected);
            for(Integer id : affectedClusters.keySet())
            {
                if(!global_affected_clusters.contains(affectedClusters.get(id)))
                {
                    global_affected_clusters.add(affectedClusters.get(id));
                }
            }
      }

   }
   // get affected core from affected vertices and the affected clusters
   public Set<Integer> getCores(Set<Integer> vertices,HashMap<Integer,Set<Integer>> affected_cluster){
        Set<Integer> cores=new HashSet<>();
        for(Integer i : vertices)
        {
            boolean tv=false;
            // check if the vertices does not belong any cluster
            for(int j=0;j<clusters.size();j++) {
                if(clusters.get(j).contains(j))
                {
                    tv=true;
                }
            }
            // check if the vertex does not belong to any clusters and its a core vertex (to create new cluster)
            if (!tv& idCoreVertices.contains(i)) {
               cores.add(i);
            }

        }
        return cores;
   }
    public Set<Integer> getCores0(Set<Integer> vertices){
        Set<Integer> cores=new HashSet<>();
         for(Integer i : vertices) {

            if (idCoreVertices.contains(i)) {
                cores.add(i);
            }
        }

        return cores;
    }
    public Set<Set<Integer>> getAffectedClusters(Set<Integer> affectedVertices){
        Set<Set<Integer>> affectedClusters=new HashSet<>();
            while(affectedVertices.size()>0){
                Iterator<Integer> it=affectedVertices.iterator();
                Integer vx=it.next();
            for(Set<Integer> c : this.getClusters()){
                if(c.contains(vx)){
                    affectedClusters.add(c);
                }
            }
           // affectedVertices.remove(vx);
        }
        return  affectedClusters;
    }

    public Set<Integer> getAffectedBridge(Set<Integer> affectedVertices){

        Set<Set<Integer>> affectedClusters=this.getAffectedClusters(affectedVertices);
        Set<Integer> allvertices=new HashSet<>();
        // add all vertices of  the affected clusters in one variable
        for(Set<Integer> c : affectedClusters){
           for(Integer vx: c){
               allvertices.addAll(getVertex(vx).getNeigbords());
           }
        }
        Set<Integer> affectedbridges=new HashSet<>();
      //  affectedbridges.addAll(allvertices);
        for(Integer b : this.bridge_vertices){
            if(allvertices.contains(b))
            {
                affectedbridges.add(b);
            }
        }
       return affectedbridges;

    }

    public Set<Integer> getGlobal_affected_vertices() {
        return global_affected_vertices;
    }

    public Set<Integer> getGlobal_remaining_vertices() {
        return global_remaining_vertices;
    }

    public List<Set<Integer>> getGlobal_affected_clusters() {
        return global_affected_clusters;
    }

    public void checkNewBridge(Set<Integer> affectedVertices){
        for(Integer vx: affectedVertices){
          for(Set<Integer> cluster : this.clusters)
          {
              if(isBridge(vx,cluster))
              {
                  this.bridge_vertices.add(vx);
              }
          }
        }
      //  this.outiler_vertices.removeAll(this.bridge_vertices);
    }


}
