package graphTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import structure.Edge;
import BLADYG.Master;

public class GraphTools {

public static void main(String[] args) throws Exception
{
	//GraphTools.snapToMetisConverter("roadNet-TX.txt", "roadNet-TX.txt_metis");
	//GraphTools.randomPartitions(2, 4100000,"randomPartitioning_");
	//DistKCore.centralizedApproach("facebook_combined.txt", "bindata.txt",null);

	GraphTools.snapToMetisConverter("soc-LiveJournal11.txt", "soc-LiveJournal_metis");
}
	
public static void snapToMetisConverter(String snapfile, String metisfile) throws IOException 
{
	Hashtable<Integer, Vector>  metisHash=new Hashtable<Integer, Vector> () ;
	BufferedReader reader = new BufferedReader(new FileReader(snapfile));
	Vector<Edge> v3=new Vector<Edge>();
	String line="";
	long nbEdges=0;
	long nbNodes=0;
	long nbEntries=0;
	while((line=reader.readLine())!=null)
	{
		String[] parts = line.split("\\s");
		//if((!line.equals(""))&&(Integer.parseInt(parts[0])!=0)&&((Integer.parseInt(parts[1])!=0))&&((Integer.parseInt(parts[1])<1367184))&&((Integer.parseInt(parts[1])<1367184)))
		if(!line.equals(""))
		//if((!line.equals(""))&&(Integer.parseInt(parts[0])!=0)&&((Integer.parseInt(parts[1])!=0)))
		//if((Integer.parseInt(parts[0])!=0)&&((Integer.parseInt(parts[1])!=0)))
		{
			int node1=Integer.parseInt(parts[0]);
			int node2=Integer.parseInt(parts[1]);
			node1++;
			node2++;
			nbEdges++;	
			/*
			Edge e1 =new Edge(node1,node2);
			//Edge e2 =new Edge(node2,node1);
			if(!v3.contains(e1))
			{
				v3.addElement(e1);
					nbEdges++;			
			}

			System.out.println(nbEdges+" Edges processed");
			*/

			if(node1>nbNodes)
				nbNodes=node1;
			if(node2>nbNodes)
				nbNodes=node2;
			
			if (metisHash.containsKey(node1))
			{
				
				Vector<Integer> v=(Vector)metisHash.get(node1);
				if(!v.contains(node2))
				{
					v.addElement(node2);
				}
					
				metisHash.put(node1,v);

			}
			else
			{
				
				Vector<Integer> v=new Vector();
				v.addElement(node2);
				metisHash.put(node1,v);
			}
			
			if (metisHash.containsKey(node2))
			{
				
				Vector<Integer> v=(Vector)metisHash.get(node2);
				if(!v.contains(node1))
				{
					v.addElement(node1);
				}
				metisHash.put(node2,v);

			}
			else
			{
				Vector<Integer> v=new Vector();
				v.addElement(node1);
				metisHash.put(node2,v);

			}
			
	
		}
	}

	reader.close();
	
	System.out.println(nbNodes +" Nodes and "+nbEdges+" Edges");
	
	FileWriter metis = new FileWriter(metisfile);
	metis.write(nbNodes+" "+nbEdges+"\n");
	

	int count=0;
	for (int i=1;i<=nbNodes;i++)
	{
		if(metisHash.containsKey(i))
		{
			Vector v=metisHash.get(i);
			for (int j=0;j<v.size();j++)
			{
					metis.append((int) v.elementAt(j)+" "); 
					//count++;
			}
			metis.append("\n");
		}
		else
		{
			count++;
			metis.append(1+"\n");
			
		}
	}	
	System.out.println("count = "+count);
	metis.close();
	
}
public static int randInt(int min, int max) 
{

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
public static void randomPartitions(int nbPart, int nbNodes, String filePrefix) throws IOException
{
	//BufferedReader reader = new BufferedReader(new FileReader(graphFile));
	FileWriter partFile = new FileWriter(filePrefix+nbNodes+"_"+nbPart);
	
	for(int i=0;i<=nbNodes;i++)
	{
		int part=randInt(1,nbPart);
		 partFile.append(i+" "+part+"\n");
	}
	partFile.close();
	//String line="";
	/*
	while((line=reader.readLine())!=null)
	{
		if(!line.equals(""))
		{
			//System.out.println("test");
			//String[] parts = line.split("\\s");
			int part=randInt(1,nbPart);
			FileWriter fichier = new FileWriter("randPart_"+part,true);
			 fichier.append(line+"\n");
			 fichier.close();
			 
			
			 
		}
	}
	reader.close();
	*/
}
public static Vector generateFrontierNodesFromFile(String frontierFile, int nbInsert, int nbDelete) throws IOException
{
	Vector vFrontierNodes=new Vector();
	String line="";
	int ind=0;
	BufferedReader reader = new BufferedReader(new FileReader(frontierFile));
	while((line=reader.readLine())!=null)
	{
		ind++;
		//System.out.println("test");
		if(!line.equals(""))
		{
			String[] parts = line.split("\\s");
			
		//	vFrontierNodes.addElement(new Edge(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),1));
			//vFrontierNodes.addElement(new Edge(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),2));

		}
		if(ind==nbInsert)
		{
			break;
		}		
	}
	
	reader.close();
	
	ind=0;
	reader = new BufferedReader(new FileReader(frontierFile));
	while((line=reader.readLine())!=null)
	{
		ind++;
		//System.out.println("test");
		if(!line.equals(""))
		{
			String[] parts = line.split("\\s");
			
			//vFrontierNodes.addElement(new Edge(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),1));
		//	vFrontierNodes.addElement(new Edge(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),2));

		}
		if(ind==nbDelete)
		{
			break;
		}		
	}
	
	reader.close();
	return vFrontierNodes;
	
}
public static Hashtable constructPartitionsRandom(String partitionsFile, String graphFile, String frontierFile, int nbPartitions, String prefixPart) throws IOException
{
	Hashtable<Integer,Integer> hashPartitions= new Hashtable<Integer,Integer>();
	Hashtable<Integer,Integer> InvertedHashPartitions= new Hashtable<Integer,Integer>();
	String line="";
	BufferedReader reader = new BufferedReader(new FileReader(partitionsFile));
	BufferedReader graphReader = new BufferedReader(new FileReader(graphFile));
	FileWriter frontierNodes = new FileWriter(frontierFile);
	while((line=reader.readLine())!=null)
	{
		if(!line.equals(""))
		{
			//System.out.println("test");
			String[] parts = line.split("\\s");
			hashPartitions.put(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
			
			if(InvertedHashPartitions.containsKey(Integer.parseInt(parts[1])))
			{
				InvertedHashPartitions.put(Integer.parseInt(parts[1]), InvertedHashPartitions.get(Integer.parseInt(parts[1]))+1);
			}
			else
			{
				InvertedHashPartitions.put(Integer.parseInt(parts[1]),1);
			}
		}
	}
	reader.close();
	
	
	for(int i=1;i<=nbPartitions;i++)
	{
		 FileWriter fich = new FileWriter(""+prefixPart+i);
		 fich.close();
	}
	
	while((line=graphReader.readLine())!=null)
	{
		
		if(!line.equals(""))
		{
			String[] parts = line.split("\\s");
			
			if(hashPartitions.get(Integer.parseInt(parts[0]))==hashPartitions.get(Integer.parseInt(parts[1]))){
				//System.out.println("test2");
				 FileWriter fichier = new FileWriter(prefixPart+hashPartitions.get(Integer.parseInt(parts[0])),true);
				// PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("myfile.txt", true)));
				 fichier.append(line+"\n");
				 fichier.close();
			}
			else
			{
				//frontierNodes.write(line+"\n");
				frontierNodes.append(Integer.parseInt(parts[0])+" "+Integer.parseInt(parts[1])+" "+hashPartitions.get(Integer.parseInt(parts[0]))+" "+hashPartitions.get(Integer.parseInt(parts[1]))+"\n");
				
			}
		}
		
	}
	
	frontierNodes.close();
	Master.HashPartitions=hashPartitions;

	return InvertedHashPartitions;
}
public static Hashtable constructPartitionsMetis(String partitionsFile, String graphFile, String frontierFile, int nbPartitions, String prefixPart) throws IOException
{
	Hashtable<Integer,Integer> hashPartitions= new Hashtable<Integer,Integer>();
	Hashtable<Integer,Integer> InvertedHashPartitions= new Hashtable<Integer,Integer>();
	String line="";
	BufferedReader reader = new BufferedReader(new FileReader(partitionsFile));
	BufferedReader graphReader = new BufferedReader(new FileReader(graphFile));
	FileWriter frontierNodes = new FileWriter(frontierFile);
	int node=0;
	while((line=reader.readLine())!=null)
	{
		if(!line.equals(""))
		{
			//System.out.println(line);
			//String[] parts = line.split("\\s");
			int partIdentifier=Integer.parseInt(line)+1;
			if(InvertedHashPartitions.containsKey(partIdentifier))
			{
				InvertedHashPartitions.put(partIdentifier, InvertedHashPartitions.get(partIdentifier)+1);
			}
			else
			{
				InvertedHashPartitions.put(partIdentifier,1);
			}
			hashPartitions.put(node,partIdentifier);
			node++;
		}
	}
	reader.close();
	
	//System.out.println(InvertedHashPartitions);
	for(int i=1;i<=nbPartitions;i++)
	{
		 FileWriter fich = new FileWriter(prefixPart+i);
		 fich.close();
	}
	
	while((line=graphReader.readLine())!=null)
	{
		
		if(!line.equals(""))
		{
			String[] parts = line.split("\\s");
			
			if(hashPartitions.get(Integer.parseInt(parts[0]))==hashPartitions.get(Integer.parseInt(parts[1]))){
				//System.out.println("test2");
				 FileWriter fichier = new FileWriter(prefixPart+hashPartitions.get(Integer.parseInt(parts[0])),true);
				// PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("myfile.txt", true)));
				 fichier.append(line+"\n");
				 fichier.close();
			}
			else
			{
				//frontierNodes.write(line+"\n");
				frontierNodes.append(Integer.parseInt(parts[0])+" "+Integer.parseInt(parts[1])+" "+hashPartitions.get(Integer.parseInt(parts[0]))+" "+hashPartitions.get(Integer.parseInt(parts[1]))+"\n");
				
			}
		}
		
	}
	
	frontierNodes.close();
	
	Master.HashPartitions=hashPartitions;
	return InvertedHashPartitions;
}
}

