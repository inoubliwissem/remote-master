package BLADYG;

import java.io.*;

import java.time.format.DateTimeFormatter;
import java.util.*;


import messageBLADYG.ClusterIdentificationMsg;
import messageBLADYG.MasterToWorkerMsg;
import messageBLADYG.WorkerToMasterMsg;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.routing.FromConfig;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import messageBLADYG.UserToMaster;



public class Master extends UntypedActor {

    private final int nrOfWorkers;
    private final int nrOfPartitions;
   // private final String graphFile;
    //private final String partFile;
    //private final String partMethod;
   // private final int nbInsertions;
   // private final int nbDeletions;
   // private final String TypeOfEdges;
    private PrintWriter rstf = null;
    private BufferedWriter DISCANLog = null;
    private DateTimeFormatter dtf=null;

    int MsgCount = 0;
    public static boolean inWork = false;
    public static boolean clusterReady = false;
    static int runningWorkers = 0;
    Vector<Integer> inWorkMembre;
    long starttime;
    static Hashtable<Integer, ActorRef> InfoWorkers;
    static Hashtable<Integer, ActorRef> Membres;

    static int indiceEdges = 0;
    int countWorkers = 0;
    static int nrOfResults = 0;
    private final long start = System.currentTimeMillis();
    private ActorRef workerRouter;
    long startTime;
    Vector frontierEdges, runtimesInsertions, runtimesDeletions, randomEdges;
    // the final results variable
    private List<Set<Integer>> global_clusters;
    private Set<Integer> global_bridges;
    private Set<Integer> global_outliers;

    // the intermediate results
    private List<Set<Integer>> temp_clusters;
    private Set<Integer> temp_bridges;
    private Set<Integer> temp_outliers;



    Hashtable<Integer, Integer> InvertedHashPartitions;
    public static Hashtable<Integer, Integer> HashPartitions;
    // to save all results from worker
    Vector results = new Vector();
    // router 
    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(), "workerRouter");

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    //cluster
    Cluster cluster = Cluster.get(getContext().system());

    // variable to englobe all external vertices
    Vector<Integer> external_vertices;

    // affected vertices
    Set<Integer> affected;
    // variables to ensure the micro batch processing
    int maxEvent;
    int nbEvent;

    //subscribe to cluster changes
    @Override
    public void preStart() throws IOException, InterruptedException {
        //#subscribe
        try {
            log.error("Master starting " + getSelf().path().name());
            cluster.subscribe(getSelf(), MemberEvent.class);
            log.info("Initializing");
            inWorkMembre = new Vector<Integer>();
            // startBLADYGComputation();
            Master.Membres.put(0, getSelf());
            Master.inWork = false;
        } catch (Exception e) {
            log.error("Exception from preStart method in master " + e.getMessage());
        }
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        try {
            cluster.unsubscribe(getSelf());
            log.info("cluster to be stoped...");


        } catch (Exception e) {
            log.error("Exception from postStop method in master " + e.getMessage());
        }
    }



    //Constructor of the Master
    public Master(final int nrOfWorkers, int nrOfPartitions) throws IOException {

        this.nrOfPartitions = nrOfPartitions;
        this.nrOfWorkers = nrOfWorkers;

        try {
            InfoWorkers = new Hashtable<Integer, ActorRef>();
            Membres = new Hashtable<Integer, ActorRef>();
            external_vertices = new Vector<>();
            affected=new HashSet<>();
            maxEvent=3;
            nbEvent=0;
            log.info("The Master constructor is executed ...");
            DISCANLog=new BufferedWriter(new FileWriter("DISCANLOG" ));
            // DISCANLog= new BufferedWriter("log");
           // DISCANLog.close();
        } catch (Exception e) {
            log.error("Exception from constructor in master " + e.getMessage());
        }
    }

    public Vector generateRandomEdges(int nbInsertions, int nbDeletions) {
        Vector vRandomEdges = new Vector();
        Vector vRandomEdgesDeletions = new Vector();
        /*

        */
        return vRandomEdges;
    }

    //method to identify all workers
    public void startBLADYGComputation() throws InterruptedException {
        log.info("Number of partitions: " + nrOfPartitions);
        log.info("Number of workers: " + nrOfWorkers);
        for (int start = 0; start < nrOfWorkers; start++) {
            Master.runningWorkers++;
            backend.tell(new ClusterIdentificationMsg(), getSelf());
            //  Thread.sleep(2000);
        }
        log.error("DISCAN " + InfoWorkers.size());
        InfoWorkers.put(0, getSelf());
        Membres.put(0, getSelf());
    }

    // method to process all message received from user or from workers
    public void onReceive(Object message) throws Exception {
        // message to identify if the workers is runing
        if (message instanceof ClusterIdentificationMsg) {
            countWorkers++;
            InfoWorkers.put(countWorkers, getSender());
            // display all worker in runing
            if (countWorkers == nrOfWorkers) {
                log.error("cluster identification on  master" + InfoWorkers.size());
                for (int i = 0; i < nrOfWorkers; i++) {
                    InfoWorkers.get(i).tell(new MasterToWorkerMsg(i, "WORKERSINFO", InfoWorkers), getSelf());
                }
            }
        }
        //recive message from workers
        else if (message instanceof WorkerToMasterMsg) {
            WorkerToMasterMsg obj = (WorkerToMasterMsg) message;
            if (obj.getOperationInfo().toUpperCase().equals("RSTLOADGRAPH")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("your graph has been loaded in "+(System.currentTimeMillis()-startTime));
                    DISCANLog.write("End load graph from user "+(System.currentTimeMillis()-startTime)+"\n");
                    MsgCount = 0;
                    inWork = false;
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("RSTDESTROY")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("your sub graph has been destroyed");
                    MsgCount = 0;
                    inWork = false;
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("RSTLOADPARTITION")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("you sub graph has been loaded in  "+(System.currentTimeMillis()-startTime));
                    for (int i = 0; i < Master.Membres.size(); i++) {
                       // log.info("worker n " + Master.Membres.get(i).path().name() + " " + i);
                    }
                    MsgCount = 0;
                    inWork = false;
                    DISCANLog.write("LOADPARTITIONS RunTime :"+(System.currentTimeMillis()-startTime)+"\n");
                }
            }
            // results from workers which have sent their results
            // get all external vertices from the partitions
            else if (obj.getOperationInfo().toUpperCase().equals("RSTEXTERNALVM")) {
                external_vertices.add(Integer.parseInt(obj.getObject().toString()));
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    for (int i = 1; i < InfoWorkers.size(); i++) {
                        Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "EXTERNALVM2", external_vertices), getSelf());
                        Thread.sleep(1000);
                    }
                    MsgCount = 0;
                    inWork = false;
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("RSTMEMBRES")) {
                MsgCount++;
                Master.Membres.put(MsgCount, getSender());
                if (nrOfWorkers == MsgCount) {
                    log.info("We have " + Membres.size() + " workers and master" + Master.Membres.keySet());
                    // in this step we will depulicate the members list to all workers
                    for (int i = 0; i < Membres.size(); i++) {
                        for (int j = 0; j < Membres.size(); j++) {
                            Membres.get(i).tell(new MasterToWorkerMsg(j, "ADDMEMBRE", null), Membres.get(j));
                        }
                    }
                    inWork = false;
                    clusterReady = true;
                    MsgCount = 0;
                    log.info("our cluster is ready for any operation " + inWork);
                }

            } else if (obj.getOperationInfo().toUpperCase().equals("RSTEXTERNALV")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1) * (Master.Membres.size() - 1)) {
                    log.info("we have finished a migration of vertices, and we well send to all worker NOB message to create virtual vertex ");
                    MsgCount = 0;
                  //  inWork = false;
                    for (int i = 1; i < Master.Membres.size(); i++) {
                        Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "NOB", null), getSelf());
                    }
                 //   DISCANLog.write("end externlv2 :"+(startTime-System.currentTimeMillis()));
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("END")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("we have finished " + obj.getObject() +" in "+(System.currentTimeMillis()-startTime));
                    if(obj.getObject().equals("NOB"))
                    {
                        DISCANLog.write("end externlv2 :"+(System.currentTimeMillis()-startTime)+"\n");
                    }
                    MsgCount = 0;
                    inWork = false;
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("RSTSENT")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("we have finished sent operation  " + obj.getObject()+"in "+(System.currentTimeMillis()-startTime));
                    for (int i = 1; i < Master.Membres.size(); i++) {
                        Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "ADDINTERNALV", null), getSelf());
                    }
                    MsgCount = 0;
                    inWork = false;
                    DISCANLog.write("Sent message   "+(System.currentTimeMillis()-startTime)+"\n");
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("ENDLOCALSCAN")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("all worker have been finished their LOCAL SCAN in "+(System.currentTimeMillis()-startTime));
                    DISCANLog.write("endsacn from user "+(System.currentTimeMillis()-startTime)+"\n");
                   // DISCANLog.close();
                    startTime=System.currentTimeMillis();
                    MsgCount = 0;
                    log.info("we start a combiner function:");
                   // DISCANLog.write("start combiner from user "+System.currentTimeMillis()+"\n");
                   // DISCANLog.close();
                    startTime=System.currentTimeMillis();
                    for (int i = 1; i < Master.Membres.size(); i++) {
                      Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "COMBINER", null), getSelf());
                    }
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("INTERSECTIONCORES")) {
                MsgCount++;
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("all worker have been finished an intersection step");
                    MsgCount = 0;
                    inWork = false;
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("CLUSTERS")) {
                MsgCount++;
                List<Set<Integer>> clusters = (List<Set<Integer>>) obj.getObject();
                //log.info(obj.getSenderWorkerID() + " :> " + obj.getObject());
                if (clusters != null & clusters.size() > 0) {
                    for (Set<Integer> cluster : clusters) {
                        global_clusters.add(cluster);
                    }
                }
               // Thread.sleep(1000);
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("all workers have send their local clusters");
                  //  DISCANLog.write("end computing of list of cluster from all worker to master"+System.currentTimeMillis()+"\n");
                  //  log.info("global clusters" + global_clusters);
                    MsgCount = 0;
                    log.info("we request  bridges vertices:");
                    rstf.write("=========================Clusters================================= \n");
                    for (Set<Integer> c : global_clusters) {
                        rstf.write("" + c + "\n");
                    }
                    rstf.write("================================================================== \n");
                    for (int i = 1; i < Master.Membres.size(); i++) {
                       Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "BRIDGES", null), getSelf());
                    }
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("DCLUSTERS")) {
                MsgCount++;
                List<Object> msg = (List<Object>) obj.getObject();
                Set<Integer> affected=(Set<Integer>)msg.get(0);
                List<Set<Integer>> clusters =(List<Set<Integer>>) msg.get(1);
                // remove all affected clusters which have almost one affected vertex
                List<Set<Integer>> clusters_tbr=new ArrayList<>();
               // this.global_clusters.removeAll(clusters_tbr);
                 clusters_tbr.clear();
                if (clusters != null & clusters.size() > 0) {
                    for (Set<Integer> c : clusters) {
                     if(!this.temp_clusters.contains(c)) {
                         this.temp_clusters.add(c);
                      }
                    }
                }
                //remove affected bridges and add them to the outlier
                Set<Integer> TBR=new HashSet<>();
                for(Integer b: affected){
                    if(this.global_bridges.contains(b)){
                       TBR.add(b);
                    }
                }
                this.global_bridges.removeAll(TBR);
                this.global_outliers.addAll(TBR);

                //  log.info("Dcluster "+obj.getSenderWorkerID()+" new updated clusters "+clusters_tbr);
                if (MsgCount == (Master.Membres.size() - 1)) {
                    if(affected.size()>0)
                    {
                        for(Integer vx: affected){
                            for(Set<Integer> c: this.global_clusters)
                            {
                                if(c.contains(vx) && !clusters_tbr.contains(c)){

                                    clusters_tbr.add(c);
                                }
                            }
                        }
                    }
                    this.global_clusters.removeAll(clusters_tbr);
                   for(Set<Integer> c : this.temp_clusters)
                   {
                       if(!this.global_clusters.contains(c)){
                         this.global_clusters.add(c);
                       }
                   }
                    this.temp_clusters.clear();
                  //  System.out.println("new clusters  "+temp_clusters+" and the clusters must be removed are "+clusters_tbr+" when the real clusters are  "+this.global_clusters);
                    log.info("all workers have send their new changed clusters"+this.global_clusters);
                    rstf.write("new Incriment \n ");
                    rstf.write("==========================clusters=================================================\n");
                    rstf.write(""+this.global_clusters+"\n");
                    rstf.write("===================================================================================\n");
                    MsgCount=0;
                    // request the bridge vertices

                    for (int i = 1; i < Master.Membres.size(); i++) {
                        Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "DBRIDGES", affected), getSelf());
                    }
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("ENDCLUSTERSUPDATE")) {
                MsgCount++;

                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("all workers have finished their local combiner "+obj.getObject());
                    MsgCount=0;
                    inWork=false;
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("RSTBRIDGES")) {
                MsgCount++;
                try {
                    Set<Integer> bridges = (Set<Integer>) obj.getObject();
                    if (bridges != null & bridges.size() > 0) {
                        for (Integer bridge : bridges) {
                            global_bridges.add(bridge);
                        }
                    }
                    if (MsgCount == (Master.Membres.size() - 1)) {
                        log.info("all workers has send their local bridges");
                       // log.info(" list of first global bridges :" + global_bridges);
                        MsgCount = 0;
                        log.info("we request  outliers vertices:");
                        for (int i = 1; i < Master.Membres.size(); i++) {
                            Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "OUTLIERS", null), getSelf());
                        }
                    }
                } catch (Exception exp) {
                    log.error("erreur from master : global bridges " + exp.getMessage());
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("RSTBRIDGES2")) {
                MsgCount++;
                try {
                    Set<Integer> bridges = (Set<Integer>) obj.getObject();
                    log.info(bridges+" bridge send from "+obj.getSenderWorkerID());

                    if (bridges != null & bridges.size() > 0) {
                        for (Integer bridge : bridges) {
                            global_bridges.add(bridge);
                        }
                    }
                    if (MsgCount == (Master.Membres.size() - 1)) {
                        log.info("all workers has send their new local bridges");
                        // log.info(" list of first global bridges :" + global_bridges);
                        MsgCount = 0;
                        log.info("we request the new  outliers vertices:");
                        for (int i = 1; i < Master.Membres.size(); i++) {
                            Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "DOUTLIERS", null), getSelf());
                        }
                    }
                } catch (Exception exp) {
                    log.error("erreur from master : global bridges " + exp.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("RSTOUTLIERS")) {
                MsgCount++;
                Set<Integer> outliers = (Set<Integer>) obj.getObject();

                if (outliers != null & outliers.size() > 0) {
                    for (Integer o : outliers) {
                        global_outliers.add(o);
                    }
                }
                Thread.sleep(1000);
                if (MsgCount == (Master.Membres.size() - 1)) {
                    log.info("all workers has send their local outliers");
                    log.info(" list of global outliers :" + global_outliers);
                    MsgCount = 0;
                    inWork = false;
                    rstf.write("=========================Outliers================================= \n");
                    rstf.write("" + global_outliers + "\n");
                    rstf.write("================================================================== \n");
                    rstf.close();
                    log.info("End outliers "+this.global_clusters);
                }


            } else if (obj.getOperationInfo().toUpperCase().equals("ALLBRIDGES")) {
                MsgCount++;
                Set<Integer> allbridges = (Set<Integer>) obj.getObject();

                if (allbridges != null & allbridges.size() > 0) {
                    global_bridges.addAll(allbridges);
                }
               // Thread.sleep(1000);
                if (MsgCount == (Master.Membres.size() - 1)) {
                   // global_outliers.removeAll(global_bridges);
                    log.info("all workers has send their final bridges");
                   // log.info(" list of global bridges :" + global_bridges);
                   // this.DISCANLog.write("get all bridges vertices "+System.currentTimeMillis()+"\n");
                    //this.DISCANLog.close();
                    MsgCount = 0;
                    inWork = false;
                    rstf.write("=========================Bridges================================= \n");
                    rstf.write("" + global_bridges + "\n");
                    rstf.write("================================================================== \n");
                    //rstf.close();
                    for (int i = 1; i < Master.Membres.size(); i++) {
                        Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "LASTOUTLIERS", global_bridges), getSelf());
                    }

                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("DALLBRIDGES")) {
                MsgCount++;
                Set<Integer> allbridges = (Set<Integer>) obj.getObject();

                if (allbridges != null & allbridges.size() > 0) {
                    global_bridges.addAll(allbridges);
                }
                // Thread.sleep(1000);
                if (MsgCount == (Master.Membres.size() - 1)) {
                    // global_outliers.removeAll(global_bridges);
                    log.info("all workers has send their final bridges");
                    // log.info(" list of global bridges :" + global_bridges);
                    // this.DISCANLog.write("get all bridges vertices "+System.currentTimeMillis()+"\n");
                    //this.DISCANLog.close();
                    MsgCount = 0;
                    inWork = false;
                    rstf.write("=========================Bridges================================= \n");
                    rstf.write("" + global_bridges + "\n");
                    rstf.write("================================================================== \n");
                    //rstf.close();
                    for (int i = 1; i < Master.Membres.size(); i++) {
                        Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "DLASTOUTLIERS", global_bridges), getSelf());
                    }

                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("RSTLASTOUTLIERS")) {
                MsgCount++;
                Set<Integer> outliers = new HashSet<>();
                outliers = (Set<Integer>) obj.getObject();
                this.global_outliers.addAll(outliers);
                if (MsgCount == (Membres.size() - 1)) {
                    this.DISCANLog.write("end combiner "+(System.currentTimeMillis()-startTime)+"\n");
                    this.DISCANLog.close();
                    rstf.write("=========================Outliers================================= \n");
                    rstf.write("" + this.global_outliers + "\n");
                    rstf.write("================================================================== \n");
                    rstf.write("Runing time is "+(System.currentTimeMillis()-startTime)+"\n");
                   // rstf.close();

                    log.info("we have finished a scan programm in "+(System.currentTimeMillis()-startTime));
                    MsgCount = 0;
                    inWork = false;
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("DRSTLASTOUTLIERS")) {
                MsgCount++;
                Set<Integer> outliers = new HashSet<>();
                outliers = (Set<Integer>) obj.getObject();
                this.global_outliers.addAll(outliers);
                if (MsgCount == (Membres.size() - 1)) {
                   // this.DISCANLog.write("end combiner "+(System.currentTimeMillis()-startTime)+"\n");
                   // this.DISCANLog.close();
                    rstf.write("=========================Outliers================================= \n");
                    rstf.write("" + this.global_outliers + "\n");
                    rstf.write("================================================================== \n");
                    rstf.write("Runing time is "+(System.currentTimeMillis()-startTime)+"\n");
                   // rstf.close();

                    log.info("we have finished a new update on the  scan programm in "+(System.currentTimeMillis()-startTime));
                    MsgCount = 0;
                    inWork = false;
                  //  this.affected.clear();

                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("WORKERS")) {
                MsgCount++;
                if (MsgCount == (Membres.size() - 1)) {
                    log.info("all workers have been started");
                    MsgCount = 0;
                    inWork = false;
                }
            } else if (obj.getOperationInfo().toUpperCase().equals("RSTINTERNALV")) {
                MsgCount++;
                if (MsgCount == (Membres.size() - 1)) {
                    log.info("we have added the first internal vertices");
                    MsgCount = 0;
                    inWork = false;
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("RSTDELETEEDGE")) {
                MsgCount++;
                Set<Integer> concerned=(Set<Integer>)obj.getObject();
               // this.affected.addAll(concerned);
                log.info("from master, we have deleted an edge and in the follow we present all affected vertices "+concerned+" from "+obj.getSenderWorkerID());
                if (MsgCount == (Membres.size() - 1)) {
                    log.info("we have finished a removing an edge");
                    MsgCount = 0;
                    inWork = false;
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("RSTDELETEVERTEX")) {
                MsgCount++;
                Set<Integer> concerned=(Set<Integer>)obj.getObject();
                // this.affected.addAll(concerned);
                log.info("from master, we have deleted an existing vertex and in the fellow we present all affected vertices "+concerned+" from "+obj.getSenderWorkerID());
                if (MsgCount == (Membres.size() - 1)) {
                    log.info("we have finished a removing requested  vertex");
                    MsgCount = 0;
                    inWork = false;
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("RSTADDEDGE")) {
                MsgCount++;
                Set<Integer> concerned=(Set<Integer>)obj.getObject();
                // this.affected.addAll(concerned);
                log.info("from master, we have added a new edge and in the fellow we present all affected vertices "+concerned+" from "+obj.getSenderWorkerID());
                if (MsgCount == (Membres.size() - 1)) {
                    log.info("we have finished  adding an new edge");
                    MsgCount = 0;
                    inWork = false;
                }
            }

            else if (obj.getOperationInfo().toUpperCase().equals("RSTADDVERTEX")) {
                MsgCount++;
                try {
                    if (((Set<Integer>) obj.getObject()) != null) {
                        Set<Integer> concerned = (Set<Integer>) obj.getObject();
                        this.affected.addAll(concerned);
                        log.info("from master, we have added a new  vertex and in the fellow we present all affected vertices " + concerned + " from " + obj.getSenderWorkerID());
                    }

                    if (MsgCount == (Membres.size() - 1)) {
                        log.info("we have finished adding a new vertex" + this.affected);
                        MsgCount = 0;
                        inWork = false;
                    }
                }
                catch(Exception e)
                {
                    log.error("Erreur from RSTADDVERTEX"+e.getMessage());
                }
            }
            else if (obj.getOperationInfo().toUpperCase().equals("RSTUPDATE")) {
                MsgCount++;
                try {

                    if (MsgCount == (Membres.size() - 1)) {
                        log.info(" update request performed");
                        this.nbEvent++;
                        if(nbEvent==this.maxEvent) {
                            this.startTime = System.currentTimeMillis();
                            for (int i = 1; i < Master.Membres.size(); i++) {
                                Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "CLUSTERSUPDATE", null), getSelf());
                            }
                        }

                        MsgCount = 0;
                        this.nbEvent=0;
                       // inWork = false;
                    }
                }
                catch(Exception e)
                {
                    log.error("Erreur from RSTADDVERTEX"+e.getMessage());
                }
            }
            else {
                log.error("we d'ont have a methode for this message");
            }


        }
        // if the master receives a message from user
        else if (message instanceof UserToMaster) {
            UserToMaster obj = (UserToMaster) message;

            // if a user needs to load graph in cluster
            if (obj.getAction().startsWith("LOADGRAPH")) {
                inWork = true;
                rstf = new PrintWriter("clustering");
               // DISCANLog=new BufferedWriter(new FileWriter("DISCANLOG" ));
                dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                // DISCANLog.write("master constructor "+dtf.format(LocalDate.now())+" \n");
              //  DISCANLog.write("master constructor "+System.currentTimeMillis()+"\n");
                startTime=System.currentTimeMillis();
            //    DISCANLog.write("loadGraph from user "+System.currentTimeMillis()+"\n");
                // start all workers to load graph
                String[] parts = obj.getAction().split(":");
                for (int i = 1; i < Membres.size(); i++) {
                    log.info("Load the partion num " + parts[1]);
                   // backend.tell(new MasterToWorkerMsg(i, "LOADGRAPH", new String(parts[1] + (i))), getSelf());
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "LOADGRAPH", new String(parts[1])), getSelf());

                   // Thread.sleep(1000);
                }

            }
            else if (obj.getAction().startsWith("SPLIT")) {
                    inWork = true;
                    //split a graph
                    String graphfile="G";
                    try {
                        Process process = Runtime.getRuntime().exec("src/main/resources/splitGraph.sh " + graphfile + " " + this.nrOfPartitions);
                    }catch(Exception e)
                    {
                        System.out.println("Split function "+e.getMessage());
                    }
                    rstf = new PrintWriter("clustering");
                    DISCANLog=new BufferedWriter(new FileWriter("DISCANLOG" ));
                    dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    // DISCANLog.write("master constructor "+dtf.format(LocalDate.now())+" \n");
                    //  DISCANLog.write("master constructor "+System.currentTimeMillis()+"\n");
                  //  DISCANLog.write("loadGraph from user "+System.currentTimeMillis()+"\n");
                    // start all workers to load graph
                    String[] parts = obj.getAction().split(":");
                    for (int i = 1; i < Membres.size(); i++) {
                        log.info("load the partion num " + (i));
                       //SPL backend.tell(new MasterToWorkerMsg(i, "LOADSUBGRAPH", new String(parts[1] + (i))), getSelf());
                        Thread.sleep(1000);
                    }

                }
            // if a user needs to destroy all data
            else if (obj.getAction().equals("DESTROY")) {
               // inWork = true;

                this.affected.clear();
                this.global_bridges.clear();
                this.global_clusters.clear();
                this.global_outliers.clear();
                this.external_vertices.clear();
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "DESTROY", null), getSelf());
                   // Thread.sleep(1000);
                }
            }
            // if a user needs to destroy all data
            else if (obj.getAction().startsWith("LOADPARTT")) {
                // inWork = true;
                String parts[]= obj.getAction().toString().split(":");
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "LOADPARTT", parts[1]), getSelf());
                    // Thread.sleep(1000);
                }
            }
            // if a user needs to get external vertices : not OK
            else if (obj.getAction().equals("EXTERNALV")) {
                log.error("from master external nodes needed");

                inWork = true;
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "EXTERNALV", null), getSelf());
                    Thread.sleep(1000);
                }
            }
            // if a user needs to get external vertices: ok
            else if (obj.getAction().equals("EXTERNALV2")) {
                log.error("from master external nodes needed");
                inWork = true;
               // DISCANLog.write("externalv from user "+System.currentTimeMillis()+"\n");
                    startTime=System.currentTimeMillis();
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "EXTERNALV2", null), getSelf());
                    //Thread.sleep(1000);
                }
            }

            // if a user needs to fix the inetrnal vertices on each partition ( in partitionning step)
            if (obj.getAction().startsWith("INTERNALV")) {
                log.error("from master we request the internal nodes needed");
                inWork = true;
             //   DISCANLog.write("internalv from user "+System.currentTimeMillis()+"\n");
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "INTERNALV", null), getSelf());
                    Thread.sleep(1000);
                }
            }

            // if a user needs to get external vertices
            else if (obj.getAction().equals("ADDV")) {
                log.error("from master external nodes needed");
                inWork = true;
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "ADDV", null), getSelf());
                    Thread.sleep(1000);
                }
            } else if (obj.getAction().equals("NBNODES")) {
                for (int i = 0; i < nrOfPartitions; i++) {
                    backend.tell(new MasterToWorkerMsg(i, "NBNODES", null), getSelf());
                    Thread.sleep(1000);
                }
            } else if (obj.getAction().equals("MEMBRES")) {
                log.info("we have we run membre function " + nrOfWorkers + " " + Master.Membres.keySet());
                inWork = true;
                for (int i = 0; i < nrOfWorkers; i++) {
                    backend.tell(new MasterToWorkerMsg(i, "MEMBRES", null), getSelf());
                }
            } else if (obj.getAction().equals("PRINTPARTITION")) {
                inWork = true;
              //  DISCANLog.write("print from user "+System.currentTimeMillis()+"\n");
                for (int i = 0; i < nrOfWorkers; i++) {
                    backend.tell(new MasterToWorkerMsg(i, "PRINTPARTITION", null), getSelf());
                }

            } else if (obj.getAction().equals("SHAREEDGES")) {
                inWork = true;
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "SHAREEDGES", null), getSelf());
                }
            } else if (obj.getAction().equals("LOCALSCAN")) {
                log.info("we start the SCAN algorithm on each worker:");
                inWork = true;
              //  DISCANLog.write("local from user "+System.currentTimeMillis()+"\n");
                startTime=System.currentTimeMillis();
                // prepare a global variable to our final results
                global_clusters = new ArrayList<Set<Integer>>();
                global_bridges = new HashSet<Integer>();
                global_outliers = new HashSet<Integer>();

                temp_clusters = new ArrayList<Set<Integer>>();
                temp_bridges = new HashSet<Integer>();
                temp_outliers = new HashSet<Integer>();
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "SCAN", null), getSelf());
                    log.info("send master to worker " + Master.Membres.get(i));

                }
            }
            // if a user needs to get external vertices
            else if (obj.getAction().equals("LSTMEMBRES")) {
                log.info("we will print all worker");
                inWork = true;
                for (int i = 1; i < Master.Membres.size(); i++) {
                    log.info(" send to Worker " + i + " path " + Master.Membres.get(i).path());
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "LSTMEMBRES", null), getSelf());

                }

            } else if (obj.getAction().startsWith("LOADPARTITION")) {
                log.info("we will load all partition");
                inWork = true;
                String parts[] = obj.getAction().split(":");
                 starttime=System.currentTimeMillis();
             //   DISCANLog.write("LOADPARTITION message "+System.currentTimeMillis()+"\n");
               // System.out.println(obj.getAction());
                for (int i = 1; i < Master.Membres.size(); i++) {
                   log.info("sent "+new String(parts[1])+"to "+i);
                   Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "LOADPARTITION", new String(parts[1])), getSelf());

                }
            } else if (obj.getAction().equals("PRINT")) {
                log.info("we will print the current partition");
                inWork = true;
                for (int i = 1; i < Master.Membres.size(); i++) {

                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "PRINT", null), getSelf());

                }
            } else if (obj.getAction().equals("SENT")) {
                log.info("we will sent all vertices of  the current partition");
                inWork = true;
                startTime=System.currentTimeMillis();
               // DISCANLog.write("SENT message "+System.currentTimeMillis()+"\n");
                for (int i = 1; i < Master.Membres.size(); i++) {

                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "SENT", null), getSelf());

                }
            } else if (obj.getAction().startsWith("SAVEPARTITION")) {
                log.info("we will sent all vertices of  the current partition");
                inWork = true;
                startTime=System.currentTimeMillis();
              //  DISCANLog.write("SAVE PARTITION message "+System.currentTimeMillis()+"\n");
                for (int i = 1; i < Master.Membres.size(); i++) {

                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "SAVE", obj.getAction()), getSelf());

                }
            } else if (obj.getAction().equals("SHAREDEDGES")) {
                log.info("we will share all edges");
                inWork = true;
                for (int i = 1; i < Master.Membres.size(); i++) {

                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "SHAREDEDGES", null), getSelf());

                }
            }
            // incremental part
            // message to add a vertex associated with an existing vertex
            else if (obj.getAction().startsWith("ADDEDGE")) {
                inWork = true;
                String msg[]=obj.getAction().split(":");
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "ADDEDGE", msg[1]), getSelf());
                }
            }
            // message to add an edge between tow existing vertices
            else if (obj.getAction().startsWith("ADDNEWEDGE")) {
                inWork = true;
                String msg[]=obj.getAction().split(":");
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "ADDNEWEDGE", msg[1]), getSelf());
                }
            }
            // message to delete an existeng vertex
            else if (obj.getAction().startsWith("DELETEVERTEX")) {
                inWork = true;
                String msg[]=obj.getAction().split(":");
                for (int i = 1; i < Master.Membres.size(); i++) {

                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "DELETEVERTEX", msg[1]), getSelf());
                }
            }
            // message to delete an existing edge
            else if (obj.getAction().startsWith("DELETEEDGE")) {
                inWork = true;
                String msg[]=obj.getAction().split(":");
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "DELETEEDGE", msg[1]), getSelf());
                }
            }
            // message to delete an existing edge
            else if (obj.getAction().startsWith("CLUSTERSUPDATE")) {
                inWork = true;
               // String msg[]=obj.getAction().split(":");
                this.startTime=System.currentTimeMillis();
                for (int i = 1; i < Master.Membres.size(); i++) {
                    Master.Membres.get(i).tell(new MasterToWorkerMsg(i, "CLUSTERSUPDATE",null), getSelf());
                }
            }
            else if (obj.getAction().startsWith("SAVERST")) {
                this.rstf.close();
                log.info("we just write all results in the clustering file");
            }


        } else {
            // log.error("message "+message);
            unhandled(message);
        }
    }
}
