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

package org.lanes;

//////////////STANDARD///////////////////
import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.net.*;
////////////////////////////////////////

import org.lanes.utility.*;
import org.lanes.utility.string.*;
import org.lanes.text.nlp.*;
import org.lanes.text.mining.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import org.apache.commons.lang.StringUtils;
import net.didion.jwnl.JWNLException;
import LBJ2.nlp.SentenceSplitter;
import LBJ2.nlp.Sentence;

public class Example{
	
	public static void main(String[] args) {
	
		testTextAnalysis();
		//testUnithoodAnalysis();
		//testDistSimilarity();
		//testContentBearingness();
		//testConceptualisation();
		testLemmatiser();
		//testFuzzyMatcher();
		//testEntityRecogniser();
	}

	public static void testTextAnalysis(){
		//String input = "The manufacturing of coconut cake adheres to the traditional recipe handed down over many generations.";
		String input = "Please send CV to Turners Auctions Hamilton, Attention ? Branch Manager. P O Box 1512, Hamilton; or email CV to <EMAIL>";

		PhraseExtractor pe 			= new PhraseExtractor(input);//0 FOR PHRASE, 1 FOR WORD
		Map<String,String> offsets 	= pe.getLemmasByOffset();//THE KEY CONTAINS THE PHRASES, e.g., "bovine colostrum", and THE VALUE CONTAINS THE PHRASE TYPES, e.g., "NOUNPHRASE"
		Map<String,String> phrases 	= pe.getPhrases();//THE KEY CONTAINS THE PHRASES, e.g., "bovine colostrum", and THE VALUE CONTAINS THE PHRASE TYPES, e.g., "NOUNPHRASE"
		//PHRASE TYPES {NOUNPHRASE,VERBPHRASE,REFERENT,QUESTION,CONNECTIVE,DESCRIPTOR}
		
		KeyphraseAnalyser ka 			= new KeyphraseAnalyser(phrases);
		Map<String,Double> phraseweights= ka.getPhraseScores();
		
		Iterator iterator1 = offsets.keySet().iterator();
		while (iterator1.hasNext()) {
			String offset 	= (String) iterator1.next();
			String phrase 	= (String) offsets.get(offset);
			String type 	= (String) phrases.get(phrase);
			double weight 	= (double) phraseweights.get(phrase);
			
			System.err.println(offset + " [" + phrase + "] [" + type + "] [" + weight + "]");
		}
	}
	public static void testUnithoodAnalysis(){
		String candidate = "United States of America";
		String connector = "of";
		
		UnithoodAnalyser unitobj = new UnithoodAnalyser();
		boolean isunitornot = unitobj.isUnit(candidate,connector);
		
		System.err.println(candidate + " is unit [" + isunitornot + "]");
	}
	public static void testDistSimilarity(){
		String phrasestring = "[lion][tiger][leopard][umbrella]";
		
		Similarity solr 	= new Similarity();
		Double[] avesims	= solr.groupAverageSimilarity(phrasestring);
		double avesim		= avesims[0];
		double avesimnonzero = avesims[1];
		System.err.println("group-ave-sim is [" + avesim + "]");
		
		Map<String,Integer> phrases 	= new HashMap<String,Integer>();
		Matcher matcherphr = Pattern.compile("\\[(.+?)\\]").matcher(phrasestring);
		while(matcherphr.find()) {
	        	String phrase = matcherphr.group(1);
	        	phrases.put(phrase,0);
		}
		
		Map<String,Double> phrasepairs = new HashMap<String,Double>();
		Iterator iterator1 = phrases.keySet().iterator();
		while (iterator1.hasNext()) {
			String phr1 = (String) iterator1.next();
			Iterator iterator2 = phrases.keySet().iterator();
			while (iterator2.hasNext()) {
				String phr2 = (String) iterator2.next();
				if(!phr1.equals(phr2) && !phrasepairs.containsKey("[" + phr1 + "][" + phr2 + "]") && !phrasepairs.containsKey("[" + phr2 + "][" + phr1 + "]")){
					double sim = solr.distributedSimilarity(phr1,phr2);
					phrasepairs.put("[" + phr1 + "][" + phr2 + "]",sim);
				}
			}
		}
		
    	Clustering clusobj 	= new Clustering();
    	String clusters 	= clusobj.clusterTerms(phrasepairs,phrases,avesimnonzero);
    	
		System.err.println("clusters is [" + clusters + "]");
	}
	public static void testContentBearingness(){
		String phrasestring = "[australia][nokia][impressive][umbrella][yesterday][fallen]";
		
		Similarity simobj = new Similarity();
			
		Matcher matcherphr = Pattern.compile("\\[(.+?)\\]").matcher(phrasestring);
		while(matcherphr.find()) {
        	String phr 					= matcherphr.group(1);
			double contentbearingness 	= simobj.inverseDocFreq(phr);
			
        	System.err.println(phr + "-" + contentbearingness);
		}
	}
	public static void testConceptualisation(){
 		String phrasestring = "[lion][spiced cake][red umbrella][big bad wolf]";
		
	   	Conceptualiser concpobj	= new Conceptualiser();
		
		String verbose = "";
		Matcher matcherphr = Pattern.compile("\\[(.+?)\\]").matcher(phrasestring);
		while(matcherphr.find()) {
			String phrase = matcherphr.group(1);
	       		
			Matcher matcherphr2 = Pattern.compile("=>").matcher(phrase);
			if(matcherphr2.find()) {
	       			String[] temp = phrase.split("=>");
	       			phrase = temp[1];
	       	}
	       		
	       	String selectedconcept 	= "";
	       	int longestngram		= 0;
	       	String ngramverbose 	= "";
		    NGramAnalyser nga = new NGramAnalyser();
		    Map<String,Double> ngrams 	= nga.getNGrams(phrase);
			Map<String,Double> sortedngrams = MapSorter.sortMap(ngrams,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
			Iterator iterator4 = sortedngrams.keySet().iterator();
			while (iterator4.hasNext()) {
				String ngram = (String) iterator4.next();
				double relfreq = ngrams.get(ngram);
				
				String concept 	= concpobj.mapTermToConcept(ngram);
				
				if(!concept.equals("")){
					int ngramlength = CommonData.numberOfWords(ngram);
					if(ngramlength > longestngram){
						selectedconcept = concept;
						longestngram = ngramlength;
					}
				}
			}
			
			System.err.println(phrase + "-" + selectedconcept);
		}
	}
	public static void testLemmatiser(){
		
		Lemmatiser lem = new Lemmatiser();
		System.err.println(lem.lemmatisePhrase("Branch Manager & Sales Manager","NOUNPHRASE"));
		System.err.println(lem.lemmatisePhrase("octopi","NOUNPHRASE"));
		System.err.println(lem.lemmatisePhrase("red big octopuses","NOUNPHRASE"));
		System.err.println(lem.lemmatisePhrase("security breaches","NOUNPHRASE"));
		System.err.println(lem.lemmatisePhrase("confucius","NOUNPHRASE"));
		System.err.println(lem.lemmatisePhrase("delicious citruses","NOUNPHRASE"));
	}
	public static void testFuzzyMatcher(){
		
		FuzzyMatcher matcher = new FuzzyMatcher("delicious spiced cake","cake");
		matcher.findMatch();//DEFAULTS TO LEFTTORIGHT
		String matchonx =  matcher.getMatchOnX();
		String matchony =  matcher.getMatchOnY();
		double score = matcher.getMatchScore();
		System.err.println("x:" + matchonx + ", y:" + matchony + " (" + score + ")");

		matcher.setNewMatchPair("Company A Ltd","Company A");
		matcher.findMatch("RIGHTTOLEFT");//CERTAIN PROPER NAMES MAY BENEFIT FROM RIGHTTOLEFT MATCHING
		String matchonx2 =  matcher.getMatchOnX();
		String matchony2 =  matcher.getMatchOnY();
		double score2 = matcher.getMatchScore();
		System.err.println("x:" + matchonx2 + ", y:" + matchony2 + " (" + score2 + ")");

	}
	public static void testEntityRecogniser(){
   		EntityRecogniser recogobj = new EntityRecogniser();
		
		String entitystring = "[Bank of America][David Copperfield][gandhi][wallet][computer][confucius]";
		
		double threshold = 0;
		Matcher matcherent = Pattern.compile("\\[(.+?)\\]").matcher(entitystring);
		while(matcherent.find()) {
			String entity 		= matcherent.group(1);
			String entityclass 	= recogobj.determineEntityClass(entity,threshold);
				
			System.err.println(entity + "-" + entityclass);
		}
	}
}