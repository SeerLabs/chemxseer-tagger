package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.Util;

import java.io.*;

public class FileUtil {
	public static String readTextFile(String path) throws IOException{
	      FileReader in = new FileReader(path);
	      StringBuilder contents = new StringBuilder();
	      char[] buffer = new char[4096];
	      int read = 0;
	      do {
	          contents.append(buffer, 0, read);
	          read = in.read(buffer);
	      } while (read >= 0);
	      return contents.toString();
	}

}
