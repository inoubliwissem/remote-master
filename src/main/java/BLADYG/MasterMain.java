package BLADYG;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Vector;

import messageBLADYG.UserToMaster;
import messageBLADYG.WorkerToMasterMsg;


public class MasterMain {

    public static ActorRef master;

    public static void send(String msg) {
        master.tell(new WorkerToMasterMsg(0, "STARTLOADGRAPH", null), master);
    }

    public static String getMasterIP(String FileIPs) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FileIPs));
        String line = "";
        String ip = "";
        while ((line = reader.readLine()) != null) {

            if (!line.equals("")) {
                String[] parts = line.split("\\s");
                if (parts[0].toUpperCase().equals("MASTER")) {
                    ip = parts[2];
                }
            }
        }

        reader.close();
        return ip;
    }

    public static String getSeedNodesConf(String FileIPs) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FileIPs));
        String line = "";
        String seed = "[";
        //int port=0;
        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                String[] parts = line.split("\\s");
                seed += "\"akka.tcp://DISCAN@" + parts[2] + ":2552\",";

            }
        }
        seed = seed.substring(0, seed.length() - 1);
        seed += "]";
        reader.close();
        return seed;
    }

    public static int getNumerOfWorker(String file) throws IOException {

        int nb= 0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";


        while ((line = reader.readLine()) != null) {
            if (!line.equals("") && line.startsWith("worker")) {
             nb++;
            }
        }

        reader.close();
        return  nb;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length == 1) {
           // final String graphFile = args[0];
          //  final String partFile = args[1];
          //  final String partMethod = args[2];

          //  final int nbInsertions = Integer.parseInt(args[5]);
          //  final int nbDeletions = Integer.parseInt(args[5]);
          //  final String TypeOfEdges = args[6];
            final String clusterFile = args[0];
            final int nbWorkers = getNumerOfWorker(clusterFile);
            final int nbPartitions = nbWorkers;

            int port = 2552;

            String hostname = MasterMain.getMasterIP(clusterFile);
            String seedNodes = MasterMain.getSeedNodesConf(clusterFile);

            final Config config = ConfigFactory.parseString("akka.cluster.roles = [masterRole]  ").
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.maximum-frame-size=125829 kB")).
                    withFallback(ConfigFactory.parseString("akka.cluster.allow-weakly-up-members = on")).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = \"" + hostname + "\"")).
                    withFallback(ConfigFactory.parseString("akka.cluster.role.workerRole.min-nr-of-members = " + nbWorkers)).
                    withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.threshold =12")).
                    withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.acceptable-heartbeat-pause = 600s")).
                    withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.heartbeat-interval = 10s")).
                    withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.heartbeat-request.expected-response-after = 600s")).
                    withFallback(ConfigFactory.parseString("transport-failure-detector.acceptable-heartbeat-pause = 600s")).
                    withFallback(ConfigFactory.parseString("connection-timeout = 600s")).
                    withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes = " + seedNodes).
                            withFallback(ConfigFactory.load("DISCAN")));



            final Config configLocal = ConfigFactory.parseString("akka.cluster.roles = [masterRole] ").
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=0")).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.maximum-frame-size=125829 kB")).
                    withFallback(ConfigFactory.parseString("akka.cluster.allow-weakly-up-members = on")).
                    withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.acceptable-heartbeat-pause = 600s")).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = \"127.0.0.1\"")).
                    withFallback(ConfigFactory.parseString("akka.cluster.role.workerRole.min-nr-of-members = " + nbWorkers)).
                    withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes = [\"akka.tcp://DISCAN@127.0.0.1:2552\"]").
                     withFallback(ConfigFactory.load("DISCAN")));

            final ActorSystem system = ActorSystem.create("DISCAN", configLocal);
            System.out.println("configuration"+system.settings());

          //  master = system.actorOf(Props.create(Master.class, nbWorkers, nbPartitions, graphFile, partFile, partMethod, nbInsertions, nbDeletions, TypeOfEdges), "master");
              master = system.actorOf(Props.create(Master.class, nbWorkers, nbPartitions), "master");

            Vector<String> operations=new Vector<String>();
            operations.add("MEMBRES");
            operations.add("LOADGRAPH");
            operations.add("PRINTPARTITION");
            operations.add("EXTERNALV2");
            operations.add("LOCALSCAN");

            Vector<String> operation_partition=new Vector<String>();
            operation_partition.add("MEMBRES");
            operation_partition.add("LOADPARTITION");
            operation_partition.add("WRITEPARTITION");


            int i=0;
            master.tell(new UserToMaster(null, "MEMBRES"), master);

         /*   Thread.sleep(5000);
            master.tell(new UserToMaster(null, "MEMBRES"), master);
            Thread.sleep(5000);
            master.tell(new UserToMaster(null, "LOADGRAPH:Graph"), master);
            Thread.sleep(5000);
            master.tell(new UserToMaster(null, "EXTERNALV2"), master);
            Thread.sleep(5000);
            master.tell(new UserToMaster(null, "LOCALSCAN"), master); */
          //  Thread.sleep(5000);
           // master.tell(new UserToMaster(null, "ADDEDGE:24;0"), master);
           // Thread.sleep(10000);
          //  master.tell(new UserToMaster(null, "CLUSTERSUPDATE"), master);


            while(true  )
            {
             //   System.out.println("i am in lessening to execute each function"+Master.inWork);
               //if(!Master.inWork && i<operations.size() && Master.clusterReady) {
              //  if(!Master.inWork ) {
                    System.out.println("chose an operation to be excuted");
                    Scanner sc = new Scanner(System.in);
                  // int pos=Integer.parseInt(sc.next());
                   master.tell(new UserToMaster(null, sc.next()), master);
                   // System.out.println("we will execute operation : "+operations.get(pos));
                    long startTime = System.nanoTime();
                    //   master.tell(new UserToMaster(null, operations.get(pos)), master);
                    long endTime = System.nanoTime();
                    long totalTime = endTime - startTime;
                    System.out.println("******************************************************************"+totalTime+"********************");
                    i++;
               }

           /*

             master.tell(new ClusterIdentificationMsg(), master);
             Thread.sleep(10000);
             master.tell(new UserToMaster(null, "MEMBRES"), master);
             Thread.sleep(10000);
            master.tell(new UserToMaster(null, "MEMBRES"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "LOADGRAPH:subGraph"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "EXTERNALV2"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "LOCALSCAN"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "DESTROY"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "PRINT"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "LOADGRAPH:subGraph"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "EXTERNALV2"), master);
            Thread.sleep(10000);
            master.tell(new UserToMaster(null, "LOCALSCAN"), master);
            // add edge to add new vertex
            //  master.tell(new UserToMaster(null, "ADDEDGE:13;0"), master);
          //  master.tell(new UserToMaster(null, "ADDEDGE:12;4"), master);
          // master.tell(new UserToMaster(null, "ADDNEWEDGE:11;4"), master);
           // master.tell(new UserToMaster(null, "ADDNEWEDGE:4;9"), master);
        //  master.tell(new UserToMaster(null, "ADDNEWEDGE:1;3"), master);
        //    master.tell(new UserToMaster(null, "ADDNEWEDGE:6;9"), master);
         // master.tell(new UserToMaster(null, "DELETEVERTEX:5"), master);
           // master.tell(new UserToMaster(null, "DELETEEDGE:7;8"), master);
CLUSTESRUPDATE to get all affected vertices


            */








        } else {
            System.out.println("Check your parameters on Master side");
        }

    }

}
