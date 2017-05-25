/* 
	Copyright (C) 2009-2014	Wilson Wong (http://wilsonwong.me)

	This file is part of LANES (Language Analysis Essentials).

    LANES is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LANES is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LANES. If not, see <http://www.gnu.org/licenses/>.
*/

package org.lanes.text.nlp;

//////////////STANDARD///////////////////
import org.lanes.utility.*;
import org.lanes.text.mining.*;
import org.lanes.utility.string.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.*;
////////////////////////////////////////

import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
import LBJ2.nlp.seg.Token;
import LBJ2.nlp.SentenceSplitter;
import LBJ2.nlp.WordSplitter;
import LBJ2.nlp.seg.PlainToTokenParser;
import LBJ2.parse.ChildrenFromVectors;

public class PhraseExtractor{
	
	private long exetimesofar = 0;
	
	private Map<String,String> keyphrases 		= new HashMap<String,String>();
	private Map<String,String> phrasestolemma 	= new HashMap<String,String>();
	private Map<String,String> lemmasbyoffset	= new HashMap<String,String>();
	private Map<String,String> phrasesbyoffset	= new HashMap<String,String>();
	private Map<String,String> topics			= new HashMap<String,String>();
	
	public PhraseExtractor(String text){
		long timestart = System.currentTimeMillis();
		
		Lemmatiser lemmatiserobj = new Lemmatiser();
		
		Matcher replace00 = Pattern.compile("\"").matcher(text);
		text = replace00.replaceAll("");
		Matcher replace01 = Pattern.compile("[\\s]+").matcher(text);
		text = replace01.replaceAll(" ");

	 	String[] input = {text};
		
		UnithoodAnalyser unit 	= new UnithoodAnalyser();
		
		//////POS PATTERN FOR EXTRACTING PHRASES
		Map<Integer,String> orderedpatterns 	= new HashMap<Integer,String>();
		Map<String,String> phrasepatterns 		= new HashMap<String,String>();

		orderedpatterns.put(1,"NOUNPHRASE-UNITHOOD");
		orderedpatterns.put(2,"NOUNPHRASE");
		orderedpatterns.put(3,"VERBPHRASE");
		orderedpatterns.put(4,"REFERENT");
		orderedpatterns.put(5,"QUESTION");
		orderedpatterns.put(6,"CONNECTIVE");
		orderedpatterns.put(7,"DESCRIPTOR");

		phrasepatterns.put("NOUNPHRASE-UNITHOOD","((?:\\([\\d]+:JJ[^\\(\\)]+\\))*(?:\\([\\d]+:NN[^\\(\\)]+\\))+\\([\\d]+:(?:CC|IN|TO)[^\\(\\)]+\\)(?:\\([\\d]+:JJ[^\\(\\)]+\\))*(?:\\([\\d]+:NN[^\\(\\)]+\\))+)");
		phrasepatterns.put("NOUNPHRASE","((?:\\([\\d]+:(?:CD|JJ)[^\\(\\)]+\\))*(?:\\([\\d]+:NN[^\\(\\)]+\\))+)");
		phrasepatterns.put("VERBPHRASE","((?:(?:\\([\\d]+:(?:MD|RB)[^\\(\\)]+\\))*(?:\\([\\d]+:V[^\\(\\)]+\\))+(?:\\([\\d]+:(?:RB|RP)[^\\(\\)]+\\))*(?:\\([\\d]+:V[^\\(\\)]+\\))*)+)");
		phrasepatterns.put("REFERENT","(\\([\\d]+:PRP[^\\(\\)]+\\))");
		phrasepatterns.put("QUESTION","(\\([\\d]+:(?:WP|WRB)[^\\(\\)]+\\))");
		phrasepatterns.put("CONNECTIVE","(\\([\\d]+:(?:CC|IN|TO)[^\\(\\)]+\\))");
		phrasepatterns.put("DESCRIPTOR","((?:\\([\\d]+:RB[^\\(\\)]+\\))*(?:\\([\\d]+:JJ[^\\(\\)]+\\))+)");
 		//////////////////////////////////////
 		
		///////TO EXPAND CONTRACTIONS/////////
		Map<String,String> contractions = new HashMap<String,String>();

		contractions.put("'ll","will");
		contractions.put("n't","not");
		contractions.put("'d","had");
		contractions.put("'ve","have");
		contractions.put("'re","are");
		contractions.put("'s","is");
		contractions.put("'m","am");

		Map<String,String> contractexceptions = new HashMap<String,String>();

		contractexceptions.put("ca not","cannot");

	    //////////////////////////////////////
	    
  		try{

			String taggedinput = "";
			POSTagger tagger = new POSTagger();
			int offset = 1;

			text = TextNormaliser.collapseMultilineText(text);
			
			PlainToTokenParser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(input)));
			for (Token word = (Token) parser.next(); word != null;word = (Token) parser.next()){
				String tag = tagger.discreteValue(word);
				taggedinput = taggedinput + "(" + offset + ":" + tag + " " + word.form + ")";
				offset++;
			}
 			
      		//System.err.println(taggedinput);
      			
  			List<String> duplicates = new ArrayList<String>();

			List orders = new ArrayList(orderedpatterns.keySet());
			Collections.sort(orders);
			Iterator iterator = orders.iterator();
			while (iterator.hasNext()) {
				int order 			= (Integer) iterator.next();
				String phrasetype	= orderedpatterns.get(order);
				String pattern		= phrasepatterns.get(phrasetype);
				
				Matcher matchernp = Pattern.compile(pattern).matcher(taggedinput);
				while(matchernp.find()) {
			       	String phrasewithtag 	= matchernp.group(1);
					String type				= phrasetype;
					
					String poffset = "";
					String unithoodconnectorpos = "";
					Matcher matcheroffset = Pattern.compile("\\(([^:]+):([^\\s]+)").matcher(phrasewithtag);
					while(matcheroffset.find()){
				    	String o = matcheroffset.group(1);
				    	String c = matcheroffset.group(2);
		       			poffset = poffset + "," + o + "";

						Matcher matcherconnector = Pattern.compile("(?:CC|IN|TO)").matcher(c);
		       			if(type.equals("NOUNPHRASE-UNITHOOD") && matcherconnector.find()){
		       			    unithoodconnectorpos = c;
							//System.err.println("\tunithoodconnectorpos[" + unithoodconnectorpos + "]");
		       			}
		       		}
					//poffset = poffset + ",";
					Matcher replace0 = Pattern.compile("^,").matcher(poffset);
					poffset = replace0.replaceAll("");
					
					String phrase					= "";
					String unithoodconnectorwrd 	= "";
					Matcher matchertoken = Pattern.compile(":([^\\s]+)\\s([^\\s]+)\\)").matcher(phrasewithtag);
					while(matchertoken.find()){
			       			String pos 		= matchertoken.group(1);
			       			String token 	= matchertoken.group(2);
			       			
			       			if(contractions.containsKey(token)){
			       				token = contractions.get(token);
			       			}
			       			
							phrase = phrase + token + " ";
						
			       			if(type.equals("NOUNPHRASE-UNITHOOD") && pos.equals(unithoodconnectorpos)){
			       			    unithoodconnectorwrd = token;
								//System.err.println("\tunithoodconnectorwrd[" + unithoodconnectorwrd + "]");
			       			}
					}
					Matcher replace3 = Pattern.compile("\\s$").matcher(phrase);
					phrase = replace3.replaceAll("");
					
					Iterator iterator2 = contractexceptions.keySet().iterator();
					while (iterator2.hasNext()) {
						String pattern2 = (String) iterator2.next();
						String replace2	= contractexceptions.get(pattern2);
						
						phrase = phrase.replaceAll("\\b" + pattern2 + "\\b", replace2);
					}
					
					boolean isunitornot = false;
					if(type.equals("NOUNPHRASE-UNITHOOD")){
						isunitornot = unit.isUnit(phrase,unithoodconnectorwrd);
					}
					
					if((type.equals("NOUNPHRASE-UNITHOOD") && isunitornot) || (!type.equals("NOUNPHRASE-UNITHOOD"))){
						boolean issubstring = false;
						
						for(String dup: duplicates){
							Matcher matcherdup = Pattern.compile(poffset).matcher(dup);
							if(matcherdup.find()){
								issubstring = true;
						    }
						}
						
						Matcher matchernonalphanum = Pattern.compile("^[^\\w\\d]+$").matcher(phrase);
						if(!matchernonalphanum.find() && !issubstring){
							
							if(type.equals("NOUNPHRASE-UNITHOOD")){
							   	type = "NOUNPHRASE";
							}
							
							String lemma = lemmatiserobj.lemmatisePhrase(phrase,type);
							
							Matcher replace4 = Pattern.compile("\\s+").matcher(lemma);
							lemma = replace4.replaceAll(" ");
							Matcher replace5 = Pattern.compile("\\s+").matcher(phrase);
							phrase = replace5.replaceAll(" ");

							keyphrases.put(lemma,type);
							phrasestolemma.put(phrase,lemma);
							
							lemmasbyoffset.put(poffset,lemma);
							phrasesbyoffset.put(poffset,phrase);
							
			      			//System.err.println("[" + order + "][" + poffset + "][" + type + "][" + phrasewithtag + "][" + phrase + "(" + lemma + ")]");
							duplicates.add(poffset);
						}
					}
				}
			}
      	}
		catch (Exception e){
		    	e.printStackTrace();
		}
		
		topics = (new Lexicon()).detectTopics(keyphrases);
		
		exetimesofar = (System.currentTimeMillis() - timestart);
	}
	public Map<String,String> getPhrases(){
		return keyphrases;
	}
	public Map<String,String> getLemmasByOffset(){
		return lemmasbyoffset;
	}
 	public Map<String,String> getTopics(){
		return topics;
	}
}