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

public class KeyphraseAnalyser{

	private long exetimesofar = 0;
	
	private Map<String,Double> keyphrases 	= new HashMap<String,Double>();
	private double averageweight 		= 0;
	
   	public KeyphraseAnalyser(Map<String,String> phrases){
		long timestart = System.currentTimeMillis();

		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 			= dbase.establishConnection("lanes");
		
    	Similarity solr = new Similarity();
		
		Map<String,Double> phraseweights = solr.inverseDocFreq(phrases);
		
		double sumweight 	= 0;
		double cnt			= 0;
		Iterator iterator = phrases.keySet().iterator();
		while (iterator.hasNext()) {
			String phrase 		= (String) iterator.next();
	        String phrasetype 	= phrases.get(phrase);
			
			double r = 0;
			if(phrase.length() > 1 && !phrasetype.equals("NULL") && phraseweights.containsKey(phrase)){
				r = phraseweights.get(phrase);
				if(r > 0){
					sumweight = sumweight + r;
					cnt++;
				}
			}
			keyphrases.put(phrase,r);
		}
		if(cnt > 0){
			averageweight = sumweight/cnt;
		}
		
		dbase.closeConnection(conn);

		exetimesofar = (System.currentTimeMillis() - timestart);
  	}
 	public Map<String,Double> getPhraseScores(){
		return keyphrases;
	}
}