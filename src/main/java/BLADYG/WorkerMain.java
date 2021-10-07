package BLADYG;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import akka.cluster.Cluster;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import akka.actor.ActorSystem;
import akka.actor.Props;


public class WorkerMain {

    public static String getWorkerIP(String FileIPs, int ind) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(FileIPs));
        String line = "";
        String ip = "";
        while ((line = reader.readLine()) != null) {

            if (!line.equals("")) {
                String[] parts = line.split("\\s");
                if ((parts[0].toUpperCase().equals("WORKER")) && (Integer.parseInt(parts[1]) == ind)) {
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
        int port = 0;
        while ((line = reader.readLine()) != null) {
            if (!line.equals("")) {
                String[] parts = line.split("\\s");
                seed += "\"akka.tcp://DISCAN@" + parts[2] + ":" + 2552 + "\",";
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

    public static void main(String[] args) throws IOException,Exception {

        // parser object to get the parameters
    /*
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader("parameters.json"));
        JSONObject parameters = (JSONObject) obj;

        long nb_workers=(long)((JSONObject) obj).get("Nb_worker");
        long portNumber=(long)((JSONObject) obj).get("portNumber");
        String hostName=(String)((JSONObject) obj).get("hostname");
        JSONArray msg = (JSONArray) parameters.get("seednodes");
        Iterator<String> iterator = msg.iterator();
        String seednodes="[";
        while (iterator.hasNext()) {
            seednodes+=iterator.next()+",";
        }
        seednodes=seednodes.substring(0, seednodes.length() - 1);
        seednodes+="]";*/
        // Override the configuration of the port when specified as program argument
       if (args.length == 2) {

            final String indPort = args[0];
            final String clusterFile =args[1];
            final int nbWorkers =getNumerOfWorker(clusterFile);

            int port = Integer.parseInt(indPort) + 2550;
         //  int port = 2552;
            int workerId=Integer.parseInt(indPort);

            String hostname = WorkerMain.getWorkerIP(clusterFile, Integer.parseInt(args[0]));
            String seedNodes = WorkerMain.getSeedNodesConf(clusterFile);


            System.out.println("[Worker] Hostname = " + hostname + " port " + port);
            System.out.println("[Worker] Seed nodes = " + seedNodes);


          final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
                  withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.maximum-frame-size=125829 kB")).
                    withFallback(ConfigFactory.parseString("akka.cluster.roles = [workerRole]")).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = \"" + hostname + "\"")).
                    withFallback(ConfigFactory.parseString("akka.cluster.role.workerRole.min-nr-of-members = " + nbWorkers)).
                  withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.threshold =12")).
                  withFallback(ConfigFactory.parseString("akka.cluster.allow-weakly-up-members = on")).
                  withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.acceptable-heartbeat-pause = 600s")).
                  withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.heartbeat-interval = 10s")).
                  withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.heartbeat-request.expected-response-after = 600s")).
                  withFallback(ConfigFactory.parseString("transport-failure-detector.acceptable-heartbeat-pause = 600s")).
                  withFallback(ConfigFactory.parseString("connection-timeout = 600s")).
                    withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes = " + seedNodes)).
                    withFallback(ConfigFactory.load("DISCAN"));

            final Config configLocal = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.maximum-frame-size=125829 kB")).
                    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = \"127.0.0.1\"")).
                    withFallback(ConfigFactory.parseString("akka.cluster.allow-weakly-up-members = on")).
                    withFallback(ConfigFactory.parseString("akka.cluster.failure-detector.acceptable-heartbeat-pause = 600s")).
                    withFallback(ConfigFactory.parseString("akka.cluster.role.workerRole.min-nr-of-members = " + nbWorkers)).
                    withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes = [\"akka.tcp://DISCAN@127.0.0.1:2552\"]").
                            withFallback(ConfigFactory.parseString("akka.cluster.roles = [workerRole]")).
                            withFallback(ConfigFactory.load("DISCAN")));
         //  final Config conf2 = ConfigFactory.load("applicationRemote.conf");

            final ActorSystem system = ActorSystem.create("DISCAN", configLocal);

            Cluster.get(system).registerOnMemberUp(new Runnable() {
                @Override
                public void run() {
                    system.actorOf(Props.create(Worker.class, workerId,nbWorkers), "worker");
                }
            });
        } else {
            System.out.println("Check your parameters on Worker side");
        }
    }

}
