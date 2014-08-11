//inheritred as is from bsun. only modified to be compatible with mallet 2
package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;

import java.io.*;

import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.Constants;

public class PartialStringPattern extends Pipe implements Serializable {
    String name;
    Pattern regex;
//    int number;//frequency

public PartialStringPattern(String name, Pattern regex) {
        this.name = name;
        this.regex = regex;
//        this.number = n;
    }

    public Instance pipe(Instance carrier) {
        TokenSequence ts = (TokenSequence) carrier.getData();
//        System.out.println("******total size of tokens="+ts.size());
        String subString;
        int count;
        for (int i = 0; i < ts.size(); i++) {
//            System.out.println(""+i+"<<<"+ts.size()+"");\
            count = 0;
            Token t = ts.get(i);
            String w = t.getText();
            //will not count redundant times, should only count the longest or the shortest?
//            System.out.println("*");
            for (int start=0;start<w.length();start++){
                for (int end=start+1;end<=w.length();end++){
//                    System.out.println("*"+w+":"+start+","+end);
                    subString = w.substring(start,end);
//                    System.out.println("~"+subString+":"+start+","+end);
                    if (regex.matcher(subString).matches()){
//                        System.out.println("*PSPattern:"+subString+"="+regex.toString());
                        count++;
                        break;
                    }
                }
                if (count>0) break;
            }
            if (count>0){
                t.setFeatureValue(name, 1.0);
//                System.out.println("*PSPattern:"+w+"="+regex.toString());
            }
        }
        return carrier;
    }

    // Serialization

    private static final long serialVersionUID = Constants.SVUID_PARTIAL_STRING_PATTERN;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
//        System.out.println("Writing PartialStringPattern>>>");
        out.writeInt(CURRENT_SERIAL_VERSION);
        out.writeObject(name);
        out.writeObject(regex);
//        out.writeInt(number);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        System.out.println("Reading PartialStringPattern>>>");
        int version = in.readInt();
        this.name = (String) in.readObject();
        this.regex = (Pattern) in.readObject();
//        this.number = in.readInt();
    }

}
