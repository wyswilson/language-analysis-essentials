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

public class SentenceAnalyser{
	
	private long exetimesofar = 0;

	private Map<String,Double> sentencelevelmetadata = new HashMap<String,Double>();
	
    	public SentenceAnalyser(String input){
		long timestart = System.currentTimeMillis();

		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 			= dbase.establishConnection("lanes");

		double sentiment	= 0;
		double affirmation 	= 0;
		double greeting 	= 0;
		double question 	= 0;
		double cancellation = 0;
		
		Map<String,String> emoacks = new HashMap<String,String>();
		try{
			Statement s1 = conn.createStatement ();
			s1.executeQuery("SELECT lex_variable,lex_value FROM lexicon WHERE (lex_variable = 'HAPPY' OR lex_variable = 'SAD' OR lex_variable = 'YES' OR lex_variable = 'NO' OR lex_variable = 'GREET' OR lex_variable = 'QUESTION' OR lex_variable = 'CANCEL')");
			ResultSet rs1 = s1.getResultSet ();
			while(rs1.next()){
				String var 		= rs1.getString("lex_variable");
				String values 	= rs1.getString("lex_value");
				Matcher matchervals = Pattern.compile("\\{([^\\{\\}]+)\\}").matcher(values);
				while(matchervals.find()) {
			        String value = matchervals.group(1);
					emoacks.put(value,var);
				}
			}
			rs1.close ();
			s1.close ();
		}
		catch (Exception e){
		    	e.printStackTrace();
		}
		
		Iterator iterator2 = emoacks.keySet().iterator();
		while (iterator2.hasNext()) {
			String val 	= (String) iterator2.next();
	        	String vartype 	= emoacks.get(val);

			String matchfoundmiddle = "FALSE";
			Matcher matchervarval1 = Pattern.compile("\\b" + val + "\\b").matcher(input);
			if(matchervarval1.find()) matchfoundmiddle = "TRUE";

			String matchfoundstart = "FALSE";
			Matcher matchervarval2 = Pattern.compile("^" + val + "\\b").matcher(input);
			if(matchervarval2.find()) matchfoundstart = "TRUE";
			
			if(matchfoundmiddle.equals("TRUE") && vartype.equals("HAPPY")){
				sentiment++;
			}
			if(matchfoundmiddle.equals("TRUE") && vartype.equals("SAD")){
				sentiment--;
			}
			if(matchfoundmiddle.equals("TRUE") && vartype.equals("YES")){
				affirmation++;
			}
			if(matchfoundmiddle.equals("TRUE") && vartype.equals("NO")){
				affirmation--;
			}
			if(matchfoundstart.equals("TRUE") && vartype.equals("GREET")){
				greeting++;
			}
			if(matchfoundstart.equals("TRUE") && vartype.equals("QUESTION")){
				question++;
			}
			if((matchfoundstart.equals("TRUE") || matchfoundmiddle.equals("TRUE")) && vartype.equals("CANCEL")){
				cancellation++;
			}
   		}
   		
		sentencelevelmetadata.put("sentiment",sentiment);
		sentencelevelmetadata.put("affirmation",affirmation);
		sentencelevelmetadata.put("greeting",greeting);
		sentencelevelmetadata.put("question",question);
		sentencelevelmetadata.put("cancellation",cancellation);
		
		dbase.closeConnection(conn);

		exetimesofar = (System.currentTimeMillis() - timestart);
    }

 	public double getSentiment(String type){
		return sentencelevelmetadata.get(type);
    }
}