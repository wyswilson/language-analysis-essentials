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

import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.*;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.morph.*;
import net.didion.jwnl.util.cache.*;
import net.didion.jwnl.util.factory.*;

public class Lemmatiser{
	
	public String lemmatisePhrase(String phrase, String phrasetype){
		String lemma = phrase;
		try{
		    JWNL.initialize( new FileInputStream(CommonData.wordnetconfig) );
	        Dictionary wordnet = Dictionary.getInstance();
	       
	        String headword = "";
	        String leftpart = "";
			Matcher matchheadword = Pattern.compile("\\s([^\\s]+)$").matcher(phrase);
			if(matchheadword.find()){
				headword = matchheadword.group(1);
				if(headword.contains("-")){
					headword = phrase;
					phrasetype = "DONOTLEMMATISE";
				}
				else{
					Matcher replace3 = Pattern.compile("\\s[^\\s]+$").matcher(phrase);
			 		leftpart = replace3.replaceAll("");	       
				}				
			}
			else{
				headword = phrase;
			}
			//System.out.println(leftpart + " - " + headword);

	        POS pos = null;
	        if(phrasetype.equals("NOUNPHRASE")){
	    	   pos = POS.NOUN;
	        }
	        else if(phrasetype.equals("VERBPHRASE")){
	    	   pos = POS.VERB;
	        }
	       
	        

	        if(pos != null){
		       List<String> baseforms = wordnet.getMorphologicalProcessor().lookupAllBaseForms(pos, headword);
		       lemma = baseforms.get(0);
	        }
			//System.out.println(leftpart + " - " + lemma);

	        if(!leftpart.equals("")){
	    	   leftpart = leftpart.trim();
	    	   lemma = leftpart + " " + lemma;
	        }
		}
		catch(JWNLException e){
			//System.err.println("1:" + e.getMessage());
		}
		catch(FileNotFoundException e){
			//System.err.println("2:" + e.getMessage());
		}		
		catch(Exception e){
			//System.err.println("3:" + e.getMessage());
		}				
		
		return lemma;
	}


}