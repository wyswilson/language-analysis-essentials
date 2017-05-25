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

package org.lanes.text.mining;

//////////////STANDARD///////////////////
import org.lanes.text.nlp.*;
import org.lanes.utility.*;
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

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import org.apache.commons.lang.StringUtils;

public class UnithoodAnalyser {

	private HttpSolrServer solrserver	= null;
	private Similarity simobj		= null;
	
	public UnithoodAnalyser (){
		solrserver 	= new HttpSolrServer(CommonData.corpuswikiconfig);
		simobj		= new Similarity();
	}

    public boolean isUnit(String potentialphrase, String connector) {

        String left = "";
        String right = "";
        String current = "left";
        for (String token : potentialphrase.split(" ")) {

            if (!token.equals(connector) && current.equals("left")) {
                left = left + token + " ";
            } else if (token.equals(connector)) {
                current = "right";
            } else if (current.equals("right")) {
                right = right + token + " ";
            }
        }
        Matcher replace1 = Pattern.compile(" $").matcher(left);
        left = replace1.replaceAll("");
        Matcher replace2 = Pattern.compile(" $").matcher(right);
        right = replace2.replaceAll("");

        boolean isunitornot = false;
        if (!left.equals("") && !right.equals("")) {
        	isunitornot = isUnit(left, connector, right);
        }

        return isunitornot;
    }

    public boolean isUnit(String left, String connector, String right) {
        long timestart = System.currentTimeMillis();

        double N = simobj.getTotalDocCount();

        //double ns = simobj.getTitleCount(left + " " + connector + " " + right);
        //double ps = ns/N;
        
        boolean isunitornot = false;
        //if(ps > 0){
	        double nx = simobj.getDocCount(left);
	        double ny = simobj.getDocCount(right);
	
	        double px = nx / N;
	        double py = ny / N;
	        
	        double nxy = simobj.getDocCount(left, right);
		        
	        double pxy = nxy / N;
	        
	        double pmi = Math.log10(pxy / (px * py));
	        double normalisedpmi = pmi/-Math.log10(pxy);
	        
	        double idrleft = (nx - nxy) / nx;
	        double idrrght = (ny - nxy) / ny;
	        double idr = idrleft * idrrght;	        
	        
	        if((pmi >= 1.1) || ((0.5 <= pmi && pmi < 0.7) && (idr < 0.6)) || ((0.7 <= pmi && pmi < 0.9) && (0.6 <= idr && idr < 0.75)) || ((0.9 <= pmi && pmi < 1.1) && (0.75 <= idr && idr < 0.95))){
	        	isunitornot = true;
	        }
	        
        //}
   
        return isunitornot;
    }
}