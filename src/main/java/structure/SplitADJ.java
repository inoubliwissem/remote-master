package structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class SplitADJ {
    private HashMap<Integer, Set<Integer>> vertices;
    private Set<Integer> id_vertices;
    private Set<String> edges;
    public SplitADJ()
    {
        vertices=new HashMap<>();
        id_vertices=new HashSet<>();
        edges=new HashSet<>();
    }

    public void addVertex(String line) {

        String parts[] =line.split("\t");
        int f=Integer.parseInt(parts[0]);
        int e=Integer.parseInt(parts[1]);
        id_vertices.add(Integer.parseInt(parts[0]));
        id_vertices.add(Integer.parseInt(parts[1]));

        edges.add(line);
        if(vertices.containsKey(f))
        {
            vertices.get(f).add(e);
            vertices.get(f).add(f);
        }
        else
        {   Set<Integer>neighbors=new HashSet<>();
            neighbors.add(e);
            neighbors.add(f);
            vertices.put(f,neighbors);
        }
        if(vertices.containsKey(e))
        {
            vertices.get(e).add(f);
            vertices.get(e).add(e);
        }
        else
        {   Set<Integer>neighbors=new HashSet<>();
            neighbors.add(f);
            neighbors.add(e);
            vertices.put(e,neighbors);
        }

    }
    public void loadPart(String file)
    {
        BufferedReader br = null;
        FileReader fr = null;
        List<String> lines=new ArrayList<String>();
        try{
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    lines.add(line);
                }
            }

            lines.stream().forEach(x->this.addVertex(x));

        } catch(Exception e)
        {
            System.out.println("loarpart"+e.getMessage());
        }

    }

    public HashMap<Integer, Set<Integer>> getVertices() {
        return vertices;
    }

    public void setVertices(HashMap<Integer, Set<Integer>> vertices) {
        this.vertices = vertices;
    }

    public Set<Integer> getId_vertices() {
        return id_vertices;
    }

    public void setId_vertices(Set<Integer> id_vertices) {
        this.id_vertices = id_vertices;
    }

    public Set<String> getEdges() {
        return edges;
    }

    public void setEdges(Set<String> edges) {
        this.edges = edges;
    }

    public void removeVertex(Set<Integer> list)
    {
        id_vertices.removeAll(list);
        for(Integer v : list)
        {
            vertices.remove(v);

        }
    }

    public void saveAsText(String name)
    {
        try {
            PrintWriter writer = new PrintWriter(name, "UTF-8");
            String line="";
            for(Integer v : this.id_vertices)
            {
             line+=""+v;
             for(Integer nei : vertices.get(v))
             {
                 line+=" "+nei;
             }
             writer.write(line+"\n");
             line="";
            }
              writer.close();
        }catch (Exception e)
        {

        }
    }
}

