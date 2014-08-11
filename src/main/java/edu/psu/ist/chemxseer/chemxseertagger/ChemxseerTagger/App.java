package edu.psu.ist.chemxseer.chemxseertagger.ChemxseerTagger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String test = "I want to play some football today. \n\nWho wants to come? it will be fun.";
        String [] parts = test.split("^$");
        for(String s : parts){
        	System.out.println(s);
        }
        
    }
}
