/*
 * this feature returns the cluster id of the word as computer by word2vec
 * on a collection of 700k chemical journal abstracs
 * commas, dots, parenthesis, braces, and capitilization need to be removed first
 */
package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;

import java.io.*;
import java.util.*;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.Constants;
import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers.LuceneTokenizer;



public class WordCluster extends Pipe implements Serializable {
	
	static private HashMap<String,Integer> WordClustersDict;
	private String FeatureName;
	
	public WordCluster(String featureName){
		this.FeatureName = featureName;
		if (WordClustersDict == null)
		{
			synchronized (WordCluster.class) {
				if (WordClustersDict == null)
					readWordClassFile();
			}
		}
		
	}
	
	public Instance pipe(Instance carrier) {
		/*
		 * very important note:
		 * mallet expects binary features,(indicator)
		 * since we have multiple word classes, we assing each 
		 * class a feature, hence class 5 becomes FeatureName+5
		 */
		TokenSequence ts = (TokenSequence) carrier.getData();
		
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			String w = t.getText();
			//System.out.println(String.format("Word %d is %s",i,w));
			int cluster_id = getClusterForWord(w);
			if (cluster_id < Integer.MAX_VALUE){
				t.setFeatureValue(String.format("%s%d", this.FeatureName,cluster_id), 1.0 );
			}
				
		}
		return carrier;
		
	}
	
	static public int getClusterForWord(String word)
	{
		// probably no need for synchronization now as its done in the constructor, 
		// but anyway
		if (WordClustersDict == null)
		{
			synchronized (WordCluster.class) {
				if (WordClustersDict == null)
					readWordClassFile();
			}
		}
		/*
		word = word.replace(" ", "");
		word = word.replace(".", "");
		word = word.replace(",", "");
		word = word.replace("(", "");
		word = word.replace(")", "");
		word = word.replace("{", "");
		word = word.replace("}", "");
		*/
		String[] parts = LuceneTokenizer.tokenizeNoStopWords(word);
		if (parts != null && parts.length ==1){
			word = parts[0];
		}
		
		
		return WordClustersDict.containsKey(word) ? WordClustersDict.get(word.toLowerCase()).intValue() : Integer.MAX_VALUE;
		//return  WordClustersDict.get(word.toLowerCase()).intValue() ;
	}
	
	private static void readWordClassFile()
	{
		try
		{
			WordClustersDict = new HashMap<String,Integer>();
			InputStream is = WordCluster.class.getResourceAsStream("/wordclusters/chem-classes.sorted.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts.length == 2){
					WordClustersDict.put(parts[0].trim(), Integer.parseInt( parts[1].trim()));
				}
				
			}
			is.close();
		}
		catch (Exception e)
		{
			System.err.println("Failed ot read formula blacklist");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	// Serialization
	// legacy code form bsun, just adapted it
    private static final long serialVersionUID = Constants.SVUID_PARTIAL_STRING_PATTERN;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
//        System.out.println("Writing PartialStringPattern>>>");
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(this.FeatureName);
        out.writeObject(WordClustersDict);
//        out.writeInt(number);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        System.out.println("Reading PartialStringPattern>>>");
        int version = in.readInt();
        this.FeatureName = (String) in.readObject();
        WordClustersDict = (HashMap<String, Integer>) in.readObject();
//        this.number = in.readInt();
    }

}
