package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers;

import java.io.*;
import java.util.*;

import opennlp.tools.sentdetect.*;
import opennlp.tools.util.Span;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.*;

public class SentenceTokenizer implements ITokenizer {

	public List<Token> tokenize(String text){
		
		SentenceDetectorME sentenceDetector;
		List<Token> tokens = new ArrayList<Token>();
		try {
			sentenceDetector = new SentenceDetectorME(this.loadSentenceDetectorModel());
			Span sentences[] = sentenceDetector.sentPosDetect(text);
			for (Span s: sentences){
				tokens.add(new Token(text.substring (s.getStart(), s.getEnd()),s.getStart(),s.getEnd()) );
				
			}
			return tokens;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private SentenceModel loadSentenceDetectorModel() throws IOException{
		// FIX ME TODO
		//InputStream modelIn = new FileInputStream(new File("/Users/madian/code/chemxseer-tagger/ChemxseerTagger/src/resources/test.bin"));
		InputStream modelIn = getClass().getResourceAsStream("/en-sent.bin");
		if (modelIn == null){
			System.out.println("resource is null");
		}
		/*if (modelIn2 == null){
			System.out.println("resource2 is null");
		}*/
		SentenceModel model = new SentenceModel(modelIn);
		return model;
	}
	
	public static void main( String[] args ){
		String sentence = "  First sentence. Second sentence. ";
		SentenceTokenizer st = new SentenceTokenizer();
		List<Token> sentences = st.tokenize(sentence);
		for (Token t : sentences)
			System.out.println(t.toString());
		System.out.println(sentence.length());
	}
	
}
