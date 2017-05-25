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

public class SocketCommunication{
	private InetAddress serveraddr = null;
	private ServerSocket svrsocket = null;
 	private Socket connection = null;
 	private String module = "";
	
	public SocketCommunication(String ipaddr, int port, String type) {
		try{
	    		if(type.equals("server")){
				serveraddr 	= InetAddress.getByName(ipaddr);
				svrsocket 	= new ServerSocket(port, 20, serveraddr);
			}
			else{
				connection = new Socket(ipaddr,port);
			}
		}
		catch(Exception exception){
			System.out.println(exception.getMessage());
		}
    	}
    	public void sendClientRequest(String toserver){
		try{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			w.write(toserver,0,toserver.length());
			w.newLine();
			w.flush();
		}
		catch(Exception exception){
			System.out.println(exception.getMessage());
		}
		System.out.println("\tmessage (" + toserver + ") sent (" + connection.toString().toLowerCase() + ")");
    	}
    	public String waitForServerResponse(){
    	    	String serverresp = "";
		try{
			BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			serverresp = r.readLine().trim();
		}
		catch(Exception exception){
			System.out.println(exception.getMessage());
		}
		System.out.println("\tmessage (" + serverresp + ") received (" + connection.toString().toLowerCase() + ")");
		
		return serverresp;
    	}
	public String[] parseCommunication(String communication){
	    	String[] response = new String[3];

		Matcher matcheroutputs = Pattern.compile("\\{(.*?)\\}").matcher(communication);
		if(matcheroutputs.find()){
			String output = matcheroutputs.group(1);
			if(!output.equals("")){
				String[] temp1 	= output.split(";");
				response[0]	= temp1[0].split(":")[1];
				response[1] 	= temp1[1].split(":")[1];
				String[] temp2 	= temp1[2].split(":");
				if(temp2.length > 1){
					response[2] = temp2[1];
				}
				else{
				    	response[2] = "";
				}
			}
		}
		
		return response;
	}
	public String waitForClientRequest(){
		String input = "";
		try{
			connection 			= svrsocket.accept();
			BufferedReader inputstream 	= new BufferedReader(new InputStreamReader(connection.getInputStream()));
			input = inputstream.readLine();
		}
		catch(Exception exception){
			System.out.println(exception.getMessage());
		}
		System.out.println("\tmessage (" + input + ") received (" + connection.toString().toLowerCase() + ")");
			
		return input;
	}
	public void sendServerMessage(String msg){
		try{
			PrintWriter outstream = new PrintWriter(connection.getOutputStream(), true);
			outstream.println(msg);
		}
		catch(Exception exception){
			System.out.println(exception.getMessage());
		}
		System.out.println("\tmessage (" + msg + ") sent (" + connection.toString().toLowerCase() + ")");
	}
}