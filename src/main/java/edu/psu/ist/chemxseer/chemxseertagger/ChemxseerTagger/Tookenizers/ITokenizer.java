package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers;

import java.util.List;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.Token;

public interface ITokenizer {
	
	public List<Token> tokenize(String text);

}
