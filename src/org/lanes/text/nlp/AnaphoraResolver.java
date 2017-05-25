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

public class AnaphoraResolver{

	private long exetimesofar = 0;

	private Map<String,String> newcontextstopic = new HashMap<String,String>();
	
	public Map<String,String> resolveAnaphora(Map<String,String> phrases, Map<String,String> phrasestopics, Map<String,Double> oldcontexts, Map<String,String> oldcontextstype, Map<String,String> oldcontextstopic, String domain){
		long timestart = System.currentTimeMillis();
		
		Map<String,String> newcontextstype = new HashMap<String,String> ();

		Map<String,Double> sortedoldcontexts = MapSorter.sortMap(oldcontexts,"DESC");

		String referent 	= "";
		String referenttype 	= "";
		String referenttopic 	= "";
		Iterator iterator1 = phrases.keySet().iterator();
		while (iterator1.hasNext()) {
			String potentialpronoun			= (String) iterator1.next();
			String potentialpronountype 	= phrases.get(potentialpronoun);
			String potentialpronountopic 	= phrasestopics.get(potentialpronoun);
			if(potentialpronountype.equals("REFERENT")){
				Iterator iterator2 = sortedoldcontexts.keySet().iterator();
				while(iterator2.hasNext() && referent.equals("")) {
					String phrase 		= (String) iterator2.next();
					String phrasetype 	= oldcontextstype.get(phrase);
					String phrasetopic 	= oldcontextstopic.get(phrase);
					if(phrasetopic != null){
						Matcher matchertopic = Pattern.compile(domain).matcher(phrasetopic);
						if(phrasetype.equals("NOUNPHRASE") && !phrasetopic.equals("") && matchertopic.find()){
							referent 		= phrase;
							referenttype 	= phrasetype;
							referenttopic 	= phrasetopic;
						}
					}
				}
				newcontextstype.put(referent,referenttype);
				newcontextstopic.put(referent,referenttopic);
			}
			else{
				newcontextstype.put(potentialpronoun,potentialpronountype);
				newcontextstopic.put(potentialpronoun,potentialpronountopic);
			}
		}
		
		exetimesofar = (System.currentTimeMillis() - timestart);

		return newcontextstype;
	}
	public Map<String,String> getRevisedTopic(){
		return newcontextstopic;
	}
}