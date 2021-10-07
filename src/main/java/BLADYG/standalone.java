package BLADYG;

import java.io.IOException;

public class standalone {

    public void Start() {

    }

    public static void main(String[] args) throws InterruptedException, IOException, Exception {

        //Starting the workers

        WorkerMain.main(new String[]{"1", "ec2machinesL"});
        WorkerMain.main(new String[]{"2", "ec2machinesL"});
        WorkerMain.main(new String[]{"3", "ec2machinesL"});
        //starting the master 
        MasterMain.main(new String[]{"ec2machinesL"});

    /*  if(args.length>0)
        {
            if(args[0].equals("m"))
            {
                MasterMain.main(new String[]{ args[1]});

            }
            else if(args[0].equals("w"))
            {
                WorkerMain.main(new String[]{args[1], args[2]});
            }

        }
        else
        {
            System.out.println(" you must verifier your arguments");
        }
*/
    }
}
