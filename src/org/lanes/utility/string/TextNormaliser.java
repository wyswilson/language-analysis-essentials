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
import org.lanes.text.nlp.*;
import org.lanes.utility.*;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.*;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.lang.StringUtils;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class TextNormaliser{
	
	
	public static String collapseMultilineText(String text) {
		
		text = text.replaceAll("[\\r\\n]", ". "); 
		text = text.replaceAll("(?:[\\s]*[\\.]+[\\s]*)+", ". "); 
		text = text.replaceAll("[\\s]+", " "); 

		return text;
   	}
	
	public static List<String> cleanLightHTML(String html){

		html = html.replaceAll("&nbsp;", " "); 
		html = html.replaceAll("[\\{\\}\\[\\]]", "");
		html = html.replaceAll("&amp;", "&");
		html = html.replaceAll("(?i)<div.*?>(.*?)<\\/div>", "$1\n"); 
		html = html.replaceAll("(?i)<strong.*?>(.*?)<\\/strong>", "[$1] ");
		html = html.replaceAll("(?i)<br\\/?>", "\n");//MUST COME BEFORE <b>
		html = html.replaceAll("(?i)<b.*?>(.*?)<\\/b>", "[$1] ");
		html = html.replaceAll("(?i)<em>(.*?)<\\/em>", "[$1] ");
		html = html.replaceAll("(?i)<i>(.*?)<\\/i>", "[$1] ");
		html = html.replaceAll("(?i)<u>(.*?)<\\/u>", "[$1] ");

		html = html.replaceAll("[\\s\\n]+\\]", "]");
		html = html.replaceAll("\\[[\\s\\n]+", "[");		
		html = html.replaceAll("[\\s]*:\\]", "]");
		html = html.replaceAll("(?i)<[\\/]?[uo]l.*?>", "");

		html = html.replaceAll("(?i)<li.*?>(.+?)(?=<li>)", "{$1}\n");
		html = html.replaceAll("(?i)<li.*?>(.+?)\\n", "{$1}\n");
		html = html.replaceAll("(?i)<\\/li>", " ");
		html = html.replaceAll("(?i)<[\\/]?div.*?>", " ");
		html = html.replaceAll("(?i)<\\/?center>", " ");
		html = html.replaceAll("(?i)<\\/?p.*?>", " ");
		html = html.replaceAll("(?i)<\\/?li>", " ");
		html = html.replaceAll("(?i)<\\/?font.*?>", " ");
		html = html.replaceAll("(?i)<\\/?hr.*?>", " ");
		html = html.replaceAll("\\[\\]", "");

    	Pattern pattern = Pattern.compile("[\u00B7\u2022]\\s*(.+?)\n",  (Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE) );
		Matcher matcher = pattern.matcher(html);
        html = matcher.replaceAll("{$1}\n");

		html = html.replaceAll("\\s\\}", "}");

		html = html.replaceAll("(?i)(?:[\\w\\.]+)@(?:[\\w]+\\.)+(?:[\\w]+)", "<EMAIL>");
		html = html.replaceAll("(?i)(?:http:\\/\\/)?(?:[\\w]+\\.)+(?:[\\w]+)", "<URL>");
		html = html.replaceAll("\\s*\\/\\s*", ", ");

		//html = html.replaceAll("\\s+", " ");

		html = Normalizer.normalize(html, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

		List<String> lineobj = new ArrayList<String>();
		String[] lines = html.split("\\n");
		for(String line : lines){
			line = line.trim();
			if(!line.equals("")){
				lineobj.add(line);
			}	
		}

	
		return lineobj;
	}
}