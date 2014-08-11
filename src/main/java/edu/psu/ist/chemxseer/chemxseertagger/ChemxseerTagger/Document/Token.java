package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document;

public class Token {
	public String Text;
	public int Begin;
	public int End;
	public boolean IsNewSentence ;
	
	public Token(){
		
	}
	public Token(String token_text, int offset, int end){
		this.Text = token_text;
		this.Begin = offset;
		this.End = end;
	}
	public String toString(){
		return String.format("%s\t%d\t%d", this.Text, this.Begin, this.End);
	}
	
}
