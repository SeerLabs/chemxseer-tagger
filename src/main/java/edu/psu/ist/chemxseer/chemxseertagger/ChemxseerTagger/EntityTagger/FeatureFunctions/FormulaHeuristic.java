package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions;



import java.io.Serializable;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

public class FormulaHeuristic extends Pipe implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String FeatureName ;
	
	public FormulaHeuristic(String featureName){
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
		for (int i = 0; i < ts.size(); i++) {
			Token t = ts.get(i);
			String w = t.getText();
			if (isFormula(w)){
				t.setFeatureValue(this.FeatureName, 1.0 );
			}
			
		}
		return carrier;
		
	}
	public static boolean isFormula(String word){
		char[] chars = word.toCharArray();
		boolean previous_letter_is_lower = false;
		for (char c : chars){
			if (Character.isLowerCase(c)){
				if (previous_letter_is_lower){
					return false;
				}
				else{
					previous_letter_is_lower = true;
				}
			}
			else{
				previous_letter_is_lower = false;
			}
				
		}
		return true;
	}

}
