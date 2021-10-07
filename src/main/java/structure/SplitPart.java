package structure;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class SplitPart {
    private  Set<Integer> vertices;
    private HashMap<Integer,Set<Integer>> list_vertex;
    private  Set<Integer> ext_vertices;
    private  Set<Integer> int_vertices;
    private   Set<String> edges;
/*
    public  SplitPart(){
        vertices=new HashSet<Integer>();
        edges=new ArrayList<String>();
    }*/


   public Set<Integer> getVertices() {
        return vertices;
    }

    public HashMap<Integer,Set<Integer>> getListVerteces()
    {
        return list_vertex;
    }

    public void setVertices(Set<Integer> vertices) {
        this.vertices = vertices;
    }

    public Set<Integer> getExt_vertices() {
        return ext_vertices;
    }

    public void setExt_vertices(Set<Integer> ext_vertices) {
        this.ext_vertices = ext_vertices;
    }

    public Set<Integer> getInt_vertices() {
        return int_vertices;
    }

    public void setInt_vertices(Set<Integer> int_vertices) {
        this.int_vertices = int_vertices;
    }

    public Set<String> getEdges() {
        return edges;
    }

    public void setEdges(Set<String> edges) {
        this.edges = edges;
    }

    public  void addLine(String elm)
    {
        String parts []=elm.split("\t");
        vertices.add(Integer.parseInt(parts[0]));
        vertices.add(Integer.parseInt(parts[1]));

        edges.add(elm);
    }

    public void addVertex(String line) {

        String parts[] =line.split("\t");
        int f=Integer.parseInt(parts[0]);
        int e=Integer.parseInt(parts[1]);
        vertices.add(Integer.parseInt(parts[0]));
        vertices.add(Integer.parseInt(parts[1]));

        edges.add(line);
        if(list_vertex.containsKey(f))
        {
            list_vertex.get(f).add(e);
            list_vertex.get(f).add(f);
        }
        else
        {   Set<Integer>neighbors=new HashSet<>();
            neighbors.add(e);
            neighbors.add(f);
            list_vertex.put(f,neighbors);
        }
        if(list_vertex.containsKey(e))
        {
            list_vertex.get(e).add(f);
            list_vertex.get(e).add(e);
        }
        else
        {   Set<Integer>neighbors=new HashSet<>();
            neighbors.add(f);
            neighbors.add(e);
            list_vertex.put(e,neighbors);
        }

    }

    public void addedges(Set<String> edges)
    {
        this.edges.addAll(edges);
    }

   public void readgraph(String path)
   {
       vertices=new HashSet<Integer>();
       ext_vertices=new HashSet<Integer>();
       int_vertices=new HashSet<Integer>();
       list_vertex=new HashMap<>();
       edges=new HashSet<>();
       BufferedReader br = null;
       FileReader fr = null;
       int nbl=0;
       List<String> lines=new ArrayList<String>();
       try{
           fr = new FileReader(path);
           // roadNet-TX.txt roadNet-CA.txt com-lj.ungraph.txt roadNet-CA.txt
           br = new BufferedReader(fr);
           String line;
           boolean add_Fron_edge = false;
          lines=br.lines().filter(l->!(l.startsWith("#"))).collect(Collectors.toList());

          /* while ((line = br.readLine()) != null) {

               if (!line.startsWith("#")) {
                   lines.add(line);
                   nbl++;
                   if(nbl%10000==0)
                   {
                       System.out.println(nbl);
                       nbl=0;
                   }

               }

           }*/
           lines.stream().forEach(x->this.addVertex(x));
           System.out.println("you partition  has been loaded with "+edges.size());


       } catch(Exception e)
       {
           System.out.println(e.getMessage());
       }


   }
   public void addExternalVertex(Set<Integer> list)
   {
       for( Integer v : list)
       {
           if(this.vertices.contains(v))
           {
               this.ext_vertices.add(v);
               this.vertices.remove(v);
           }
       }
   }
   public void removeExternalVertex(Set<Integer> external_vertices)
   {
       vertices.removeAll(external_vertices);
   }
    public void partionning(int idp,String file)
    {
        edges.clear();
        BufferedReader br = null;
        FileReader fr = null;
        List<String> lines=new ArrayList<String>();
        try{
            fr = new FileReader(file);
            // roadNet-TX.txt roadNet-CA.txt com-lj.ungraph.txt roadNet-CA.txt
            br = new BufferedReader(fr);
            String line;
            boolean add_Fron_edge = false;

            while ((line = br.readLine()) != null) {

                if (!line.startsWith("#")) {
                    lines.add(line);

                }

            }
            lines.stream().forEach(x->this.addLine(x));


        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }





        try {
                PrintWriter writer = new PrintWriter("partition"+idp, "UTF-8");

               // this.ext_vertices.removeAll(this.int_vertices);
                 List<String> int_edge=new ArrayList<>();
                 Set<Integer> added_vertex=new HashSet<>();
                 Set<Integer> last_vertex=new HashSet<>();

         //   System.out.println("partition "+idp+" liste "+list_vertex+"id ve "+list_vertex.keySet()+" ext "+getExt_vertices());

                for(String edge : edges)
                {
                    String parts[]=edge.split("\t");
                    Integer from=Integer.parseInt(parts[0]);
                    Integer end=Integer.parseInt(parts[1]);

                    if(list_vertex.keySet().contains(from) && list_vertex.keySet().contains(end))
                    {
                        writer.println(edge);
                        int_edge.add(edge);
                        added_vertex.add(from);
                        added_vertex.add(end);

                    }
                }
                last_vertex.addAll(this.int_vertices);
                last_vertex.removeAll(added_vertex);
                if(last_vertex.size()>0)
                {
                 for(Integer i : last_vertex)
                {
                    writer.println(i + "\t" + i);
                }
                }
                edges.removeAll(int_edge);
                // cat edges
                writer.println("####");
                for(String edge : edges)
                {
                    String parts[]=edge.split("\t");
                    Integer from=Integer.parseInt(parts[0]);
                    Integer end=Integer.parseInt(parts[1]);
                    if(this.list_vertex.keySet().contains(from)|| this.list_vertex.keySet().contains(end))
                    {
                        writer.println(edge);

                    }

                }
                writer.close();
            } catch(Exception e)
            {

            }


    }
    public void partionningOnTowFile(int idp,String file)
    {
        edges.clear();
        BufferedReader br = null;
        FileReader fr = null;
        List<String> lines=new ArrayList<String>();
        try{
            fr = new FileReader(file);
            // roadNet-TX.txt roadNet-CA.txt com-lj.ungraph.txt roadNet-CA.txt
            br = new BufferedReader(fr);
            String line;
            boolean add_Fron_edge = false;

            while ((line = br.readLine()) != null) {

                if (!line.startsWith("#")) {
                    lines.add(line);

                }

            }
            lines.stream().forEach(x->this.addLine(x));


        } catch(Exception e)
        {
            System.out.println(e.getMessage());
        }





        try {
          //  PrintWriter writer = new PrintWriter("partition"+idp, "UTF-8");
            PrintWriter writer_intern = new PrintWriter("partition"+idp+".internal", "UTF-8");
            PrintWriter writer_extern = new PrintWriter("partition"+idp+".external", "UTF-8");
            // this.ext_vertices.removeAll(this.int_vertices);
            List<String> int_edge=new ArrayList<>();
            Set<Integer> added_vertex=new HashSet<>();
            Set<Integer> last_vertex=new HashSet<>();

            //   System.out.println("partition "+idp+" liste "+list_vertex+"id ve "+list_vertex.keySet()+" ext "+getExt_vertices());

            for(String edge : edges)
            {
                String parts[]=edge.split("\t");
                Integer from=Integer.parseInt(parts[0]);
                Integer end=Integer.parseInt(parts[1]);

                if(list_vertex.keySet().contains(from) && list_vertex.keySet().contains(end))
                {
                  //  writer.println(edge);
                    writer_intern.println(edge);
                    int_edge.add(edge);
                    added_vertex.add(from);
                    added_vertex.add(end);

                }
            }
            last_vertex.addAll(this.int_vertices);
            last_vertex.removeAll(added_vertex);
            if(last_vertex.size()>0)
            {
                for(Integer i : last_vertex)
                {
                   // writer.println(i + "\t" + i);
                    writer_intern.println(i + "\t" + i);
                }
            }
            edges.removeAll(int_edge);
            // cat edges
           // writer.println("####");
            for(String edge : edges)
            {
                String parts[]=edge.split("\t");
                Integer from=Integer.parseInt(parts[0]);
                Integer end=Integer.parseInt(parts[1]);
                if(this.list_vertex.keySet().contains(from)|| this.list_vertex.keySet().contains(end))
                {
                  //  writer.println(edge);
                    writer_extern.println(edge);

                }

            }
          //  writer.close();
            writer_intern.close();
            writer_extern.close();
        } catch(Exception e)
        {

        }


    }
    public Integer getallconections(Integer v)
    {
        int nb=0;
        Set<Integer> neig=new HashSet<>();
        neig=list_vertex.get(v);
        for( Integer x : neig)
        {
            nb+=list_vertex.get(x).size();
        }
        return nb;
    }

    public int getConnection(Integer v)
    {
        int nb=0;
        for( String e : this.edges)
        {
            String parts[]=e.split("\t");
            Integer from=Integer.parseInt(parts[0]);
            Integer end=Integer.parseInt(parts[1]);


            if(v.equals(from) || v.equals(end))
            {

                nb++;
            }
        }
        return  nb;
    }

    public void printpartition()
    {
      //  System.out.println(vertices);

        System.out.println("in this partition  we have the  vertices are "+this.vertices +" and external vertices "+this.ext_vertices+ " and first Internal vertices are "+this.int_vertices+" edges "+this.edges);
    }

    public void addInternalVertex()
    {
       /* Set<Integer> list=new HashSet<>();
        list.addAll(this.vertices);
        list.removeAll(this.ext_vertices);
        this.int_vertices.addAll(list);*/
        Set<Integer> list=new HashSet<>();
        list.addAll(this.vertices);
        list.removeAll(this.ext_vertices);
        this.int_vertices.addAll(list);

    }
    public void removeExternalVertex()
    {
        for(Integer i : this.ext_vertices)
        {
            list_vertex.remove(i);
        }
    }
}
