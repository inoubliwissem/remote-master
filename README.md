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
