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

public class FileIO{
	
	public FileIO(){
	    
	}
	public Map<String,String> readBufferToHash(String classpathtoresource){
		Map<String,String> map = new HashMap<String,String>();
		try {
		    BufferedReader buffreader 	= null;
			InputStream inputstream 	= getClass().getResourceAsStream(classpathtoresource);
			URL urlforreading 			= getClass().getResource(classpathtoresource);
			if(inputstream != null){//IF NOT FILE (IE, DIRECTORY), CANNOT BE READ
				buffreader = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
				//System.err.println("file-io: file [" + classpathtoresource + "] opened");
			}
			else{
				//System.err.println("file-io error: file [" + classpathtoresource + "] cannot be opened");
			}

		    	
			String nextline = "";
			while ((nextline = buffreader.readLine()) != null){
				nextline = nextline.trim().toLowerCase();
				if(!nextline.equals("")){
					map.put(nextline,"");
				}
			}
		}
		catch (IOException e){
			//System.out.println("file-io error: " + e.getMessage());
		}
		return map;
	}
	public static String readFile(String filepathname){
		String text = "";
		try {
			String nextline = "";
			BufferedReader buffreader = new BufferedReader(new FileReader(filepathname));
			while ((nextline = buffreader.readLine()) != null){
				text = text + nextline;
			}
		}
		catch (IOException e){
			System.out.println("file-io error: " + e.getMessage());
		}
		return text;
	}
    public static boolean writeFile(String filepathname, String content, boolean append){
		boolean written = false;
		try{
			BufferedWriter fileout = new BufferedWriter(new FileWriter(filepathname,append));
			fileout.write(content);
			fileout.close();
			written = true;
		}
		catch (Exception e){
			System.out.println("file-io error: " + e.getMessage());
		}

		return written;
   	}
	public static List<File> getFileListing(File aStartingDir) throws FileNotFoundException {
		validateDirectory(aStartingDir);
		List<File> result = getFileListingNoSort(aStartingDir);
		Collections.sort(result);
		return result;
	}
	private static List<File> getFileListingNoSort(File aStartingDir) throws FileNotFoundException {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for(File file : filesDirs) {
			result.add(file); //always add, even if directory
			if ( ! file.isFile() ) {
				List<File> deeperList = getFileListingNoSort(file);
				result.addAll(deeperList);
			}
		}
		return result;
	}
	private static void validateDirectory (File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
		  throw new IllegalArgumentException("directory should not be null.");
		}
		if (!aDirectory.exists()) {
		  throw new FileNotFoundException("directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
		  throw new IllegalArgumentException("is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
		  throw new IllegalArgumentException("directory cannot be read: " + aDirectory);
		}
	}
}