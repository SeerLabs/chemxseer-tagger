package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Tookenizers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import bsh.util.Util;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Util.*;
import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.Token;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.Token;

public class SentenceAndWordTokenizer implements ITokenizer {

	public List<Token> tokenize(String text) {
		SentenceTokenizer sTokenizer = new SentenceTokenizer();
		OscarTokenizer oTokenizer = new OscarTokenizer();
		List<Token> tokens = new ArrayList<Token>();
		List<Token> sentences = sTokenizer.tokenize(text);
		for (Token sentence : sentences){
			List<Token> words = oTokenizer.tokenize(sentence.Text);
			if (words.size() > 0)
				words.get(0).IsNewSentence = true;
			for (Token word : words){
				word.Begin = sentence.Begin + word.Begin;
				word.End = sentence.Begin + word.End;
				tokens.add(word);
			}
		}
		return tokens;
	}

	public void writeTokens(List<Token> tokens, String out_path){
		File file = new File(out_path);
		Writer output =  null; 
		try{
			output = new BufferedWriter(new FileWriter(file));
			if (tokens.size() > 0){
				output.write(String.format("%s\t%d\t%d\n", tokens.get(0).Text, 
						tokens.get(0).Begin, tokens.get(0).End));
			}
			if (tokens.size() > 1)
			{
				for (Token t : tokens.subList(1, tokens.size() -1)){
					if (t.IsNewSentence){
						output.write("\n");
					}
					output.write(String.format("%s\t%d\t%d\n", t.Text, t.Begin, t.End));
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public List<Token> tokenizeFile (String path) throws IOException{
		String content = FileUtil.readTextFile(path);
		return this.tokenize(content);
	}

	public static void main( String[] args ){

		String usage = "java tokenizer <file|folder-to-tokenize> <output-file|folder>";
		if (args.length <2)
		{
			System.out.println(usage);
			//System.exit(0);
			String sentence = "  First sentence. Second sentence. ";
			ITokenizer st = new SentenceAndWordTokenizer();
			List<Token> sentences = st.tokenize(sentence);
			if (sentences.size() > 0){
				System.out.println(sentences.get(0).toString());
			}
			if (sentences.size() > 1)
			{
				for (Token t : sentences.subList(1, sentences.size() -1)){
					if (t.IsNewSentence){
						System.out.println();
					}
					System.out.println(t.toString());
				}
			}
		}
		else
		{
			try{
				File in = new File(args[0]);
				File out = new File(args[1]);
				SentenceAndWordTokenizer tokenizer = new SentenceAndWordTokenizer();
				if (in.isFile()){
					List<Token> tokens = tokenizer.tokenizeFile(in.getAbsolutePath());
					tokenizer.writeTokens(tokens, out.getAbsolutePath());
				}
				else{
					// it's a directory
					String[] files = in.list();
					System.out.println(String.format("%d files are in directory %s",files.length, in.getAbsoluteFile()));
					String outfolder = args[1];
					if (!outfolder.endsWith("/"))
						outfolder += "/";
					for (int i =0; i < files.length; i++){
						File subfile = new File(in.getAbsolutePath() + "/"+ files[i]);
						if (subfile.isFile()){
							List<Token> tokens = tokenizer.tokenizeFile(subfile.getAbsolutePath());
							String out_path = outfolder+ subfile.getAbsolutePath().substring(subfile.getAbsolutePath().lastIndexOf("/")+1);
							tokenizer.writeTokens(tokens, out_path);
						}
					}

				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}




	}

}
