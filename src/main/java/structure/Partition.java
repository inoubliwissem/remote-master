package structure;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Partition implements Serializable {
    private int idp;
    // local edges
    private List<Edge> edges;
    // local vertices
    private List<Vertex> vertices;
    // id of vertex in this partition to verifier if one vertex is in this partition or not
    private Set<Integer> idvertices;
    // frontiers vertices with all partitions
    private List<Vertex> frantVertices;
    // cuts edges with all partitions
    private List<Edge> cutsEdges;
    // index of frontiers vertices  in others partitions
    private Set<Integer> externFrontiersVetices;
    // list of neighbors partition of the current partition
    private Set<Integer> neighborsPartition;
    // list of external vertex
    private List<Vertex> ext_vertex;
    // all vertices
    private List<Vertex> all_vertices;
    //ids of frontiers vertices
    private Set<Integer> ids_fronties;
    // ids of internal vertex
    private Set<Integer> internal;
    private HashMap<Integer,Set<Integer>> graph_i;
    private HashMap<Integer,Set<Integer>> graph_e;



    public Partition() {
        // local edges
        edges = new ArrayList<Edge>();
        // local vertices
        vertices = new ArrayList<Vertex>();
        // frontiers vertices with all partitions
        frantVertices = new ArrayList<Vertex>();
        // cuts edges with all partitions
        cutsEdges = new ArrayList<Edge>();
        // list of neighbors partition of the current partition
        neighborsPartition=new HashSet<Integer>();
        // index of frontiers vertices  in others partitions
        externFrontiersVetices=new HashSet<Integer>();
        // list of external vertices
        ext_vertex=new ArrayList<Vertex>();
        // all vertices external and internal vertices
        all_vertices=new ArrayList<>();
        all_vertices.addAll(vertices);


        idvertices=new HashSet<Integer>();
        ids_fronties=new HashSet<>();
        internal=new HashSet<>();
        graph_e=new HashMap<>();
        graph_i=new HashMap<>();
    }
    //  function to add a vertex V to list of vertex "vertices".
    public void addVertex(Edge e,boolean border) {
        boolean tv = false;
        boolean tv2 = false;
        for (Vertex v : vertices) {
            if (v.getId() == e.getFromNode()) {
                v.addNeigbor(e.getFromNode());
                v.addNeigbor(e.getEndNode());
                tv = true;
            }
            if (v.getId() == e.getEndNode()) {
                v.addNeigbor(e.getFromNode());
                v.addNeigbor(e.getEndNode());
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
            idvertices.add(vi.getId());
            vertices.add(vi);



            if (border)
                frantVertices.add(vi);

        }
        if (tv2 == false) {
            Vertex vi = new Vertex(e.getEndNode());
            vi.addNeigbor(e.getFromNode());
            vi.addNeigbor(e.getEndNode());


            vertices.add(vi);
            idvertices.add(vi.getId());

            if (border) {
                frantVertices.add(vi);
            }

        }
    }
    public void addSingleVertex(int node)
    {

        Vertex vi = new Vertex(node);
        vi.addNeigbor(node);
        vertices.add(vi);
        idvertices.add(vi.getId());
       // vi.printVertex();
    }
    public void addSingleVertex(Vertex v)
    {

        vertices.add(v);
        idvertices.add(v.getId());
        // vi.printVertex();
    }
    public void addSingleEdge(int internal,int newnode)
    {
        Edge e =new Edge(internal,newnode);
      //  e.printEdge();
        e.setfNode(getVertex(internal));
        e.seteNode(getVertex(newnode));
        e.similarityCalculation();
      //  e.printEdge();
        edges.add(e);
    }


    // function to add externals neighbors
    public void addExternalNeighbors()
    {
   /*   for( Edge e : cutsEdges)
        {
            if(idvertices.contains(e.getFromNode()))
            {
                getVertex(e.getFromNode()).addNeigbor(e.getEndNode());
            }
            else if(idvertices.contains(e.getEndNode()))
            {
                getVertex(e.getEndNode()).addNeigbor(e.getFromNode());
            }
        }*/

        cutsEdges.stream().parallel().forEach(e->{
            if(idvertices.contains(e.getFromNode()))
            {
                getVertex(e.getFromNode()).addNeigbor(e.getEndNode());
            }
            else if(idvertices.contains(e.getEndNode()))
            {
                getVertex(e.getEndNode()).addNeigbor(e.getFromNode());
            }

        });

    }
    public void loadgraph(String subGraph, int idp )
    {
        this.idp=idp;
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        frantVertices = new ArrayList<Vertex>();
        cutsEdges = new ArrayList<Edge>();
        neighborsPartition=new HashSet<Integer>();
        externFrontiersVetices=new HashSet<Integer>();
        idvertices=new HashSet<Integer>();
        // list of external vertices
        ext_vertex=new ArrayList<Vertex>();

        //
        ids_fronties=new HashSet<>();
        // read an input graph
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(subGraph);
            br = new BufferedReader(fr);
            String line;
            boolean add_Fron_edge=false;

            while ((line = br.readLine()) != null) {

                if(!line.startsWith("#")) {
                    String parts[] = line.split("\t");
                    // if we have two nodes the line and we are still in simple adges parts
                    if (parts.length > 1 && !add_Fron_edge) {
                        int fn = Integer.parseInt(parts[0]);
                        int en = Integer.parseInt(parts[1]);
                        if(fn!=en) {
                            edges.add(new Edge(fn, en));
                            addVertex(new Edge(fn, en), false);
                        }
                        else
                        {
                            addSingleVertex(en);
                        }
                    }
                    // if we have two nodes the line and we are switching to border edge "after first #"
                    else if((parts.length > 1 && add_Fron_edge))
                    {
                        int fn = Integer.parseInt(parts[0]);
                        int en = Integer.parseInt(parts[1]);
                        edges.add(new Edge(fn, en));
                        cutsEdges.add(new Edge(fn, en));
                    }
                }
                else
                {
                    add_Fron_edge=true;
                }
            }
            // get externals vertex
            for(Edge e : cutsEdges)
            {
                externFrontiersVetices.add(e.getFromNode());
                externFrontiersVetices.add(e.getEndNode());
            }
            for(Vertex v : vertices)
            {
                externFrontiersVetices.remove(v.getId());
            }



        } catch (Exception e) {
            System.out.println("Fraction ereur " + e.getMessage());
        }

    }

    public void addline2(String l, boolean type)
    {
        String parts[] = l.split("\t");

        // if we have two nodes the line and we are still in simple adges parts
        if (parts.length > 1 & !type) {
            int fn = Integer.parseInt(parts[0]);
            int en = Integer.parseInt(parts[1]);
            edges.add(new Edge(fn, en));
            if (graph_i.keySet().contains(fn)) {
                graph_i.get(fn).add(en);
            } else {
                Set<Integer> nei = new HashSet<>();
                nei.add(fn);
                nei.add(en);
                graph_i.put(fn, nei);
            }
            if (graph_i.keySet().contains(en)) {
                graph_i.get(en).add(fn);
            } else {
                Set<Integer> nei = new HashSet<>();
                nei.add(fn);
                nei.add(en);
                graph_i.put(en, nei);
            }
        }
        else  if(type) {
            int fn = Integer.parseInt(parts[0]);
            int en = Integer.parseInt(parts[1]);
                   cutsEdges.add(new Edge(fn, en));
                   edges.add(new Edge(fn, en));
                   if(graph_i.keySet().contains(fn))
                   {
                       graph_i.get(fn).add(en);
                      // externFrontiersVetices.add(en);
                   }
                   else
                   {   Set<Integer> nei=new HashSet<>();
                       nei.add(fn);
                       nei.add(en);
                       graph_i.put(fn, nei);
                       externFrontiersVetices.add(fn);
                   }
                   if(graph_i.keySet().contains(en))
                   {
                       graph_i.get(en).add(fn);
                     //  externFrontiersVetices.add(fn);
                   }
                   else
                   {    Set<Integer> nei=new HashSet<>();
                       nei.add(fn);
                       nei.add(en);
                       graph_i.put(en, nei);
                       externFrontiersVetices.add(en);
                   }
               }

    }

    public void addline(String line, boolean type)
    {
        String parts[] = line.split("\t");

        // if we have two nodes the line and we are still in simple adges parts
        if (parts.length > 1 && !type) {
            int fn = Integer.parseInt(parts[0]);
            int en = Integer.parseInt(parts[1]);
            if(fn!=en) {
                edges.add(new Edge(fn, en));
                addVertex(new Edge(fn, en), false);
            }
            else
            {
                addSingleVertex(en);
            }
        }
        // if we have two nodes the line and we are switching to border edge "after first #"
        else if((parts.length > 1 && type))
        {
            int fn = Integer.parseInt(parts[0]);
            int en = Integer.parseInt(parts[1]);
            edges.add(new Edge(fn, en));
            cutsEdges.add(new Edge(fn, en));
        }

    }



    public Partition(String subGraph, int idp) {
        this.idp=idp;
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        frantVertices = new ArrayList<Vertex>();
        cutsEdges = new ArrayList<Edge>();
        neighborsPartition=new HashSet<Integer>();
        externFrontiersVetices=new HashSet<Integer>();
        idvertices=new HashSet<Integer>();
        // list of external vertices
        ext_vertex=new ArrayList<Vertex>();

        //
        ids_fronties=new HashSet<>();

        graph_e=new HashMap<>();
        graph_i=new HashMap<>();
        // read an input graph
        BufferedReader br_internal = null;
        FileReader fr_internal = null;

        BufferedReader br_external = null;
        FileReader fr_external = null;
        try {
         //   fr = new FileReader(subGraph);
         //   br = new BufferedReader(fr);
            String line;
            Set<String> internalLines=new HashSet<>();
            Set<String> cutLines=new HashSet<>();
            int nbl=0;
            boolean add_Fron_edge=false;

            fr_internal = new FileReader(subGraph+".internal");
            br_internal = new BufferedReader(fr_internal);

            fr_external= new FileReader(subGraph+".external");
            br_external = new BufferedReader(fr_external);

            internalLines=br_internal.lines().filter(l->!(l.startsWith("#"))).collect(Collectors.toSet());
            cutLines=br_external.lines().filter(l->!(l.startsWith("#"))).collect(Collectors.toSet());

            internalLines.stream().forEach(l->this.addline2(l,false));
            cutLines.stream().forEach(l->this.addline2(l,true));

           // System.out.println("sub graph has been loaded "+ internalLines.size()+" and external "+cutLines.size()+" we start to get external vertices");
         //   System.out.println(graph_i+" fron "+externFrontiersVetices);
            // create the real vertex from HashMap
            for(Integer v : graph_i.keySet())
            {
                if(!externFrontiersVetices.contains(v)) {
                Vertex node = new Vertex(v, graph_i.get(v));
                idvertices.add(v);
                vertices.add(node);
                }
            }


            // get externals vertex externFrontiersVetices
    /*    cutsEdges.stream().parallel().forEach(e-> {
                externFrontiersVetices.add(e.getFromNode());
                externFrontiersVetices.add(e.getEndNode());
            });
            vertices.stream().parallel().forEach(v->{
                externFrontiersVetices.remove(v.getId());

            });*/

        //   System.out.println("you sub graph has been loaded with "+edges.size()+" edge s and "+cutsEdges.size()+" cut edges");

        } catch (Exception e) {
            System.out.println("Fraction ereur " + e.getMessage());
        }
    }
    public Partition(String subGraph, int idp,int sansExternalV) {
        this.idp=idp;
        edges = new ArrayList<Edge>();
        vertices = new ArrayList<Vertex>();
        frantVertices = new ArrayList<Vertex>();
        cutsEdges = new ArrayList<Edge>();
        neighborsPartition=new HashSet<Integer>();
        externFrontiersVetices=new HashSet<Integer>();
        idvertices=new HashSet<Integer>();
        // list of external vertices
        ext_vertex=new ArrayList<Vertex>();
        //
        ids_fronties=new HashSet<>();
        // read an input graph
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(subGraph);
            br = new BufferedReader(fr);
            String line;
            boolean add_Fron_edge=false;
            while ((line = br.readLine()) != null) {

                if(!line.startsWith("#")) {
                    String parts[] = line.split("\t");


                    // if we have two nodes the line and we are still in simple adges parts
                    if (parts.length > 1) {
                        int fn = Integer.parseInt(parts[0]);
                        int en = Integer.parseInt(parts[1]);
                        edges.add(new Edge(fn, en));
                        addVertex(new Edge(fn, en),false);
                    }
                    // if we have two nodes the line and we are switching to border edge "after first #"
                    else if((parts.length > 1 && add_Fron_edge))
                    {
                        int fn = Integer.parseInt(parts[0]);
                        int en = Integer.parseInt(parts[1]);
                        edges.add(new Edge(fn, en));
                       // cutsEdges.add(new Edge(fn, en));
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Fraction ereur " + e.getMessage());
        }
    }

    public void printPartition()
    {
        System.out.println(this.getIdp()+ " we have "+vertices.size()+" vertices "+idvertices+" in this partition "+ this.idp+" external v"+this.externFrontiersVetices);
        for(Vertex v : this.ext_vertex)
        {
            v.printVertex();
        }
        this.printEdges();
        this.saveasTextFile();
        /*
        System.out.println("=================================================================");
        for( Vertex v : vertices)
        {
            System.out.println(v.getId()+" ");
            v.printVertex();
        }
        System.out.println("=============================");
        System.out.println("we have "+edges.size()+" edge in this partition");
        for (Edge e : edges)
        {
            System.out.println(e.getFromNode()+"    "+e.getEndNode());
        }

        System.out.println("=============================");
        System.out.println("we have "+cutsEdges.size()+" border edges in this partition");
        for (Edge e : cutsEdges)
        {
            System.out.println(e.getFromNode()+"    "+e.getEndNode());
        }
        System.out.println("we have "+externFrontiersVetices.size()+" external vertices in this partition");
        for( Integer v : externFrontiersVetices)
        {
            System.out.println(v+" ");
        }

        System.out.println("all id vertices");
        System.out.println(idvertices);
        if(this.neighborsPartition.size()>0)
        {
            System.out.println("this partition "+this.idp+" have a list of neighbors "+neighborsPartition);
        }

        System.out.println("we have "+this.ext_vertex.size()+" real external vertices in this partition");
        for(Vertex v : this.ext_vertex)
        {
            v.printVertex();
        }

        System.out.println("=========================End============================");
        System.out.println("=========================Simmilarity============================");
         for(Edge e : this.edges)
         {
             System.out.println("from "+e.getFromNode()+" to "+e.getEndNode()+" sim = "+e.getSimilarity());
         }

        System.out.println("=========================End Simmilarity============================");
        System.out.println(this.idp+" "+ids_fronties);*/
    }

    public int getIdp() {
        return idp;
    }

    public boolean isNeighbord(Partition p){
        for(Integer i : externFrontiersVetices)
        {
            if (p.idvertices.contains(i))
            {
                return true;
            }
        }
        return false;
    }

    public void setIdp(int idp) {
        this.idp = idp;
    }

    public Set<Integer> getNeighborsPartition() {
        return neighborsPartition;
    }

    public void setNeighborsPartition(Set<Integer> neighborsPartition) {
        this.neighborsPartition = neighborsPartition;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Vertex> getFrantVertices() {
        return frantVertices;
    }

    public Set<Integer> getFrantiersVertices()
    {
        return this.ids_fronties;
    }

    public void setFrantVertices(List<Vertex> frantVertices) {
        this.frantVertices = frantVertices;
    }

    public Vertex getVertex(Integer id)
    {

        List<Vertex> lst=this.vertices.stream().parallel().filter(v->v.getId()==id).collect(Collectors.toList());
        if(lst.size()==0)
            return  null;
        else
            return lst.get(0);

        /*for(Vertex v : vertices)
        {
            if (v.getId()==id)
                return  v;
        }
        return null;*/
    }



    public List<Edge> getCutsEdges() {
        return cutsEdges;
    }

    public void setCutsEdges(List<Edge> cutsEdges) {
        this.cutsEdges = cutsEdges;
    }
    public void addPartitionNeighbor(int idp)
    {
        this.neighborsPartition.add(idp);
    }
    public void addVertexToPartition(Vertex v)
    {
        if(!vertices.contains(v.getId())) {
            vertices.add(v);
            idvertices.add(v.getId());
            /////
            externFrontiersVetices.add(v.getId());
            //////
        }

    }
    public void addborderVertex(Vertex v)
    {
        // if a vertex V have neighbors not in the partition we must create these to ensure a correct compute of similarity
      for(Integer nei : v.getNeigbords())
      {
          addVertexToPartition(new Vertex(nei));
      }
      addVertexToPartition(v);
    }
    //several external Vertices have a neighbors not part of the partition, so we must create these
    public void addNoiseNeighbors()
    {
     for(Integer i : externFrontiersVetices)
     {
         Vertex v=getVertex(i);
         for(Integer n : v.getNeigbords())
         {
             if(!idvertices.contains(n))
             {
                 idvertices.add(n);
                 addborderVertex(new Vertex(n));
             }
         }
     }
    }
    public List<Integer>getexternalVertexOfPartion(Partition p)
    {
        List<Integer> externalvertex=new ArrayList<Integer>();
        for(Integer v : p.idvertices)
        {
            if(this.externFrontiersVetices.contains(v))
            {
                externalvertex.add(v);
            }
        }
        return externalvertex;
    }


    public void addPartitionNeighbor(Partition p)
    {
        //***************************from P in parameters => P2 to current P=> P1*********************
        //*******************************P1***********************************************************
        // in this method we test if tow partitions share at least one shared frontier Vertex
        // ad it id to the list of neighbor of the current partition
         System.out.println("cut edge for this partition in P" +this.getIdp());

        System.out.println("external vertex in P1 and from P "+p.getIdp()+" "+this.externFrontiersVetices);
        List<Integer> externalvertice=getexternalVertexOfPartion(p);
        // if we dont have any external vertices with Partition P
        if(externalvertice.size()<1)
        {
          return;
        }


        System.out.println(this.getexternalVertexOfPartion(p));
        //list vertex to be added from P1 to P2
        Set<Integer> borderVertex_p1=new HashSet<Integer>();
        // process to add all frontiers vertex from P2 to P1

        for(Integer v : externalvertice)
        {
            Vertex vx=p.getVertex(v);
            // for each cutedge we add the neighbors fo the vertex VX
            for(Edge e: this.cutsEdges)
            {
              if(e.getEndNode()==v)
              {
                  vx.addNeigbor(e.getFromNode());
                  borderVertex_p1.add(e.getFromNode());
              }
              if(e.getFromNode()==v)
              {
                    vx.addNeigbor(e.getFromNode());
                    borderVertex_p1.add(e.getFromNode());
              }
            }
            // get  all neighbors for all external vertex (vertex from P2 and has a links with the current Partition
            // all neighbors of external vertex represent a border vertex in P1
            // we must add a external  vertices in the current partition
            System.out.println("addvertextopartion  P "+this.getIdp());
            this.addVertexToPartition(vx);


            vx.printVertex();
        }
        System.out.println("we have the next vertices be migred to P"+p.getIdp()+" "+borderVertex_p1);
        // add a border vertices from P1 to P2
       System.out.println("in this step we have "+idvertices+"partition "+this.getIdp());
        // if we find one vertex not in the partition and in the list of frontier list
        // we must get it from partition P and push it to the current partition
        for(Integer i :borderVertex_p1)
        {   // if we find a vertex not part of this partition P1, we added this from P2
            // using p.getVertex(id), whene P the second partion
            if(!idvertices.contains(i))
            {   Vertex va=p.getVertex(i);
               // this.addVertexToPartition(p.getVertex(i));
                System.out.println("addbordervertice P "+this.getIdp());
                this.addborderVertex(va);
            }
        }
        //several external Vertices have a neighbors not part of the partition, so we must create these
        this.addNoiseNeighbors();
        System.out.println("we have the next vertices be migred to P"+p.getIdp()+" ( after correction)"+borderVertex_p1);

//****************************from P=>P1 in parameter to current P=>P2******************************************
        System.out.println("cut edge for  partition  P"+p.getIdp());
       System.out.println("external vertex in P2 and from P"+this.getIdp()+" "+p.getexternalVertexOfPartion(this));

        //list vertex to be added from P2 to P1
        Set<Integer> borderVertex_p2=new HashSet<Integer>();
        // process to add all frontiers vertex P1 to P2
        for(Integer v : p.getexternalVertexOfPartion(this))
        {
            Vertex vx=this.getVertex(v);
            for(Edge e: p.getCutsEdges())
            {
                if(e.getEndNode()==v)
                {
                    vx.addNeigbor(e.getFromNode());
                    borderVertex_p2.add(e.getFromNode());
                }
                if(e.getFromNode()==v)
                {
                    vx.addNeigbor(e.getEndNode());
                    borderVertex_p2.add(e.getEndNode());
                }
            }
            // get  all neighbors for all external vertex (vertex from P2 and has a links with the current Partition
            // all neighbors of external vertex represent a border vertex in P1
           // borderVertex_p1.addAll(vx.getNeigbords());
            // we must add border vertices
           // vx.printVertex();
            // add external vertex to P2
            System.out.println("addvevertextopartionP "+p.getIdp());
            p.addVertexToPartition(vx);

        }
        // process to add neighbor of frontier vertex P1 to P2
       System.out.println("we have the next vertices be migred to P2"+borderVertex_p2);
      System.out.println("in this step we have "+p.idvertices +"partition P2");
        // if we find one vertex not in the partition and in the list of frontier list
        // we must get it from partition P and push it to the current partition
        for(Integer i :borderVertex_p2)
        {
            if(!p.idvertices.contains(i))
            {   Vertex va=this.getVertex(i);
             //   va.printVertex();
               // p.addVertexToPartition(this.getVertex(i));
                System.out.println("addborderverticeP2 ");
                p.addborderVertex(va);
            }
        }
        System.out.println("we have the next vertices be migred to P1 ( after correction)"+borderVertex_p2);
        //several external Vertices have a neighbors not part of the partition, so we must create these
         p.addNoiseNeighbors();


        System.out.println("partition 1");
        for(Vertex v : vertices)
            v.printVertex();
        System.out.println("Partition 2");
        for(Vertex v : p.vertices)
            v.printVertex();


    }

    public Set<Integer> getExternFrontiersVetices() {
        return externFrontiersVetices;
    }

    public Set<Integer> getIdvertices() {
        return idvertices;
    }

    public List<Vertex> getExt_vertex() {
        return ext_vertex;
    }

    public void setExt_vertex(List<Vertex> ext_vertex) {
        this.ext_vertex = ext_vertex;
    }
    public void setExt_vertex(Vertex v) {
        this.ext_vertex.add(v);
    }

    public  Set<Integer> getIntersection(Set<Integer> lst1, Set<Integer> lst2) {
        Set<Integer> rst = new HashSet<Integer>();
        if (lst1.size() > lst2.size()) {
            for (Integer i : lst2) {
                if (lst1.contains(i)) rst.add(i);
            }
        }
        else {
            for (Integer i : lst1) {
                if (lst2.contains(i)) rst.add(i);
            }
        }
        return rst;
    }

    public Set<Integer> addV()
    {
     Set<Integer> list=new HashSet<Integer>();
     for(Vertex v : this.vertices)
     {
         Set<Integer> neigh=v.getStrongNeigbords();
         for(Integer i : neigh) {
             if (!this.idvertices.contains(i))
             {
               list.add(i);
               // we will add a neigbord as node
             }
         }

     }

     return list;
    }
    public void setVertex(Vertex n, Integer new_vertex, Integer old_vertex)
    {

        this.getVertex(new Integer(old_vertex)).addNeigbor(new_vertex);
        if(!this.getIdvertices().contains(new_vertex) && this.getVertex(old_vertex).getNeigbords().size()>0) {
            this.addSingleVertex(new Vertex(new_vertex));
            this.idvertices.add(new_vertex);
            this.externFrontiersVetices.add(new_vertex);
        }
    }
    public void printEdges(){
        System.out.println("Partition "+this.idp);
        for(Edge e : edges)
        {
            e.printEdge();
        }
    }

    public void computeFrontierVertices()
    {
        Set<Integer> internal_v=new HashSet<>();
        internal_v.addAll(this.getIdvertices());
        internal_v.removeAll(this.getExternFrontiersVetices());
        for(Edge e : this.getCutsEdges())
        {

            if(internal_v.contains(e.getEndNode()))
            {
             this.ids_fronties.add(e.getEndNode());
            }
            else
            {
                this.ids_fronties.add(e.getFromNode());
            }
        }
    }

    public void removeCutEdges(Integer v)
    {
        for (Iterator<Edge> iter = this.cutsEdges.listIterator(); iter.hasNext(); ) {
            Edge a = iter.next();
            if (a.getEndNode()==v || a.getFromNode()==v) {
                iter.remove();
            }
        }
    }

    public void saveasTextFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("partition" + this.idp));


            writer.write("================================================================= \n");
            for (Vertex v : vertices) {
                writer.write(v.getId()+" "+v.getNeigbords()+" \n");
            }
            writer.write("================================================================= \n");
            writer.write("we have " + edges.size() + " edge in this partition \n");

            for (Edge e : edges) {
                writer.write(e.getFromNode() + "    " + e.getEndNode() +"\n");
            }

            writer.write("================================================================= \n");
            writer.write("we have " + cutsEdges.size() + " border edges in this partition \n");

            for (Edge e : cutsEdges) {
                writer.write(e.getFromNode() + "    " + e.getEndNode() +"\n");
            }

            writer.write("we have " + externFrontiersVetices.size() + " external vertices in this partition \n");
            for (Integer v : externFrontiersVetices) {

                writer.write(v + " ");
            }
           writer.write("\n");
            writer.write("all id vertices \n");
            writer.write(idvertices+"\n");
            writer.write("we have " + this.ext_vertex.size() + " real external vertices in this partition \n");
            writer.write("\n");
            writer.write("internal  vertices \n");
            writer.write(this.getInternalVertex()+"\n");
           // writer.write("we have " + this.ext_vertex.size() + " real external vertices in this partition \n");

            for (Vertex v : this.ext_vertex) {
                writer.write(v.getId()+" "+v.getNeigbords()+" \n");
            }
            writer.write("=========================End============================ \n");
           writer.write("=========================Simmilarity============================ \n");

            for (Edge e : this.edges) {
                writer.write("from " + e.getFromNode() + " to " + e.getEndNode() + " sim = " + e.getSimilarity()+"\n");
            }

            writer.write("=========================End Simmilarity============================ \n");

           writer.write(this.idp + " " + ids_fronties);

           writer.write("=========================Local clustering informations============================ \n");
           writer.write("=========================local clusters=============================================\n");

           writer.close();

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
// methods in strem

    public Set<Integer> getInternalVertex()
    {
        Set<Integer> intern = new HashSet<>();
        intern.addAll(this.getIdvertices());
        intern.removeAll(this.getExternFrontiersVetices());
        return intern;
    }

public void addSecondVertex(int idv)
{
    this.vertices.add(new Vertex(idv));
    this.idvertices.add(idv);
    this.externFrontiersVetices.add(idv);
}

    // method to add new edge in stream
    public void StremAddNewCutEdge(Edge e){
        e.similarityCalculation();
        edges.add(e);
        cutsEdges.add(e);
        // add a end vertex as an frontiere vertices
      //this.ids_fronties.add(e.getFromNode());
    }
    public void addFrontVertex(int v)
    {
        this.ids_fronties.add(v);
    }
    // method to add new vertex in stream
    public void StreamAddNewVertex(Vertex v)
    {

     if(idvertices.contains(v.getId()))
     {
       this.getVertex(v.getId()).addNeigbors(v.getNeigbords());
         this.idvertices.add(v.getId());
         this.externFrontiersVetices.add(v.getId());
        this.ext_vertex.add(v);
     }
     else {
         this.vertices.add(v);
         this.idvertices.add(v.getId());
         this.externFrontiersVetices.add(v.getId());
         this.ext_vertex.add(v);
     }


     //this.all_vertices.add(v);

     for(Integer n : v.getNeigbords())
     {
         if(!this.idvertices.contains(n))
         {
             this.addSecondVertex(n);
         }
     }

    }

   public void addExtVertex(Vertex v)
   {
      this.ext_vertex.add(v);
      this.idvertices.add(v.getId());
      this.externFrontiersVetices.add(v.getId());
   }

   public void deleteVertex(Integer v)
   {
       if(this.idvertices.contains(v))
       {   // compute list of internal vertices
           Set<Integer> intern = this.getInternalVertex();
           Vertex n=  this.getVertex(v);
           List<Integer> neighbors=n.getNeigbords();
           // if a V  is an external vertex
           if(!intern.contains(v))
           {   // if the vertex is an first external vertex

               if(neighbors.size()>0)
               {   // get the vertex concerned by the deleting and delete it from vertices list
                   List<Vertex> tbr=this.vertices.stream().parallel().filter(nd->nd.getId()==v).collect(Collectors.toList());
                   this.vertices.removeAll(tbr);
                   // get and delete the concerned vertex from external vertices
                   List<Vertex> externaltbr=this.ext_vertex.stream().parallel().filter(nd->nd.getId()==v).collect(Collectors.toList());
                   this.ext_vertex.removeAll(externaltbr);
                   // remove the vertex from all ids of vertices of the partition
                   this.idvertices.remove(v);
                   // remove the vertex from frontier vertices
                   this.externFrontiersVetices.remove(v);
                   // remove the id of vertex from all their neighbors
                   this.vertices.stream().parallel().filter(vx->vx.getNeigbords().contains(v)).forEach(vx->vx.removeNeighbor(v));
                   // delete all edge from or to the vertex
                   List<Edge> edgeTBR =this.edges.stream().parallel().filter(e->(e.getEndNode()==v || e.getFromNode()==v)).collect(Collectors.toList());
                   this.edges.removeAll(edgeTBR);
                   List<Edge> cutedgeTBR =this.cutsEdges.stream().parallel().filter(e->(e.getEndNode()==v || e.getFromNode()==v)).collect(Collectors.toList());
                   this.cutsEdges.removeAll(cutedgeTBR);
                   // recompute the concerned similarity : all edges which contain one of  neighbors of a deleted vertex
                   this.edges.stream().filter(ex->(neighbors.contains(ex.getFromNode())|| neighbors.contains(ex.getEndNode()))).forEach(e->e.similarityCalculationAfterDelete(v));

                   // we will delete all second vertex associated with the deleted vertex and do not attached with another external vertex
                  //  1) get the list of second external vertex
                   List<Vertex> secondVX=this.vertices.stream().parallel().filter(vx->vx.getNeigbords().size()==0).collect(Collectors.toList());

                   for(Vertex vx : secondVX){
                       // if we do not have any connection with the vx we delete it
                       if(this.vertices.stream().parallel().filter(nd->(nd.getNeigbords().contains(vx.getId()))).count()==0)
                       {
                       this.vertices.remove(vx) ;
                       this.idvertices.remove(vx.getId());
                       this.externFrontiersVetices.remove(vx.getId());

                       }
                   }


               }
               // if V is a second external vertex
               else
               {
                   // remove V from all list of neighbors of each vertex in current partition
                   this.vertices.stream().parallel().filter(vx->vx.getNeigbords().contains(v)).forEach(vx->vx.removeNeighbor(v));
                   List<Vertex> tbr=this.vertices.stream().parallel().filter(nd->nd.getId()==v).collect(Collectors.toList());
                   this.vertices.removeAll(tbr);
                  // this.ext_vertex.removeAll(tbr);
                   this.idvertices.remove(v);
                   this.externFrontiersVetices.remove(v);
               }
           }
           // if the vertex te be removed is belongs on internal vertices
           else
           {
               List<Vertex> tbr=this.vertices.stream().parallel().filter(nd->nd.getId()==v).collect(Collectors.toList());
               this.vertices.removeAll(tbr);
               frantVertices.removeAll(tbr);
               this.idvertices.remove(v);
               this.ids_fronties.remove(v);
               // remove V from all list of neighbors
               this.vertices.stream().parallel().filter(vx->vx.getNeigbords().contains(v)).forEach(vx->vx.removeNeighbor(v));
               // remove external vertex
              Set<Vertex> exTBR=this.ext_vertex.stream().parallel().filter(vx->vx.toBeRemoved(intern)).collect(Collectors.toSet());
              this.ext_vertex.removeAll(exTBR);
              this.vertices.removeAll(exTBR);
              for(Vertex vx:exTBR) {
                  this.idvertices.remove(vx.getId());
                  this.externFrontiersVetices.remove(vx.getId());

              }
              // check all second vertices
               // get list of second vertices
               List<Vertex> secondv=this.vertices.stream().parallel().filter(vx->vx.getNeigbords().size()==0).collect(Collectors.toList());
               // store  ids of seconds vertex  into list
               Set<Integer> idsconds=new HashSet<>();
               for(Vertex i : secondv)
               {   boolean tv=false;
                   for(Vertex vx: this.ext_vertex)
                   {
                       if(vx.getNeigbords().contains(i.getId()))
                       {
                           tv=true;
                       }
                   }
                   if(!tv)
                       idsconds.add(i.getId());
               }
               // get a list of second vertex which not belong on neighbors of any vertex in this partition and delete them in second step
               Set<Vertex> secondVTBD=vertices.stream().parallel().filter(vx->(idsconds.contains(vx.getId()))).collect(Collectors.toSet());
               vertices.removeAll(secondVTBD);
               this.idvertices.removeAll(idsconds);
               this.externFrontiersVetices.removeAll(idsconds);
               // end

               List<Edge> edgeTBR =this.edges.stream().parallel().filter(e->(e.getEndNode()==v || e.getFromNode()==v)).collect(Collectors.toList());

               this.edges.removeAll(edgeTBR);

               List<Edge> cutedgeTBR =this.cutsEdges.stream().parallel().filter(e->(e.getEndNode()==v || e.getFromNode()==v)).collect(Collectors.toList());

               this.cutsEdges.removeAll(cutedgeTBR);

              // this.edges.stream().forEach(e->e.similarityCalculationAfterDelete(v));
               this.edges.stream().parallel().filter(ex->(neighbors.contains(ex.getEndNode())|| neighbors.contains(ex.getFromNode()))).forEach(ex->ex.similarityCalculationAfterDelete(v));
               // maintains the frontier vertices
             /*  for(Integer vx : ids_fronties) {
                   if ((this.cutsEdges.stream().filter(e -> (e.getFromNode() == vx) || e.getEndNode() == vx).count() == 0) && this.ids_fronties.contains(vx)) {
                       this.ids_fronties.remove(vx);
                   }
               }*/

           }

       }


   }
   public void removeUselessSecondVertex()
   {
       // get list of second vertices
       List<Vertex> secondv=this.vertices.stream().parallel().filter(vx->vx.getNeigbords().size()==0).collect(Collectors.toList());
       // store  ids of seconds vertex  into list
       Set<Integer> idsconds=new HashSet<>();
       for(Vertex i : secondv)
       {   boolean tv=false;
           for(Vertex vx: this.ext_vertex)
           {
               if(vx.getNeigbords().contains(i.getId()))
               {
                   tv=true;
               }
           }
           if(!tv)
               idsconds.add(i.getId());
       }
       // get a list of second vertex which not belong on neighbors of any vertex in this partition and delete them in second step
       Set<Vertex> secondVTBD=vertices.stream().parallel().filter(vx->(idsconds.contains(vx.getId()))).collect(Collectors.toSet());
       vertices.removeAll(secondVTBD);
       this.idvertices.removeAll(idsconds);
       this.externFrontiersVetices.removeAll(idsconds);
   }

   public void updateFrontierVertex()
   {
      Set<Integer> intern=this.getInternalVertex();
       Set<Integer> lst=new HashSet<>();
       for(Edge e : cutsEdges)
       {
           lst.add(e.getFromNode());
           lst.add(e.getEndNode());
       }

      Set<Integer> frontiers= lst.stream().parallel().filter(elm->(!this.ids_fronties.contains(elm) && intern.contains(elm) )).collect(Collectors.toSet());

   this.ids_fronties.addAll(frontiers);


   }
   public void deleteEdge( int v1, int v2)
   {
       List<Edge> lst_edges_TBR=this.edges.stream().parallel().filter(e->((e.getFromNode()==v1 && e.getEndNode()==v2)|| (e.getFromNode()==v2 && e.getEndNode()==v1))).collect(Collectors.toList());
       List<Edge> cutEdges_TBR=this.cutsEdges.stream().parallel().filter(e->((e.getFromNode()==v1 && e.getEndNode()==v2)|| (e.getFromNode()==v2 && e.getEndNode()==v1))).collect(Collectors.toList());
       // if the edge is an internal edge
       if(cutEdges_TBR.size()==0 && lst_edges_TBR.size()>0)
       {
        this.edges.removeAll(lst_edges_TBR);
        this.getVertex(v1).removeNeighbor(v2);
        this.getVertex(v2).removeNeighbor(v1);
       }
       // if we have an internal edge and one of the both vertices is second external vertex
       else if(cutEdges_TBR.size()==0 && lst_edges_TBR.size()==0)
       {
           if(this.idvertices.stream().parallel().filter(v->(v==v1 || v==v2)).count()>0)
            {
                if(this.idvertices.contains(v1)) {
                this.getVertex(v1).removeNeighbor(v2);
                 }
                if(this.idvertices.contains(v2)) {
                    this.getVertex(v2).removeNeighbor(v1);
                }

                  this.removeUselessSecondVertex();
             }

       }
       // if we have an cut edge
       else if(cutEdges_TBR.size()>0 && lst_edges_TBR.size()>0)
       {
           Set<Integer> internVertex=this.getInternalVertex();
           // test what is the external vertex to be deleted
           if(internVertex.contains(v1))
           {
               this.edges.removeAll(lst_edges_TBR);
               this.cutsEdges.removeAll(cutEdges_TBR);
               // maintains the frontier vertices
               if(this.cutsEdges.stream().parallel().filter(e->(e.getFromNode()==v1)|| e.getEndNode()==v1).count()==0)
               {
                   this.ids_fronties.remove(v1);
               }
               // remove v2 from list of neighbors of v1
               this.getVertex(v1).removeNeighbor(v2);
               this.getVertex(v2).removeNeighbor(v1);
               Vertex vTBR = this.getVertex(v2);
               // we delete  the vertex v2 if it have only one connection with frontier vertices
               // example 3 5  and 2 5 the 5 vertex  have two  connections then we do not delete it
               if(!vTBR.isExternal(this.ids_fronties))
               {
                   // delete v2 and their second neighbors
                   this.vertices.remove(vTBR);
                   this.idvertices.remove(v2);
                   this.ext_vertex.remove(vTBR);
                   this.externFrontiersVetices.remove(v2);
                   // test if the vertex have be connected with anothor external vertex example:edge 3:5
                   if(this.ext_vertex.stream().parallel().filter(v->(v.getNeigbords().contains(v2) && v.getId()!=v2)).count()>0) {
                       this.addSecondVertex(v2);
                   }
               }
               // delete all useless second veretx
               this.removeUselessSecondVertex();
           }
           else if(internVertex.contains(v2))
           {
               this.edges.removeAll(lst_edges_TBR);
               this.cutsEdges.removeAll(cutEdges_TBR);
               // maintains the frontier vertices
               if(this.cutsEdges.stream().parallel().filter(e->(e.getFromNode()==v2)|| e.getEndNode()==v2).count()==0)
               {
                   this.ids_fronties.remove(v2);
               }
               // remove v1 from list of neighbors of v2
               this.getVertex(v2).removeNeighbor(v1);
               this.getVertex(v1).removeNeighbor(v2);
               Vertex vTBR = this.getVertex(v1);
               if(!vTBR.isExternal(this.ids_fronties))
               {
                   // delete v2 and their second neighbors
                   this.vertices.remove(vTBR);
                   this.idvertices.remove(v1);
                   this.ext_vertex.remove(vTBR);
                   this.externFrontiersVetices.remove(v1);
                   // test if the vertex have be connected with anothor external vertex example:edge 3:5
                   if(this.ext_vertex.stream().parallel().filter(v->(v.getNeigbords().contains(v1) && v.getId()!=v1)).count()>0) {
                       this.addSecondVertex(v1);
                   }
               }
               // delete all useless second veretx
               this.removeUselessSecondVertex();

           }
       }
       // get all affected edges and recomute the similarity
       this.edges.stream().parallel().filter(e->((e.containsNeighbor(v1)|| e.containsNeighbor(v2)))).forEach(e->e.similarityCalculation());

   }


}
