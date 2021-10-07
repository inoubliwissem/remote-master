package structure;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Graph implements Serializable {

    HashMap< Integer, Node> listOfNodes = new HashMap<Integer, Node>();
    ;
	 HashMap< Integer, Edge> listOfEdges = new HashMap<Integer, Edge>();

    ;
         int partion;

    public int getPartion() {
        return partion;
    }

    public void setPartion(int partion) {
        this.partion = partion;
    }

    public HashMap<Integer, Node> getListOfNodes() {
        return listOfNodes;
    }

    public void addNode(Node n) {

        listOfNodes.put(n.getNode(), n);
    }
/*
    public void setNode(Node n) {
        listOfNodes.put(n.getNode(), n);
    }

    public Node getNode(Integer id) {
        return listOfNodes.get(id);
    }

    public Vector<Edge> getEdgesFromNode(Node fromnode) {
        Vector<Edge> edges = new Vector<>();
        for (Map.Entry<Integer, Edge> edge : listOfEdges.entrySet()) {
            if (edge.getValue().refNode1 == fromnode.getNode()) {
                edges.add(edge.getValue());
            }
        }
        return edges;
    }

    public Vector<Edge> getEdgesToNode(Node tonode) {
        Vector<Edge> edges = new Vector<>();
        for (Map.Entry<Integer, Edge> edge : listOfEdges.entrySet()) {
            if (edge.getValue().refNode2 == tonode.getNode()) {
                edges.add(edge.getValue());
            }
        }
        return edges;
    }
/*
    public Edge getEdge(Node node1, Node node2) {
        Edge e = null;
        for (Map.Entry<Integer, Edge> edge : listOfEdges.entrySet()) {
            if (edge.getValue().refNode1 == node1.getNode() && edge.getValue().refNode2 == node2.getNode()) {
                e = edge.getValue();
            }
        }
        return e;
    }

    public Edge getEdge(Integer idedge) {
        Edge e = null;
        for (Map.Entry<Integer, Edge> edge : listOfEdges.entrySet()) {
            if (edge.getKey() == idedge) {
                e = edge.getValue();
            }
        }
        return e;

    }

    public void setListOfNodes(HashMap<Integer, Node> listOfNodes) {
        this.listOfNodes = listOfNodes;
    }

    public HashMap<Integer, Edge> getListOfEdges() {
        return listOfEdges;
    }

    public void setListOfEdges(HashMap<Integer, Edge> listOfEdges) {
        this.listOfEdges = listOfEdges;
    }
/*
    public void addedge(Node n1, Node n2) {
        Edge e = new Edge(listOfEdges.size() + 1, n1.getNode(), n2.getNode());
        listOfEdges.put(e.getEdgeID(), e);
    }


    public void addedge(Node n1, Node n2, int p1, int p2) {
        Edge e = new Edge(listOfEdges.size() + 1, n1.getNode(), n2.getNode(), p1, p2);
        listOfEdges.put(e.getEdgeID(), e);
    }

    public void parseGraph(String graphFile) throws NumberFormatException, IOException {
        int countEdge = 1;
        String line = "";
        BufferedReader graphReader = new BufferedReader(new FileReader(graphFile));
        while ((line = graphReader.readLine()) != null) {

            if (!line.equals("")) {
                String[] parts = line.split("\\s");
                int refNode1 = Integer.parseInt(parts[0]);
                int refNode2 = Integer.parseInt(parts[1]);
                Node node1 = new Node(refNode1);
                Node node2 = new Node(refNode2);
                listOfNodes.put(refNode1, node1);
                listOfNodes.put(refNode2, node2);
                Edge edge = new Edge(countEdge, refNode1, refNode1);
                listOfEdges.put(countEdge, edge);
                countEdge++;
                //System.out.println(node1);
            }

        }
        graphReader.close();

        for (Map.Entry<Integer, Edge> entry : listOfEdges.entrySet()) {

            //System.out.println(entry.getValue().refNode1);    
        }
    }
*/
    public static void main(String[] args) throws NumberFormatException, IOException {
        Graph g = new Graph();
        //g.parseGraph("data/file");
    }
}
