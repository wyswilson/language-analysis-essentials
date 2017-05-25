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

package org.lanes.utility;

//////////////STANDARD///////////////////
import java.sql.*;
import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.net.*;
////////////////////////////////////////

public class Lexicon{
	public static Map<String,String> getStopWords(){
		DatabaseConnection dbase	= new DatabaseConnection();
		Connection conn				= dbase.establishConnection("lanes");
		
		Map<String,String> stopwords = new HashMap<String,String>();
	    try{
			String statement = "SELECT lex_value FROM lexicon WHERE lex_type = 'STOPWORD'";
			PreparedStatement stopwlookup = conn.prepareStatement(statement);
			ResultSet resultset = stopwlookup.executeQuery();
			if(resultset.next()){
				String stops = resultset.getString(1);
				Matcher matchervals = Pattern.compile("\\{([^\\{\\}]+)\\}").matcher(stops);
				while(matchervals.find()) {
			        String value = matchervals.group(1);
			        stopwords.put(value,"");
				}
				
			}
			resultset.close();
			stopwlookup.close();
		}
		catch (Exception e){
		}
		
		dbase.closeConnection(conn);
		
		return stopwords;
	}
	public static Map<String,Double> expandPhrases(Map<String,Double> phrases){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 			= dbase.establishConnection("lanes");

		Map<String,Double> expandedphrases 	= new HashMap<String,Double>();
		Map<String,String> wordvariants 	= new HashMap<String,String>();
		try{
			Statement s1 = conn.createStatement();
			s1.executeQuery("SELECT lex_variable,lex_value FROM lexicon WHERE lex_type = 'WORDVARIANT'");
			ResultSet rs1 = s1.getResultSet ();
			while(rs1.next()){
				String type 		= rs1.getString("lex_variable");
				String words 		= rs1.getString("lex_value");
				Matcher matchervals = Pattern.compile("\\{([^\\{\\}]+)\\}").matcher(words);
				while(matchervals.find()) {
			        String value = matchervals.group(1);
			        wordvariants.put(value,type);
				}
	 		}
			rs1.close ();
			s1.close ();
			
			Iterator iterator1 = phrases.keySet().iterator();
			while (iterator1.hasNext()) {
				String phrase = (String) iterator1.next();
				double weight = phrases.get(phrase);
				if(wordvariants.containsKey(phrase)){
					String typetomatch = wordvariants.get(phrase);
					Iterator iterator2 = wordvariants.keySet().iterator();
					while (iterator2.hasNext()) {
						String variant 	= (String) iterator2.next();
						String type 	= wordvariants.get(variant);
						if(typetomatch.equals(type)){
							expandedphrases.put(variant,weight);
						}
					}
				}
				else{
					expandedphrases.put(phrase,weight);
				}
			}
 		}
		catch (Exception e){
		}

 		dbase.closeConnection(conn);

		return expandedphrases;
	}
	
	///////////FOR QUESTIONANSWERING// REQUIRED TABLES ARE GONE//////////////
	public Map<String,String> detectTopics(Map phrases){
    	
    		Map<String,String> phrasetopics = new HashMap<String,String> ();
    	
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");
		
		Iterator iterator = phrases.keySet().iterator();
		while (iterator.hasNext()) {
			String phrase = (String) iterator.next();
			String tops = "";
			try{
			    String[] tokens = Pattern.compile(" ").split(phrase);
			    for (int i = 0; i < tokens.length; i++) {
     				String phr = tokens[i];
					Statement s1 = conn.createStatement ();
					String statement1 = "SELECT topic_topic FROM topic WHERE topic_keyword = '" + phr + "'";
					s1.executeQuery(statement1);
					ResultSet rs1 = s1.getResultSet ();
					while(rs1.next()){
						String top = rs1.getString("topic_topic");
						Matcher matchertop = Pattern.compile("\\[" + top + "\\]").matcher(tops);
						if(!matchertop.find()) {
							tops = tops + "[" + top + "]";
						}
					}
					rs1.close ();
					s1.close ();
				}
			}
			catch (Exception e){
			}
 			phrasetopics.put(phrase,tops);
  		}
		
		dbase.closeConnection(conn);
		
		return phrasetopics;
    }
 	public int getN(){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");
		
		int n = 0;
		try{
			PreparedStatement statement = conn.prepareStatement("select count(*) as n from wikipedia_lookupreverse");
			ResultSet resultset = statement.executeQuery();
			if(resultset.next()) {
				n = resultset.getInt(1);
			}
 			resultset.close ();
			statement.close ();
		}
		catch (Exception e){
		}

		dbase.closeConnection(conn);
		
		return n;
	}
	public static Map<String,String> getTopicList(){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");

		Map<String,String> topics = new HashMap<String,String> ();
		
		try{
			Statement s1 = conn.createStatement ();
			s1.executeQuery("SELECT DISTINCT(topic_topic) FROM topic");
			ResultSet rs1 = s1.getResultSet ();
			while(rs1.next()){
				String topic = rs1.getString("topic_topic");
				topics.put(topic,topic);
	 		}
 			rs1.close ();
			s1.close ();
		}
		catch (Exception e){
		}

 		dbase.closeConnection(conn);

		return topics;
 	}
 	public Map<String,String> getAttributesAssociatedWithTopic(String domain, String topic){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");

		Map<String,String> attributes = new HashMap<String,String> ();
		
		try{
			Statement s1 = conn.createStatement();
			String statement1 = "SELECT qapair_attrs FROM qapair_domain_" + domain + " WHERE qapair_keywords LIKE '%[" + topic + "]%'";
			s1.executeQuery(statement1);
			ResultSet rs1 = s1.getResultSet ();
			while(rs1.next()){
				String attrs = rs1.getString("qapair_attrs");
				Matcher matchert = Pattern.compile("\\{([^\\{\\}]+)\\}").matcher(attrs);
				while(matchert.find()){
		       	 	String attr = matchert.group(1);
					Matcher matcherrepl = Pattern.compile("_").matcher(attr);
					attr = matcherrepl.replaceAll(" ");
					attributes.put(attr,topic);
				}
	 		}
 			rs1.close ();
			s1.close ();
		}
		catch (Exception e){
		}

 		dbase.closeConnection(conn);

		return attributes;
	}
	public static String[] containsTopic(String possiblytopic){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");

		String[] outs = new String[2];
		try{
			Statement s1 = conn.createStatement();
			for(String token : possiblytopic.split(" ")){
				String statement1 = "SELECT topic_topic FROM topic WHERE topic_keyword = '" + token + "'";
				s1.executeQuery(statement1);
				ResultSet rs1 = s1.getResultSet();
				while(rs1.next()){
				    outs[0] = token;
				    outs[1] = rs1.getString("topic_topic");
				    return outs;
		 		}
	 			rs1.close ();
		 	}
			s1.close ();
		}
		catch (Exception e){
		}
		
 		dbase.closeConnection(conn);
		
		return outs;
	}
	public static String isTopic(String topic){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");

		try{
			Statement s1 = conn.createStatement();
			String statement1 = "SELECT topic_topic FROM topic WHERE topic_keyword = '" + topic + "'";
			s1.executeQuery(statement1);
			ResultSet rs1 = s1.getResultSet();
			while(rs1.next()){
			    return rs1.getString("topic_topic");
	 		}
 			rs1.close ();
			s1.close ();
		}
		catch (Exception e){
		}

 		dbase.closeConnection(conn);

		return "";
	}
	public static boolean isDomain(String domain){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");

		try{
			Statement s1 = conn.createStatement();
			String statement1 = "SELECT topic_keyword FROM topic WHERE topic_topic = '" + domain + "'";
			s1.executeQuery(statement1);
			ResultSet rs1 = s1.getResultSet();
			while(rs1.next()){
			    return true;
	 		}
 			rs1.close ();
			s1.close ();
		}
		catch (Exception e){
		}

 		dbase.closeConnection(conn);

		return false;
	}
	public static Map<String,String> getTopicKeywordList(String domain){
		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 		= dbase.establishConnection("lanes");

		Map<String,String> keywordtotopic = new HashMap<String,String> ();

		try{
			Statement s1 = conn.createStatement();
			String statement1 = "SELECT topic_topic,topic_keyword FROM topic";
			if(!domain.equals("")){
				statement1 = "SELECT topic_topic,topic_keyword FROM topic WHERE topic_topic = '" + domain + "'";
			}
			s1.executeQuery(statement1);
			ResultSet rs1 = s1.getResultSet ();
			while(rs1.next()){
				String topic = rs1.getString("topic_topic");
				String keyword = rs1.getString("topic_keyword");
				Matcher matcherrepl = Pattern.compile("_").matcher(keyword);
				keyword = matcherrepl.replaceAll(" ");
				keywordtotopic.put(keyword,topic);
	 		}
 			rs1.close ();
			s1.close ();
		}
		catch (Exception e){
		}

 		dbase.closeConnection(conn);

		return keywordtotopic;
 	}
    public boolean sameTopic(String phrase1, String phrase2){

		DatabaseConnection dbase 	= new DatabaseConnection();
		Connection conn 			= dbase.establishConnection("lanes");
		
		boolean issametopic = false;
		try{
			String topic1 = "";
			String topic2 = "";
			Statement s1 = conn.createStatement ();
			s1.executeQuery("SELECT topic_topic FROM topic WHERE topic_keyword = '" + phrase1.toLowerCase() + "'");
			ResultSet rs1 = s1.getResultSet ();
			while(rs1.next()){
				topic1 = rs1.getString("topic_topic");
			}
			rs1.close();
			s1.close();

			Statement s2 = conn.createStatement ();
			s2.executeQuery("SELECT topic_topic FROM topic WHERE topic_keyword = '" + phrase2.toLowerCase() + "'");
			ResultSet rs2 = s2.getResultSet ();
			while(rs2.next()){
				topic2 = rs2.getString("topic_topic");
			}
			rs2.close();
			s2.close();
			
			if(!topic1.equals("") && !topic2.equals("") && topic1.equals(topic2)){
				issametopic = true;
			}
		}
		catch (Exception e){
		}

		dbase.closeConnection(conn);
		
		return issametopic;
	}
	//////////////////////////////////////////////////////////////////
}