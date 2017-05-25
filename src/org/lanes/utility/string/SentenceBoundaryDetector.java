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

package org.lanes.utility.string;

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

import LBJ2.nlp.*;

public class SentenceBoundaryDetector{
    	public static Map<Integer,String> split(String text){
	 	String[] input = {text};
	   	Map<Integer,String> individualsentences = new HashMap<Integer,String>();
	    	
	    	int j = 0;
		SentenceSplitter splitter = new SentenceSplitter(input);
		Sentence[] sentences = splitter.splitAll();
		for (int i = 0; i < sentences.length; i++){
			String sentence = sentences[i].text.trim();
			//System.out.println(i + ":" + j + ":" + sentence);
			Matcher findhttp = Pattern.compile("(?:more information here|http:)",Pattern.CASE_INSENSITIVE).matcher(sentence);
			if(!findhttp.find() && !sentence.equals("")){
				individualsentences.put(j,sentence);
				j++;
			}
		}
		
		return individualsentences;
	}
}