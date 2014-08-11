package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;

import java.io.Serializable;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

public class SentenceBeginning extends Pipe implements Serializable{
	

	private static final long serialVersionUID = 1L;
	private String FeatureName;
	public SentenceBeginning(String featureName){
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
		if (ts.size() > 0){
			Token t = ts.get(0);
			t.setFeatureValue(this.FeatureName, 1.0 );
		}
		return carrier;
		
	}

}
