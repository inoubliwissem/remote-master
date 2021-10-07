# DISCAN: Distributed and Incremntal Structural Clustering Algorithm for Networks
# Required software and libraries
- Java 8 
- maven 3.5 or higher
- Akka
# Build the jar file
- clone this poject
- cd DISCAN
- ```shell
      $ mvn package
   ```
 # Configure the cluster of machines
 ## Configuration
Let's consider that we have three machines with an Ubuntu operating system.
##### Create a new user
Create a new user and change it as a root user
```
sudo adduser discan
sudo adduser discan sudo
```
##### Install java on all machines 
```
sudo apt install openjdk-8-jdk
```
##### Edit hostname file 
For each node to communicate with each other by name, open the  /etc/hosts file and  add the private IP addresses of the three machine.

```sudo nano /etc/hots```
Put the following lines:
```
10.0.2.1 master
10.0.2.2 slave1 
10.0.2.3 slave2
```
####  Setup  SSH Passwordless 
##### **Install the SSH server and client** (all machines)
```{r, engine='bash', count_lines}
sudo apt-get install openssh-client
sudo apt-get install openssh-server
```
Create a new OpenSSL key pair with the following commands:
```{r, engine='bash', count_lines}
$ ssh-keygen -t rsa -P ""
Generating public/private rsa key pair.
Enter file in which to save the key (/home/discan/.ssh/id_rsa):
**Created directory '/home/dscan/.ssh'.
Enter passphrase (empty for no passphrase): 
Enter same passphrase again:
Your identification has been saved in /home/discan/.ssh/id_rsa.
Your public key has been saved in /home/dscan/.ssh/id_rsa.pub.
```
Enable **ssh** to access to the local machine with adding the public key into the **authorized_keys** file
```{r, engine='bash', count_lines}
cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys
```
Now we can test it this configuration is done using this command in order to access to the local machine without password
```{r, engine='bash', count_lines}
ssh localhost
```
Share the **.ssh** folder to all other slave machines
```{r, engine='bash', count_lines}
scp -r .ssh discan@slave1:/home/discan
scp -r .ssh discan@slave2:/home/discan
```
Verify that can connect to machine without password
```{r, engine='bash', count_lines}
ssh slave1
```
### At each machine download and build the jar file
```{r, engine='bash', count_lines}
git clone https://github.com/inoubliwissem/remote-master.git
cd remote-master
mvn package
```

### Start master service in the master machine and worker service in the slave machines
```{r, engine='bash', count_lines}
java -cp DISCAN.jar BLADYG.standalone p1 id nb machines
```
where:
- DISCAN.jar the jar file
- BLADYG.standalone the main class
- p1 type of service (master or worker) p1 should be m or w
- id machine id
- nb number of worker
- machines text file contains all used machines

the structure of the machine file is like the next structure:<br>
master 0 10.0.1.20<br>
worker 1 10.0.1.35 <br>
worker 2 10.0.1.34 <br>
worker 3 10.0.1.38 <br>
worker 4 10.0.1.32 <br>
worker 5 10.0.1.31 <br>
worker 6 10.0.1.36 <br>
worker 7 10.0.1.37 <br>
worker 8 10.0.1.33 <br>
worker 9 10.0.1.40 <br>
### Use the DISCAN algorithm
At this step, the framework is ready to perform your request and to verify that all workers are available tape the next commande in the terminal<br> 
1 MEMBRES <br>
When all workers are available, we can use the cluster, so we start by dividing our graph file into sub-file using splitGraph.sh under the resource folder, the splitGraph script takes the name of the graph and number of partitions, example /splitGraph.sh dblp.txt 10 <br> after we assent each sub-file to worker machine.<br>
now we can apply the loadgraph function by the master machine <br>
2 LOADGraph <br>
this function takes the affected sub-file and after that using the "EXTERNALV2" command that compute the frontier vertices<br>
3 EXTERNALV2 <br>
4 LOCALSCAN  this command compute the local clustering <br>
5 MERGE      Here we combine the local results <br>
So here we have compute the initial graph clustering and the clustering file is generated to show the actual clustering, and now can add/delete edge or node using the newt function ADDEDGE,ADDNEWEDGE, DELETEVERTEX, DELETEVERTEX, DELETEEDGE, where:<br>
1 ADDEDGE:file         file contains couples of exiting nodes in the inital graph <br>
2 ADDNEWEDGE:file         file contains couples of exiting nodes and new nodes (this function used to add new vertices)<br>
3 DELETEVERTEX:file         file contains list of vertices to be deleted <br>
4 DELETEEDGE:file         file contains list of edges to be deleted <br>

After a batch of update, we can get the new clusering schema using the UPDATE commande<br>

6 UPDATE <br>

If we need to show the final result we can use the PRINT command





