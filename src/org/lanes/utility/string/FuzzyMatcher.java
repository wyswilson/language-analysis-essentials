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

package org.lanes.utility.string;

//////////////STANDARD///////////////////
import org.lanes.text.nlp.*;
import org.lanes.utility.*;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.lang3.StringUtils;

public class FuzzyMatcher{
	private String matchonx 	= "";
	private String matchony 	= "";
	private String longstr 		= "";
	private String shortstr 	= "";
	private Metaphone phonetic 	= null;
	private boolean allowngram  = true;
	private int matchiteration	= 0;

	public FuzzyMatcher(boolean ngram){
		phonetic = new Metaphone();
		allowngram = ngram;
	}
	public FuzzyMatcher(){
	}
	public FuzzyMatcher(String x1, String y1){
		phonetic = new Metaphone();
		setMatchPair(x1,y1);
	}
	public void setNewMatchPair(String x1, String y1){
		matchonx = "";
		matchony = "";
		setMatchPair(x1,y1);
	}
	public void setMatchPair(String x1, String y1){
		if(x1.length() > y1.length()){
			longstr = x1;
			shortstr = y1;
		}
		else{
			longstr = y1;
			shortstr = x1;
		}
	}
	public Map<String,Integer> constructNGrams(String str){
		NGramAnalyser ngramobj = new NGramAnalyser();
		Map<String,Integer> allngrams = new HashMap<String,Integer>();
		for(int i = 1; i < str.length(); i++){
			List<String> ngrams = ngramobj.findNGrams(i,str);
			
			Iterator<String> iteobj = ngrams.iterator();
			while (iteobj.hasNext()) {
				String ngram = (String) iteobj.next();
				allngrams.put(ngram,i);
			}
		}
		
		return allngrams;
	}
	public void setMatchOn(String x1, String y1){
		//System.out.println("matchon: " + x1 + "," + y1);
		matchonx = x1;
		matchony = y1;
	}
	public String getMatchOnX(){
		return matchonx;
	}
	public String getMatchOnY(){
		return matchony;
	}	
	public int matchRecursion(String xstr, String ystr, String type, int iteration, String direction){
		
		String xprime = xstr;
		String yprime = ystr;
		if(iteration > 0){
			if(direction.equals("RIGHTTOLEFT")){
				xprime = xstr.replaceAll("[^\\s]+$", "");
				yprime = ystr.replaceAll("[^\\s]+$", "");
			}
			else{
				xprime = xstr.replaceAll("^[^\\s]+", "");
				yprime = ystr.replaceAll("^[^\\s]+", "");
			}
		}

		xprime = xprime.trim();
		yprime = yprime.trim();
		
		iteration++;
		
		if(type.equals("reducex")){
			//System.out.println("reducex:" + xprime + "," +  ystr);
			if(isSimilar(xprime, ystr)){
				setMatchOn(xprime,ystr);
				return iteration;
			}
			else if(isAcronym(xprime, ystr) && iteration <= 1){//ONLY ALLOW ACRO TO WORK WITH NO TRUNCATION
				setMatchOn(xprime,ystr);
				return iteration;
			}
			else if(!xprime.equals("")){
				return matchRecursion(xprime,ystr,type,iteration,direction);
			}
			else{
				return -1;
			}
		}
		else if(type.equals("reducey")){  
			//System.out.println("reducey:" + xstr + "," +  yprime);
			if(isSimilar(xstr, yprime)){
				setMatchOn(xstr, yprime);
				return iteration;
			}
			else if(isAcronym(xstr, yprime) && iteration <= 1){//ONLY ALLOW ACRO TO WORK WITH NO TRUNCATION
				setMatchOn(xstr, yprime);
				return iteration;
			}
			else if(!yprime.equals("")){
				return matchRecursion(xstr,yprime,type,iteration,direction);
			}
			else{
				return -1;
			}
		}
		else{
			return -1;
		}
	}
	public boolean isBothUpperCase(String x1, String y1){
		if(StringUtils.isAllUpperCase(x1) && StringUtils.isAllUpperCase(y1)){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean isSimilar(String x1, String y1){
		try{

			//System.out.println(x1 + " (" + x1.length() + ") -" + y1 + " (" + y1.length() + ") = " + stringSim(x1, y1) + ", phonetic:" + phonetic.isMetaphoneEqual(x1, y1) + ", isbothupper:" + isBothUpperCase(x1,y1) );

			if(!isBothUpperCase(x1,y1) && stringSim(x1, y1) > 0.99 && x1.length() > 4 && y1.length() > 4 && phonetic.isMetaphoneEqual(x1, y1)){//woolwoths vs woolworths, phonetic false
				return true;
			}			
			else if(isBothUpperCase(x1,y1) && stringSim(x1, y1) == 1.0){
				return true;
			}
			return false;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public static double stringSim(String str1, String str2) {
		double curveconstant = 0.2;//0.05 (close to straight line)
		double scaleconstant = 2.71799;

		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();

		int edist = StringUtils.getLevenshteinDistance(str1,str2);
 		double sim = 0;
		if(edist == 0){
			sim = 1;
		}
		else{
			sim = Math.exp(-(Math.pow(edist,curveconstant)))*scaleconstant;
		}
		
		return sim;
   	}
	public int matchNGramRecursion(String x1, String y1){

		Map<String,Integer> ngramsx = constructNGrams(x1);
		Map<String,Integer> ngramsy = constructNGrams(y1);
		
		Map<String,Integer> sortedngramsx = MapSorter.sortMap(ngramsx,"DESC");
		Map<String,Integer> sortedngramsy = MapSorter.sortMap(ngramsy,"DESC");
		
		Iterator<String> iteobjx = sortedngramsx.keySet().iterator();
		while (iteobjx.hasNext()) {
			String ngramx = (String) iteobjx.next();
			int ngramxlen = ngramsx.get(ngramx);
			
			Iterator<String> iteobjy = sortedngramsy.keySet().iterator();
			while (iteobjy.hasNext()) {
				String ngramy = (String) iteobjy.next();
				int ngramylen = ngramsy.get(ngramy);
				
				if(isSimilar(ngramx,ngramy)){
					
					setMatchOn(ngramx, ngramy);
					return 10000;
				}
				
			}
		}
		
		return -2;
	}
	public int findMatch(){
		
		return findMatch("LEFTTORIGHT");
	}
	public int findMatch(String direction){
		
		int matchat = matchRecursion(longstr,shortstr,"reducex",0,direction);
		//System.out.println("=>" + longstr + " - " + shortstr + " - " + matchat);

		if(matchat == -1){
			matchat = matchRecursion(longstr,shortstr,"reducey",0,direction);
		}
		if(matchat == -1 && allowngram){
			matchat = matchNGramRecursion(longstr,shortstr);
		}

		matchiteration = matchat;

		return matchat;
	}
	public boolean isMostlyUpperCase(String str){
		String[] chars = str.split(".");
		int totalchar = chars.length;
		int uppercasecnt = 0;
		for(String char_ : chars){
			if(StringUtils.isAllUpperCase(char_)){
				uppercasecnt++;
			}
		}

		double uppercasepercent = (double)uppercasecnt/(double)totalchar;
		if(uppercasepercent > 0.9){
			return true;
		}
		return false;

	}
	public boolean isAcronym(String x, String y){
		List<String> functionwords = new ArrayList<String>();
		functionwords.add("of");
		functionwords.add("and");
		functionwords.add("in");

		//System.out.print("isAcro: " + x + "," + y + "=");
		if(!x.equals("")){
			String[] tokens = x.split(" ");
			String acrox = "";
			for(String token : tokens){
				token = token.trim();
				if(!token.equals("") && !functionwords.contains(token.toLowerCase())){
					String firstchar = token.substring(0,1);
					acrox = acrox + firstchar;
				}
				/*
				if(!token.equals("")){
					String firstchar = token.substring(0,1);
					if(isMostlyUpperCase(firstchar)){
						acrox = acrox + firstchar;
					}
				}
				*/
			}
			
			//System.out.println(x + "(" + acrox + ") vs " + y);
			if(acrox.toLowerCase().equals(y.toLowerCase())){
				//System.out.println("true");
				return true;
			}
			else{
				//System.out.println("false");
				return false;
			}
		}
		else{
			//System.out.println("false");
			return false;
		}
	}
	public double getMatchScore(){
		double score = 0.0;
		
		Matcher replace1    = Pattern.compile("[^\\w]").matcher(matchonx);
		String matchonxnorm = replace1.replaceAll("");

		Matcher replace2   = Pattern.compile("[^\\w]").matcher(longstr);
		String longstrnorm = replace2.replaceAll("");
		

		boolean isacro = isAcronym(matchonx,shortstr);


		//Matcher matchsubstr = Pattern.compile(matchonx,Pattern.CASE_INSENSITIVE).matcher(matchony);
		//boolean issubstr = matchsubstr.find();

		//System.out.println("scorecompute: (" + matchonxnorm + "," + longstrnorm+ "), isacro(" + matchonx + "," + shortstr + "=" + isacro + ")");

		//issubstr && 
		if(!isacro && !matchonxnorm.equals("") && !longstrnorm.equals("")){
			double sizereduced = (double) matchonxnorm.length();
			double sizeoriginal = (double) longstrnorm.length();
			
			score = sizereduced/sizeoriginal;
		}
		else if(isacro){
			score = 2.0;
		}
		
		score = Math.round(score*100.0)/100.0;
		
		return score;
	}
}