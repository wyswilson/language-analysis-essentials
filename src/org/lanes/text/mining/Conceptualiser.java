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

public class Conceptualiser{

	private HttpSolrServer solrserver	= null;
	private Similarity simobj			= null;
	
   	public Conceptualiser(){
		solrserver 	= new HttpSolrServer("http://localhost:8983/solr/wikipedia");
  		simobj		= new Similarity();
	}
	public String mapTermToConcept(String term){
		long timestart = System.currentTimeMillis();

	    String mappedconcept = "";
		try{
			List<String> parents = new ArrayList<String>();
		    	if(mappedconcept.equals("")){
		    	    //System.err.println("matchExact");
		    	    	
		    		mappedconcept = matchExact(term);
		    		if(!mappedconcept.equals("")){
		    			parents = findNeighbours(mappedconcept,"HYPERNYMY");
		    		}
		    		
		    		//System.err.println("mappedconcept: " + mappedconcept + ", parents.size: " + parents.size() + "");
		    	}
		    	if((mappedconcept.equals("")) || (!mappedconcept.equals("") && parents.size() == 0)){
		    	    //System.err.println("matchSynonym");

		    	    if(mappedconcept.equals("")){
		    			 mappedconcept = matchSynonym(term);
		    		}
		    		else{
		    			 mappedconcept = matchSynonym(mappedconcept);
		    		}
		    		
		    	    if(!mappedconcept.equals("")){

		    	    	parents = findNeighbours(mappedconcept,"HYPERNYMY");

		    	    }
		    	}
		    	//System.err.println("mappedconcept: " + mappedconcept + ", parents.size: " + parents.size() + "");
		    	
			if((!mappedconcept.equals("") && parents.size() == 0)){
		    	//System.err.println("resolvePolysemy");
		    	    	
		    	mappedconcept = resolvePolysemy(mappedconcept,term,findNeighbours(mappedconcept,"POLYSEMY"));
				
				//System.err.println("mappedconcept: " + mappedconcept + ", parents.size: " + parents.size() + "");
			}
		}
		catch(Exception e){
		}

		Matcher replace = Pattern.compile("\\s").matcher(mappedconcept);
		mappedconcept = replace.replaceAll("_");

		
		return mappedconcept;
	}
  	
	public String matchExact(String term){
		String mappedterm 	= "";

		
	    try{
			String termmod = term;
			termmod = "\"" + termmod + "\"";
			
			double maxsim = 0.90;
			
			ModifiableSolrParams param = simobj.formulateQuery("titleText:"+termmod,5);
			QueryResponse response = solrserver.query(param);
			for (SolrDocument doc : response.getResults()){
			    Collection<String> fnames = doc.getFieldNames();
				for (String fname : fnames) {
					if(fname.equals("titleText")){
						String title = (String) doc.getFieldValue(fname);
	    	
						//Matcher replace1 = Pattern.compile("[^\\w\\d]").matcher(title);
						//String titletmp = replace1.replaceAll(" ");
						//Matcher replace2 = Pattern.compile("[^\\w\\d]").matcher(term);
						//String termtmp = replace2.replaceAll(" ");
						
    		    		double sim = FuzzyMatcher.stringSim(title,term);

						//System.err.println("\t[" + title + "][" + sim + "]");
    		    				
						if(sim > maxsim){
						    mappedterm = title;
							maxsim = sim;
						}
						else{
						}
					}
				}
			}
		}
		catch(Exception e){
		    	e.printStackTrace();
		}

		
		return mappedterm;
	}
	public String resolvePolysemy(String term, String originalterm, List<String> senses){
		String mappedterm 	= "";
		
		Map<String,Double> weightedsenses = new HashMap<String,Double>();
    	for(String sense: senses){
    	    	double sim = simobj.distributedSimilarity(sense,originalterm);
    	    	weightedsenses.put(sense,sim);
    	}

    	Map<String,Double> sortedsenses = MapSorter.sortMap(weightedsenses,"DESC");//MAPPED TO THE LONGEST ONE, MORE SPECIFIC
		Iterator iterator1 = sortedsenses.keySet().iterator();
		while (iterator1.hasNext()) {
			String sense 	= (String) iterator1.next();
			double weight 	= weightedsenses.get(sense);
			if(mappedterm.equals("")){
				mappedterm = sense;
			}
		}

		
		return mappedterm;
	}
	public String matchSynonym(String term){
		String mappedterm 	= "";

		//System.err.println("match-synonym[" + term + "]");
		
		double maxsim = 0;
		
		String tokenstr = "";
		String[] tokens = term.split(" ");
		for(String token: tokens){
		    	tokenstr = tokenstr + "[" + token + "]";
		}
		
	   	List<String> synonyms = findNeighbours(term,"SYNONYMY");
	   	for(String synonym: synonyms){
 		   	Double[] sims 		= simobj.groupAverageSimilarity("[" + synonym + "]" + tokenstr);
 		   	double sim 		= sims[0];
 		   	double simnonzero 	= sims[1];

			//System.err.println("\t[" + synonym + "][" + sim + "]");
			if(simnonzero > maxsim){
			    	mappedterm =  synonym;
				maxsim = simnonzero;
			}
			else{
			}
	    }

		
		return mappedterm;
	}
	public List<String> findNeighbours(String concept, String type){
		return findNeighbours(concept,type,0);
	}
	public List<String> findNeighbours(String concept, String type, int iteration){
	    DatabaseConnection dbase = new DatabaseConnection();
		Connection conn = dbase.establishConnection("lanes");

	    List<String> neighbours = new ArrayList<String>();

		//System.err.println("find-neighbours[" + concept + "][" + type + "]");
	    	
		Matcher replace = Pattern.compile("\\s").matcher(concept);
		concept = replace.replaceAll("_");
	    	
		try{
			PreparedStatement statement1 = conn.prepareStatement("SELECT arg2 FROM category WHERE arg1 = ? AND reltype = ?");
			statement1.setString(1, concept);
			statement1.setString(2, type);
			ResultSet rs1 = statement1.executeQuery();
			while(rs1.next()){
				String neighbour = rs1.getString(1);
				neighbours.add(neighbour);
			}
			rs1.close();
			statement1.close();
		}
		catch(Exception e){
		    	e.printStackTrace();
		}
		
		dbase.closeConnection(conn);
		
		if(neighbours.size() == 0 && !type.equals("SYNONYMY") && iteration == 0){
		    	List<String> synonyms = findNeighbours(concept,"SYNONYMY");
	    	   	iteration++;
		    	for(String synonym: synonyms){
		    		List<String> neighs = findNeighbours(synonym,type,iteration);
		    		neighbours.addAll(neighs);
		    	}
		}


		
	    return neighbours;
	}
}