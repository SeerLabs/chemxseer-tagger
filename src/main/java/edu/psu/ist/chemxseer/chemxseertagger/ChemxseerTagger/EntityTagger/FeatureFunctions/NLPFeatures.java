package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;


import java.io.Serializable;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;



import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.PorterStemmer;
import dragon.nlp.tool.Tagger;
import dragon.nlp.tool.HeppleTagger;
import dragon.nlp.Sentence;
import dragon.nlp.Word;

public class NLPFeatures extends Pipe implements Serializable {
	
    private static final long serialVersionUID = 1L;

    private  transient Lemmatiser lemmatiser = null;
    private  transient Tagger posTagger = null;
    private static final String BASE_FOLDER="./nlpdata/";
    
    public NLPFeatures(){
    	// I hate this. This lousy design of dragon asks for folder path
    	// I should refactor thier code and get rid of this
    	System.out.println("loading lemmatiser");
    	this.lemmatiser = new dragon.nlp.tool.lemmatiser.EngLemmatiser(BASE_FOLDER+"lemmatiser",false, true);;
    	System.out.println("finished loading lemmatiser");
    	//this.posTagger = new HeppleTagger(BASE_FOLDER+"tagger");
    	System.out.println("finished loading pos tagger");
    }
    
    public Instance pipe(Instance carrier)
    {
    	TokenSequence ts = (TokenSequence) carrier.getData();
    	String[] sentence = new String[ts.size()];
    	for (int i = 0; i < ts.size(); i++) {
    		sentence[i] = ts.get(i).getText();
    	}
    	int[] pos = null;
    	if (posTagger != null)
        {
            pos = getPOS(sentence);
        }
    	for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			if (posTagger != null)
	        {
				t.setFeatureValue("POS=" + pos[i], 1);
	        }
			
			if (lemmatiser != null)
            {
                String lemma;
                if (pos == null)
                {
                    lemma = lemmatiser.lemmatize(t.getText());
                }
                else{
                	lemma = lemmatiser.lemmatize(t.getText(), pos[i]);
                }
                    
                t.setFeatureValue("LW=" + lemma, 1);
            }
			t.setFeatureValue("NC=" + getNumberClass(t.getText()), 1);
            t.setFeatureValue("BNC=" + getBriefNumberClass(t.getText()), 1);
            
            t.setFeatureValue("WC=" + getWordClass(t.getText()), 1);
            t.setFeatureValue("BWC=" + getBriefWordClass(t.getText()), 1);
            
			
    	}
    	return carrier;
    }
    
    private String getNumberClass(String text)
    {
        text = text.replaceAll("[0-9]", "0");
        return text;
    }


    private String getWordClass(String text)
    {
        text = text.replaceAll("[A-Z]", "A");
        text = text.replaceAll("[a-z]", "a");
        text = text.replaceAll("[0-9]", "0");
        text = text.replaceAll("[^A-Za-z0-9]", "x");
        return text;
    }


    private String getBriefNumberClass(String text)
    {
        text = text.replaceAll("[0-9]+", "0");
        return text;
    }


    private static String getBriefWordClass(String text)
    {
        text = text.replaceAll("[A-Z]+", "A");
        text = text.replaceAll("[a-z]+", "a");
        text = text.replaceAll("[0-9]+", "0");
        text = text.replaceAll("[^A-Za-z0-9]+", "x");
        return text;
    }


    private int[] getPOS(String[] tokens)
    {
        String[] rawTokens = new String[tokens.length];
        Word[] words = new Word[tokens.length];
        Sentence sentence = new Sentence();
        for (int i = 0; i < tokens.length; i++)
        {
            words[i] = new Word(rawTokens[i]);
            sentence.addWord(words[i]);
        }
        posTagger.tag(sentence);
        int[] pos = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++)
        {
            pos[i] = words[i].getPOSIndex();
        }
        return pos;
    }
    
    
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Lemmatiser lem = new dragon.nlp.tool.lemmatiser.EngLemmatiser("./nlpdata/lemmatiser",false, true);
		System.out.println( lem.lemmatize("had"));
		PorterStemmer stemmer = new PorterStemmer();
		System.out.println( stemmer.stem("had"));
		
	}
	
	

}
