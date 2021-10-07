package graphTools;

import java.util.Vector;


public class Statistics {
	public static double mean(Vector  numbers) 
	 {
		double sum = 0;
		 
		  // Taking the average to numbers
		  for(int i =0; i< numbers.size(); i++) {
			  //System.out.print(numbers.elementAt(i)+" ");
		   sum = sum + (int) numbers.elementAt(i);
		  }
		  //System.out.println();
		  double mean = sum/numbers.size();
		 
		  return mean;
	 }
	 public static double stdDev(Vector  numbers) 
	 {

		 /*
	  double[] numbers = new double[10];
	 
	  // Take the 10 numbers in array for which we
	  // want to calculate the standard deviation
	  numbers[0] = 23;
	  numbers[1] = 92;
	  numbers[2] = 46;
	  numbers[3] = 55;
	  numbers[4] = 63;
	  numbers[5] = 94;
	  numbers[6] = 77;
	  numbers[7] = 38;
	  numbers[8] = 84;
	  numbers[9] = 26;
	 */
		 /*
	  System.out.println("1. Get the mean of numbers. The mean is : ");
	 
	  double sum = 0;
	 
	  // Taking the average to numbers
	  for(int i =0; i< numbers.size(); i++) {
		  System.out.print(numbers.elementAt(i)+" ");
	   sum = sum + (int) numbers.elementAt(i);
	  }
	 
	  double mean = sum/numbers.size();
	 
	  System.out.println(mean);
	 */
		 
      double mean=mean(numbers);
	  //System.out.println("\n2. Get deviation of mean from each number : ");
	 
	  Vector deviations = new Vector();
	 
	  // Taking the deviation of mean from each numbers
	  for(int i = 0; i < numbers.size(); i++) {
	  //for(int i = 0; i < deviations.length; i++) {
	   deviations.addElement((int)numbers.elementAt(i) - mean);
	  // System.out.printf("%2.2f",(double)deviations.elementAt(i));
	   //System.out.print(" ");  
	  }
	 
	 // System.out.println();
	  //System.out.println("\n3. Get squares of deviations : ");
	  Vector squares = new Vector();
	 
	  // getting the squares of deviations
	  for(int i = 0; i < numbers.size(); i++) {
	  //for(int i =0; i< squares.length; i++) {
	   squares.addElement((double)deviations.elementAt(i) * (double)deviations.elementAt(i));
	  // System.out.printf("%4.2f",(double)squares.elementAt(i));
	   //System.out.print(" ");
	  }
	 
	  //System.out.println();
	  //System.out.println("\n4. Get addition of squares : ");
	 
	  double sum = 0;
	 
	  // adding all the squares
	  for(int i =0; i< squares.size(); i++) {
	   sum = sum + (double)squares.elementAt(i);
	  }
	 
	  //System.out.println(sum);
	 
	  // dividing the numbers by one less than total numbers
	  //System.out.println("\n5. Divide addition of squares by total (numbers) - 1 : ");
	  double result = sum / (numbers.size() - 1);
	 
	  //System.out.printf("%4.2f",result);
	 
	  double standardDeviation = Math.sqrt(result);
	   
	  // Taking square root of result gives the
	  // standard deviation
	  //System.out.println("\n\n6. Take the square root of result which gives");
	  //System.out.println("the Standard Deviation of the ten numbers : ");
	  //System.out.printf("%4.2f",standardDeviation);
	 return standardDeviation;
	 }
	 
	}