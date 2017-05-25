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

public class TimeDate{
    public String getCurrentTimeDate(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currenttimedate = sdf.format(cal.getTime());
		return currenttimedate;
	}
    public String getCurrentDatestamp(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String currenttimestamp = sdf.format(cal.getTime());
		return currenttimestamp;
	}
   	public String getCurrentTimestamp(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String currenttimestamp = sdf.format(cal.getTime());
		return currenttimestamp;
	}
	public String getCurrentTime(){
	    return getCurrentTime("");
	}
	public String getCurrentTime(String separator){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH" + separator + "mm" + separator + "ss");
		String currenttime = sdf.format(cal.getTime());
		return currenttime;
	}
	
}