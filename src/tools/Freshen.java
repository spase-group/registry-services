/**
 * Downloads all granules associated with a resource
 * and streams it down the HTTP response as a ZIP file.
 * <p>
 * When run as a servlet it the initialization parameters
 * <br>Resolver indicate where the registry service for resolving resource ID information.
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2009-06-26
 */

package org.spase.registry.tools;

import org.spase.registry.tools.Profiler;

import java.util.ArrayList;

import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.spase.tools.XMLGrep;
import org.spase.tools.Pair;
import org.spase.registry.client.Search;

import org.w3c.dom.Document;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

// import org.apache.commons.cli.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

public class Freshen
{
	private String	mVersion = "1.0.0";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";
	private String mOverview = "Queries a registry status service to determine new or modified resources, \n"
									 + "then generates resource profiles and updates the search engine index."
									 ;
									
	Boolean	mVerbose = false;
	String	mStatus = "/status";
	String	mResolver = "/resolver";
	String	mIndexer = "/update";
	String	mStartDate = null;
	String	mStopDate = null;
	String	mAuthority = null;
	
	String	mOriginalRequest = "";
	
	// create the Options
	Options mAppOptions = new Options();

	public Freshen() 
	{
		mAppOptions.addOption( "h", "help", false, "dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step" );
		mAppOptions.addOption( "s", "status", true, "The URL to the registry status service to look-up resource information (default: " + mStatus + ").");
		mAppOptions.addOption( "r", "resolver", true, "The URL to the registry resolver service to retrieve resource descriptions (default: " + mResolver + ").");
		mAppOptions.addOption( "i", "indexer", true, "The URL to the search index service to send resource profiles (default: " + mIndexer + ").");
		mAppOptions.addOption( "a", "authority", true, "The name of the authority to query");
		mAppOptions.addOption( "b", "startdate", true, "Start Date. The start date if the interval of interest." );
		mAppOptions.addOption( "e", "stopdate", true, "Stop Date. The stop date if the interval of interest." );
	}
		
   /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
		Freshen me = new Freshen();

		if (args.length < 1) {
			me.showHelp();
			System.exit(1);
		}
		
		// create the command line parser
		CommandLineParser parser = new PosixParser();
		try { // parse the command line arguments
         CommandLine line = parser.parse(me.mAppOptions, args);

			if(line.hasOption("h")) me.showHelp();
			if(line.hasOption("v")) me.mVerbose = true;
			if(line.hasOption("s")) me.mStatus = line.getOptionValue("s");
			if(line.hasOption("r")) me.mResolver = line.getOptionValue("r");
			if(line.hasOption("i")) me.mIndexer = line.getOptionValue("i");
			if(line.hasOption("a")) me.mAuthority = line.getOptionValue("a");
			if(line.hasOption("b")) me.mStartDate = line.getOptionValue("b");
			if(line.hasOption("e")) me.mStopDate = line.getOptionValue("e");
			
			if(me.mAuthority == null) {
				System.out.println("An authority must be specified.");
				System.exit(1);
			}
			
			me.doAction();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
			
   }

	/**
	 * Display help information.
	 **/
	public void showHelp()
	{
		System.out.println("");
		System.out.println(getClass().getName() + "; Version: " + mVersion);
		System.out.println(mOverview);
		System.out.println("");
		System.out.println("Usage: " + getClass().getName() + " [options] id");
		System.out.println("");
		System.out.println("Options:");
		
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( getClass().getName(), mAppOptions );

		System.out.println("");
		System.out.println("Acknowledgements:");
		System.out.println(mAcknowledge);
		System.out.println("");
	}

   /**
    * Perform the requested tasks
    **/
	public void doAction()
		throws Exception
	{
   	if(mStatus == null) return;
   	if(mResolver == null) return;
   	
   	Profiler profiler = new Profiler();
		
		// Call status service, request details on updated resources in the given time frame.
   	String buffer = mStatus + "?a=" + mAuthority + "&u=yes&d=yes";
   	if(mStartDate != null) buffer += "&b=" + igpp.util.Encode.urlEncode(mStartDate);
   	if(mStopDate != null) buffer += "&e=" + igpp.util.Encode.urlEncode(mStopDate);
   	
		if(mVerbose) System.out.print("Service call: " + buffer );
		
		Document doc = XMLGrep.parse(buffer);
		ArrayList<Pair> docIndex = XMLGrep.makeIndex(doc, "");
 		ArrayList<String>	modified = XMLGrep.getValues(docIndex, ".*/Results/Modified");
 		ArrayList<String>	newItems = XMLGrep.getValues(docIndex, ".*/Results/New");
 		ArrayList<String>	deleted = XMLGrep.getValues(docIndex, ".*/Results/Deleted");

		// URL of Remote Script.
		URL url = new URL (mIndexer);  // service name
		
		URLConnection urlConn = url.openConnection();
		urlConn.setDoInput (true);
		urlConn.setDoOutput (true);
		urlConn.setUseCaches (false);
		
		// Specify the header content type.
		urlConn.setRequestProperty("Content-Type", "text/xml");
		urlConn.setRequestProperty("charset", "utf-8");
		
		// Create print stream for profiler output.
		PrintStream outStream = new PrintStream (urlConn.getOutputStream());
		profiler.setOutput(outStream);
		
		// Write deleted items - if any
		
		for(String item : deleted) {
			if(mVerbose) System.out.println("Processing (deleted): " + item );
			profiler.makeDeleteItem(item);
		}
   	
		profiler.writeProfileHeader();	// For adds and modified
		
		for(String item : modified) {
	   	buffer = mResolver + "?i=" + item;
			if(mVerbose) System.out.println("Processing (modified): " + item );
			profiler.makeProfile(buffer);
			
		}
   	
		for(String item : newItems) {
	   	buffer = mResolver + "?i=" + item;
			if(mVerbose) System.out.println("Processing (new): " + item );
			profiler.makeProfile(buffer);
		}
		
		profiler.writeProfileFooter();
   	profiler.getOutput().flush();
   	profiler.getOutput().close();
   	
		// Get response data.
		BufferedReader input = new BufferedReader (new InputStreamReader(urlConn.getInputStream()) );
		String str;
		while (null != (str = input.readLine()) ) {
			System.out.println (str);
		}
		input.close ();
		
		sendCommit();
	}
	
	/**
	 * Send "commit" message to the indexer.
	 **/
	public void sendCommit()
		throws Exception
	{
		// Open URL for sending "commit" message
		URL url = new URL (mIndexer);  // service name
		
		URLConnection urlConn = url.openConnection();
		urlConn.setDoInput (true);
		urlConn.setDoOutput (true);
		urlConn.setUseCaches (false);

		// Specify the header content type.
		urlConn.setRequestProperty("Content-Type", "text/xml");
		urlConn.setRequestProperty("charset", "utf-8");
		
		// Create print stream for profiler output.
		PrintStream outStream = new PrintStream(urlConn.getOutputStream());
		outStream.println("<commit/>");

   	outStream.flush();
   	outStream.close();
   	
		// Get response data.
		BufferedReader input = new BufferedReader (new InputStreamReader(urlConn.getInputStream()) );
		String str;
		while (null != (str = input.readLine()) ) {
			System.out.println (str);
		}
		input.close ();
	}	

	public Option getAppOption(String opt) {	return mAppOptions.getOption(opt);	}
   
   private void setVerbose(String value) { mVerbose = igpp.util.Text.isTrue(value); }
	private void setVerbose(boolean value) { mVerbose = value; }
	private Boolean getVerbose() { return mVerbose; }
	
	public void setB(String value) { setStartDate(value); }
	public void setStartDate(String value) { mStartDate = value; }
	public String getStartDate() { return mStartDate; }
	
	public void setE(String value) { setStopDate(value); }
	public void setStopDate(String value) { mStopDate = value; }
	public String getStopDate() { return mStopDate; }
	
	private void setStatus(String value) { mStatus = value; }
	public String getStatus() { return mStatus; }
	
	private void setResolver(String value) { mResolver = value; }
	public String getResolver() { return mResolver; }
	
	private void setAuthority(String value) { mAuthority = value; }
	public String getAuthority() { return mAuthority; }
	
   	
}
   
