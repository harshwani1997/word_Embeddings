import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class main {
	
 // This function reads the word and iterates the glove file, returns the wordembedding list of the word in the glove 
//	file that matches the input word.
	public static ArrayList<Double> readGetWordEm(File file, String word) throws IOException {
		
		ArrayList<Double> wordEmbedding = new ArrayList<>();
        List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
		int index=0;
		
		while(index<lines.size())
		{
			String str = lines.get(index++);
			String[] array = str.split(" ");
			
			if(array[0].equals(word))
			{
				for(int i=1;i<array.length;i++)
				{
					wordEmbedding.add(Double.parseDouble(array[i]));
				}
			}
		}
		return wordEmbedding;
	}
	
  //This functions take in a wordembedding, runs through the file and gets wordEmbeddings for all the words, and calculates 
//	cosine similarity between this and the input wordembedding and returns the word whose wordEmbedding was the most similar
//	to the input wordembedding.
	public static String getWordandSimEm(File file, ArrayList<Double> wordEm) throws IOException
	{
		if(wordEm.size()==0)
		{
			System.out.print("Such a word does not exist");
			return "";
		}
		
		int index=0;
		List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
		
		String word = "";
		while(index<lines.size())
		{
			ArrayList<Double> wordEmbedding = new ArrayList<>();
			String str = lines.get(index++);
			String[] array = str.split(" ");
			
			for(int i=1;i<array.length;i++)
			{
				wordEmbedding.add(Double.parseDouble(array[i]));
			}
			
			if(equalLists(wordEmbedding, wordEm))
			{
				word = array[0];
				break;
			}
		}
		
		ArrayList<Double> simWordEm = getSimWordEm(file, wordEm);
		return word;
	}
	
	
	private static ArrayList<Double> getSimWordEm(File file, ArrayList<Double> wordEm) throws IOException
	{
		int index=0;
		List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
		Double maxCos = Double.MIN_VALUE;
		ArrayList<Double> simWordEm = new ArrayList<>();
		
		while(index<lines.size())
		{
			ArrayList<Double> wordEmbedding = new ArrayList<>();
			String str = lines.get(index++);
			String[] array = str.split(" ");
			String word = "";
			
			for(int i=1;i<array.length;i++)
			{
				wordEmbedding.add(Double.parseDouble(array[i]));
			}
			
			//to avoid getting the same word list
			if(equalLists(wordEmbedding, wordEm))
			{
				continue;
			}
			
			Double disCos = getCosine(wordEmbedding, wordEm);
			if(disCos>maxCos)
			{
				maxCos = disCos;
				word = array[0];
			    simWordEm = wordEmbedding;
			}
		}
		return simWordEm;
	}

//	This helper function computes the cosine similarity between two wordEmbeddings lists
	private static Double getCosine(ArrayList<Double> wordEmbedding, ArrayList<Double> wordEm)
	{
		double dotProduct = 0.0;
		double wordEmbeddingDenom = 0.0;
		double wordEmDenom = 0.0;
		for(int i=0;i<wordEmbedding.size();i++)
		{
			dotProduct = dotProduct + wordEmbedding.get(i)*wordEm.get(i);
			wordEmbeddingDenom = wordEmbeddingDenom + wordEmbedding.get(i)*wordEmbedding.get(i);
			wordEmDenom =  wordEmDenom + wordEm.get(i)*wordEm.get(i);
		}
		double sqrta1 = Math.sqrt(wordEmbeddingDenom);
		double sqrta2 = Math.sqrt(wordEmDenom);
		
		return dotProduct / (sqrta1*sqrta2);
	}

//	This helper function checks if the two given lists are equal or not
	private static boolean equalLists(ArrayList<Double> wordEmbedding, ArrayList<Double> wordEm) 
	{
		
		for(int i=0;i<wordEmbedding.size();i++)
	  {
		  Double d1 = wordEmbedding.get(i);
		  Double d2 = wordEm.get(i);
		  if(Double.compare(d1, d2) != 0)
		  {
			  return false;
		  }
	  }
		return true;
	}

//	This helper function computes the wordEmbebdding score between two inout words	
	public static Double getScore(File file, String word1, String word2) throws IOException
	{
		ArrayList<Double> wordEm1 = readGetWordEm(file, word1.toLowerCase());
		ArrayList<Double> wordEm2 = readGetWordEm(file, word2.toLowerCase());
		System.out.println(word1 + " " + word2);
		return getCosine(wordEm1, wordEm2);
	}
	
	//This function writes the required output file(writes the two wordembedding scores, overall correlation)
	public static void getOutputFile(File wordSim, File file, FileWriter fstream) throws IOException
	{
		 List<String> lines = Files.readAllLines(wordSim.toPath(),StandardCharsets.UTF_8);
		 int index=0;
		 
		 ArrayList<Double> humanList = new ArrayList<>();
		 ArrayList<Double> wordEmList = new ArrayList<>();
		 
		 while(index<lines.size())
		 {
			 String line = lines.get(index++);
			 if(line.charAt(0)=='#')
			 {
				 continue;
			 }
			 
			 String[] wordSimarr = line.split("\t");
			 double wordEmScore  = getScore(file, wordSimarr[1], wordSimarr[2]);
			 humanList.add(Double.parseDouble(wordSimarr[3]));
			 wordEmList.add(wordEmScore);
			 fstream.write(wordSimarr[1] + " " + wordSimarr[2] + " " + wordSimarr[3] + " " + wordEmScore + "\n");
		 }
		 
		 fstream.write("\n");
		 fstream.write("This is the Overall Correlation Score: " + getCorrelation(humanList, wordEmList) + "\n");
		 fstream.write("\n");
	}
	
//	This function gets the correlation between the two arraylists(wordEmbedding for two words)
	public static double getCorrelation(ArrayList<Double> humanList, ArrayList<Double> wordEmList)
	{
		double[] arr1 = new double[humanList.size()];
		double[] arr2 = new double[humanList.size()];
		
		for(int i=0;i<humanList.size();i++)
		{
			arr1[i] = humanList.get(i);
			arr2[i] = wordEmList.get(i);
		}
		double correlationScore = new PearsonsCorrelation().correlation(arr1, arr2);
		return correlationScore;
	}
	
	//OptionalPart..this is the analogy function which computes the fourth year using three input words
	public static String getAnalogy(File file, String word1, String word2, String word3) throws IOException
	{
		ArrayList<Double> wordEm1 = readGetWordEm(file, word1);
		ArrayList<Double> wordEm2 = readGetWordEm(file, word2);
		ArrayList<Double> wordEm3 = readGetWordEm(file, word3);
		double calculated = 0.0;
		
		ArrayList<Double> wordEm4 = new ArrayList<>();
		for(int i=0;i<wordEm1.size();i++)
		{
	        calculated = wordEm2.get(i) - wordEm1.get(i) + wordEm3.get(i);
			wordEm4.add(calculated);
		}
		
		int index=0;
		List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
		Double maxCos = Double.MIN_VALUE;
		String analogy = "";
		
		while(index<lines.size())
		{
			ArrayList<Double> wordEmbedding = new ArrayList<>();
			String str = lines.get(index++);
			String[] array = str.split(" ");
			
			if(array[0].equals(word1) || array[0].equals(word2) || array[0].equals(word3))
			{
				continue;
			}
			
			for(int i=1;i<array.length;i++)
			{
				wordEmbedding.add(Double.parseDouble(array[i]));
			}
			
			DecimalFormat df = new DecimalFormat("#.####");
			df.setRoundingMode(RoundingMode.CEILING);
			Double disCos = Double.parseDouble(df.format(getCosine(wordEmbedding, wordEm4).doubleValue()));
			if(disCos>maxCos)
			{
				maxCos = disCos;
				analogy = array[0];
				System.out.println(analogy + " " + disCos);
			}
		}
	  return analogy;  	
	}
	
	public static void main(String[] args) throws IOException
	{
		File file = new File("C:\\Users\\Admin\\Desktop\\NYU Courant(2nd sem)\\NLP\\NLP Assignment 7\\glove.6B.50d.txt");
		//File file = new File("glove.6B.300d.txt");
		String word = "money";
		ArrayList<Double> wordEm = readGetWordEm(file, word);
		System.out.println(wordEm);
		
		String word1 = getWordandSimEm(file, wordEm);
		System.out.print(word1);
		
		double score = getScore(file, "money" , "cash");
		System.out.print(score);
		
		String analogy = getAnalogy(file, "woman", "princess", "man");
		System.out.print(analogy);
		
//		This is the local system path, which needs to be replaced by the path of the wordim-353.txt file in your system
		File wordsim = new File("C:\\Users\\Admin\\Desktop\\NYU Courant(2nd sem)\\NLP\\NLP Assignment 7\\wordsim-353.txt");
		//File wordsim = new File("wordsim-353.txt");
		
		//FileWriter fstream = new FileWriter("C:\\Users\\Admin\\Desktop\\NYU Courant(2nd sem)\\NLP\\NLP Assignment 7\\report1.txt");
		FileWriter fstream = new FileWriter("report1.txt");
		getOutputFile(wordsim, file, fstream);
		
		//Adding some analogy examples to my output file
		fstream.write("Below are some of the analog examples: " + "\n");
		fstream.write("\n");
		fstream.write("man:king::woman:" + getAnalogy(file, "man", "king", "woman") + "\n");
		fstream.write("nurse:woman::doctor:" + getAnalogy(file, "nurse", "woman", "doctor") + "\n");
		fstream.write("son:man::daughter:" + getAnalogy(file, "son", "man", "daughter") + "\n");
		fstream.write("woman:girl::man:" + getAnalogy(file, "woman", "girl", "man") + "\n");
		fstream.write("tiger:animal::boy:" + getAnalogy(file, "tiger", "animal", "boy") + "\n");
		fstream.write("human:man::animal:" + getAnalogy(file, "human", "man", "animal") + "\n");
		fstream.write("media:radio::fruit:" + getAnalogy(file, "media", "radio", "fruit") + "\n");
		fstream.write("prince:king::princess:" + getAnalogy(file, "prince", "king", "princess") + "\n");
		fstream.write("woman:princess::man:" + getAnalogy(file, "woman", "princess", "man") + "\n");
		fstream.write("man:father::woman:" + getAnalogy(file, "man", "father", "woman") + "\n");
		fstream.write("girl:aunt::boy:" + getAnalogy(file, "girl", "aunt", "boy") + "\n");
		fstream.write("man:human::cat:" + getAnalogy(file, "man", "human", "cat") + "\n");
		fstream.close();
		
	}
}
