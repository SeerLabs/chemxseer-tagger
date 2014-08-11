package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.shingle.*; 

import java.util.*;
import java.io.*;

public class LuceneTokenizer {

	static Logger logger = Logger.getLogger(LuceneTokenizer.class.getName());
	
	public static String[] tokenizeNoStopWords(String string){
		List<String> stopwords = new ArrayList<String>();
		CharArraySet cas = new CharArraySet(Version.LUCENE_40, stopwords, true);
		return tokenizeString(new StandardAnalyzer(Version.LUCENE_40,cas), string);
	}
	
	public static String[] tokenize(String string)
	{
		return tokenizeString(new StandardAnalyzer(Version.LUCENE_40), string);
	}
	
	public static String[] tokenizeNgram(String string, int n)
	{
		if (n == 1)
			return tokenize(string);
		else {
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
			ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, n);
			return tokenizeString(shingleAnalyzer, string);
		}
	}
	
	private static String[] tokenizeString(Analyzer analyzer, String string) {
	    List<String> result = new ArrayList<String>();
	    try {
	      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
	      while (stream.incrementToken()) {
	        result.add(stream.getAttribute(CharTermAttribute.class).toString());
	        
	      }
	    } catch (IOException e) {
	      // not thrown b/c we're using a string reader...
	      throw new RuntimeException(e);
	    }
	    //return result;
	    return (String[]) result.toArray(new String[0]);
	  }
	
	public static String tokensToSentence(String[] tokens){
		StringBuilder sb = new StringBuilder();
		for (String s : tokens){
			sb.append(s);
			sb.append(" ");
		}
		if (sb.length() > 0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}

