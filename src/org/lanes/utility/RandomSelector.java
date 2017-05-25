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

public class RandomSelector{
	private double []Totals;

    	public RandomSelector (double []weights) {
        	Totals = new double[weights.length];
        	initWRNG(weights);
    	}
    	public RandomSelector () {
   	}
 	public static int nonWeightedRandomNumber(int size){
 		Random randomGenerator = new Random();
		return randomGenerator.nextInt(size);
 	}
	/*
	* Initializing function of Random Number generator
	* @param weights in a double array. Note that the weights here are assumed to
	* be positive. If there are negative ones. Sort the Totals array before the binary search
	*/
	private void initWRNG (double []weights) {
		double runningTotal = 0;
		int i = 0;
		for (double w : weights) {
		    runningTotal += w;
		    Totals[i++] = runningTotal;
		}
	}
	/*
	* @return the weighted random number. Actually this sends the weighted randomly
	* selected index of weights vector.
	*/
	public int next() {
		Random rnd = new Random(System.nanoTime());
		double rndNum = rnd.nextDouble() * Totals[Totals.length - 1];
		int sNum = Arrays.binarySearch(Totals, rndNum);
		int idx = (sNum < 0) ? (Math.abs(sNum) - 1) : sNum;
		return idx;
	}
}