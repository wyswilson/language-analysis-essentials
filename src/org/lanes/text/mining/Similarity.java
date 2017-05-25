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

package org.lanes.text.mining;

//////////////STANDARD///////////////////
import org.lanes.text.nlp.*;
import org.lanes.utility.*;
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

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import org.apache.commons.lang.StringUtils;

public class Similarity{
	
	private HttpSolrServer solrserver	= null;
	private Map<String,String> stopwords= null;
	private static Connection conn 		= null;
	private double N 					= 0;
	
	public Similarity (){
		conn 		= (new DatabaseConnection()).establishConnection("lanes");
		solrserver 	= new HttpSolrServer(CommonData.corpuswikiconfig);
		
		N 			= preloadTotalDocCount();
		stopwords 	= Lexicon.getStopWords();
	}
	public double getTotalDocCount(){
	    	return N;
	}
	public double preloadTotalDocCount(){
	    double n = 0;
	    	
		ModifiableSolrParams param = formulateQuery("id:*",1);
		Double[] stats = sendQuery(param);
		n = stats[0];
		
		return n;
	}
	public ModifiableSolrParams formulateQuery(String query, int rows){
	    	
    	ModifiableSolrParams params = new ModifiableSolrParams();

		Matcher replace1 = Pattern.compile("_").matcher(query);
		query = replace1.replaceAll(" ");
		
		params.set("q", query);
		Matcher matcherunigram = Pattern.compile("^text:([^\\s]+)$").matcher(query);
		if(matcherunigram.find()){
			String searchterm = matcherunigram.group(1);
			params.set("fl", "*,totaltermfreq(text," + searchterm + ")");
		}
		params.set("fq", 
			"-titleText:\"(disambiguation)\" " +
				"-titleText:\"Book:\" " +
				"-titleText:\"Category:\" " +
				"-titleText:\"Education Program:\" " +
			"-titleText:\"File:\" " +
			"-titleText:\"Image:\" " +
			"-titleText:\"Help:\" " +
			"-titleText:\"Media:\" " +
			"-titleText:\"MediaWiki:\" " +
			"-titleText:\"Portal:\" " +
			"-titleText:\"Template:\" " +
			"-titleText:\"TimedText:\" " +
			"-titleText:\"User:\" " +
			"-titleText:\"Similarity:\" " +
			"-titleText:\"WP:\" " +
			"-titleText:\"Wikipedia:\" " +
			"-titleText:\"WT:\"");
		params.set("start", 0);
		params.set("rows", rows);
		//params.set("debugQuery", true);
		//params.set("debug.explain.structured", true);
		
		return params;
	}
	public ArrayList<String> getArticleTitles(ModifiableSolrParams params){
		ArrayList<String> titles = new ArrayList<String>();

	    try{
			QueryResponse response = solrserver.query(params);
			
			for (SolrDocument doc : response.getResults()){
			    	Collection<String> fnames = doc.getFieldNames();
				for (String fname : fnames) {
					Matcher matchername = Pattern.compile("titleText").matcher(fname);
					if(matchername.find()){
						String title = (String) doc.getFieldValue(fname);
						titles.add(title);
					}
				}
			}
		    	params.clear();
		}
		catch(Exception e){
			//System.err.println (e.getMessage());
		}
		
		return titles;
	}
	private Double[] sendQuery(ModifiableSolrParams params){
	    double n = 0;
	    double f = 0;
	    try{
			QueryResponse response = solrserver.query(params);
	    	n = (double)response.getResults().getNumFound();
			
			for (SolrDocument doc : response.getResults()){
			    	Collection<String> fnames = doc.getFieldNames();
				for (String fname : fnames) {
					Matcher matcherfname = Pattern.compile("totaltermfreq").matcher(fname);
					if(matcherfname.find()){
						f = (Double) doc.getFieldValue(fname);
					}
				}
			}
		    	params.clear();
		}
		catch(Exception e){
			//System.err.println (e.getMessage());
		}
		
		Double[] outputs = new Double[2];
		outputs[0] = n;
		outputs[1] = f;
		
		return outputs;
	}
	public double getDocCount(String term1, String term2){

		Matcher replace1 = Pattern.compile("_").matcher(term1);
		term1 = replace1.replaceAll(" ");
		Matcher replace2 = Pattern.compile("_").matcher(term2);
		term2 = replace2.replaceAll(" ");
		
		double n = 0;
		double f = 0;
		String phrasescombi = "";
		
		if((term2.equals("") && !stopwords.containsKey(term1.toLowerCase())) || (!term2.equals("") && !stopwords.containsKey(term1.toLowerCase()) && !stopwords.containsKey(term2.toLowerCase()))){
			try{
				ModifiableSolrParams param = null;
				if(!term2.equals("")){
					param = formulateQuery("text:\"" + term1 + "\" AND \"" + term2 + "\"",1);
				}
				else{
					param = formulateQuery("text:\"" + term1 + "\"",1);
				}
				
				Double[] stats = sendQuery(param);
				n = stats[0];
				f = stats[1];
				
			}
			catch(Exception e){
			   	//System.err.println(e.getMessage());
			}
		}
		
		return n;
	}
	public double getDocCount(String term){
		return getDocCount(term,"");
	}
	public double inverseDocFreq(String term){
		double n = getDocCount(term);
		double normalisedidf = 0;
		if(n > 0){
			double idf = Math.log(N/n)/Math.log(2);
	    		normalisedidf = Math.exp(-1/idf);
	    	}
	    	return normalisedidf;
	}
	public Map<String,Double> inverseDocFreq(Map<String,String> phrases){
		Map<String,Double> phraseweights = new HashMap<String,Double>();
		
		Iterator iterator = phrases.keySet().iterator();
		while (iterator.hasNext()) {
			String phrase = (String) iterator.next();
			phraseweights.put(phrase,inverseDocFreq(phrase));
		}
		
		return phraseweights;
	}
	public double distributedSimilarity(String phrase1, String phrase2){
 		
 		double similarity = 0;
		if(phrase1.toLowerCase().equals(phrase2.toLowerCase())){
 		    	similarity = 1;
 		}
 		else{
			double n1 = getDocCount(phrase1);
			double n2 = getDocCount(phrase2);
			double n12 = getDocCount(phrase1,phrase2);
			
			if(N > 0 && n1 > 0 && n2 > 0 && n12 > 0){
				double logN		= Math.log10(N*N);
				double logn1	= Math.log10(n1*n1);
				double logn2	= Math.log10(n2*n2);
				double logn12	= Math.log10(n12*n12);
				
				double max_logn2n1 = 0;
				double min_logn2n1 = 0;
				
				if(logn1 < logn2){
					max_logn2n1 = logn2;
					min_logn2n1 = logn1;
				}
				else{
					max_logn2n1 = logn1;
					min_logn2n1 = logn2;
				}
				double nwd = 0;
				if((logN - min_logn2n1) > 0){
					nwd = (max_logn2n1 - logn12)/(logN - min_logn2n1);
					if(nwd > 0) similarity = Math.exp(-0.6*nwd);
				}
			}
		}
		similarity = CommonData.roundDecimal(similarity,"#.####");
		
		return similarity;
	}
 	public Double[] groupAverageSimilarity(String phrasestring){
 		Map<String,String> phrases1 = new HashMap<String,String>();
 		Map<String,String> phrases2 = new HashMap<String,String>();
		Matcher matcherphr = Pattern.compile("\\[(.+?)\\]").matcher(phrasestring);
		while(matcherphr.find()) {
	        	String phrase = matcherphr.group(1);
	       	 	phrases1.put(phrase,"");
	        	phrases2.put(phrase,"");
		}

		double pairing = 0;
		double simsum = 0;
		double pairingnonzero = 0;
		Map<String,String> duplicates = new HashMap<String,String>();
		Iterator iterator1 = phrases1.keySet().iterator();
		while (iterator1.hasNext()) {
			String phr1 = (String) iterator1.next();
			Iterator iterator2 = phrases2.keySet().iterator();
			while (iterator2.hasNext()) {
				String phr2 = (String) iterator2.next();
				if(!phr1.equals(phr2) && !duplicates.containsKey("<" + phr1 + "><" + phr2 + ">")){
					double sim = distributedSimilarity(phr1,phr2);
					simsum = simsum + sim;
					pairing++;
					if(sim > 0){
					    	pairingnonzero++;
					}
					duplicates.put("<" + phr1 + "><" + phr2 + ">","");
					duplicates.put("<" + phr2 + "><" + phr1 + ">","");
				}
			}
		}

		double simave = 0;
		if(pairing > 0){
			simave = simsum/pairing;
		}
		double simavenonzero = 0;
		if(pairingnonzero > 0){
		    	simavenonzero = simsum/pairingnonzero;
		}
		simave = CommonData.roundDecimal(simave,"#.####");
		simavenonzero = CommonData.roundDecimal(simavenonzero,"#.####");
		
		Double[] outs = new Double[2];
		outs[0] = simave;
		outs[1] = simavenonzero;
		return outs;
	}
}