package BLADYG;

import akka.cluster.ClusterEvent;

import messageBLADYG.*;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.util.Timeout;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;

import structure.*;

public class Worker extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    Timeout timeout = new Timeout(Duration.create(20, "seconds"));
    Cluster cluster = Cluster.get(getContext().system());
    int workerID;
    int nbworker;
    static Hashtable<Integer, ActorRef> InfoWorkersInWorker;
    private Partition p;
    private SplitPart sp;
    private Clustering c;
    List<Set<Integer>> local_clusters;
    Set<Integer> local_bridges;
    HashMap<Integer, Vector<Integer>> global_External_V;

    Set<Integer> global_affected ;
    int MsgCount = 0;

    Hashtable<Integer, ActorRef> Membres;
    SplitADJ part;

    public String getStringFromSetInt(Set<Integer> list)
    {
        String content="";
        for(Integer i : list){
            content+=i+",";
        }
        if(content.length()>1)
        {
            content = content.substring(0, content.length() - 1);
        }
        return content;
    }
    public void sendToAllWorkers(String msg,Object data)
    {
        for (int i = 1; i < this.Membres.size(); i++) {

             this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, msg, data), getSelf());
        }

    }
    public void sendToMaster(String msg,Object data)
    {
        Membres.get(0).tell(new WorkerToMasterMsg(workerID, msg, data), getSelf());
    }

    public String getStringFromListInt(Set<Integer> list)
    {
        String content="";
        for(Integer i : list){
            content+=i+",";
        }
        if(content.length()>1)
        {
            content = content.substring(0, content.length() - 1);
        }
        return content;
    }
    public Set<Integer> getSetFromString(String content)
    {
        Set<Integer> list=new HashSet<>();
        String parts[]= content.split(",");
        for(int i=0;i<parts.length;i++)
        {
            list.add(new Integer(parts[i]));
        }
        return list;
    }

    //subscribe to cluster changes
    @Override
    public void preStart() throws IOException, InterruptedException {
//#subscribe
        cluster.subscribe(getSelf(), ClusterEvent.MemberEvent.class);
        //  log.error("worker starting "+getSelf().path().name());
        // this.Membres.put(this.workerID,getSelf());

    }

    public Worker(int workerID, int nbworker) {
        this.workerID = workerID;
        this.nbworker = nbworker;
        log.info("starting worker ID " + this.workerID);
        this.Membres = new Hashtable<Integer, ActorRef>();
        global_External_V = new HashMap<>();
        part = new SplitADJ();
        global_affected=new HashSet<>();

        //  this.Membres.put(this.workerID,getSelf());
    }


    @Override
    public void onReceive(Object message) throws Exception {
        //   System.out.println(getSelf().path().name()+workerID+" worker recives message from "+sender().path().name()+" message "+message.getClass().getName());
        // step to Identification
        if (message instanceof ClusterIdentificationMsg) {
            //log.info("worker recives cluster identification message");
            getSender().tell(new ClusterIdentificationMsg(), getSelf());
            //log.info("worker ID "+workerID+" sender"+getSender().path());

        }
        // parse all action between master to workers
        //********************************************************Master2Worker****************************************************************
        else if (message instanceof MasterToWorkerMsg) {
            MasterToWorkerMsg obj = (MasterToWorkerMsg) message;
            // if master sent a message to load a graph or partion of graph
            if (obj.getOperationInfo().toUpperCase().equals("LOADGRAPH")) {
                //get path of partition from input message
                String path_file = obj.getObject().toString();
                // workerID = obj.getReceiverWorkerID() + 1;
                //parse the file to get all vertices and edges
                log.info("we have recive a  partition " + path_file+ " file from master to " + obj.getReceiverWorkerID()+" :");

               p = new Partition(path_file+this.workerID, workerID);
             //  p.addExternalNeighbors();
              // p.printPartition();

               getSender().tell(new WorkerToMasterMsg(obj.getReceiverWorkerID(), "RSTLOADGRAPH", null), getSelf());

            }
            // if master sent a message to destroy partion of the worker
            else if (obj.getOperationInfo().toUpperCase().equals("DESTROY")) {

              //  this.p.destroy();
             //   this.c.destroy();
                getSender().tell(new WorkerToMasterMsg(obj.getReceiverWorkerID(), "RSTDESTROY", null), getSelf());
            }

            // if master sent a message to get external vertices
            else if (obj.getOperationInfo().toUpperCase().equals("EXTERNALV")) {
                log.error("from worker external nodes needed " + this.Membres.size());
                log.error("w0" + this.Membres.get(0).path());
                log.error("worker ID " + this.workerID + " /" + this.Membres.keySet());
                log.error("w n" + this.Membres.get(this.Membres.size() - 1).path() + " nb" + (this.Membres.size() - 1));
                for (int i = 1; i < this.Membres.size(); i++) {
                    if (i != workerID) {
                        log.error("send from " + this.workerID + " to " + i);
                        this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "EXTERNALV", p.getExternFrontiersVetices()), getSelf());
                    }
                }
            }
            // this message to compute external vertex by group
            else if (obj.getOperationInfo().toUpperCase().equals("EXTERNALV2")) {
                //  log.error("from worker external 2 nodes needed " + this.Membres.size());
                //  log.error("w" + this.Membres.get(0).path());
                //    log.error("worker ID " + this.workerID+" /"+this.Membres.keySet());
                //  log.error("w n" + this.Membres.get(this.Membres.size()-1).path()+" nb "+(this.Membres.size()-1));
                for (int i = 1; i < this.Membres.size(); i++) {
                    // if (i != this.workerID) {

                   // log.error("send from " + this.workerID + " to " + i + " is " + p.getExternFrontiersVetices() + " / all vertices" + p.getIdvertices()+" frontiers vertices are"+p.getFrantiersVertices());
                    this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "EXTERNALV2", p.getExternFrontiersVetices()), getSelf());
                    //   }
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("ADDV")) {
                log.info("add v in " + workerID + " =>" + p.addV());
            } else if (obj.getOperationInfo().toUpperCase().equals("ADDINTERNALV")) {
                this.sp.addInternalVertex();
                getSender().tell(new WorkerToMasterMsg(workerID, "RSTINTERNALV", workerID), getSelf());
            } else if (obj.getOperationInfo().toUpperCase().equals("SHAREEDGES")) {
                // we will sent to all workers our edges
                for (int i = 1; i < this.Membres.size(); i++) {
                    this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "SHAREEDGES", this.sp.getEdges()), getSelf());
                }
            }
            // EXTERNALVM2 message to  get when we can find each external vertex
            else if (obj.getOperationInfo().toUpperCase().equals("EXTERNALVM2")) {

                log.info("worker " + obj.getReceiverWorkerID() + " vertices " + obj.getObject());

            } else if (obj.getOperationInfo().toUpperCase().equals("NBNODES")) {

                log.info(workerID + " we have " + p.getIdvertices().size() + " in partion Num" + p.getIdp() + " " + p.getIdvertices());
                for (int i = 0; i < this.Membres.size(); i++) {
                    log.info("in worker num " + workerID + " :" + this.Membres.get(i).path());
                }

            } else if (obj.getOperationInfo().toUpperCase().equals("MEMBRES")) {
                log.info("i m here "+ this.workerID);
                getSender().tell(new WorkerToMasterMsg(workerID, "RSTMEMBRES", workerID), getSelf());

            } else if (obj.getOperationInfo().toUpperCase().equals("PRINTPARTITION")) {
                try {
                    sp.printpartition();
                } catch (Exception e) {
                    log.error("Exception from print partition  " + e.getMessage());
                }
                getSender().tell(new WorkerToMasterMsg(workerID, "END", "print partition"), getSelf());

            } else if (obj.getOperationInfo().toUpperCase().equals("LASTOUTLIERS")) {
                try {
                    Set<Integer> brigdes=(Set<Integer>)obj.getObject();
                    this.c.removeOutliers(brigdes);
                    getSender().tell(new WorkerToMasterMsg(workerID, "RSTLASTOUTLIERS", c.getReal_outiler_vertices()), getSelf());
                } catch (Exception e) {
                    log.error("Exception from print partition  " + e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("DLASTOUTLIERS")) {
                try {
                    Set<Integer> brigdes=(Set<Integer>)obj.getObject();
                    this.c.removeOutliers(brigdes);
                    getSender().tell(new WorkerToMasterMsg(workerID, "DRSTLASTOUTLIERS", c.getReal_outiler_vertices()), getSelf());
                } catch (Exception e) {
                    log.error("Exception from print partition  " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("LOADPARTT")) {
                try {
                   String file=obj.getObject().toString()+this.workerID;
                   this.part.loadPart(file);


                   HashMap<Integer,Set<Integer>> local_vertex = part.getVertices();
                   for(int i =1;i<this.Membres.size();i++)
                   {
                       if(i!= this.workerID)
                       {
                           this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "SHAREVERTEX", local_vertex), getSelf());
                       }
                   }
                } catch (Exception e) {
                    log.error("Exception from print partition  " + e.getMessage());
                }
            }



            // message to add a neighbors of border after their migration   NOB
            else if (obj.getOperationInfo().toUpperCase().equals("NOB")) {
                //  log.info("we well create "+this.workerID);
                List<Vertex> external = p.getExt_vertex();
                for (Vertex v : external) {
                    for (Integer nei : v.getNeigbords()) {
                        if (!p.getIdvertices().contains(nei)) {
                            p.addVertexToPartition(new Vertex(nei));
                        }
                    }

                }
                getSender().tell(new WorkerToMasterMsg(workerID, "END", "NOB"), getSelf());

            }
            // message to start a clustering function
            else if (obj.getOperationInfo().toUpperCase().startsWith("SCAN")) {
                log.info("we start a SCAN in woker num :" + workerID);
                String[] parts = obj.getOperationInfo().split(",");
                double sigma = 0.7;
                int mu = 3;
                if (parts.length > 1) {
                    sigma = Double.parseDouble(parts[1]);
                    mu = Integer.parseInt(parts[2]);
                }
               //  p.printPartition();
                p.computeFrontierVertices();
                c = new Clustering(p);
                c.calculateSimilarity();
                // we must set a mu and fi when we send our query
               c.SCAN(sigma, mu);
                this.local_clusters = new ArrayList<Set<Integer>>();
                this.local_bridges = new HashSet<Integer>();
                log.info("we have been finished the SCAN on worker num :" + workerID);
              // c.printDetails();
               // log.info("worker " + workerID + " " + c.getClusters() + " core " + c.getIdCores() + " border " + c.getBorder_vertices() + " bridge " + c.getBridge_vertices() + " outiers" + c.getOutiler_vertices());
                //getSender().tell(new WorkerToMasterMsg(workerID, "ENDLOCALSCAN", workerID), getSelf());
                Membres.get(0).tell(new WorkerToMasterMsg(workerID, "ENDLOCALSCAN", " save partitions"), getSelf());


            } else if (obj.getOperationInfo().toUpperCase().equals("COMBINER")) {
                // we send all cores vertices to all workers to get intersection between them in all partitions
                // list of core vertices and not in the external vertices
                Set<Integer> listCores = new HashSet<Integer>();
                for (Integer core : this.c.getIdCores()) {
                    if (!p.getExternFrontiersVetices().contains(core)) {
                        listCores.add(core);
                    }
                }
                // log.info("im worker "+workerID+" i have the list of clusters  "+c.getClusters()+" and list of core "+c.getIdCores()+" and internal core "+listCores);

                for (int i = 1; i < this.Membres.size(); i++) {
                  //  if (i != this.workerID) {
                    //  log.error("send " + listCores + " from " + workerID + " to " + i+" partition cores"+p.getInternalVertex());
                      // log.error("send " + c.getIdCores()+ " from " + workerID + " to " + i);
                    //    this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "CORESV", c.getIdCores()), getSelf());
                     this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "CORESV", listCores), getSelf());

                   // }
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("RSTLOCALSCAN")) {
                // we send to master the local clusters they  not contains cores vertex in external vertices list

                for (Set<Integer> c : c.getClusters()) {
                    Set<Integer> cores = this.c.getCoreOfCluster(c, p.getExternFrontiersVetices());
                  //  log.info("cores " + cores);
                    if (cores.size() == 0) {
                        local_clusters.add(c);
                    }

                }
                getSender().tell(new WorkerToMasterMsg(workerID, "CLUSTERS", local_clusters), getSelf());
            } else if (obj.getOperationInfo().toUpperCase().equals("BRIDGES")) {
                // master request all workers to get their bridges
                //  then, in first step each worker send all outliers nodes to all others worker to check if the nodes have others connections
                // to be considered as brides
                Set<Integer> bridges = new HashSet<Integer>();
                this.c.reComputeBridgeOutliers();
                bridges = c.getReal_bridge_vertices();

                for (int i = 1; i < this.Membres.size(); i++) {
                    if (i != workerID) {
                     //   log.error("send list of local bridges " + bridges + " from " + workerID + " to " + i);
                        this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "LOCALBRIDGES", bridges), getSelf());
                    }
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("DBRIDGES")) {
                // master request all workers to get their new  bridges according the new changes

                // get affected vertices in order to check the new bridges
                Set<Integer> affected_vertices=(Set<Integer>)obj.getObject();
                Set<Integer> bridges = new HashSet<Integer>();
                bridges= this.c.reComputeDynamicBridgeOutliers(affected_vertices);
              //  log.info("new bridges are "+bridges);
                for (int i = 1; i < this.Membres.size(); i++) {
                    if (i != workerID) {
                        //   log.error("send list of local bridges " + bridges + " from " + workerID + " to " + i);
                      this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "DLOCALBRIDGES", bridges), getSelf());
                    }
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("OUTLIERS")) {
                // according this message we sent to all others workers my local outliers
                Set<Integer> outliers = new HashSet<Integer>();
                outliers = c.getReal_outiler_vertices();
                HashMap<Integer, Set<Integer>> localoutliers = new HashMap<>();
                // get for each outliers the cluster associated with him
                for (Integer o : outliers) {
                    localoutliers.put(o, c.getCluster(o));
                }
                for (int i = 1; i < this.Membres.size(); i++) {
                    //if (i != this.workerID) {
                      //  log.error("send list of local outliers " + outliers + " from " + workerID + " to " + i + " local outliers" + localoutliers);
                        this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "LOCALOUTLIERS", outliers), getSelf());
                   // }
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("DOUTLIERS")) {
                // according this message we sent to all others workers my local outliers
                Set<Integer> outliers = new HashSet<Integer>();
                outliers = c.getReal_outiler_vertices();
                HashMap<Integer, Set<Integer>> localoutliers = new HashMap<>();
                // get for each outliers the cluster associated with him
                for (Integer o : outliers) {
                    localoutliers.put(o, c.getCluster(o));
                }
                for (int i = 1; i < this.Membres.size(); i++) {
                    //if (i != this.workerID) {
                    //  log.error("send list of local outliers " + outliers + " from " + workerID + " to " + i + " local outliers" + localoutliers);
                    this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "DLOCALOUTLIERS", this.c.getOutiler_vertices()), getSelf());
                    // }
                }
            }


            else if (obj.getOperationInfo().toUpperCase().equals("ADDMEMBRE")) {
                // put all memebre from master
                // log.info("add membre : worker ID "+obj.getReceiverWorkerID()+" worker "+sender().path());
                this.Membres.put(obj.getReceiverWorkerID(), sender());
                if (this.Membres.size() == this.nbworker + 1) {
                    //  log.info(" Worker id = "+this.workerID+" list of workers "+this.Membres.keySet()+" master "+this.Membres.get(0).path());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("LOADPARTITION")) {

                //get path of partition from input message
                String path_file = obj.getObject().toString();
                try {
                    // workerID = obj.getReceiverWorkerID() + 1;
                    //parse the file to get all vertices and edges
                    log.info("we have recive a " + obj.getObject().toString() + " file from master to " + obj.getReceiverWorkerID());
                    sp = new SplitPart();
                    sp.readgraph(path_file+this.workerID);
                    log.info("worker "+this.workerID+" has been load it sub graph"+path_file+this.workerID);
                    getSender().tell(new WorkerToMasterMsg(obj.getReceiverWorkerID(), "RSTLOADPARTITION", null), getSelf());
                } catch (Exception e) {
                    log.error("Exception from loadpartition method in worker " + e.getMessage());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("PRINT")) {
                try {
                    //sp.printpartition();
                    this.p.printPartition();
                    this.c.printlocalclustering(this.workerID);
                  //  log.info("we try to print partition num " + this.workerID);
                    getSender().tell(new WorkerToMasterMsg(obj.getReceiverWorkerID(), "END", "print the current partition"), getSelf());
                } catch (Exception e) {
                    log.error("Exception from PRINT method in workee " + e.getMessage());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("SENT")) {
                try{
                // in this step we well send all vertices to others workers to get the internal vertices
                for (int i = 1; i < Membres.size(); i++) {
                    // if(i!=this.workerID) {
                    // this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "VERTICES", this.sp.getVertices()), getSelf());
                    this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "VERTICES", this.sp.getListVerteces()), getSelf());


                    //  log.info("we are a worker num " + this.workerID + " we want to sent my vertices " + sp.getVertices() + " to worker num " + i);
                    //  }
                }
            }
                catch(Exception e)
                {
                  log.error("sent message "+this.workerID+" "+e.getMessage());
                }

            } else if (obj.getOperationInfo().toUpperCase().equals("INTERNALV")) {

                // in this step we well send all external vertices  to others workers to get if any exV can be an internalV in others partition
                //  List<Pair<Integer,Integer>> list_ExT=new ArrayList<>();
                HashMap<Integer, Vector<Integer>> list_ExT = new HashMap<>();
                for (Integer i : this.sp.getExt_vertices()) {
                    int nb = sp.getConnection(i);
                    //list_ExT.add(new Pair<Integer, Integer>(i,new Integer(nb)));
                    Vector<Integer> elm = new Vector<>();
                    elm.add(nb);
                    elm.add(this.workerID);
                    list_ExT.put(i, elm);
                }

                for (int i = 1; i < Membres.size(); i++) {
                    // if(i!=this.workerID) {
                    this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "INTERNALV", list_ExT), getSelf());
                 //   log.info("we are a worker num " + this.workerID + " we want to sent my vertices " + sp.getVertices() + " to worker num " + i + "   " + list_ExT + " " + sp.getEdges());
                    //  }
                }

            } else if (obj.getOperationInfo().toUpperCase().startsWith("SAVE")) {
                try {
                    String parts[] =obj.getObject().toString().split(":");
                   // sp.partionning(this.workerID,parts[1]);partionningOnTowFile
                    sp.partionningOnTowFile(this.workerID,parts[1]);
                    Membres.get(0).tell(new WorkerToMasterMsg(obj.getReceiverWorkerID(), "END", " save partitions"), getSelf());
                } catch (Exception e) {
                    log.error("Exception from save method in worker " + e.getMessage());
                }

            }
            // message to process an adding a new vertex associted to an existing vertex
            else if (obj.getOperationInfo().toUpperCase().equals("ADDEDGE")) {
                try {
                    String parts[] = (String[]) obj.getObject().toString().split(";");
                  //  log.info(" worker" + workerID + " " + parts[0] + " " + parts[1]);
                    // get internal vertices in the partition
                    Set<Integer> intern = this.p.getInternalVertex();
                    // test if a new vertex is an internal or external vertex
                    if (intern.contains(new Integer(parts[1])) && !this.p.getFrantiersVertices().contains(new Integer(parts[1]))) {
                      //  log.info("Find a depended vertex on worker  " + this.workerID + " because it is belong her internal list" + intern+" ");
                        // add a new vertex who is a parts[0] in a new edge
                        this.p.addSingleVertex(new Integer(parts[0]));
                        // add parts[1] as an neighbor of  a new vertex added
                        this.p.getVertex(new Integer(parts[0])).addNeigbor(new Integer(parts[1]));
                        // add parts[0] as an neighbor of  a new vertex added
                        this.p.getVertex(new Integer(parts[1])).addNeigbor(new Integer(parts[0]));
                        // add a new edge
                        this.p.addSingleEdge(new Integer(parts[0]), new Integer(parts[1]));
                        // get a vertex must be updated, which have a new vertex
                        Vertex nodeToUpdated = p.getVertex(new Integer(parts[1]));
                        //  System.out.println(workerID + "Internal vertices " + this.p.getIdvertices() + "  external v " + this.p.getExternFrontiersVetices());
                       // send the vertex "nodeToUpdated" associated with a new vertex added to all partitions to modify them
                        this.p.getEdges().stream().filter(e->(e.getFromNode()==new Integer(parts[1]) || e.getEndNode()==new Integer(parts[1]))).forEach(e->e.addneighbors(new Integer(parts[1]),new Integer(parts[0])));

                        for (int i = 1; i < this.Membres.size(); i++) {

                            this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "SETUPNODES1", nodeToUpdated), getSelf());
                        }
                    }
                    else if(intern.contains(new Integer(parts[1])) && this.p.getFrantiersVertices().contains(new Integer(parts[1])))
                    {   // if a new vertex is an external vertex
                        // add a new vertex who is a parts[0] in a new edge
                        this.p.addSingleVertex(new Integer(parts[0]));
                        // add parts[1] as an neighbor of  a new vertex added
                        this.p.getVertex(new Integer(parts[0])).addNeigbor(new Integer(parts[1]));
                        // add parts[0] as an neighbor of  a new vertex added
                        this.p.getVertex(new Integer(parts[1])).addNeigbor(new Integer(parts[0]));
                        // add a new edge
                        this.p.addSingleEdge(new Integer(parts[0]), new Integer(parts[1]));
                        // get a vertex must be updated, which have a new vertex
                        Vertex nodeToUpdated = p.getVertex(new Integer(parts[1]));
                        // send the vertex "nodeToUpdated" associated with a new vertex added to all partitions to modify them

                        for (int i = 1; i < this.Membres.size(); i++) {
                           this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "SETUPNODES2", obj.getObject().toString()), getSelf());

                        }
                    }
                    else
                    {
                       Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTADDVERTEX", null), getSelf());

                      // log.info("i m a worker number "+this.workerID+" i don't  have any change");
                       //   System.out.println("i m worker "+this.workerID+" and i do not have ony  changed vertex ");
                    }


                } catch (Exception e) {
                    log.error("Exception from node method in worker " + e.getMessage());
                }

            }
            // message from user to invokes a cluster to add a new edge
            else if (obj.getOperationInfo().toUpperCase().equals("ADDNEWEDGE"))
            {
            try {
                String parts[] = obj.getObject().toString().split(";");
                Integer n1 = Integer.parseInt(parts[0]);
                Integer n2 = Integer.parseInt(parts[1]);
               // get list of internal vertices
               Set<Integer> intern = this.p.getInternalVertex();
                // internal to internal vertex
                if(intern.contains(n1) && intern.contains(n2))
                {
                    this.p.getVertex(n1).addNeigbor(n2);
                    this.p.getVertex(n2).addNeigbor(n1);
                    this.p.addSingleEdge(n1,n2);
                    Set<Integer> affected = new HashSet<>();
                    affected.addAll(this.p.getVertex(n1).getNeigbords());
                    affected.addAll(this.p.getVertex(n2).getNeigbords());
                    // recompute the similarity
                    this.p.getEdges().stream().filter(c->(affected.contains(c.getFromNode()) && affected.contains(c.getEndNode()))).forEach(e->e.similarityCalculation());
                    // if one vertex among the edge is a border vertex we must send it to other neighbors (worker) to add it
                    List<Vertex> border_vertex=new ArrayList<Vertex>();
                    if(this.p.getFrantiersVertices().contains(n1) && this.p.getFrantiersVertices().contains(n2))
                    {
                     border_vertex.add(this.p.getVertex(n1));
                     border_vertex.add(this.p.getVertex(n2));
                    }
                    else if(this.p.getFrantiersVertices().contains(n2))
                    {
                        border_vertex.add(this.p.getVertex(n2));
                        this.global_affected.addAll(this.p.getVertex(n1).getNeigbords());
                    }
                    else if(this.p.getFrantiersVertices().contains(n1))
                    {
                        border_vertex.add(this.p.getVertex(n1));
                        this.global_affected.addAll(this.p.getVertex(n2).getNeigbords());
                    }

                     // send the border vertex to all workers
                    for (int i = 1; i < this.Membres.size(); i++) {
                       this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "SETBORDERVERTEX", border_vertex), getSelf());
                    }

                }
                // edge from an internal vertex and an external vertex
                else{

                       if(intern.contains(n1))
                       {
                       Edge e =new Edge();
                       e.setfNode(this.p.getVertex(n1));
                       e.setFromNode(n1);
                       e.seteNode(null);
                       e.setEndNode(n2);

                           for (int i = 1; i < this.Membres.size(); i++) {
                               this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "ADDNEWEDGE", e), getSelf());
                           }
                       }
                }


            }   catch (Exception e) {
                log.error("Exception from addnewedge method in worker " + e.getMessage());
            }

            }

            // message from user to ivoque a cluseter to delete an existing vertex
            else if (obj.getOperationInfo().toUpperCase().equals("DELETEVERTEX"))
            {
                try{
                  Integer node=Integer.parseInt(obj.getObject().toString());

                    // get concerned vertex
                    Set<Integer> affected =new HashSet<>();

                   Vertex v=this.p.getVertex(node);
                   if(v!=null)
                   {
                       affected.addAll(v.getNeigbords());
                       for(Integer nei : v.getNeigbords()){
                           affected.addAll(this.p.getVertex(nei).getNeigbords());
                       }
                   }
                 /*    if(this.p.getVertices().stream().filter(v->(v.getId()==node) && v.getNeigbords().size()>0).count()>0 )
                    {
                        affected.addAll(this.p.getVertex(node).getNeigbords());
                    }
                    else  if(this.p.getVertices().stream().filter(v->(v.getId()==node) && v.getNeigbords().size()>0).count()>0 )
                    {
                        affected.addAll(this.p.getVertex(node).getNeigbords());
                    }*/
                    affected.remove(node);
                    this.p.deleteVertex(node);
                    // delete the vertex from the cluster
                    this.c.deleteVertex(node);
                    this.global_affected.addAll(affected);
                    this.global_affected.remove(node);

                    Set<Integer> TBR=new HashSet<>();
                    for(Integer vx: affected){
                        if(this.c.getCores_vertices().contains(vx) || this.c.getBorder_vertices().contains(vx))
                        {
                            Vertex vrt=this.c.getVertex(vx);

                            for(Integer nei:vrt.getNeigbords()){
                                if(this.c.getBridge_vertices().contains(nei)){
                                    TBR.add(nei);
                                }
                            }
                        }
                    }

                        affected.addAll(TBR);

                   // this.p.saveasTextFile();
                    // return all results to master
                    //  log.info("send from "+this.workerID+" "+affected);
                    //Membres.get(0).tell(new WorkerToMasterMsg(this.workerID, "RSTDELETEVERTEX", affected), getSelf());
                    this.sendToAllWorkers("CHECKNODES",affected);

                }   catch (Exception e) {
                    log.error("Exception from DELETEVERTEX method in worker " +this.workerID+" " + e.getMessage()+" cause "+e.getCause());
                }

            }

            // message from user to ivoque a cluseter to delete an existing EDGE
            else if (obj.getOperationInfo().toUpperCase().equals("DELETEEDGE"))
            {
                try{
                    String parts[] =obj.getObject().toString().split(";");
                    //log.info("worker "+this.workerID+" = "+obj.getObject().toString());
                    Integer v1=Integer.parseInt(parts[0]);
                    Integer v2=Integer.parseInt(parts[1]);
                    this.p.deleteEdge(v1,v2);
                    // get concerned vertex
                    Set<Integer> affected =new HashSet<>();
                    // only directly affected
                    if(this.p.getIdvertices().contains(v1) && this.p.getIdvertices().contains(v2))
                    {
                        affected.addAll(this.p.getVertex(v1).getNeigbords());
                        affected.addAll(this.p.getVertex(v2).getNeigbords());

                    }
                    else if (this.p.getIdvertices().contains(v1) && !this.p.getIdvertices().contains(v2))
                    {
                        affected.addAll(this.p.getVertex(v1).getNeigbords());
                    }
                    else if (!this.p.getIdvertices().contains(v1) && this.p.getIdvertices().contains(v2))
                    {
                        affected.addAll(this.p.getVertex(v2).getNeigbords());
                    }
                    //all affected vertices (directly and inderectly
                  /*  if(this.p.getVertices().stream().filter(v->(v.getId()==v1) && v.getId()==v2 && v.getNeigbords().size()>0).count()>0 )
                    {
                        affected.addAll(this.p.getVertex(v1).getNeigbords());
                        affected.addAll(this.p.getVertex(v2).getNeigbords());
                    }
                    else if(this.p.getVertices().stream().filter(v->(v.getId()==v1) && v.getNeigbords().size()>0).count()>0 )
                    {
                        affected.addAll(this.p.getVertex(v1).getNeigbords());
                    }
                   else  if(this.p.getVertices().stream().filter(v->(v.getId()==v2) && v.getNeigbords().size()>0).count()>0 )
                    {
                        affected.addAll(this.p.getVertex(v2).getNeigbords());
                    }*/
                    this.global_affected.addAll(affected);


                    Set<Integer> TBR=new HashSet<>();
                    for(Integer vx: affected){
                        if(this.c.getCores_vertices().contains(vx) || this.c.getBorder_vertices().contains(vx))
                        {
                            Vertex node=this.c.getVertex(vx);

                            for(Integer nei:node.getNeigbords()){
                                if(this.c.getBridge_vertices().contains(nei)){
                                    TBR.add(nei);
                                }
                            }
                        }
                    }

                       affected.addAll(TBR);

                    // return all results to master
                  //  log.info("send from "+this.workerID+" "+affected);
                   /// Membres.get(0).tell(new WorkerToMasterMsg(this.workerID, "RSTDELETEEDGE", affected), getSelf());
                    this.sendToAllWorkers("CHECKNODES",affected);
                    //this.p.saveasTextFile();


                }   catch (Exception e) {
                    log.error("Exception from DELETEEDGE method in worker "+this.workerID+" = " + e.getMessage());
                }

            }

            else if (obj.getOperationInfo().toUpperCase().equals("CLUSTERSUPDATE")) {
                try {
                    // check the new clustering
                    //remove  removed vertices
                    Set<Vertex> affected=new HashSet<>();
                    Set<Integer> affected_id=new HashSet<>();
                    for( Integer v : this.global_affected)
                    {
                        if(this.p.getIdvertices().contains(v))
                        {
                            affected.add(p.getVertex(v));
                            affected_id.add(v);
                        }
                    }

                     // we apply our senarios for each affected vertex
                    // then for each vertex we get its  old status and new status (core,border, bridge or outlier)
                    // accroding to each case we apply the updates
                    // 1 Step : check changes of each affected vertex
                        // sub step  get all affected edge in order to minimise the checking step ine ach step
                    Set<Edge> affected_edges=this.c.getEdges().stream().filter(e->(affected_id.contains(e.getFromNode())|| affected_id.contains(e.getEndNode()))).collect(Collectors.toSet());

                  //  System.out.println("Print all affected edge in worker "+this.workerID);
                  //  affected_edges.forEach(e->e.printEdge());
                     HashMap<Integer,String> changes=new HashMap<Integer,String>();
                    //checking core vertices
                  //   for(Integer v : global_affected){
                //    System.out.println("worker "+this.workerID+"  "+affected_id);
               //     System.out.println("The global affected vertices are "+this.global_affected);

                    if(affected_edges.size()>0) {

                        // apply the checking status
                         c.checkStrongConnections(affected_edges);
                         c.checkCores(affected_edges);
                         c.checkBorders(affected_edges);
                         c.clusteringMainenance(affected_id);
                         // remove affected bridges
                        Set<Integer> bridgesTBR=new HashSet<>();
                        for(Integer br:affected_id){
                            if(this.c.getBridge_vertices().contains(br)){
                                bridgesTBR.add(br);
                            }
                        }
                        this.c.getBridges_vertices().removeAll(bridgesTBR);
                        this.c.getOutiler_vertices().addAll(bridgesTBR);

                    }
                  // list of changed and internal core
                 Set<Integer> listCores = new HashSet<Integer>();
                    for (Integer v : affected_id) {
                        if (this.c.getIdCores().contains(v)) {
                            listCores.add(v);
                        }
                    }
                    // Msg variable contains the affected vertices and the list of cores
                    // affected vertices in order to check the changed clusters in the master machine
                    for (int i = 1; i < this.Membres.size(); i++) {
                    this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "DCORESV", listCores), getSelf());
                      }

                } catch (Exception e) {
                    log.error("Exception from add new edge method in worker " + this.workerID + " = " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("CHECKBRIDGES")) {
                try {
                  //    Set<Integer>affected=global_affected;
                 //  Set<Integer> bridges=this.c.getAffectedBridge(affected);

                    //this.global_affected.addAll(bridges);
                    log.info("we check the bridges"+this.workerID+" all "+global_affected+" /");

                } catch (Exception e) {
                    log.error("Exception from checkBridges " + this.workerID + " = " + e.getMessage());
                }
            }



            else {
                log.error("we do not  have a method for this message  :" + obj.getOperationInfo());
            }
            // if worker get addnode message from master he saves this node in local graph

        }
        /*
         ***********************************************************************************************************************************************************
         ***********************************************************************************************************************************************************
         ***********************************************************************************************************************************************************
         */
        // if a worker recive messages from its neighbors to store  some links
//********************************************************Worker2Worker****************************************************************
        else if (message instanceof WorkerToWorkerMsg) {
            WorkerToWorkerMsg obj = (WorkerToWorkerMsg) message;
            if (obj.getOperationInfo().toUpperCase().equals("EXTERNALV")) {
                try {
                    // log.info("i m worker ID " + workerID + " i recive msg from " + ((WorkerToWorkerMsg) message).getSenderID() + " vertices " + obj.getObject());
                    Set<Integer> vertices = new HashSet<Integer>();
                    vertices = (Set<Integer>) obj.getObject();
                    for (Integer i : vertices) {
                        if (p.getIdvertices().contains(i)) {
                          //  log.info("erreur" + obj.getSenderID());

                            // log.info("Vertex " + i + " from worker  " + obj.getSenderID() + " is membre of the partion on worker num " + workerID);
                            getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "ADDNODE", p.getVertex(i)), getSelf());
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception from externalV method in worker " + e.getMessage());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("EXTERNALV2")) {
                //   log.info("i m worker ID " + this.workerID + " i recive msg from " + ((WorkerToWorkerMsg) message).getSenderID() + " vertices " + obj.getObject());
                Set<Integer> vertices = new HashSet<Integer>();

                vertices = (Set<Integer>) obj.getObject();
                try {
                    List<Vertex> nodes = new Vector<Vertex>();
                    String ch = "";

                    for (Integer i : vertices) {
                        if (p.getIdvertices().contains(i)) {
                            nodes.add(p.getVertex(i));
                        }
                    }
                    getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "ADDNODE2", nodes), getSelf());
                } catch (Exception e) {
                    log.error("Exception from externalV2 method in worker " + e.getMessage());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("ADDNODE2")) {
                List<Vertex> nodes = (List<Vertex>) obj.getObject();
                try {
                    for (Vertex v : nodes) {

                        p.addVertexToPartition(v);
                        p.setExt_vertex(v);
                    }
                    //  log.info("node " + vs.getId() + " with " + vs.getNeigbords() + " from " + obj.getSenderID() + " to be added to " + workerID);
                    //notifies  master when worker finiched its work
                    // log.info(":we will add the next node from "+obj.getSenderID()+" to "+workerID);
                    this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTEXTERNALV", null), getSelf());
                    // log.info("I m a "+workerID+" and i  recive the nodes from "+obj.getSenderID());
                } catch (Exception e) {
                    log.error("Exception from addnode2 method in worker " + e.getMessage());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("ADDNODE")) {
                Vertex vs = (Vertex) obj.getObject();
                p.addVertexToPartition(vs);
                p.setExt_vertex(vs);
                //  log.info("node " + vs.getId() + " with " + vs.getNeigbords() + " from " + obj.getSenderID() + " to be added to " + workerID);
                //notifies  master when worker finiched its work
                this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTEXTERNALV", null), getSelf());

            } else if (obj.getOperationInfo().toUpperCase().equals("CORESV")) {
                // list of internal  core from sender worker
                Set<Integer> cores = (Set<Integer>) obj.getObject();
                // set of core shered
                Set<Integer> shared_cores = new HashSet<Integer>();
                try {
                     //  log.info("recieved core "+cores +"from "+obj.getSenderID()+" and current core are "+c.getIdCores()+" in "+this.workerID);
                    for (Integer core : cores) {
                        if (p.getExternFrontiersVetices().contains(core) && c.getIdCores().contains(core)) {
                       // if (p.getExternFrontiersVetices().contains(core) && c.getIdCores().contains(core)) {
                            shared_cores.add(core);
                           //log.info("we merge tow cluster accordin the core "+core);
                        }
                    }
                    Map<Integer, Set<Integer>> shared_cluster = new HashMap<Integer, Set<Integer>>();
                    // if we have almost one core shared with current and input partition
                    if (shared_cores.size() > 0) {
                        // parse all shared core

                        for (Integer core : shared_cores) {
                            // get a cluster which contains a shared core
                            for (Set<Integer> c : c.getClusters()) {
                                if (c.contains(core)) {
                                    shared_cluster.put(core, c);
                                   // this.c.deleteCluster(core);
                                }
                            }
                        }

                 //    log.info("recieved internal core "+cores +"from "+obj.getSenderID()+" and current core are "+c.getIdCores()+" in "+this.workerID+" shared core are "+shared_cores+" and shared cluster are "+shared_cluster);

                    }

                    // log.info("we well send to "+getSender());
                  getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "INTERSECTIONCORES", shared_cluster), getSelf());

                 //   log.info("liste of cores from " + obj.getSenderID() + " to   " + workerID + " " + cores + " / current cores " + c.getIdCores() + " intersection " + shared_cluster);
                    //notifies the master when the worker have been finished its work
                 //    this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "INTERSECTIONCORES", null), getSelf());
                } catch (Exception e) {
                    log.error("Exception from corev method in worker " + e.getMessage());
                }
            }
            // check the changed clusters
            else if (obj.getOperationInfo().toUpperCase().equals("DCORESV")) {

             //   List<Set<Integer>> msg= (List<Set<Integer>>) obj.getObject();
                Map<Integer, Set<Integer>> shared_cluster = new HashMap<Integer, Set<Integer>>();
                try {
                // list of internal and affected  core from sender worker
                Set<Integer> cores=(Set<Integer>)obj.getObject() ;
                Set<Integer> affected;
                // set of core shared
                Set<Integer> shared_cores = new HashSet<Integer>();
                    if(cores.size()>0) {
                        for (Integer v : cores) {
                                for (Set<Integer> c : this.c.getClusters()) {
                                 if(c.contains(v) && this.p.getExternFrontiersVetices().contains(v))
                                     {
                                      shared_cores.add(v) ;
                                      shared_cluster.put(v,c);
                                     }
                                }
                        }
                    }
                   getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "DINTERSECTIONCORES", shared_cluster), getSelf());
                } catch (Exception e) {
                    log.error("Exception from Dcoresv method in worker " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("INTERSECTIONCORES")) {

                try {
                    MsgCount++;
                  //  log.info("Intersection of cores vertex " + MsgCount + "/" + (this.Membres.size() - 2));
                    Map<Integer, Set<Integer>> v = (Map<Integer, Set<Integer>>) obj.getObject();
                   // log.info("I m a worker number " + workerID + " i just recive a" + v.keySet() + " from " + obj.getSenderID());
                   // log.info("I m a worker number " + workerID + " i just recive a" + v.keySet() + " from " + obj.getSenderID());
                    // if we have intersections with others partitions we must merges the clusters
                    if (!v.isEmpty()) {
                        //Set<Integer> cluster=(Set<Integer>)v.get(0);
                        //  Integer core=(Integer) v.get(1);
                        //log.info("Itersection core step "+workerID+" cluster shared core"+v.keySet());
                        // in this step we well merge the clusters that contain a shared core
                        //  c.mergeCluster(cluster,core);
                        for (Integer core : v.keySet()) {

                            c.mergeCluster(v.get(core), core);
                        }
                    } else {
                        // log.info("Itersection core step (null) "+workerID+" vector "+v);
                    }

                    if (MsgCount == this.Membres.size() - 1) {
                      // log.info("I m a worker number " + workerID + " i well send my cluster list to master");
                       this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "CLUSTERS", c.getClusters()), getSelf());
                       // this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "CLUSTERS", local_clusters), getSelf());
                        MsgCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception from intersection cores method in worker " + e.getMessage());
                }

            }

            else if (obj.getOperationInfo().toUpperCase().equals("DINTERSECTIONCORES")) {

                try {
                    MsgCount++;
                    Map<Integer, Set<Integer>> v =(Map<Integer, Set<Integer>>) obj.getObject();
                    // if we have intersections with others partitions we must merge the clusters
                    if (!v.isEmpty()) {
                        // in this step we well merge the clusters that contain a shared affected core
                        for (Integer core : v.keySet()) {
                           // c.mergeClusterDynamic(v.get(core), core);
                            // megre the clusters
                            for(Set<Integer> c : this.c.getClusters())
                            {
                                if(c.contains(core))
                                {
                                    c.addAll(v.get(core));
                                }
                            }
                        }
                    }
                    // prepare clusters updted according to the affected vertices
                   List<Set<Integer>> updated_clusters=new ArrayList<>();
                    for(Integer vx : this.global_affected){
                      for(Set<Integer> cl : this.c.getClusters())  {
                          if(cl.contains(vx) & !updated_clusters.contains(cl)){
                              updated_clusters.add(cl);
                              break;
                          }
                      }
                    }
                   // this.global_affected.clear();
                    if (MsgCount == this.Membres.size() - 1) {
                        List<Object> msg_TBS =new ArrayList<>();
                        msg_TBS.add(global_affected);
                        msg_TBS.add(this.c.getClusters());
                       // System.out.println("i will send to master "+msg_TBS+" from worker "+this.workerID);
                       this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "DCLUSTERS", msg_TBS), getSelf());
                        MsgCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception from Dintersection cores method in worker " + e.getMessage());
                }

            }



            else if (obj.getOperationInfo().toUpperCase().equals("OUTLIERSV")) {
                try {
                    MsgCount++;
                    Set<Integer> outliers = (Set<Integer>) obj.getObject();
                    for (Integer i : outliers) {
                        if (c.getBridge_vertices().contains(i)) {
                            local_bridges.add(i);
                        }
                    }
                    if (MsgCount == this.Membres.size() - 2) {
                       // log.info("I m a worker number " + workerID + " i well send my cluster list to master");
                        this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "CLUSTERS", c.getClusters()), getSelf());
                        MsgCount = 0;

                    }
                } catch (Exception e) {
                    log.error("Exception from Foutliers method in worker " + e.getMessage());
                }

            } else if (obj.getOperationInfo().toUpperCase().equals("VERTICES")) {
                // in this step we recive all vertices from all workers to define the external vertices
                // an external V that is a part in several partition
                // log.info("we are a worker num " + this.workerID + " we just recive list of  vertices " + obj.getObject() + " from worker num " + obj.getSenderID());
                MsgCount++;
                // Set<Integer> vertices_ex = new HashSet<Integer>();
                HashMap<Integer,Set<Integer>> neigh=new HashMap<>();

                //  if (this.workerID != obj.getSenderID())
                //  vertices_ex = (Set<Integer>) obj.getObject();
                neigh=(HashMap<Integer,Set<Integer>>)obj.getObject();
                //sp.ext_vertices.addAll(vertices_ex);


                try {

                    for(Integer v : neigh.keySet())
                    {
                        if(sp.getListVerteces().keySet().contains(v))
                        {  int comming_v=neigh.get(v).size();
                            int local_v=sp.getListVerteces().get(v).size();
                            if( comming_v > local_v)
                            {
                                sp.getExt_vertices().add(v);
                            }
                            else if(comming_v==local_v)
                            {
                                if(obj.getSenderID()>this.workerID)
                                {
                                    sp.getExt_vertices().add(v);
                                }
                            }
                        }
                    }
                  //  System.out.println(workerID+" all vertex "+sp.getListVerteces()+"  external v"+sp.getExt_vertices());
                    sp.removeExternalVertex();

                }
                catch (Exception e) {
                    log.error("E supp vertices" + e.getMessage());
                }

                if (MsgCount == this.Membres.size() - 2) {
                    //  log.info("I m a worker number "+ workerID+" i well send end  to master");
                //    System.out.println("partition "+workerID+" ="+sp.getListVerteces().keySet());
                    this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTSENT", " migration step"), getSelf());

                    MsgCount = 0;


                }


            } else if (obj.getOperationInfo().toUpperCase().equals("INTERNALV")) {
                HashMap<Integer, Vector<Integer>> list_ExT = (HashMap<Integer, Vector<Integer>>) obj.getObject();
                HashMap<Integer, Vector<Integer>> list_TBA = new HashMap<>();
                for (Integer v : this.sp.getVertices()) {
                    // determine for each vertex V his number of connections with the current partition
                    if (list_ExT.containsKey(v)) {
                        int nbl = sp.getConnection(v);

                        int nbd = list_ExT.get(v).get(0);
                        if (nbd < nbl) {
                            Vector<Integer> elm = new Vector<>();
                            elm.add(nbl);
                            elm.add(this.workerID);
                            list_ExT.put(v, elm);
                        }
                    }
                }
                if (obj.getSenderID() != this.workerID)
                    for (int i = 1; i < this.Membres.size(); i++) {
                        this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, workerID, "RSTINTERNALV", list_ExT), getSelf());
                    }


            } else if (obj.getOperationInfo().toUpperCase().equals("RSTINTERNALV")) {
                MsgCount++;
                HashMap<Integer, Vector<Integer>> list = new HashMap<>();
                list = (HashMap<Integer, Vector<Integer>>) obj.getObject();
                for (Integer i : list.keySet()) {
                    if (!global_External_V.containsKey(i)) {

                        this.global_External_V.put(i, list.get(i));
                    } else {
                        int nbc = this.global_External_V.get(i).get(0);
                        if (list.get(i).get(0) > nbc) {
                            this.global_External_V.put(i, list.get(i));
                        }
                    }
                }
                //  log.info("Combiner in worker "+this.workerID+ " list  "+obj.getObject()+" "+this.sp.ext_vertices);
                if (MsgCount == this.Membres.size() - 1) {
                 //   log.info("all globla external v are " + this.global_External_V + " " + this.sp.getExt_vertices() + " worker " + this.workerID);
                    Vector<Integer> liste = new Vector<Integer>();
                    liste.addAll(this.sp.getExt_vertices());
                    for (Integer i : this.sp.getExt_vertices()) {
                        int nb = this.sp.getConnection(i);

                        if (this.global_External_V.containsKey(i)) {

                            if (this.global_External_V.get(i).get(0).equals(nb) && this.global_External_V.get(i).get(1).equals(this.workerID)) {
                                this.sp.getInt_vertices().add(i);
                                //  System.out.println("local Conection "+nb+ " remote "+global_External_V.get(i));

                            }
                        }
                    }
                    // notife a master that we have finished a shared of internal vertices
                    this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "END", "Internal Vertice"), getSelf());
                    MsgCount = 0;
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("LOCALBRIDGES")) {
                // in this step, all workers sent their local bridges, and each worker receive an bridge
                // will check it
                Set<Integer> externalBridges = new HashSet<Integer>();
                externalBridges = (Set<Integer>) obj.getObject();
                // bridge must be deleted " which have border or core status"
                Set<Integer> bridgesTBD = new HashSet<Integer>();
                try {
                    for (Integer br : externalBridges) {
                        // if an external bridge is a core or border vertex in the partition we must delete it
                        if (c.getIdCores().contains(br) || c.getBorder_vertices().contains(br)) {
                            bridgesTBD.add(br);
                        }
                    }
                } catch (Exception e) {

                }
                // return the  bridge must be deleted after checking
                getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "DELETEBRIDGES", bridgesTBD), getSelf());

            }
            else if (obj.getOperationInfo().toUpperCase().equals("DLOCALBRIDGES")) {
                Set<Integer> externalBridges = new HashSet<Integer>();
                externalBridges = (Set<Integer>) obj.getObject();
                // bridge must be deleted " which have border or core status"
                Set<Integer> bridgesTBD = new HashSet<Integer>();
                try {
                    for (Integer br : externalBridges) {
                        // if an external bridge is a core or border vertex in the partition we must delete it
                        if (c.getIdCores().contains(br) || c.getBorder_vertices().contains(br)) {
                            bridgesTBD.add(br);
                        }
                    }
                } catch (Exception e) {

                }

                getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "DYDELETEBRIDGES", bridgesTBD), getSelf());

            }
            else if (obj.getOperationInfo().toUpperCase().equals("DELETEBRIDGES")) {
                try {
                    MsgCount++;

                    Set<Integer> bridgetbd = (Set<Integer>) obj.getObject();

                    if (!bridgetbd.isEmpty()) {
                       // c.getBridge_vertices().removeAll(bridgetbd);
                        c.getReal_bridge_vertices().removeAll(bridgetbd);
                    }
                    // -2 : 1 for master  1 the same worker
                    if (MsgCount == this.Membres.size() - 2) {
                    //    log.info("I m a worker number " + workerID + " i well send my bridges list to master");
                        this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTBRIDGES", c.getReal_bridge_vertices()), getSelf());
                      //  this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTBRIDGES", bridgetbd), getSelf());
                        MsgCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception from bridges method in worker " + e.getMessage());
                }

            }
            else if (obj.getOperationInfo().toUpperCase().equals("DYDELETEBRIDGES")) {
                try {
                    MsgCount++;

                    Set<Integer> bridgetbd = (Set<Integer>) obj.getObject();

                    if (!bridgetbd.isEmpty()) {
                        c.getBridge_vertices().removeAll(bridgetbd);
                        c.getReal_bridge_vertices().removeAll(bridgetbd);
                    }
                    // -2 : 1 for master  1 the same worker
                    if (MsgCount == this.Membres.size() - 2) {
                        //    log.info("I m a worker number " + workerID + " i well send my bridges list to master");
                        this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTBRIDGES2", c.getBridge_vertices()), getSelf());
                        //  this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTBRIDGES", bridgetbd), getSelf());
                        MsgCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception from bridges method in worker " + e.getMessage());
                }

            }

            else if (obj.getOperationInfo().toUpperCase().equals("LOCALOUTLIERS")) {
                // in this step we receive the outliers from all workers, when we have any vertices in this list
                // and it is a core, border or bridges vertices we sent it to be deleted from his partition
                Set<Integer> externalOutliers = new HashSet<Integer>();
                externalOutliers = (Set<Integer>) obj.getObject();

                // outliers must be deleted " which have border, bridges or core status"
                Set<Integer> outliersTBD = new HashSet<Integer>();
                try {
                    for (Integer br : externalOutliers) {
                        if (c.getIdCores().contains(br) || c.getBorder_vertices().contains(br) || c.getReal_bridge_vertices().contains(br)) {
                            outliersTBD.add(br);
                        }
                    }
                } catch (Exception e) {

                }
                if(!(this.workerID==obj.getSenderID())) {
                   // log.info("we are a worker " + workerID + " a receive " + externalOutliers + " from " + obj.getSenderID() + " and my our outlier are" + this.c.getOutiler_vertices());
                   // log.info("we are a worker " + workerID + " i send a list of outliers must be deleted " + outliersTBD);
                     getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "DELETEOUTLIERS", outliersTBD), getSelf());

                }

            }
            else if (obj.getOperationInfo().toUpperCase().equals("DLOCALOUTLIERS")) {
                // in this step we receive the outliers from all workers, when we have any vertex in this list
                // and it is a core, border or bridge vertex we sent it to be deleted from his partition
                Set<Integer> externalOutliers = new HashSet<Integer>();
                externalOutliers = (Set<Integer>) obj.getObject();

                // outliers must be deleted " which have border, bridges or core status"
                Set<Integer> outliersTBD = new HashSet<Integer>();
                try {
                    for (Integer br : externalOutliers) {
                        if (c.getIdCores().contains(br) || c.getBorder_vertices().contains(br) || c.getReal_bridge_vertices().contains(br)) {
                            outliersTBD.add(br);
                        }
                    }
                } catch (Exception e) {

                }
                if(!(this.workerID==obj.getSenderID())) {
                    // log.info("we are a worker " + workerID + " a receive " + externalOutliers + " from " + obj.getSenderID() + " and my our outlier are" + this.c.getOutiler_vertices());
                    // log.info("we are a worker " + workerID + " i send a list of outliers must be deleted " + outliersTBD);
                    getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "DYNDELETEOUTLIERS", outliersTBD), getSelf());

                }

            }

            else if (obj.getOperationInfo().toUpperCase().equals("DELETEOUTLIERS")) {
             //   log.info(workerID + " : we receive a list of outliers to be deleted from " + obj.getSenderID());
                try {
                    MsgCount++;
                    Set<Integer> outlierstbd = (Set<Integer>) obj.getObject();
                    if (!outlierstbd.isEmpty()) {
                      //  c.getOutiler_vertices().removeAll(outlierstbd);
                       c.getReal_outiler_vertices().removeAll(outlierstbd);
                    }
                    List<Set<Integer>> clusters = c.getClusters();
                    Set<Integer> localOutlires = new HashSet<Integer>();
                    HashMap<Integer, Set<Integer>> Loutliers = new HashMap<>();
                if(clusters!=null){
                    Set<Integer> localout=c.getOutiler_vertices();
                  for (Set<Integer> cluster : clusters) {
                        for (Integer elm : cluster) {
                            Vertex v = p.getVertex(elm);
                            if(v!=null) {
                            for (Integer o : localout) {

                                    if (v.getNeigbords().contains(o)) {
                                        localOutlires.add(o);
                                        Loutliers.put(o, cluster);

                                    }
                                }
                            }
                        }
                    }}
                 //   log.info("all outliers in workers " + workerID + " =>" + c.getOutiler_vertices() + " " + localOutlires);


                    if (MsgCount == this.Membres.size() - 2) {
                        //   log.info("I m a worker number "+workerID+" i well send my outliers list to master"+Loutliers);
                        // in this step we sent final local outliers to all workers to verifie if
                        // we have an outliers is connected with an other cluster,
                        for (int i = 1; i < this.Membres.size(); i++) {
                               this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "FOULIERS", Loutliers), getSelf());
                        }
                        MsgCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception from outliers method in worker " + e.getMessage() +" couse  "+e.getLocalizedMessage());
                }

            }
            else if (obj.getOperationInfo().toUpperCase().equals("DYNDELETEOUTLIERS")) {
                //   log.info(workerID + " : we receive a list of outliers to be deleted from " + obj.getSenderID());
                try {
                    MsgCount++;
                    Set<Integer> outlierstbd = (Set<Integer>) obj.getObject();
                    if (!outlierstbd.isEmpty()) {
                        //  c.getOutiler_vertices().removeAll(outlierstbd);
                        c.getReal_outiler_vertices().removeAll(outlierstbd);
                    }
                    List<Set<Integer>> clusters = c.getClusters();
                    Set<Integer> localOutlires = new HashSet<Integer>();
                    HashMap<Integer, Set<Integer>> Loutliers = new HashMap<>();
                    if(clusters!=null){
                        Set<Integer> localout=c.getOutiler_vertices();
                        for (Set<Integer> cluster : clusters) {
                            for (Integer elm : cluster) {
                                Vertex v = p.getVertex(elm);
                                if(v!=null) {
                                    for (Integer o : localout) {
                                        if (v.getNeigbords().contains(o)) {
                                            localOutlires.add(o);
                                            Loutliers.put(o, cluster);
                                        }
                                    }
                                }
                            }
                        }}
                    if (MsgCount == this.Membres.size() - 2) {
                        //   log.info("I m a worker number "+workerID+" i well send my outliers list to master"+Loutliers);
                        // in this step we sent final local outliers to all workers to verifie if
                        // we have an outliers is connected with an other cluster,
                        for (int i = 1; i < this.Membres.size(); i++) {
                            this.Membres.get(i).tell(new WorkerToWorkerMsg(workerID, i, "DFOULIERS", Loutliers), getSelf());
                        }
                        MsgCount = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception from outliers method in worker " + e.getMessage() +" couse  "+e.getLocalizedMessage());
                }

            }


            else if (obj.getOperationInfo().toUpperCase().equals("FOULIERS")) {

                try {
                    //Set<Integer> foutliers=(Set<Integer>)obj.getObject();
                    // list of outlier and thiers clusters
                   if(!(this.workerID==obj.getSenderID()) ){
                        HashMap<Integer, Set<Integer>> Loutliers = (HashMap<Integer, Set<Integer>>) obj.getObject();
                        Set<Integer> bridges = new HashSet<Integer>();

                   //   log.info("i m a worker " + this.workerID + " a recive a fouliter from " + obj.getSenderID() + " a next list of final outlier" + Loutliers);
                       if(Loutliers!=null) {
                           for (Integer out : Loutliers.keySet()) {
                               if (Loutliers.get(out).size() > 0){
                                if (c.isBridge(out, Loutliers.get(out))) {
                                bridges.add(out);
                               }
                           }
                           }
                       }
                      //  log.info(Loutliers+"woekr "+this.workerID+" "+bridges+"/"+this.c.getOutiler_vertices());
                        // MsgCount=0;
                         getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "NEWBRIDGES", bridges), getSelf());

                    }
                  //  MsgCount=0;
                   //   log.info("i m a worker " + this.workerID + " a recive a fouliter from " + obj.getSenderID() + " a next list of final outlier" + Loutliers);
                   //   log.info("i m a worker " + this.workerID + " a recive a fouliter from " + obj.getSenderID() + " a next list of final outlier" + Loutliers);

                    // log.info(Loutliers+":"+bridges+" local outliers"+this.c.getOutiler_vertices());
                   // getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "NEWBRIDGES", bridges), getSelf());

                } catch (Exception e) {
                    log.error("Exception from F outliers method in worker " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("DFOULIERS")) {

                try {
                    //Set<Integer> foutliers=(Set<Integer>)obj.getObject();
                    // list of outlier and thiers clusters
                    if(!(this.workerID==obj.getSenderID()) ){
                        HashMap<Integer, Set<Integer>> Loutliers = (HashMap<Integer, Set<Integer>>) obj.getObject();
                        Set<Integer> bridges = new HashSet<Integer>();
                        //log.info("i m a worker " + this.workerID + " a recive a fouliter from " + obj.getSenderID() + " a next list of final outlier" + Loutliers);
                        if(Loutliers!=null) {
                            for (Integer out : Loutliers.keySet()) {
                                if (Loutliers.get(out).size() > 0){
                                    if (c.isBridge(out, Loutliers.get(out))) {
                                        bridges.add(out);
                                    }
                                }
                            }
                        }
                        //log.info(Loutliers+"woekr "+this.workerID+" "+bridges+"/"+this.c.getOutiler_vertices());
                        // MsgCount=0;
                        getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "DNEWBRIDGES", bridges), getSelf());

                    }
                    //  MsgCount=0;
                    //   log.info("i m a worker " + this.workerID + " a recive a fouliter from " + obj.getSenderID() + " a next list of final outlier" + Loutliers);
                    //   log.info("i m a worker " + this.workerID + " a recive a fouliter from " + obj.getSenderID() + " a next list of final outlier" + Loutliers);

                    // log.info(Loutliers+":"+bridges+" local outliers"+this.c.getOutiler_vertices());
                    // getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "NEWBRIDGES", bridges), getSelf());

                } catch (Exception e) {
                    log.error("Exception from F outliers method in worker " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("NEWBRIDGES")) {
                try {
                    MsgCount++;
                    //log.info(this.workerID+"we receive "+obj.getObject()+" from "+obj.getSenderID()+" nbmsg "+MsgCount);
                    Set<Integer> newbridges = (Set<Integer>) obj.getObject();
                    Set<Integer> allbridges = new HashSet<Integer>();
                    //  Set<Integer> alloutliers=new HashSet<Integer>();
                   // allbridges.addAll(newbridges);
                    this.c.removeOutliers(newbridges);
                    this.c.getBridge_vertices().addAll(newbridges);
                    this.c.getReal_bridge_vertices().addAll(newbridges);
                  //  allbridges.addAll(c.getBridge_vertices());

                   // log.info("New bridges must be added to global list : from  " + this.workerID + " is " + newbridges+" nb msg"+MsgCount);
                    if(MsgCount==Membres.size()-2) {
                       // log.info("final bridges must be added are " + this.workerID + " is " + this.c.getBridge_vertices());
                        this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "ALLBRIDGES", this.c.getReal_bridge_vertices()), getSelf());
                        // this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTOUTLIERS", c.getOutiler_vertices()), getSelf());
                        MsgCount=0;
                    }

                } catch (Exception e) {
                    log.error("Exception from F outliers method in worker " + e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("DNEWBRIDGES")) {
                try {
                    MsgCount++;
                    //log.info(this.workerID+"we receive "+obj.getObject()+" from "+obj.getSenderID()+" nbmsg "+MsgCount);
                    Set<Integer> newbridges = (Set<Integer>) obj.getObject();
                    Set<Integer> allbridges = new HashSet<Integer>();
                    //  Set<Integer> alloutliers=new HashSet<Integer>();
                    // allbridges.addAll(newbridges);
                    this.c.removeOutliers(newbridges);
                    this.c.getBridge_vertices().addAll(newbridges);
                    this.c.getReal_bridge_vertices().addAll(newbridges);
                    //  allbridges.addAll(c.getBridge_vertices());

                    // log.info("New bridges must be added to global list : from  " + this.workerID + " is " + newbridges+" nb msg"+MsgCount);
                    if(MsgCount==Membres.size()-2) {
                        // log.info("final bridges must be added are " + this.workerID + " is " + this.c.getBridge_vertices());
                        this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "DALLBRIDGES", this.c.getBridge_vertices()), getSelf());
                        // this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTOUTLIERS", c.getOutiler_vertices()), getSelf());
                        MsgCount=0;
                    }

                } catch (Exception e) {
                    log.error("Exception from F outliers method in worker " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("SHAREEDGES")) {
                try {
                    MsgCount++;
                    Set<String> edges = new HashSet<>();

                    edges = (Set<String>) obj.getObject();
                    this.sp.addedges(edges);
                    //  this.sp.edges.addAll(edges);
                    if (MsgCount == this.Membres.size() - 1) {
                        //log.info("New bridges must be added to global list : from  "+workerID+" is "+allbridges);
                        this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "END", "shared edges "), getSelf());
                        //this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTOUTLIERS", alloutliers), getSelf());
                    }
                } catch (Exception e) {
                    log.error("Exception from F outliers method in worker " + e.getMessage());
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("ADDEDGE")) {
                try {
                    MsgCount++;

                    if (MsgCount == this.Membres.size() - 1) {
                      //  log.info("add edges  " + workerID + " is " + obj.getObject());
                        // this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "END", "shared edges "), getSelf());
                        //this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTOUTLIERS", alloutliers), getSelf());
                    }
                } catch (Exception e) {
                    log.error("Exception from add edge method in worker " + e.getMessage());
                }
            }
            // maintain
            else if (obj.getOperationInfo().toUpperCase().equals("SETUPNODES1")) {
                try {
                    // get vertex which be connected with a new vertex ( NodeToupdated )
                    Vertex n = (Vertex) obj.getObject();
                    if (n != null) {
                       // get vertex n2 from the current partition which be connected with a new vertex
                        Vertex n2 = p.getVertex(n.getId());
                        // if we have find the vertex in current partition
                        if (n2 != null) {

                            if (n2.getNeigbords().size() > 0) {
                              // we compute all distance can be updated with the new update
                               this.p.getEdges().stream().filter(e->((n2.getNeigbords().contains(e.getFromNode()) && n2.getId()==e.getEndNode()) || (n2.getNeigbords().contains(e.getEndNode()) && n2.getId()==e.getFromNode()))).forEach(e->e.similarityCalculation());
                              //  Set<Edge> edgesTBUpdated=  this.p.getEdges().stream().filter(e->(e.getFromNode()==n2.getId()|| e.getEndNode()==n2.getId())).collect(Collectors.toSet());
                              //  System.out.println("EDges TBUpdated are "+edgesTBUpdated);
                               // edgesTBUpdated.stream().forEach(e->e.printEdge());
                                // we fix a list of vertices that they can change their status (border, core, outlier or bridge )
                                Set<Integer> affected_vertice=new HashSet<>();
                               // System.out.println(this.p.getFrantiersVertices()+" worker "+this.p.getIdp());

                                 // this boucle to get all affected vertices (directly and indirectly
                              //  for(Integer i : n.getNeigbords())
                             //   {
                                  //  affected_vertice.addAll(p.getVertex(i).getNeigbords());
                                   // System.out.println(this.workerID+" "+ i+" " +p.getVertex(i).getNeigbords());
                               // }
                                // in this case we take only the directly affected vertices
                                affected_vertice.addAll(n.getNeigbords());
                                this.global_affected.addAll(affected_vertice);

                                Set<Integer> TBR=new HashSet<>();
                                for(Integer vx: affected_vertice){
                                    if(this.c.getCores_vertices().contains(vx) || this.c.getBorder_vertices().contains(vx))
                                    {
                                        Vertex node=this.c.getVertex(vx);

                                        for(Integer nei:node.getNeigbords()){
                                            if(this.c.getBridge_vertices().contains(nei)){
                                                TBR.add(nei);
                                            }
                                        }
                                    }
                                }

                                 affected_vertice.addAll(TBR);

                               // Membres.get(0).tell(new WorkerToMasterMsg(this.workerID, "RSTADDVERTEX", affected_vertice), getSelf());
                               // this.p.saveasTextFile();
                              //  log.info("I m worker number "+this.workerID+" and i have the next changed vertices "+affected_vertice);

                                // send all affected vertex to all workers to check their new  status
                               for (int i = 1; i < this.Membres.size(); i++) {

                                   this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "CHECKNODES", affected_vertice), getSelf());
                                }

                            }
                        }
                        else
                        {
                            // note if the master that we do not have a work to compute this just to have a number of message equals to number of worker
                         //   Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTADDVERTEX", new HashSet<Integer>()), getSelf());
                         //   log.info("I m worker number "+this.workerID+" and i don't have any chnaged vertiex");

                        }

                    }

                } catch (Exception e) {
                    log.error("Exception from update SETUPNODES1 method in worker "+ workerID+" " + e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("SETUPNODES2")) {

                try {

                    String edge=(String)obj.getObject();
                    String parts []=edge.split(";");
                    Integer new_Vertex=Integer.parseInt(parts[0]);
                    Integer old_Vertex=Integer.parseInt(parts[1]);
                    if(this.p.getVertices().stream().filter(v->v.getId()==old_Vertex).count()==1)
                    {
                    // get the old vertex must be update
                        Vertex n2 = p.getVertex(old_Vertex);
                        if (n2 != null && !this.p.getInternalVertex().contains(old_Vertex)) {

                            // if this vertex is a second vertex n this partition
                            if (n2.getNeigbords().size() > 0) {
                                //log.info(" we are in "+this.workerID+" "+n2.getId()+" its neig "+n2.getNeigbords());
                                // if we have an a N vertex on the partition we update it and add a enw vertex as an external vertex
                                this.p.getVertex(old_Vertex).addNeigbor(new_Vertex);
                               // n2.addNeigbor(new_Vertex);
                               // log.info( this.p.getVertex(old_Vertex).getId()+" we are in "+this.workerID+" "+n2.getId()+" its neig "+ this.p.getVertex(old_Vertex).getNeigbords());

                                // we add new vertex as an second vertex in this partition since it is connected with a external vertex
                                this.p.addSecondVertex(new_Vertex);

                                this.p.getEdges().stream().filter(e->(e.getFromNode()==old_Vertex || e.getEndNode()==old_Vertex)).forEach(e->e.addneighbors(old_Vertex,new_Vertex));


                                Set<Integer> affected_vertice=new HashSet<>();
                                for(Integer i : n2.getNeigbords())
                                {
                                     affected_vertice.addAll(p.getVertex(i).getNeigbords());
                                }
                                this.global_affected.addAll(affected_vertice);
                                // compute the affected bridges
                                // 1: check affected clusters
                                // 2: get affected bridges
                                // 3: add affected bridges to all affected vertices
                                Set<Integer> TBR=new HashSet<>();
                                for(Integer vx: affected_vertice){
                                    if(this.c.getCores_vertices().contains(vx) || this.c.getBorder_vertices().contains(vx))
                                    {
                                        Vertex node=this.c.getVertex(vx);

                                        for(Integer nei:node.getNeigbords()){
                                            if(this.c.getBridge_vertices().contains(nei)){
                                                TBR.add(nei);
                                            }
                                        }
                                    }
                                }
                               affected_vertice.addAll(TBR);

                               // this.p.saveasTextFile();
                              //  Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTADDVERTEX", affected_vertice), getSelf());
                               // log.info("I m worker number "+this.workerID+" and i have the next changed vertices "+affected_vertice);

                                 for (int i = 1; i < this.Membres.size(); i++) {

                                    this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "CHECKNODES", affected_vertice), getSelf());
                                }


                            }
                        }

                   }
                   else
                    {
                        // noteif the master that we do not have a work to compute this just to have a number of message equals to number of worker
                     //   Membres.get(0).tell(new WorkerToMasterMsg(this.workerID, "RSTADDVERTEX", new HashSet<Integer>()), getSelf());
                        log.info("I m worker number "+this.workerID+" and i don't have any changed vertex");
                    }


                } catch (Exception e) {
                    log.error("Exception from update vertex method in worker "+ workerID+" " + e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("CHECKNODES"))
            {

                MsgCount++;
                try{
                Set<Integer> affected_vertices=new HashSet<>();
                affected_vertices= (Set<Integer>) obj.getObject();
                this.global_affected.addAll(affected_vertices);
                // get the affected edge
                if(MsgCount==(this.Membres.size()-1)) {

                   // Set<Integer> lst=this.c.getAffectedBridge(this.global_affected);
                   // this.global_affected.addAll(lst);
                    System.out.println("the affected are "+this.global_affected+" in worker "+this.workerID);
                   Membres.get(0).tell(new WorkerToMasterMsg(this.workerID, "RSTUPDATE", null), getSelf());
                    MsgCount=0;
                }

            } catch (Exception e) {
                log.error("Exception from CHECKNODES method in worker " + e.getMessage());
            }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("ADDNEWEDGE"))
            {   MsgCount=0;
                try{
                   // get the internal vertex in this partition
                    Set<Integer> intern = this.p.getInternalVertex() ;
                     // when we receive an object
                    if(obj.getObject()!=null) {
                        // when we receive an edge
                        Edge e=(Edge)obj.getObject();
                        //we test if e.endNode is belongs on internal vertex of this partition
                        if (intern.contains(e.getEndNode())) {
                          //  log.info("worker "+this.workerID+" internal vertex "+intern+" edge from "+e.getfNode().getId()+" end node"+e.getEndNode());
                            // add the internal vertex (endNode in E) to new edge E
                            e.seteNode(this.p.getVertex(e.getEndNode()));
                            // add from node as a neighbor of endNode in partition
                            this.p.getVertex(e.getEndNode()).addNeigbor(e.getFromNode());
                            //  // add endNode node as a neighbor of fromNode
                            e.geteNode().addNeigbor(e.getFromNode());
                            // add from node as a neighbor of endNode in new edge
                            e.getfNode().addNeigbor(e.getEndNode());
                            // add a new vertex
                            this.p.StreamAddNewVertex(e.getfNode());
                            this.p.StremAddNewCutEdge(e);
                            this.p.getVertex(e.getFromNode()).addNeigbor(e.getEndNode());

                            for (int i = 1; i < this.Membres.size(); i++) {
                                    this.Membres.get(i).tell(new WorkerToWorkerMsg(this.workerID, i, "ADDNEWEDGE2", e), getSelf());
                            }
                        }
                    }
            }
             catch (Exception e) {
                    log.error("Exception from ADDNEWEDGE WORKER method in worker " + e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("ADDNEWEDGE2"))
            {
                try{
                    // get a list of internal vertices
                    Set<Integer> intern = this.p.getInternalVertex();
                    // get the new edge E from worker which have the second node of the new edge
                    Edge e=(Edge)obj.getObject();
                    // if the first node (from node) belongs on this partition
                    if(intern.contains(e.getFromNode()))
                    {
                     // if the end vertex in the current partition
                     if(this.p.getIdvertices().contains(e.getEndNode()))
                     {
                         // we add e.end vertex to from node neighbors
                         this.p.getVertex(e.getFromNode()).addNeigbor(e.getEndNode());
                        // we get  neighbors of end node from current partition ( existing node ) example 9 []
                         List<Integer> neighbors=  this.p.getVertex(e.getEndNode()).getNeigbords();
                        // log.info(" neig"+neighbors+"worker id "+this.workerID+" "+e.getEndNode());
                         // if the end vertex is an external vertex we add from vertex as an neighbor to it
                         if(neighbors.size()>0)
                            {
                                // add From node as an neighbor in endNode
                                 this.p.getVertex(e.getEndNode()).addNeigbor(e.getFromNode());
                            }
                            // else the end vertex is a second external vertex, we will add all neighbors of from vertex
                         // and if we have a neighbor do not among of this partition we create it as a second vertex
                            else
                               {
                                 //add From node as an neighbor in endNode
                                //this.p.getVertex(e.getEndNode()).addNeigbors(neighbors);
                                for(Integer i : e.geteNode().getNeigbords() )
                                {
                                    if(!this.p.getIdvertices().contains(i))
                                      {
                                        this.p.addSecondVertex(i);
                                      }
                                    neighbors.add(i);
                                }
                                this.p.getVertex(e.getEndNode()).addNeigbors(neighbors);
                               }
                         // add end node as an external vertex
                           this.p.addExtVertex(e.geteNode());
                     }
                     // if we do not have a s.end node in this partition
                     else
                     {

                     this.p.getVertex(e.getFromNode()).addNeigbor(e.getEndNode());
                         this.p.StreamAddNewVertex(e.geteNode());
                     }
                     // for both cases in partition. belong partition we add new cuts edge
                        this.p.StremAddNewCutEdge(e);
                        Set<Integer> idvertices=new HashSet<>();
                        idvertices.addAll(e.geteNode().getNeigbords());
                        idvertices.addAll(e.getfNode().getNeigbords());
                       this.p.getEdges().stream().filter(le->(idvertices.contains(le.getFromNode())|| idvertices.contains(le.getEndNode()))).forEach(le->le.similarityCalculation());
                    }
                    else
                    {
                        // re compute similarity

                        Set<Integer> idvertices=new HashSet<>();
                        idvertices.addAll(e.geteNode().getNeigbords());
                        idvertices.addAll(e.getfNode().getNeigbords());
                        this.p.getEdges().stream().filter(le->(idvertices.contains(le.getFromNode())|| idvertices.contains(le.getEndNode()))).forEach(le->le.similarityCalculation());

                      //  this.p.getEdges().stream().filter(le->(le.getEndNode()==e.getEndNode() || le.getEndNode()==e.getFromNode()) || le.getFromNode()==e.getEndNode()|| le.getFromNode()==e.getEndNode()).forEach(le->le.similarityCalculation());
                        if(this.p.getIdvertices().contains(e.getEndNode()) )
                        {
                            if(this.p.getVertex(e.getEndNode()).getNeigbords().size()>0)
                            {
                                this.p.getVertex(e.getEndNode()).addNeigbor(e.getFromNode());
                                if(!this.p.getIdvertices().contains(e.getFromNode()))
                                {
                                  this.p.addSecondVertex(e.getFromNode());
                                }
                            }

                        }
                        else if(this.p.getIdvertices().contains(e.getFromNode()) )
                        {
                            if(this.p.getVertex(e.getFromNode()).getNeigbords().size()>0)
                            {
                                this.p.getVertex(e.getFromNode()).addNeigbor(e.getEndNode());
                                if(!this.p.getIdvertices().contains(e.getEndNode()))
                                {
                                    this.p.addSecondVertex(e.getEndNode());
                                }
                            }
                        }
                    }

                    this.p.updateFrontierVertex();
                    Set<Integer> affected=new HashSet();
                    affected.addAll(e.getfNode().getNeigbords());
                    affected.addAll(e.geteNode().getNeigbords());
              //   log.info("affected with "+e.getFromNode()+" are "+e.getfNode().getNeigbords()+" and the affected with "+e.getEndNode()+" are "+e.geteNode().getNeigbords());
                    this.global_affected.addAll(global_affected);

                    Set<Integer> TBR=new HashSet<>();
                    for(Integer vx: affected){
                        if(this.c.getCores_vertices().contains(vx) || this.c.getBorder_vertices().contains(vx))
                        {
                            Vertex node=this.c.getVertex(vx);

                            for(Integer nei:node.getNeigbords()){
                                if(this.c.getBridge_vertices().contains(nei)){
                                    TBR.add(nei);
                                }
                            }
                        }
                    }
                    affected.addAll(TBR);

                  //  Membres.get(0).tell(new WorkerToMasterMsg(this.workerID, "RSTADDEDGE", affected), getSelf());
                    this.sendToAllWorkers("CHECKNODES",affected);

                }
                catch (Exception e) {
                    log.error("Exception from ADDNEWEDGE2 WORKER method in worker " + e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("SHAREVERTEX")) {
                try {

                    HashMap<Integer,Set<Integer>> local_vertex =(HashMap<Integer,Set<Integer>>)obj.getObject();
                    Set<Integer> tbr=new HashSet<>();
                   // System.out.println("Local vertex"+part.getVertices()+" remote vertex"+local_vertex);
                    for(Integer v : part.getVertices().keySet()) {
                        if (local_vertex.containsKey(v)){
                            if (part.getVertices().get(v).size() < local_vertex.get(v).size()) {
                                 part.getVertices().get(v).addAll(local_vertex.get(v));
                                tbr.add(v);
                            } else if (part.getVertices().get(v).size() == local_vertex.get(v).size()) {
                                if (this.workerID > obj.getSenderID()) {
                                     part.getVertices().get(v).addAll(local_vertex.get(v));
                                    tbr.add(v);
                                }
                            }
                    }
                    }
                    getSender().tell(new WorkerToWorkerMsg(workerID, obj.getSenderID(), "REMOVENODE", tbr), getSelf());

                    //  for()
                    if (MsgCount == this.Membres.size() - 2) {
                        //  log.info("I m a worker number "+ workerID+" i well send end  to master");
                        //  System.out.println("partition "+workerID+" ="+sp.getListVerteces().keySet());
                        //  this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTSENT", " migration step"), getSelf());

                        MsgCount = 0;


                    }
                } catch (Exception e) {
                    log.error("Exception from print partition  " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("REMOVENODE")) {
                try {
                    MsgCount++;
                      Set<Integer> tbr=(Set<Integer> )obj.getObject();
                      this.part.removeVertex(tbr);
                      this.part.saveAsText("adj"+this.workerID);
                    if (MsgCount == this.Membres.size() - 2) {
                        // log.info("Worker "+this.workerID+" "+this.part.getVertices());
                      //  this.Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTSENT", " migration step"), getSelf());

                        MsgCount = 0;


                    }
                } catch (Exception e) {
                    log.error("Exception from REMOVENODE  " + e.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("SETBORDERVERTEX"))
            {    MsgCount++;
                try{
                    // receive list of border vertex to update them, if it is belongs the current partition
                    List<Vertex> border=(List<Vertex>)obj.getObject();
                  //  log.info("=================================================="+border+" worker "+this.workerID);
                    for(Vertex v : border)
                    {
                        if(this.p.getIdvertices().contains(v.getId()))
                        {
                            Vertex n=this.p.getVertex(v.getId());
                            if(n.getNeigbords().size()>0)
                            {  // v  : new vertex
                                // n : old vertex
                                // we parse all neighbors of "v", if a vertex is  not belong on the list of neighbors of "n" we add it
                               Set<Integer> shared_neighbors=new HashSet<>();
                               shared_neighbors.addAll(v.getNeigbords());
                               shared_neighbors.removeAll(n.getNeigbords());
                               for(Integer i : shared_neighbors)
                               {
                                   this.p.getVertex(n.getId()).addNeigbor(i);
                                   if(!this.p.getIdvertices().contains(i)) {
                                       this.p.addSecondVertex(i);
                                   }
                               }
                             this.p.getEdges().stream().filter(e->(e.getFromNode()==v.getId()|| e.getEndNode()==v.getId())).forEach(e->e.similarityCalculation());


                            }
                        }
                    }
                    //
                  //  if(MsgCount==nbworker) {

                       Set<Integer> affected=new HashSet();
                       for(Vertex v : border)
                       {
                          affected.addAll(v.getNeigbords());

                       }

                      this.global_affected.addAll(affected);

                    Set<Integer> TBR=new HashSet<>();
                    for(Integer vx: affected){
                        if(this.c.getCores_vertices().contains(vx) || this.c.getBorder_vertices().contains(vx))
                        {
                            Vertex node=this.c.getVertex(vx);

                            for(Integer nei:node.getNeigbords()){
                                if(this.c.getBridge_vertices().contains(nei)){
                                    TBR.add(nei);
                                }
                            }
                        }
                    }

                          affected.addAll(TBR);
                           //  Membres.get(0).tell(new WorkerToMasterMsg(workerID, "RSTADDEDGE", affected), getSelf());
                           this.sendToAllWorkers("CHECKNODES", affected);
                           MsgCount=0;

                    // }

                    //



                }
                catch (Exception e) {
                    log.error("Exception from SETBORDERVERTEX WORKER method in worker " + e.getMessage());
                }
            }



        } else if (message instanceof String) {
         //   log.error("String " + (String) message);
        } else {

            //  log.error("we d'ont have a methode for this message  "+message.getClass().getName());

            unhandled(message);
        }

    }
}
