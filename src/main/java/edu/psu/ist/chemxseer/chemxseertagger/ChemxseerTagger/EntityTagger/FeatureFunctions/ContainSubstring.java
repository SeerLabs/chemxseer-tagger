package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Serializable;

import java.util.*;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;
//import edu.psu.chemxseer.chemical.taggers.Constants;
//import gnu.trove.THashSet;

/*
 * This class is to test the prefix and the suffix of formula or name
 */
public class ContainSubstring extends Pipe implements Serializable {
	
	public enum SubstringMatchingDirection {
		Prefix, Suffix, Anywhere
	}
	
    String name;
    //int posFlag;//-1=beginning,0=anywhere, 1=end
    SubstringMatchingDirection direction;
    //int freq;
    HashSet<String> lex;
    boolean ignoreCase;

   /* public ContainSubstring(String name, File lexiconFile,
			boolean ignoreCase, int p, int f) throws FileNotFoundException {
            this(name, new BufferedReader(new FileReader(lexiconFile)), ignoreCase, p,f);
    }*/
    //for long formula, maybe it is more possible to be a formula
    public ContainSubstring(String name, Reader lexiconReader, boolean ignoreCase, SubstringMatchingDirection direction) {
        this.name = name;
        this.direction = direction;
        this.ignoreCase = ignoreCase;
        this.lex = new HashSet<String>();
        LineNumberReader reader = new LineNumberReader(lexiconReader);
        String line;
        while (true) {
                try {
                        line = reader.readLine();//bsun read a line
                } catch (IOException e) {
                        throw new IllegalStateException();
                }
                if (line == null) {
                        break;
                } else {
                    if (ignoreCase) line=line.toLowerCase();
                    lex.add(line.intern());
                }
        }
    }

    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();
        String subString;
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            String w = t.getText();
            if (ignoreCase) w=w.toLowerCase();
            if (this.direction==SubstringMatchingDirection.Anywhere){
                for (int start=0;start<w.length();start++){
                    for (int end=start+1;end<=w.length();end++){
                        subString = w.substring(start,end);
                        if (this.lex.contains(subString)){
                        	t.setFeatureValue(this.name, 1.0);
                            break;
                        }
                    }
//                    if (count>0) break;
                }
            } else if (this.direction==SubstringMatchingDirection.Suffix){//suffix
                for (int start=w.length()-1;start>=0;start--){
                    subString = w.substring(start,w.length());
                    if (this.lex.contains(subString)){
                    	t.setFeatureValue(this.name, 1.0);
                        break;
                    }
                }
            } else if (this.direction==SubstringMatchingDirection.Prefix){//prefix
                for (int end=1;end<=w.length();end++){
                    subString = w.substring(0,end);
                    if (this.lex.contains(subString)){
                        t.setFeatureValue(this.name, 1.0);
                        break;
                    }
                }
            }

        }
        return carrier;
    }
    
    // Serialization

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(name);
        out.writeObject(lex);
        out.writeBoolean(ignoreCase);
        out.writeObject(direction);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        this.name = (String) in.readObject();
        this.lex = (HashSet<String>) in.readObject();
        this.ignoreCase = in.readBoolean();
        this.direction = (SubstringMatchingDirection) in.readObject();
    }

}
