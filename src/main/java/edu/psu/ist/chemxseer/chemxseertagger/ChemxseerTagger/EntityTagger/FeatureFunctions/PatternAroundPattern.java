/*
 * originally from bsun code. now modified to a work with mallet 2.0.x
 */

package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;


import java.io.*;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.*;

public class PatternAroundPattern extends Pipe implements Serializable {
    String name;
    Pattern myRegex;
    Pattern otherRegex;
    int offset;
    boolean matchOutside;//if offset is outside of the sequence, then match otherRegex or not

public PatternAroundPattern(String n, Pattern mine, Pattern other, int o, boolean b) {
        this.name = n;
        this.myRegex = mine;
        this.otherRegex = other;
        this.offset = o;
        this.matchOutside = b;
    }

    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();
        //String[] POSs = (String[]) carrier.getSource();
        //System.out.println("******total size of tokens="+ts.size()+",POSs length="+POSs.length);
        boolean tmpMatch = false;
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            String w = t.getText();
            String w2 = new String();
            tmpMatch = false;
            if (i+offset>=0 && i+offset<ts.size()){
                w2 = ts.get(i+offset).getText();
                if (otherRegex.matcher(w2).matches()){
                    //System.out.println("---"+w2);
                    tmpMatch = true;
                }
            } else {
                tmpMatch = this.matchOutside;
            }
            if (myRegex.matcher(w).matches() && tmpMatch){
                //System.out.println("*"+w2+" "+w);
                t.setFeatureValue(name, 1.0);
            } 
            //else System.out.println("~"+w2+" "+w);
        }
        return carrier;
    }

    // Serialization

    private static final long serialVersionUID = Constants.SVUID_PATTERN_AROUND_PATTERN;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(name);
        out.writeObject(myRegex);
        out.writeObject(otherRegex);
        out.writeInt(offset);
        out.writeBoolean(matchOutside);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        this.name = (String) in.readObject();
        this.myRegex = (Pattern) in.readObject();
        this.otherRegex = (Pattern) in.readObject();
        this.offset = in.readInt();
        this.matchOutside = in.readBoolean();
    }

}
