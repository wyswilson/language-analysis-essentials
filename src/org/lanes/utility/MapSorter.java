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

public class MapSorter{
	public static Map sortMap(Map unsortMap, String type) {

		List list = new LinkedList(unsortMap.entrySet());

		//sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
			return ((Comparable) ((Map.Entry) (o1)).getValue())
			.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		if(type.equals("DESC")) Collections.reverse(list);
		//put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
    }
    public static Map sliceByValue(Map wholemap, String valueslice){
    	Map slice = new HashMap();
    	Iterator iterator1 = wholemap.keySet().iterator();
		while (iterator1.hasNext()) {
			Object key   = iterator1.next();
			Object value = wholemap.get(key);
			if(value.equals(valueslice)){
				slice.put(key,value);
			}
		}

		return slice;
    }
}