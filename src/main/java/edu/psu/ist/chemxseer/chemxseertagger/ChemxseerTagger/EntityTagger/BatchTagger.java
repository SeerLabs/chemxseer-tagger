package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger;

import java.io.*;
import java.util.*;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Document.Token;
import  edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Util.*;

public class BatchTagger {

	private Tagger tagger;

	public static void main(String[] args) {
		String usage = "BatchTagger <input file|folder> <output file|folder> |model|";
		if (args.length < 2) {
			System.out.println(usage);
			System.exit(0);
		} else {
			String inpath = args[0];
			String outpath = args[1];
			String model_path = "";
			if (args.length == 3) {
				model_path = args[2];
			}
			Vector docs = new Vector();
			try {
				getDocs(new File(inpath), docs);
				System.out.println("file#=" + docs.size());
				Tagger tagger = null;
				if (model_path.length() > 0){
					tagger = new Tagger(model_path);
				}
				else
				{
					tagger = new Tagger();
				}

				for (int n = 0; n < docs.size(); n++) {
					File source = (File) docs.get(n);
					if (!source.exists() || !source.canRead()) {
						System.err.println("Can't read source information: "
								+ source.getAbsolutePath());
						continue;
					}

					String tmpPath = replaceAll(source.getPath(),"\\","/");
					String outPath = replaceAll(tmpPath,inpath,outpath);
					File output = new File(outPath);
					if(output.exists()) {
						System.err.println("exist!="+outPath);
						continue;
					}

					System.out.println("[Tagging] "+source.getPath()+"->"+outPath);

					String outDir = outPath.substring(0,outPath.lastIndexOf("/"));
					File outFile = new File(outDir);

					if(!outFile.exists()) {
						outFile.mkdirs();
					}

					String content = FileUtil.readTextFile(source.getAbsolutePath());
					if(content.length()>0){
						List<Token> tokens = tagger.tag(content);
						writeTags(tokens, outPath);
					}


				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	static void getDocs(File file, Vector docs) throws IOException {
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						getDocs(new File(file, files[i]), docs);
					}
				}
			} else {
				docs.add(file);
			}
		}
	}

	public static String replaceAll(String text, String st1, String st2) {
		// return replaceAll(text,text.length(),st1,st1.length(),st2);
		StringBuffer newText = new StringBuffer();
		int i, length = text.length(), length1 = st1.length();
		int len = length - length1 + 1;
		for (i = 0; i < len; i++) {
			if (text.substring(i, i + length1).compareTo(st1) == 0) {
				newText.append(st2);
				i += length1 - 1;
			} else
				newText.append(text.substring(i, i + 1));
		}
		newText.append(text.substring(i, length));
		return newText.toString();
	}

	public static void writeTags(List<Token> tags, String out_path){
		try{
			BufferedWriter outTEXT = new BufferedWriter(new FileWriter(out_path));
			for (Token t: tags){
				outTEXT.write(String.format("%s\t%d\t%d\n", t.Text,t.Begin,t.End));
			}
			outTEXT.close();

		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}
