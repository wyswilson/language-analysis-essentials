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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.net.*;
////////////////////////////////////////

public class DatabaseConnection{
    public Connection establishConnection(String databasename){
		Connection conn = null;
		
		
		String url = "jdbc:mysql://" + CommonData.connectionstr + "/" + databasename;//
		try{
			java.util.Properties connProperties = new java.util.Properties();
			connProperties.put("user", CommonData.username);
			connProperties.put("password", CommonData.password);
			connProperties.put("autoReconnect", "true");
			connProperties.put("maxReconnects", "3");
    			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//conn = DriverManager.getConnection(url, userName, password);
			conn = DriverManager.getConnection(url, connProperties);
		}
		catch (Exception e){
			System.out.println(e);
		}
 		
		return conn;
    }
    public void closeConnection(Connection conn){
    	if (conn != null){
			try{
	   			conn.close ();
			}
			catch (Exception e){}
		}
    }
    public String generateCommaSeparatedQuery(Map input){
    	
 		String inquery 	= "";
 		int cnttmp 	= 0;
 		Iterator iterator = input.keySet().iterator();
		while (iterator.hasNext()) {
			String data = (String) iterator.next();
			inquery 	= inquery + "'" + data + "'";
			if(cnttmp < input.size()-1){
				inquery = inquery + ",";
			}
			cnttmp++;
		}
 		
		return inquery;
    }
    public String escapeRegex(String str) {
		Pattern escaper = Pattern.compile("([^a-zA-z0-9])");
	    return escaper.matcher(str).replaceAll("\\\\$1");
	}
}