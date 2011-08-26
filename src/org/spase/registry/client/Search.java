/**
 * Java bean to support client search interfaces.
 *<p>
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2006-12-21
 */

package org.spase.registry.client;

import igpp.servlet.MultiPrinter;
import igpp.servlet.SmartHttpServlet;
import igpp.util.Text;
import igpp.util.Encode;

import java.io.InputStream;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Search
{
	private String	mVersion = "1.0.0";
	
	// Options
	String	mQuery = "";
	String	mFacet = "";
	String	mWords = "";
	String	mStart = "0";
	String	mRows = "50";
	String	mStartDate = "";
	String	mStopDate = "";
	String	mAction = null;
	
   /** 
	 * Command-line interface.
	 **/
	public static void main(String[] args)
   {
		Search me = new Search();
		
		System.out.println("Version: " + me.mVersion);
		System.out.println("Usage: " + me.getClass().getName() + " {config} [{options} ...]");
		
		System.out.println("Args: " + args.length);
		for(String arg : args) {
			me.reset();
			me.parseQuery(arg);
			System.out.println("Query: " + arg);
			System.out.println("   Words: " + me.getWords());
			System.out.println("   StartDate: " + me.getStartDate());
			System.out.println("   StopDate: " + me.getStopDate());
		}
   }

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mQuery = "";
		mFacet = "";
		mWords = "";
		mStart = "0";
		mRows = "50";
		mStartDate = "";
		mStopDate = "";
		mAction = null;
	}
	   
	/**
	 * Determines if a search is ready to be performed.
	 *
	 * @return true if ready, false otherwise.
	 **/
   public boolean isReady()
   {
     	// Check action state
   	if(mAction == null) return false;
      	
   	return true;
   }
   
   /**
    * Parse the internally set query string and populate internal parameters.
    *
    * The query string conforms to Apache Lucene syntax (as used by solr)
    * where facet constraints have the form "name:value". 
    **/
   public void parseQuery() 
   {
   	parseQuery(mQuery);
   }

   /**
    * Parse a query string and populate internal parameters.
    *
    * The query string conforms to Apache Lucene syntax (as used by solr)
    * where facet constraints have the form "name:value". The "name" is
    * used to determine which internal parameter to set with "value". 
    * Date ranges (x TO x) and (x AND x) are recognized and properly
    * handled.
    *
    * @param query	the query string 
    **/
   public void parseQuery(String query) 
   {
   	if(query == null) return;
   	if(query.length() == 0) return;
   	
   	String	delim = "";
   	
   	query = query.replaceAll(" TO ", "--");	// Munge to make ranges a continuous string
   	query = query.replaceAll(" AND ", " ");	// Munge to separate constriants
   	String[] part = query.split(" ");
   	
   	// Process each item
   	for(String item : part) {
   		if(item.length() == 0) continue;	// Skip blank items
	   	item = item.replaceAll("\\(", "");
   		item = item.replaceAll("\\)", "");
			String[] subpart = item.split(":", 2);
   		if(subpart.length == 1) { 	// Something of the form "name:value"
   			mWords += delim + item; 
   			delim = " ";
   		} else {	// Something of the form "name:value x value"
   			// Note: constraints on time have the format startdate:[* TO XXXX] where XXXX is the 
   			//       stop date of the constraint. Conversely with stopdate:[XXXX TO *] where XXXX
   			//       is the startdate. This is due to the way rnages in facets work.
   			if(subpart[0].compareToIgnoreCase("startdate") == 0) mStopDate = parseDateRange(subpart[1]);
   			if(subpart[0].compareToIgnoreCase("stopdate") == 0) mStartDate = parseDateRange(subpart[1]);
   		}
   	}
   }
   
   /**
    * Parse a data range expressed in the Lucene syntax.
    *
    * Supports both inclusive and exclusive date ranges.
    *
    * @param range	a date range string.
    *
    * @return	the first date string in the range or an empty string if none is found.
    **/
   public String parseDateRange(String range)
   {
   	if(range == null) return "";
   	if(range.length() == 0) return "";
   	
   	// Remove group and range delimiters
   	range = range.replaceAll("\\(", "");
   	range = range.replaceAll("\\)", "");
   	range = range.replaceAll("\\[", "");
   	range = range.replaceAll("\\]", "");
   	
   	String[] part = range.split("--");	// The range string has been munged
   	for(String item : part) {
   		if(item.length() == 0) continue;
   		if(Character.isDigit(item.charAt(0))) return item;
   	}
   	
   	return "";
   }
   
   
   /**
    * Create a Lucene query string using the internal parameter values.
    *
    * If a parameter is set it is added to the query string with the
    * facet specification of "name:value".
    *
    * @return a query string matching the current parameters expressed in Lucene syntax.
    **/
   public String makeQuery()
   {
		String delim = "";
		String query = getFacet();	// Start with facets
		
		query = query.replaceAll(" ", "+");	// Encode spaces
		
		if(query.length() > 0) delim = "+";
		
		String startDate = getStartDate();
		String stopDate = getStopDate();
		String orStart = null;
		String orStop = null;
		
		// Form start/stop date range constraint
		if(startDate.length() > 0 || stopDate.length() > 0) {	// Add time constraint
			if(startDate.length() == 0) { startDate = stopDate; orStop = igpp.util.Date.getISO8601DateString(stopDate); }
			if(stopDate.length() == 0) { stopDate = startDate; orStart = igpp.util.Date.getISO8601DateString(startDate); }
			
			startDate = igpp.util.Date.getISO8601DateString(startDate);	// Make ISO-8601 format
			stopDate = igpp.util.Date.getISO8601DateString(stopDate);	// Make ISO-8601 format
			
			query += delim + "(";	// Start time constraint group
			query += "(startdate:[*+TO+" + stopDate + "]";	// If operating on or before this date
			query += "+AND+stopdate:[" + startDate + "+TO+*])";	// And operating on or after
			
			if(orStart != null)  query += "+OR+(startdate:[" + orStart + "+TO+*])";
			if(orStop != null)  query += "+OR+(stopdate:[*+TO+" + orStop + "])";
			
			query += ")";	// End time constraint group
			
			delim = "+";
		}
		
		// Add words
		String words = "";
		if(getFacet().length() == 0) words = "*:*";	// Everything
		if(getWords().length()> 0) words = getWordsEncoded();	// Word list
		if(words.length() > 0) query += delim + words;
		
		return query;   	
   }
   
   public void setQ(String value) { setQuery(value); }	// Alternate show form
   public void setQuery(String value) { mQuery = value; }
   public String getQuery() { return mQuery; }
   
   public void setWords(String value) { mWords = igpp.util.Encode.urlDecode(value).trim(); }
   public String getWords() { return mWords; }
	public String getWordsEncoded() { try { return igpp.util.Encode.urlEncode(mWords.toLowerCase()); } catch(Exception e) { return mWords; }}
   
   public void setFacet(String value) { mFacet = value.trim(); }
   public String getFacet() { return mFacet; }
	public String getFacetEncoded() { try { return igpp.util.Encode.urlEncode(mFacet); } catch(Exception e) { return mFacet; }}
   
   public void setStart(String value) { mStart = value; }
   public String getStart() { return mStart; }
   
   public void setRows(String value) { mRows = value; }
   public String getRows() { return mRows; }
   
   public void setStartdate(String value) { setStartDate(value); }
   public void setStartDate(String value) { mStartDate = value; }
   public String getStartDate() { return mStartDate; }

   public void setStopdate(String value) { setStopDate(value); }
   public void setStopDate(String value) { mStopDate = value; }
   public String getStopDate() { return mStopDate; }
   
   public void setAction(String value) { mAction = value; }
   public String getAction() { return mAction; }
}