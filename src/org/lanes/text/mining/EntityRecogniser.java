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

public class EntityRecogniser{
	
	private HttpSolrServer solrserver	= null;
	private Similarity simobj		= null;
	
	public EntityRecogniser (){
		solrserver 	= new HttpSolrServer(CommonData.corpuswikiconfig);
		simobj		= new Similarity();
	}
	public String determineEntityClass(String entity, double acceptancethreshold){
		return determineEntityClass(entity,acceptancethreshold,1);
	}
	public String determineEntityClass(String entity, double acceptancethreshold, int iteration){
		long timestart = System.currentTimeMillis();
		
		String isa = "";
		String confidence = "0.0";
		String deteriorate = "0.0";

        NGramAnalyser nga = new NGramAnalyser();	
	    entity = entity.trim();
		//Matcher matchercap1 = Pattern.compile("^[A-Z]").matcher(entity);
		//Matcher matchercap2 = Pattern.compile("\\s[A-Z]").matcher(entity);
		//if(matchercap1.find() || matchercap2.find()){
			
			System.err.println("CANDIDATE(" + entity + ")");

			boolean exacttitlematchfound = false;

			try{
				double isPersonPt = 0.0;
				double isLocationPt = 0.0;
				double isOrganisationPt = 0.0;
				double totalTitles = 0.0;
				
				ModifiableSolrParams param = null;
				if(iteration <= 2){
					String exactintextquery = "\"" + entity + "\"";
					param = simobj.formulateQuery("titleText:" + exactintextquery + "",10);
				}
				else{
			    	    String intextquery = "";
			    	    String[] toks = entity.split(" ");
			    	    for(String tok: toks){
			    	    	intextquery = intextquery + tok + " AND ";
			    	    }
			    	    Matcher replace1 = Pattern.compile(" AND $").matcher(intextquery);
			    	    intextquery = replace1.replaceAll("");
			    	    param = simobj.formulateQuery("text:" + intextquery + "",10);
				}
				
				QueryResponse response = solrserver.query(param);
				for (SolrDocument doc : response.getResults()){
				    Collection<String> fnames = doc.getFieldNames();
					for (String fname : fnames) {
						if(fname.equals("titleText")){
							String title = (String) doc.getFieldValue(fname);
							String orititle = title;
							Matcher replace1 = Pattern.compile("\\s\\([^\\(\\)]+\\)$").matcher(title);
							title = replace1.replaceAll("");
							Matcher replace2 = Pattern.compile(",[^,]+$").matcher(title);
							title = replace2.replaceAll("");

		    				double strsim = FuzzyMatcher.stringSim(title,entity);
		    				if(strsim > 0.5 ){

		    					Map<String,Double> istypeornot = compareEntityType(orititle);
	    						System.err.println("\tOK(" + entity + "," + title + ") = " + strsim + "");
		    					
		    					double sum = istypeornot.get("PERSON") + istypeornot.get("LOCATION") + istypeornot.get("ORGANISATION");
		    					if(sum > 0){
			    					double personPercentage 	= CommonData.roundDecimal(istypeornot.get("PERSON")/sum,"#.####");
			    					double locationPercentage 	= CommonData.roundDecimal(istypeornot.get("LOCATION")/sum,"#.####");
			    					double organisationPercentage 	= CommonData.roundDecimal(istypeornot.get("ORGANISATION")/sum,"#.####");
			    					
			    					totalTitles++;
			    					
		    						System.err.println("\t\tPERSON(" + personPercentage + " = " + istypeornot.get("PERSON") + "/" + sum + ")");
		    						System.err.println("\t\tLOCATION(" + locationPercentage + " = " + istypeornot.get("LOCATION") + "/" + sum + ")");
		    						System.err.println("\t\tORGANISATION(" + organisationPercentage + " = " + istypeornot.get("ORGANISATION") + "/" + sum + ")");
			    					
			    					isPersonPt = isPersonPt + personPercentage;
			    					isLocationPt = isLocationPt + locationPercentage;
			    					isOrganisationPt = isOrganisationPt + organisationPercentage;
			    				}
			    				else if(strsim > 0.999){//IF EXACT TITLE MATCH, BUT NOT LOCATION|PERSON|ORGANISATION
			    				     	exacttitlematchfound = true;
			    				}
		    				}
		    				else{
	    						System.err.println("\tKO(" + entity + "," + title + ") = " + strsim + "");
		    				}
						}
					}
				}
				
				if(totalTitles > 0){
					double personAvePt = isPersonPt/totalTitles;
					double locationAvePt = isLocationPt/totalTitles;
					double organisationAvePt = isOrganisationPt/totalTitles;
					
					System.err.println("(PERSON): " + personAvePt + " = " + isPersonPt + "/" + totalTitles);
					System.err.println("(LOCATION): " + locationAvePt + " = " + isLocationPt + "/" + totalTitles);
					System.err.println("(ORGANISATION): " + organisationAvePt + " = " + isOrganisationPt + "/" + totalTitles);
					
					if(personAvePt > acceptancethreshold && personAvePt > locationAvePt && personAvePt > organisationAvePt){
					    	isa = "PERSON";
					    	confidence = String.valueOf(personAvePt);
					}
					else if(locationAvePt > acceptancethreshold && locationAvePt > personAvePt && locationAvePt > organisationAvePt){
					    	isa = "LOCATION";
					    	confidence = String.valueOf(locationAvePt);
					}
					else if(organisationAvePt > acceptancethreshold && organisationAvePt > personAvePt && organisationAvePt > locationAvePt){
					    	isa = "ORGANISATION";
					    	confidence = String.valueOf(organisationAvePt);
					}
				}
				
			}
			catch(Exception e){
			    	
			}
		
			if(isa.equals("") && iteration <= 2 && !exacttitlematchfound){

			    Map<String,Double> ngrams 			= nga.getNGrams(entity);
			    Map<String,Double> ngramsrelfreq 	= nga.getNGramsRelFreq(ngrams,simobj);
			    String longestnonzerofreqngram = "";
				Map<String,Double> sortedngrams = MapSorter.sortMap(ngrams,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
				Iterator iterator4 = sortedngrams.keySet().iterator();
				while (iterator4.hasNext() && longestnonzerofreqngram.equals("")) {
					String ngram = (String) iterator4.next();
					double righttoleft 	= ngrams.get(ngram);
					double relfreq 		= ngramsrelfreq.get(ngram);
					System.err.println("\t" + ngram + " (" + righttoleft + ")(" + relfreq + ")");
					if(relfreq > 0 && !ngram.equals(entity)){
						longestnonzerofreqngram = ngram;
					}
				}
				if(!longestnonzerofreqngram.equals("")){
				    iteration++;
					String results = determineEntityClass(longestnonzerofreqngram,acceptancethreshold,iteration);
					
			       	String[] splitresults	= results.split(":");
			       	isa 			= splitresults[0];
			       	confidence 		= splitresults[1];
			       	deteriorate 	= splitresults[2];
				}
			}
			else{
				deteriorate = String.valueOf(iteration);
			}
			
			System.err.println("(ISA): " + isa);

		//}
		//else{
		//	System.err.println("NOT-ENTITY(" + entity + ")");
		//}
		System.err.println("==================================================");
		System.err.println("==================================================");

		
		return isa + ":" + confidence + ":" + deteriorate;
	}
	public Map<String,Double> compareEntityType(String title){
	    	Map<String,Double> istypeornot = new HashMap<String,Double>();
    		istypeornot.put("PERSON",0.0);
    		istypeornot.put("LOCATION",0.0);
    		istypeornot.put("ORGANISATION",0.0);
	    	
	    	
		List<String> senses = (new Conceptualiser()).findNeighbours(title,"POLYSEMY");
		if(senses.size() == 0){
		    	senses.add(title);
		}

		for(String sense: senses){
			List<String> parents = (new Conceptualiser()).findNeighbours(sense,"HYPERNYMY");
			if(parents.size() > 0){
				for(String parent: parents){
					//System.err.println("\t\t" + parent);
					Matcher matcherper = Pattern.compile("(?:people|occupations|actors|scientists|politicians|academics|artists|chefs|surnames|rulers|producers)",Pattern.CASE_INSENSITIVE).matcher(parent);
					if(matcherper.find()){
					    	double tmp = istypeornot.get("PERSON");
					    	tmp++;
				    		istypeornot.put("PERSON",tmp);
				    	}
					Matcher matcherloc = Pattern.compile("(?:suburbs|capitals|cities|countries|continents|places|towns|districts|electoral_divisions|roads|district|housing_estates|communes|prefectures|states_of|nations|regions_of|territories)",Pattern.CASE_INSENSITIVE).matcher(parent);
					if(matcherloc.find()){
					    	double tmp = istypeornot.get("LOCATION");
					    	tmp++;
				    		istypeornot.put("LOCATION",tmp);
				    	}
					Matcher matcherorg = Pattern.compile("(?:hospitals|manufacturers|consortium|schools|companies|organisations|universities|colleges|ministries)",Pattern.CASE_INSENSITIVE).matcher(parent);
					if(matcherorg.find()){
					    	double tmp = istypeornot.get("ORGANISATION");
					    	tmp++;
				    		istypeornot.put("ORGANISATION",tmp);
				    	}
	  			}
	  		}
   		}
   		
   		return istypeornot;
	}
	public String determineEntityClassOld(String entity, double acceptancethreshold){
		long timestart = System.currentTimeMillis();
	    	
	    	entity = entity.trim();
	    	    
		String isa 			= "";
		String confidence		= "0";
		
		double totalfreq 		= 0;
		double personfreq 		= 0;
		double locationfreq		= 0;
		double organisationfreq		= 0;
		double medicalcondfreq		= 0;
		double consumablefreq		= 0;
		double personsimstrfactor	= 0;
		double locationsimstrfactor	= 0;
		double organisationsimstrfactor	= 0;
		double medicalcondsimstrfactor	= 0;
		double consumesimstrfactor	= 0;
	    	try{
	    	    	String intextquery = "";
	    	    	String intitlequery = "";
	    	    	String[] toks = entity.split(" ");
	    	    	for(String tok: toks){
	    	    		intextquery = intextquery + tok + " AND ";
	    	    		intitlequery = tok;
	    	    	}
			Matcher replace1 = Pattern.compile(" AND $").matcher(intextquery);
			intextquery = replace1.replaceAll("");
	    	    	
			ModifiableSolrParams param = simobj.formulateQuery("titleText:" + intitlequery + " text:" + intextquery,15);
			QueryResponse response = solrserver.query(param);
			for (SolrDocument doc : response.getResults()){
			    	boolean isPerson 		= false;
			    	boolean isLocation 		= false;
			    	boolean isOrganisation 		= false;
			    	boolean isMedicalCond 		= false;
			    	boolean isConsumable	 	= false;
			    	Collection<String> fnames = doc.getFieldNames();
				for (String fname : fnames) {
					if(fname.equals("titleText")){
						String title = (String) doc.getFieldValue(fname);
	    					double strsim = FuzzyMatcher.stringSim(title,entity);
				    		//System.err.println("title:" + title);
	    					List<String> parents = (new Conceptualiser()).findNeighbours(title,"HYPERNYMY");
	    					if(parents.size() > 0){
		    					for(String parent: parents){
    					    	//System.err.println("\t" + parent);
								Matcher matcherper = Pattern.compile("(?:people|occupations|actors|scientists|politicians|academics|artists|chefs|surnames|rulers|producers)",Pattern.CASE_INSENSITIVE).matcher(parent);
								if(matcherper.find()){
		    					    		isPerson = true;
		    					    	}

								Matcher matcherloc = Pattern.compile("(?:capitals|cities|countries|continents|places|towns|districts|electoral_divisions|roads|district|housing_estates|communes|prefectures|states|nations|regions_of|territories)",Pattern.CASE_INSENSITIVE).matcher(parent);
								if(matcherloc.find()){
		    					    		isLocation = true;
		    					    	}

								Matcher matcherorg = Pattern.compile("(?:hospitals|manufacturers|consortium|schools|companies|organisations|universities|colleges)",Pattern.CASE_INSENSITIVE).matcher(parent);
								if(matcherorg.find()){
		    					    		isOrganisation = true;
		    					    	}

								Matcher matcherddi = Pattern.compile("(?:vascular-related_cutaneous_conditions|syndromes|diseases|disorders|substance_abuse|medical_emergencies|poisoning|symptoms_and_signs)",Pattern.CASE_INSENSITIVE).matcher(parent);
								if(matcherddi.find()){
		    					    		isMedicalCond = true;
		    					    	}

								Matcher matcherconsume = Pattern.compile("(?:meats|breakfasts|desserts|foods|food|appetizers|cuisine|dishes|dairy_products|supplements|drugs|analgesics|euphoriants|stimulants|neurotransmitters|opioids|opiates)",Pattern.CASE_INSENSITIVE).matcher(parent);
								if(matcherconsume.find()){
		    					    		isConsumable = true;
		    					    	}
		    					}
	    					    	if(strsim > personsimstrfactor && isPerson){
	    					    	    	personsimstrfactor = strsim;
	    					    	}
	    					    	if(strsim > locationsimstrfactor && isLocation){
	    					    	    	locationsimstrfactor = strsim;
	    					    	}
	    					    	if(strsim > organisationsimstrfactor && isOrganisation){
	    					    	    	organisationsimstrfactor = strsim;
	    					    	}
			    				if(strsim > medicalcondsimstrfactor && isMedicalCond){
	    					    	    	medicalcondsimstrfactor = strsim;
	    					    	}
			    				if(strsim > consumesimstrfactor && isConsumable){
	    					    	    	consumesimstrfactor = strsim;
	    					    	}
		    					totalfreq++;
		    				}
					}
				}
				if(isPerson){
				    	personfreq++;
				}
				if(isLocation){
				    	locationfreq++;
				}
				if(isOrganisation){
				    	organisationfreq++;
				}
				if(isMedicalCond){
				    	medicalcondfreq++;
				}
				if(isConsumable){
				    	consumablefreq++;
				}
			}
			
			double personscore 	=  CommonData.roundDecimal((personfreq/totalfreq)*personsimstrfactor*100,"##.####");
			double locationscore 	=  CommonData.roundDecimal((locationfreq/totalfreq)*locationsimstrfactor*100,"##.####");
			double organisationscore=  CommonData.roundDecimal((organisationfreq/totalfreq)*organisationsimstrfactor*100,"##.####");
			double medicalcondscore	=  CommonData.roundDecimal((medicalcondfreq/totalfreq)*medicalcondsimstrfactor*100,"##.####");
			double consumablescore	=  CommonData.roundDecimal((consumablefreq/totalfreq)*consumesimstrfactor*100,"##.####");
			
			if(personscore > acceptancethreshold && personscore >= locationscore && personscore >= organisationscore && personscore >= medicalcondscore && personscore >= consumablescore){
			    	isa = isa + "[PERSON]";
			    	confidence = String.valueOf(personscore);
			}
			if(locationscore > acceptancethreshold && locationscore >= personscore && locationscore >= organisationscore && locationscore >= medicalcondscore && locationscore >= consumablescore){
			    	isa = isa + "[LOCATION]";
			    	confidence = String.valueOf(locationscore);
			}
			if(organisationscore > acceptancethreshold && organisationscore >= personscore && organisationscore >= locationscore && organisationscore >= medicalcondscore && organisationscore >= consumablescore){
			    	isa = isa + "[ORGANISATION]";
			    	confidence = String.valueOf(organisationscore);
			}
			if(medicalcondscore > acceptancethreshold && medicalcondscore >= personscore && medicalcondscore >= locationscore && medicalcondscore >= organisationscore && medicalcondscore >= consumablescore){
			    	isa = isa + "[MEDICALCOND]";
			    	confidence = String.valueOf(medicalcondscore);
			}
			if(consumablescore > acceptancethreshold && consumablescore >= personscore && consumablescore >= locationscore && consumablescore >= organisationscore && consumablescore >= medicalcondscore){
			    	isa = isa + "[FOODDRUG]";
			    	confidence = String.valueOf(consumablescore);
			}
			
			//System.err.println("isPerson: (" + personscore + ") = (" + personfreq + "/" + totalfreq + ") x (" + personsimstrfactor + ")");
			//System.err.println("isLocation: (" + locationscore + ") = (" + locationfreq + "/" + totalfreq + ") x (" + locationsimstrfactor + ")");
			//System.err.println("isOrganisation: (" + organisationscore + ") = (" + organisationfreq + "/" + totalfreq + ") x (" + organisationsimstrfactor + ")");
			//System.err.println("isMedicalCond: (" + medicalcondscore + ") = (" + medicalcondfreq + "/" + totalfreq + ") x (" + medicalcondsimstrfactor + ")");
			//System.err.println("isFoodDrug: (" + consumablescore + ") = (" + consumablefreq + "/" + totalfreq + ") x (" + consumesimstrfactor + ")");
		}
		catch(Exception e){
		    	e.printStackTrace();
		}

		
		return isa + ":" + confidence;
	}
}