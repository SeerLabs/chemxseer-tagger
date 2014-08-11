package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger;


import java.util.regex.*;
import java.util.Date;
import java.io.*;
import java.util.*;
import java.lang.Double;

import cc.mallet.fst.*;
import cc.mallet.optimize.Optimizable;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;
import cc.mallet.pipe.tsf.*;

import org.apache.log4j.*;

import edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger.EntityTagger.FeatureFunctions.*;





public class Trainer {
	
	static Logger  logger = Logger.getLogger(Trainer.class);
	
	int numEvaluations = 0;
    static int iterationsBetweenEvals = 16;

    private static String CAPS = "[A-Z]";
    private static String LOW = "[a-z]";
    private static String CAPSNUM = "[A-Z0-9]";
    private static String ALPHA = "[A-Za-z]";
    private static String ALPHANUM = "[A-Za-z0-9]";
    private static String PUNT = "[,;\\:?!()]";
    private static String PUNT2 = "\\.";
    private static String QUOTE = "[\"`']";
    
    private static String DIG_GREEK = "[0-9\u03b1-\u03c9\u0391-\u03a9'\"]";
    private static String DIG_GREEK_LTR = "[A-Za-z0-9\u03b1-\u03c9\u0391-\u03a9'\"]";
	
	private  CRF train(Reader reader){
		
		try {
			Date s1 = new Date();
			Pipe p = new SerialPipes (this.generatePipes() );
			InstanceList allData = new InstanceList (p);
			//TODO make reader = new FileReader (new File (args[1])) in main
			allData.addThruPipe (new LineGroupIterator (reader, Pattern.compile("^$"), true));
			logger.info("Number of predicates in training data: "+p.getDataAlphabet().size());
			Date s2 = new Date();
			logger.info("feature: "+(s2.getTime()-s1.getTime())+" milliseconds");
			Alphabet data = p.getDataAlphabet();
			Alphabet targets = p.getTargetAlphabet();
			logger.info("Number of features = "+p.getDataAlphabet().size());
			CRF crf = new CRF(allData.getDataAlphabet(),
					allData.getTargetAlphabet() );
			crf = new CRF(p,null);
			// Change below if you wanna change the connectnedness 
			crf.addFullyConnectedStatesForLabels(); 
			crf.setWeightsDimensionAsIn(allData, false);
			CRFOptimizableByLabelLikelihood optLabel =
				       new CRFOptimizableByLabelLikelihood(crf, allData);
			Optimizable.ByGradientValue[] opts =
				       new Optimizable.ByGradientValue[]{optLabel};
			
			//CRFTrainerByL1LabelLikelihood crfL1Trainer = new CRFTrainerByL1LabelLikelihood(crf);
			
			CRFTrainerByValueGradients crfTrainer =
				       new CRFTrainerByValueGradients(crf, opts);
			String[] labels = new String[]{"B-Entity", "I-Entity"};
			// TODO change this to my own segmentation evaluator
			TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
				       new InstanceList[]{allData, null},
				       new String[]{"train", "test"}, labels, labels) {
			     @Override
			     public boolean precondition(TransducerTrainer tt) {
			       // evaluate model every 5 training iterations
			       // as found on mallet demo page
			       return tt.getIteration() % 5 == 0;
			     }
			};
			crfTrainer.addEvaluator(evaluator);
			//crfL1Trainer.setMaxResets(0);
			crfTrainer.train(allData, Integer.MAX_VALUE);
			evaluator.evaluate(crfTrainer);
			Date e2 = new Date();
			logger.info("train: "+(e2.getTime()-s2.getTime())+" milliseconds");
			return crf;
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private Pipe[] generatePipes() throws Exception{
		Pipe[] featurePipes = new Pipe[] {
				new SimpleTaggerSentence2TokenSequence(),
				new RegexMatches ("INITCAP", Pattern.compile (CAPS+".*")),
		  		new RegexMatches ("CAPITALIZED", Pattern.compile (CAPS+LOW+"*")),
		  		new RegexMatches ("ALLCAPS", Pattern.compile (CAPS+"+")),
		        new RegexMatches ("ALLCAPS2", Pattern.compile (CAPS+CAPS+"+")),
		        new RegexMatches ("ONECAP", Pattern.compile (CAPS)),
		  		new RegexMatches ("MIXEDCAPS", Pattern.compile ("[A-Z][a-z]+[A-Z][A-Za-z]*")),
		  		new RegexMatches ("CONTAINSDIGITS", Pattern.compile (".*[0-9].*")),
		  		new RegexMatches ("SINGLEDIGITS", Pattern.compile ("[0-9]")),
		  		new RegexMatches ("DOUBLEDIGITS", Pattern.compile ("[0-9][0-9]")),
		  		new RegexMatches ("ALLDIGITS", Pattern.compile ("[0-9]+")),
		  		new RegexMatches ("NUMERICAL", Pattern.compile ("[-0-9]+[\\.,]+[0-9\\.,]+")),
		  		new RegexMatches ("ALPHNUMERIC", Pattern.compile ("[A-Za-z0-9]+")),
		  		new RegexMatches ("ROMAN", Pattern.compile ("[ivxdlcm]+|[IVXDLCM]+")),
		  		new RegexMatches ("MULTIDOTS", Pattern.compile ("\\.\\.+")),
		  		new RegexMatches ("ENDSINDOT", Pattern.compile ("[^\\.]+.*\\.")),
		  		// changing it new RegexMatches ("CONTAINSDASH", Pattern.compile (ALPHANUM+"+-"+ALPHANUM+"*")),
		  		new RegexMatches ("CONTAINSDASH", Pattern.compile (".+-.+")),
		  		new RegexMatches ("ENDS_IN_SIGN", Pattern.compile (".+[+-]")),
		  		new RegexMatches ("ENDS_IN_DIGIT", Pattern.compile (".*" + ALPHANUM+"+[0-9]+")),
		  		new RegexMatches ("CONTAINS_SIGN", Pattern.compile (".+[+-].+")),
		  		new RegexMatches ("DIGIT_COMMA", Pattern.compile (".*[0-9]+,.+")),
		  		new RegexMatches ("ACID", Pattern.compile ("acid")),
		  		new RegexMatches ("CARBON", Pattern.compile ("C")),
		  		new RegexMatches ("ACRO", Pattern.compile ("[A-Z][A-Z\\.]*\\.[A-Z\\.]*")),
		  		new RegexMatches ("LONELYINITIAL", Pattern.compile (CAPS+"\\.")),
		  		new RegexMatches ("SINGLECHAR", Pattern.compile (ALPHA)),
		  		new RegexMatches ("CAPLETTER", Pattern.compile ("[A-Z]")),
		  		new RegexMatches ("PUNC", Pattern.compile (PUNT)),
		  		new RegexMatches ("PUNC2", Pattern.compile (PUNT2)),
		  		new RegexMatches ("QUOTE", Pattern.compile (QUOTE)),
		  		new PartialStringPattern ("BRACKET1_1",Pattern.compile ("("+DIG_GREEK_LTR+"+)-")),
                new PartialStringPattern ("BRACKET2_1",Pattern.compile ("\\["+DIG_GREEK_LTR+"+\\]-")),

		  		//new PatternAroundPattern ("1ST_OF_SENT", Pattern.compile("[A-Z][a-z]?"), Pattern.compile(".*\\."), -1, true),
		  		/*
		  		 * removing all these features that rely on formulaAnalyzer
		  		 * should replace them with lexico
		  		 * TODO replace with lexicon, the formula lookup
		  		new FormulaStringPattern ("FORM"),//string pattern match
		  		new FormulaHasSuperscript ("FORM_SUP"),//mass number +  charge
		  		new FormulaHasLowerCase ("FORM_LOWCASE"),
		  		new FormulaLong ("LONG2",2),//consider removing these, or use the name features too
		  		new FormulaLong ("LONG3",3),
		  		new FormulaLong ("LONG4",4),
		  		new FormulaLong ("LONG6",6),*/
		  		//check all pairs
		  		/* removing this. I don't feel it's important to import
		  		new FormulaCooccur("CO-CH","C","H"),
		  		new FormulaCooccur("CO-CO","C","O"),
		  		new FormulaCooccur("CO-CN","C","N"),
		  		new FormulaCooccur("CO-CS","C","S"),
		  		new FormulaCooccur("CO-CI","C","I"),

		  		new FormulaCooccur("CO-HO","H","O"),
		  		new FormulaCooccur("CO-HN","H","N"),
		  		new FormulaCooccur("CO-HS","H","S"),
		  		new FormulaCooccur("CO-HI","H","I"),

		  		new FormulaCooccur("CO-ON","O","N"),
		  		new FormulaCooccur("CO-OS","O","S"),
		  		new FormulaCooccur("CO-OI","O","I"),

		  		new FormulaCooccur("CO-NS","N","S"),
		  		new FormulaCooccur("CO-NI","N","I"),*/
		  		 
		  		new TrieLexiconMembership("ABBV",new InputStreamReader(getClass().getResourceAsStream("/data/training/formula/lists/abbreviation.txt") ),false),
		  		// TODO re add removing these two because we end missing a lot of them
		  		//new TrieLexiconMembership("AMB_WORDS",new InputStreamReader(getClass().getResourceAsStream("/data/training/formula/lists/ambiguious_words.txt")),false),
		  		//new TrieLexiconMembership("AMB_NAMES",new InputStreamReader(getClass().getResourceAsStream("/data/training/formula/lists/ambiguious_names.txt")),false),
		  		
		  		// removing jochim because it is not necessary, we use the unigram jochem instead
		  		//new TrieLexiconMembership("JOCHIM",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/JochemV1_1.list")),true),
		  		new TrieLexiconMembership("JOCHIM_CAS",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/Jochem_CASV1_1.list")),true),
		  		new TrieLexiconMembership("JOCHIM_CHID",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/Jochem_CHIDV1_1.list")),true),
		  		new TrieLexiconMembership("JOCHIM_TOKEN",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/Jochem_unique_tokens.list")),true),
		  		// boosted terms are terms that get missed often although they are obvious
		  		new TrieLexiconMembership("BOOSTED_TERM",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/boostedterms.txt")),true),
		  		new TrieLexiconMembership("AMINOACID",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/amino-acids.txt")),true),
		  		new TrieLexiconMembership("AMINOACID_ABBREV",new InputStreamReader(getClass().getResourceAsStream("/dictionaries/amino-acids-abbrev.txt")),false),
		  		
		  		new WordCluster("WORD2VEC="), 
		  		new NLPFeatures(),
		  		new WordSoundex("SNDX=",7),
		  		new FormulaHeuristic("FORMULA_HEUR"),
		  		new SentenceBeginning("BEGIN_SENTENCE"),
		  		new ContainSubstring("PREFIX_MATCH", new InputStreamReader( getClass().getResourceAsStream("/data/training/name/prefix.txt")),true, ContainSubstring.SubstringMatchingDirection.Prefix ),
		  		new ContainSubstring("SUFFIX_MATCH", new InputStreamReader( getClass().getResourceAsStream("/data/training/name/suffix.txt")),true, ContainSubstring.SubstringMatchingDirection.Suffix ),
		  		//TODO I removed the small letter normaliation from here
		  		new TokenText ("WORD="),
		  		new TokenTextCharNGrams ("CHARNGRAM=", new int[] {2,3,4}),
		  		new TrieLexiconMembership("SIGNAL",new InputStreamReader(getClass().getResourceAsStream("/data/training/formula/lists/signal.txt")),true),
		  		//using names stuff only for these two
		  		new TrieLexiconMembership("EleNames",new InputStreamReader(getClass().getResourceAsStream("/data/training/formula/lists/chem_element_names.lst")),true),
		  		new TrieLexiconMembership("EleSymbols",new InputStreamReader(getClass().getResourceAsStream("/data/training/formula/lists/chem_element_symbols.lst")),false),
		  		//new StemTrieLexiconMembership("ChemNames",new File("../data/training/name/chemNames.lst"),true),

		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("IS.*|WORD=.*|[A-Z]+"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("ABBV"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("AMB_WORDS"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("AMB_NAMES"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("PUNT"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("PUNT2"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("ALLCAPS2"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("ONECAP"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("CAPITALIZED"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("JOCHIM"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("WORD2VEC"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("JOCHIM_TOKEN"),true),
		  		//TODO fix after importing correct formula new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("FORM"),true),
		  		// TODO fix after importing correct formula new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("FORM_SUP"),true),
		  		new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("SIGNAL"),true),
		  		//new FeaturesInWindow("WINDOW=",-2,2,Pattern.compile("ChemNames"),true),
		  		
		  		//now name features
		  		 new PartialStringPattern ("DIG_PUNC1_1",Pattern.compile (DIG_GREEK+","+DIG_GREEK)),
		         new PartialStringPattern ("DIG_PUNC2_1",Pattern.compile (","+DIG_GREEK)),
		         new PartialStringPattern ("DIG_PUNC3_1",Pattern.compile (DIG_GREEK+",")),
		         new PartialStringPattern ("DIG_PUNC4_1",Pattern.compile (DIG_GREEK+"-"+DIG_GREEK)),
		         new PartialStringPattern ("DIG_PUNC5_1",Pattern.compile (DIG_GREEK+"-")),
		         new PartialStringPattern ("DIG_PUNC6_1",Pattern.compile ("-"+DIG_GREEK)),
		         new PartialStringPattern ("LETTER_PUNC2_1",Pattern.compile ("-[A-Za-z]")),
		         new PartialStringPattern ("BRACKET1_1",Pattern.compile ("("+DIG_GREEK_LTR+"+)-")),
		         new PartialStringPattern ("BRACKET2_1",Pattern.compile ("\\["+DIG_GREEK_LTR+"+\\]-")),
		         new PartialStringPattern ("AND1_1",Pattern.compile (DIG_GREEK+"\\&"+DIG_GREEK)),
		         new PartialStringPattern ("AND2_1",Pattern.compile (DIG_GREEK+"+"+DIG_GREEK)),
		         new PartialStringPattern ("PRIME1_1",Pattern.compile (DIG_GREEK+"'")),
		         new PartialStringPattern ("PRIME2_1",Pattern.compile (DIG_GREEK+"\"")),
		         
		         /*
		          * this need to be commendted out. horrible dictionary design
		          * it hard codes the path to a lucene dictionary. absolutely unacceptable
		          * will replace it with joachim trielexicon
		         new ChemDicMatch("ChemDic2-",2,"indexName1","contents","indexName2","contents",0.0f),//from 0.0f-1.0f
		         */
		         // TODO re add me here new ContainSubstring ("end",new File("../data/training/name/end.txt"),true,1,1),
		         new TokenSequenceLowercase(),
		         new TokenText ("WORD_LOWER="),
		         new TokenTextCharNGrams ("CHARNGRAM_LOWER=", new int[] {2,3,4}),
		         new FeaturesInWindow("WINDOW=",-2,2,Pattern.compile("EleNames"),true),
		         new FeaturesInWindow("WINDOW=",0,1,Pattern.compile("BRACKET1_1"),true),
		         new FeaturesInWindow("WINDOW=",0,1,Pattern.compile("BRACKET1_2"),true),
		         new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("PREFIX_MATCH"),true),
		         //new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("infix"),true),
		         new FeaturesInWindow("WINDOW=",-1,1,Pattern.compile("SUFFIX_MATCH"),true),
		         new FeaturesInWindow("WINDOW=",0,1,Pattern.compile("INSIDE_SIGNAL"),true),
		         new FeaturesInWindow("WINDOW=",0,1,Pattern.compile("AFTER_SIGNAL"),true),
		         new FeaturesInWindow("WINDOW=",0,1,Pattern.compile("ACID"),true),
		         new TokenSequence2FeatureVectorSequence (true, true),
		  		
		};
		
		
		return featurePipes;
	}

	public void saveCRF(CRF crf, String out_path) throws IOException{
		FileOutputStream fos = new FileOutputStream(out_path);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(crf);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger.setLevel(Level.INFO);
		String usage = "java <program> <input-file> <output-crf-mode>";
		if (args.length <2){
			logger.error(usage);
			try {
				String input_file_path = "~/code/chemxseer-tagger/ChemxseerTagger/train-subset.txt";
				String output_model_path = "~/code/chemxseer-tagger/ChemxseerTagger/model.crf";;
				FileReader reader = new FileReader (new File (input_file_path));
				Trainer trainer = new Trainer();
				CRF crf = trainer.train(reader);
				trainer.saveCRF(crf, output_model_path);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("File not found");
			}
		}
		else{
			try {
				String input_file_path = args[0];
				String output_model_path = args[1];
				FileReader reader = new FileReader (new File (input_file_path));
				Trainer trainer = new Trainer();
				CRF crf = trainer.train(reader);
				trainer.saveCRF(crf, output_model_path);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("File not found");
			}
		}
			

	}

}
