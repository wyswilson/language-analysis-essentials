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

public class Clustering{

	private Similarity simobj			= null;
	
   	public Clustering(){
  		simobj = new Similarity();
	}
	public String clusterTerms(Map<String,Double> phrasepairs, Map<String,Integer> phrases, double avesimnonzero){
  		long timestart = System.currentTimeMillis();
		
		Map<String,Double> clusters		= new HashMap<String,Double>();
		Map<String,Integer> phrasesused = new HashMap<String,Integer>();
		
		Map<String,Double> sortedlist = MapSorter.sortMap(phrasepairs,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
		Iterator iterator4 = sortedlist.keySet().iterator();
		while (iterator4.hasNext()) {
			String pair 	= (String) iterator4.next();
			double sim 		= phrasepairs.get(pair);
			
			if(sim > 0){
				Matcher matcherphrase = Pattern.compile("\\[([^\\[\\]]+)\\]\\[([^\\[\\]]+)\\]").matcher(pair);
				if(matcherphrase.find()) {
			        	String phrase1 = matcherphrase.group(1);
			        	String phrase2 = matcherphrase.group(2);

					Matcher replace1 = Pattern.compile("_").matcher(phrase1);
					phrase1 = replace1.replaceAll(" ");

					Matcher replace2 = Pattern.compile("_").matcher(phrase2);
					phrase2 = replace2.replaceAll(" ");
					
					String phraseclustermembership = "NONE-JOIN";
					Map<String,Double> sortedclusters = MapSorter.sortMap(clusters,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
					Iterator iterator5 = sortedclusters.keySet().iterator();
					while (iterator5.hasNext() && phraseclustermembership.equals("NONE-JOIN")) {
						String clus = (String) iterator5.next();
						
						Matcher matcherclus1 = Pattern.compile("\\[" + phrase1 + "\\]").matcher(clus);
						Matcher matcherclus2 = Pattern.compile("\\[" + phrase2 + "\\]").matcher(clus);
						boolean found1 = matcherclus1.find();
						boolean found2 = matcherclus2.find();
						
						if(found1 && found2) {
							phraseclustermembership = "BOTH-JOIN";
						}
						else if(!found1 && found2) {
							phraseclustermembership = "ONE-JOIN{" + phrase2 + "}{" + phrase1 + "}{" + clus + "}";
						}
						else if(found1 && !found2) {
							phraseclustermembership = "ONE-JOIN{" + phrase1 + "}{" + phrase2 + "}{" + clus + "}";
						}
					}

					Matcher matcherstatus = Pattern.compile("^ONE-JOIN").matcher(phraseclustermembership);
					if(phraseclustermembership.equals("NONE-JOIN")){
						String[] clustersim = mergePairs(phrases,phrasesused,pair,sim,avesimnonzero);
						String cluster 		= clustersim[0];
						double csim			= Double.parseDouble(clustersim[1]);
						
						Matcher matcherusedphr = Pattern.compile("\\[([^\\[\\]]+)\\]").matcher(cluster);
						while(matcherusedphr.find()) {
					        	String phr = matcherusedphr.group(1);
							phrasesused.put(phr,0);								
						}
						
						if(!cluster.equals(pair)) {
							clusters.put(cluster,csim);
						}
			        	}
			        	else if(matcherstatus.find() && sim > avesimnonzero){
						Matcher extractclus = Pattern.compile("^ONE-JOIN\\{(.+?)\\}\\{(.+?)\\}\\{(.+?)\\}").matcher(phraseclustermembership);
						if(extractclus.find()) {
					        	String matchingphrase 	= extractclus.group(1);
					        	String nonmatchingphrase= extractclus.group(2);
					        	String matchingcluster 	= extractclus.group(3);
					        	String revisedcluster = matchingcluster;
							
							phrasesused.put(matchingphrase,0);								
							phrasesused.put(nonmatchingphrase,0);
														
							Matcher replace = Pattern.compile("\\[" + matchingphrase + "\\]").matcher(revisedcluster);
							revisedcluster = replace.replaceAll("[" + matchingphrase + "][" + nonmatchingphrase + "]");

							Double[] revisedsims = simobj.groupAverageSimilarity(revisedcluster);
							double revisedsimnonzero = revisedsims[1];
											
							clusters.put(revisedcluster,revisedsimnonzero);
							
							clusters.remove(matchingcluster);
					        }
			        	}
				}
			}
		}
		
		String outliercluster = "";
		Iterator iterator11 = phrases.keySet().iterator();
		while (iterator11.hasNext()) {
			String phr = (String) iterator11.next();
			
			boolean notfoundincluster  = true;
			Iterator iterator9 = clusters.keySet().iterator();
			while (iterator9.hasNext() && notfoundincluster) {
				String clus = (String) iterator9.next();
				
				Matcher extractphrclus = Pattern.compile("\\[" + phr + "\\]").matcher(clus);
				if(extractphrclus.find()) {
					notfoundincluster = false;
				}
			}
			if(notfoundincluster){
			    	outliercluster = outliercluster + "[" + phr + "]";
			}
		}	

		if(!outliercluster.equals("")){
			Double[] outlierclustersims = simobj.groupAverageSimilarity(outliercluster);
			double outlierclustersimnonzero = outlierclustersims[1];
		    	clusters.put(outliercluster,outlierclustersimnonzero);
		}

		String clusterstr 	= "";
		Map<String,Double> sortedclusters = MapSorter.sortMap(clusters,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
		Iterator iterator8 = sortedclusters.keySet().iterator();
		while (iterator8.hasNext()) {
			String cluster 	= (String) iterator8.next();
			double csim 	= clusters.get(cluster);
			clusterstr = clusterstr + "{<" + csim + ">" + cluster + "}";
		}		
		
		return clusterstr;
 	}
	public String[] mergePairs(Map<String,Integer> phrases, Map<String,Integer> phrasesused, String maxpair, double maxsim, double globalavesimnonzero){
		String cluster = maxpair;
		Map<String,Double> weightedphrases = new HashMap<String,Double>();
	    	
		Iterator iterator1 = phrases.keySet().iterator();
		while (iterator1.hasNext()) {
			String phrase = (String) iterator1.next();

			Matcher matcherphrase = Pattern.compile("\\[" + phrase + "\\]").matcher(cluster);
			if(!matcherphrase.find() && !phrasesused.containsKey(phrase)) {
				Double[] avesims = simobj.groupAverageSimilarity(cluster + "[" + phrase + "]");
				double sim = avesims[0];
				weightedphrases.put(phrase,sim);
			}
		}
	    	
		double groupavesimnonzero = 0;
		
		Map<String,Double> sortedweightedphrases = MapSorter.sortMap(weightedphrases,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
		Iterator iterator2 = sortedweightedphrases.keySet().iterator();
		while (iterator2.hasNext()) {//TRY TO INTEGRATE TERMS WHICH ARE MORE RELATED TO THE INITIAL CLUSTER FIRST.
			String phrase = (String) iterator2.next();
			
			Double[] avesims	= simobj.groupAverageSimilarity(cluster + "[" + phrase + "]");
			double sim			= avesims[0];
			double simnonzero	= avesims[1];
			if(sim > globalavesimnonzero){
				groupavesimnonzero = simnonzero;
				cluster = cluster + "[" + phrase + "]";
			}
		}
	    
		String[] returnobj = new String[2];
		returnobj[0] = cluster;
		returnobj[1] = String.valueOf(groupavesimnonzero);
		return returnobj;
	}
	public Map<String,Double> getClusters(String clusterstr){
	    	Map<String,Double> clusters = new HashMap<String,Double>();
	    	
		Matcher matcherclus = Pattern.compile("\\{([^\\{\\}]+)\\}").matcher(clusterstr);
		while(matcherclus.find()) {
	        	String simcluster = matcherclus.group(1);
			Matcher matchersimelements = Pattern.compile("<([^<>]+)><([^<>]+>").matcher(simcluster);
			if(matchersimelements.find()) {
	        		double intraclustersim = Double.parseDouble(matchersimelements.group(1));
	        		String cluster = matchersimelements.group(1);
	        		clusters.put(cluster,intraclustersim);
	        	}
	    	}
	    	
		return clusters;
    	}
}