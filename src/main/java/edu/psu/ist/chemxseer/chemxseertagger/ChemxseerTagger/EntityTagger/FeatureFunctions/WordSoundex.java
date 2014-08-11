package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;

import java.io.Serializable;

import org.apache.commons.codec.language.DoubleMetaphone;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

public class WordSoundex extends Pipe implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int MaxCodeLength = 5;
	private String FeatureName ;
	
	public WordSoundex(String featureName){
		this.FeatureName = featureName;
	}
	public WordSoundex(String featureName, int maxCodeLength){
		this.MaxCodeLength = maxCodeLength;
		this.FeatureName = featureName;
	}
	
	public Instance pipe(Instance carrier) {
		/*
		 * very important note:
		 * mallet expects binary features,(indicator)
		 * since we have multiple word classes, we assing each 
		 * class a feature, hence class 5 becomes FeatureName+5
		 */
		TokenSequence ts = (TokenSequence) carrier.getData();
		DoubleMetaphone dm = new DoubleMetaphone();
		dm.setMaxCodeLen(this.MaxCodeLength);
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			String w = t.getText();
			String code = dm.doubleMetaphone(w);
			t.setFeatureValue(String.format("%s%s", this.FeatureName,code), 1.0 );
		}
		return carrier;
		
	}

}
