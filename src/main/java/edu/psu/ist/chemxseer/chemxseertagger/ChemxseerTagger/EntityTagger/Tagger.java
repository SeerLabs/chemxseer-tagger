package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger;


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import cc.mallet.fst.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.LineGroupIterator;


import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers.*;
import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.Token;
import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Util.FileUtil;;;


public class Tagger {
	private CRF crf;
	
	String[] segmentStartTags ={"B-Entity"};
	String[] segmentContinueTags = {"I-Entity"};
	
	public Tagger() throws LoadModelException{
		
		this.crf = Model.loadAndRetrieveModel(getClass().getResourceAsStream("/models/model.crf.gz"),true);
	}
	public Tagger(String model_path) throws LoadModelException{
		this.crf = Model.loadAndRetrieveModel(model_path);
	}
	public Tagger(InputStream model_is) throws LoadModelException{
		this.crf = Model.loadAndRetrieveModel(model_is);
	}
	
	public List<Token> tag(String text){
		Pipe p = (Pipe) crf.getInputPipe();
		InstanceList allData = new InstanceList (p);
		StringBuilder sb = new StringBuilder();
		ITokenizer tokenizer = new OscarTokenizer();
		List<Token> tokens = tokenizer.tokenize(text);
		boolean first = true;
		for (Token t : tokens){
			if ( first){
				first = false;
			}
			else{
				if (t.IsNewSentence){
					sb.append("\n");
				}
			}
			sb.append(t.Text);
			sb.append("\n");
		}
		allData.addThruPipe (new LineGroupIterator (new StringReader( sb.toString()), Pattern.compile("^$"), true));
		TransducerEvaluator evaluator =
				 new TokenAccuracyEvaluator(allData, "testing");
		MultiSegmentationEvaluator mse = new MultiSegmentationEvaluator(allData, "test", new String[] {"B-Entity"}, new String[] {"I-Entity"});
		
		List<Token> chemElements = new ArrayList<Token>();
		for (int i =0 ; i < allData.size(); i++){
			chemElements.addAll(this.tagInstance(allData.get(i), tokens));
		}
		return chemElements;
		
		
	}
	
	private List<Token> tagInstance(Instance instance, List<Token> tokens){
		
		Sequence input = (Sequence) instance.getData();
		MaxLatticeDefault mld = new MaxLatticeDefault (crf,input);
		Sequence predOutput = mld.bestOutputSequence();
		//String[] words = (String[])instance.getSource();
		//String[] words = (String[]) tokens.toArray(new String[0]);
	    String[] types = new String[tokens.size()];      
	    for (int j = 0; j < predOutput.size(); j++) {
	        types[j] = (String)predOutput.get(j);
	        //System.out.println(String.format("%s\t%s\t%d\t%d",tokens.get(j) ,types[j], tokens.get(j).Begin, tokens.get(j).End ));
	    }
	    
	    List<Token> chemicalEntities = new ArrayList<Token>();
	    for (int j =0 ; j < tokens.size(); j++){
	    	boolean identifiedAsFakeFormula = false;
	    	for(int s = 0; s < segmentStartTags.length; s++) {
	    		if(types[j].equals(segmentStartTags[s])) {
	    			if ((j==0||tokens.get(j-1).Text.endsWith(".")) 
	    					&& (tokens.get(j).Text.equals("He")||tokens.get(j).Text.equals("At")
	    							||tokens.get(j).Text.equals("At")||tokens.get(j).Text.equals("As")
	    							||tokens.get(j).Text.equals("In")||tokens.get(j).Text.equals("I"))){
	    				System.out.println("~~fake formula (word):"+tokens.get(j).Text);
	    				//isFormula = false;
	    				identifiedAsFakeFormula = true;
	    			}

	    		
	    		Token element = new Token(tokens.get(j).Text, tokens.get(j).Begin, tokens.get(j).End);
	    		
	    		for(int s1 = 0; s1 < segmentContinueTags.length; s1++) {
	    			
	    			for(int j1 = j+1; j1 < types.length && types[j1].equals(segmentContinueTags[s1]) ; j1++) {
	    				//System.out.println(String.format("s1 %d, j1 %d, len types %d", s1, j1, types.length));
	    				
			    			element.End = tokens.get(j1).End;
			    			element.Text +=  " " + tokens.get(j1).Text;
			    			j=j1;
	    				

		    		}
	    		}
	    		chemicalEntities.add(element);
	    		}
	    	}

	    }
	    return chemicalEntities;
	}
	
	public static void printTokens(List<Token> tokens){
		for (Token e: tokens){
			System.out.println(e.toString());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test = "The essential oils obtained by hydrodistillation from the leaves of Tabernaemontana catharinensis had their composition analysed by GC-MS. A total of 18 substances were identified, consisting of a complex mixture of sesquiterpenes (83.52%), monoterpenes (5.46%) and triterpenes (4.56%). The main components in the oil were β-caryophyllene (56.87%), α-cadinol (12.52%), 8S,13-cedran-diol (5.41%), α-terpineol (3.99%), β-eudesmol (2.54%), caryophyllene oxide (2.51%) and ethyl iso-allocholate (2.03%) along with β-cubebene, γ-cadinene, cubenol, 1,8-cineol, o-cymene, curcumenol, spathulenol, friedeline and β-sitosterol as minor constituents. An antioxidant property was tested with the oil obtained by means of 1,1-diphenyl-2-picrylhydrazyl assay; the oil presented interesting radical scavenging activity. To the best of our knowledge, this is the first study of the composition and antioxidant activity of essential oil from the T. catharinensis collected from Brazil.";
		String model_path = "";
		String file_path = "";
		String USAGE = "java program <model_path> <file_path>";
		try{
			if (args.length < 2)
			{
				System.out.println(USAGE);
				Tagger tagger = new Tagger();
				List<Token> elements =  tagger.tag(test);
				printTokens(elements);
				
			}
			else
			{
				model_path = args[0];
				file_path = args[1];
				String content = FileUtil.readTextFile(file_path);
				Tagger tagger = new Tagger(model_path);
				List<Token> elements =  tagger.tag(content);
				printTokens(elements);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

}
