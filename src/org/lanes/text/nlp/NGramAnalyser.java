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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.*;
////////////////////////////////////////

public class NGramAnalyser{
	
   	public NGramAnalyser(){

  	}
	public List<String> findNGrams(int max, String val) {
		List<String> out = new ArrayList<String>(1000);
		String[] words = val.split(" ");
		for (int i = 0; i < words.length - max + 1; i++) {
			out.add(makeString(words, i,  max));
		}
		return out;
	}
	public String makeString(String[] words, int start, int length) {
		StringBuilder tmp= new StringBuilder(100);
		for (int i = start; i < start + length; i++) {
			tmp.append(words[i]).append(" ");
		}
		return tmp.substring(0, tmp.length() - 1);
	}
	public List<String> reduceNgrams(List<String> in, int size) {
		if (1 < size) {
			List<String> working = reduceByOne(in);
			in.addAll(working);
			for (int i = size -2 ; i > 0; i--) {
				working = reduceByOne(working);
				in.addAll(working);
			}
		}
		return in;
	}
	public List<String> reduceByOne(List<String> in) {
		List<String> out = new ArrayList<String>(in.size());
		int end;
		for (String s : in) {
			end = s.lastIndexOf(" ");
			out.add(s.substring(0, -1 == end ? s.length() : end));  
		}
		//the last one will always reduce twice - words 0, n-1 are in the loop this catches the words 1, n
		String s = in.get(in.size() -1);
		out.add(s.substring(s.indexOf(" ")+1));
		return out;
	}
	public Map<String,Double> getNGrams(String text){	
		Map<String,Double> ngrams = new HashMap<String,Double>();
		
	        for(int i = 1; i <= CommonData.numberOfWords(text); i++){
		        List<String> temp = findNGrams(i, text);
		        int position = 1;//PREFER HEAD NOUNS OVER MODIFIERS. NGRAMS ARE RETURNED LEFT TO RIGHT.
		        for(String ngram: temp){
		            	
		            	double length = ngram.length();
		            	double righttoleft = (i+position);
		            	double lengthrighttoleft = righttoleft*Math.exp(-1/length);
		            	double preference = CommonData.roundDecimal(lengthrighttoleft,"#.######");
		            	
		            	//System.err.println("\t\t(" + ngram + "," + righttoleft + "," + length + "," + Math.exp(-1/length) + ")");
		            	ngrams.put(ngram,preference);
		            	
		            	position++;
		        }
		}
		
		return ngrams;
    }
	public Map<String,Double> getNGramsRelFreq(Map<String,Double> ngrams, Similarity simobj){
		double N = simobj.getTotalDocCount();
		Map<String,Double> ngramsrelfreq = new HashMap<String,Double>();
		Iterator iterator8 = ngrams.keySet().iterator();
		while (iterator8.hasNext()) {
			String ngram 	= (String) iterator8.next();
			double n 		= simobj.getDocCount(ngram);
			double relfreq 	= CommonData.roundDecimal(n/N,"#.######");
			ngramsrelfreq.put(ngram,relfreq);
		}
		return ngramsrelfreq;
    }
}