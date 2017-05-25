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
    
public class CommonData {
	
	public static String serverip	 		= "127.0.0.1";
	public static String lemmatiserconfig 	= "E:\\lanes\\data\\conf\\file_properties.xml";
	public static String corpuswikiconfig 	= "http://127.0.0.1:8983/solr/wikipedia";
	public static String connectionstr 		= "127.0.0.1:3306";
	public static String username 			= "root";
	public static String password 			= "";//
	public static double roundDecimal(double d, String format) {
		double rounded = 0;
		try{
	        DecimalFormat twoDForm = new DecimalFormat(format);
			rounded = Double.valueOf(twoDForm.format(d));
		}
		catch (Exception e){
		}
		return rounded;
	}
	public static int numberOfWords(String str){
	    	String[] tokens = str.split("\\s");
	    	
	    	return tokens.length;
	}
}