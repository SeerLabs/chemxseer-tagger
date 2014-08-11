package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers;

import java.util.*;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.Token;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class OscarTokenizer implements ITokenizer {


	
	public List<Token> tokenize(String text){
		List<Token> tokens = new ArrayList<Token>();
		ITokeniser tokeniser = Tokeniser.getDefaultInstance();
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
                .makeTokenisedDocument(tokeniser, text);
		List<TokenSequence> oscarTokens = procDoc.getTokenSequences();
		for (TokenSequence ts : oscarTokens){
			for (uk.ac.cam.ch.wwmm.oscar.document.Token t : ts.getTokens()){
				tokens.add(new Token (t.getSurface(), t.getStart(), t.getEnd()));
			}
		}
		
		return tokens;
	}
	
	public static void main( String[] args ){
		String sentence = "  First sentence. Second sentence. ";
		ITokenizer st = new OscarTokenizer();
		List<Token> sentences = st.tokenize(sentence);
		for (Token t : sentences)
			System.out.println(t.toString());
		System.out.println(sentence.length());
	}

}
