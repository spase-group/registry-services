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

package org.spase.registry.server;

import igpp.web.ZipStream;
import igpp.servlet.SmartHttpServlet;
import igpp.xml.XMLGrep;
import igpp.xml.Pair;

import java.util.ArrayList;

import org.spase.registry.client.Search;

import org.w3c.dom.Document;
import java.net.URI;
import java.io.IOException;

// Servlet 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// import org.apache.commons.cli.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

public class Downloader extends SmartHttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8064966162023194964L;
	private String	mVersion = "1.0.0";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";
	private String mOverview = "Obtains a list of URLs associated with a resource by quering \n"
									 + "a registry server, then downloads and packages all the source files. \n"
									 + "The collection of files is packaged into a zip file and written to \n"
									 + "the output stream.";
	
	Boolean	mVerbose = false;
	String	mResolver = "/resolver";
	String	mOutput = "resource.zip";
	String	mIdentifier = null;
	String	mStartDate = null;
	String	mStopDate = null;
	String	mProvider = "VxO";
	
	String	mOriginalRequest = "";
	
	// create the Options
	Options mAppOptions = new Options();

	public Downloader() 
	{
		mAppOptions.addOption( "h", "help", false, "dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step" );
		mAppOptions.addOption( "i", "id", true, "Identifier for the desired resource.");
		mAppOptions.addOption( "o", "output", true, "Output filename (default: " + mOutput + ").");
		mAppOptions.addOption( "s", "service", true, "The URL to the registry resolver service to look-up resource information (default: " + mResolver + ").");
		mAppOptions.addOption( "b", "startdate", true, "Start Date. The start date if the interval of interest." );
		mAppOptions.addOption( "e", "stopdate", true, "Stop Date. The stop date if the interval of interest." );
		mAppOptions.addOption( "q", "query", true, "Query. Lucene style query string." );
	}
		
   /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
		Downloader me = new Downloader();
		ZipStream	zipStream = new ZipStream();

		me.mOut.setOut(System.out);

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
			if(line.hasOption("o")) me.mOutput = line.getOptionValue("o");
			if(line.hasOption("s")) me.mResolver = line.getOptionValue("s");
			if(line.hasOption("b")) me.mStartDate = line.getOptionValue("b");
			if(line.hasOption("e")) me.mStopDate = line.getOptionValue("e");
			
			if(line.getArgs().length == 0) {
				System.out.println("At least one resource ID must be passed.");
				System.exit(1);
			}
			
			// Open output file
			zipStream.open(me.mOutput);
			
			// Process all files
			for(String p : line.getArgs()) { 
				me.download(zipStream, p); 
			}
			
			zipStream.close();
			
			if(me.mVerbose) System.out.print("Output written to: " + me.mOutput);
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
	 * Send the capabilities information to the current output stream.
	 *
	 * The capabilities is packaged in an XML formatted response document.
	 **/
	public void sendCapabilities(String title)
   	throws Exception
	{
		String param[] = {"help", "id", "startdate", "stopdate"};
		ArrayList<String> aware = null;
			
		sendCapabilities(title, mOverview, mAcknowledge, param, aware);
	}

	/**
	 * Initialize servlet.
	 *
	 * When instantiated as a servlet the framework calls this method to 
	 * perform initialization tasks.
	 **/
   public void init()
   	throws ServletException
   {
		super.init();

   	String value = getServletConfig().getInitParameter("Verbose");	// Whether to output step-by-step information
   	if(value != null) setVerbose(value);
   	
   	value = getServletConfig().getInitParameter("Resolver");	// Where files are stored
   	if(value != null) setResolver(value);
   	
   	value = getServletConfig().getInitParameter("Provider");	// Where files are stored
   	if(value != null) setProvider(value);
   }

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mIdentifier = null;
		mStartDate = null;
		mStopDate = null;
	}

	/** 
	 * Load options from HTTP request.
	 *
    * @param request	the {@link HttpServletRequest} with request information.
	 **/
	public void setFromRequest(HttpServletRequest request) 
	{
		reset();	// Clear query parameters

		setIdentifier(igpp.util.Text.getValue(request.getParameter("i"), getIdentifier()));
		setIdentifier(igpp.util.Text.getValue(request.getParameter("id"), getIdentifier()));
		
		setStartDate(igpp.util.Text.getValue(request.getParameter("b"), getStartDate()));
		setStartDate(igpp.util.Text.getValue(request.getParameter("strtDate"), getStartDate()));
		
		setStopDate(igpp.util.Text.getValue(request.getParameter("e"), getStopDate()));
		setStopDate(igpp.util.Text.getValue(request.getParameter("stopDate"), getStopDate()));
	}
	   
   /**
    * Process a HTTP post request.
    *
    * Called as part of the servlet framework when a HTTP post event occurs.
    *
    * @param request	the {@link HttpServletRequest} with request information.
    * @param response	the {@link HttpServletResponse} to stream output.
    **/
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		try {
	    	doAction(request, response);
		} catch(IOException i) {
			throw i;
		} catch(Exception e) {
			throw new ServletException(e);
		}
	}
   
   /**
    * Process a HTTP get request.
    *
    * Called as part of the servlet framework when a HTTP get event occurs.
    *
    * @param request	the {@link HttpServletRequest} with request information.
    * @param response	the {@link HttpServletResponse} to stream output.
    **/
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		try {
			doAction(request, response);
		} catch(IOException i) {
			throw i;
		} catch(Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	                
   /**
    * Process a HTTP request.
    *
    * Unifies the handling of HTTP get and post events.
    * Processes passed parameters and performs the appropriate tasks.
    *
    * @param request	the {@link HttpServletRequest} with request information.
    * @param response	the {@link HttpServletResponse} to stream output.
    **/
	public void doAction(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		setFromRequest(request);

		if(request.getParameter("h") != null) { // Send self documentation
			mOut.setOut(response.getWriter());
			response.setContentType("text/html");
			sendCapabilities(request.getRequestURI()); 
			return; 
		}
		
		// Fix-up service URLs
		if(getResolver().indexOf("://") == -1) setResolver(getQualifiedURL(request, getResolver()));
		
		if(mIdentifier == null) return;
		
		String query = igpp.util.Text.getValue(request.getParameter("q"), null);
		if(query != null) {
			Search search = new Search();
			search.parseQuery(query);	// Parse lucene query string
			setStartDate(search.getStartDate());
			setStopDate(search.getStopDate());
		}
		
		// Determine name for zip file based on resource ID
		// Replace "/" in URL path to "-" to create a name.
		URI tempUri = new URI(mIdentifier);
		String baseName = tempUri.getPath().substring(1).replaceAll("/", "-");
		
		// Reconstruct original request.
   	mOriginalRequest = request.getRequestURL().toString();
   	mOriginalRequest += "?" + request.getQueryString();
		
		download(response, baseName, mIdentifier);
	}
	
	/**
	 * Package and stream a resource as a HTTP response.
	 *
	 * Queries a resolver service to determine the granules corresponding
	 * to the current request parameters. Creates a ZIP file stream for output
	 * which is tied to the HTTP response stream. Retrieves each granule and 
	 * adds it to the ZIP stream. The path to each data file
	 * is ZIP stream is set to the passed baseName. 
	 * Generates an acknowledgement message and adds it as the ZIP file
	 * comment. Some ZIP file browsers will display the comment when the
	 * ZIP file is opened. Also adds acknowledgement as a file to the ZIP package.
	 * Generates an info file to describe the package.
	 *
    * @param response	the {@link HttpServletResponse} to stream output.
    * @param baseName	the path to set for each data file added to the ZIP package.
    * @param resourceID	the resource identifier of the parent data resource.
	 **/ 
   public void download(HttpServletResponse response, String baseName, String resourceID)
   	throws Exception
   {
   	if(response == null) return;
		if(baseName == null) return;
		if(resourceID == null) return;
		
		if(mVerbose) System.out.print("ResourceID: " + resourceID );
		
   	String url = mResolver + "?id=" + resourceID + "&g=yes";
   	if(mStartDate != null) url += "&b=" + mStartDate;
   	if(mStopDate != null) url += "&e=" + mStopDate;
   	
		if(mVerbose) System.out.print("Service call: " + url );
   	
		Document doc = null;
		try {
			doc = XMLGrep.parse(url);
		} catch(Exception e) {
			ZipStream zip = new ZipStream();
			zip.open(response, baseName);
			zip.addTextFile("error.txt", 
				  "Unable to retrieve resource description for: " + resourceID + "\n\n"
				  + "Trying with:\n" + url + "\n\n"
				  	);
			zip.close();
			return;
		}
		ArrayList<Pair> docIndex = XMLGrep.makeIndex(doc, "");
 		ArrayList<String>	values = XMLGrep.getValues(docIndex, ".*/URL");

		String ackMessage = null;
 		ArrayList<String>	ackList = XMLGrep.getValues(docIndex, ".*/Acknowledgement");
		if(ackList.size() > 0) ackMessage = "Please include the following acknowledgement(s) when using the data:\n\n";
		for(String item : ackList) ackMessage += item + "\n\n";
		
		String info = "";
		if(igpp.util.Text.isListEmpty(values)) {
			info = "No matching granules (data files) were found.";
			response.setContentType("text/html");
			mOut.setOut(response.getWriter());
			mOut.println(info);
			return;
		} else {	
			info = "Package contains " + values.size() + " granule" + (values.size() == 1 ? "" : "s") + " (data file).";
		}
			
 		try {
			// ZipStream.toHttpServletResponse(response, baseName, values);
			ZipStream zip = new ZipStream();
			zip.open(response, baseName);
			zip.addURL("./spase.xml", url + "&r=yes");	// "r" adds full resource description
			zip.add(".", baseName, values);	// Add all matching files
			if(ackMessage != null) zip.setComment(ackMessage);
			zip.addTextFile("acknowledgement.txt", ackMessage);
			zip.addTextFile("info.txt", 
				  "Retrieved from " + mProvider + " on " + igpp.util.Date.now() + "\n\n"
				+ "You can down this data again with the URL:\n\n"
				+ mOriginalRequest + "\n\n"
				+ info);
			zip.close();
		} catch(Exception e) {
			// Do nothing
			e.printStackTrace();
		}
   }	
		
	/**
	 * Package and stream a resource as a HTTP response.
	 *
	 * Queries a resolver service to determine the granules corresponding
	 * to the passed resource ID and adds each file to the passed ZipStream. 
	 *
    * @param zipStream	the {@link ZipStream} to add files.
    * @param resourceID	the resource identifier of the parent data resource.
    **/
   public void download(ZipStream zipStream, String resourceID)
   	throws Exception
   {
   	if(mResolver == null) return;
		if(zipStream == null) return;
		if(resourceID == null) return;
		
		if(mVerbose) System.out.print("ResourceID: " + resourceID );
		
   	String buffer = mResolver + "?id=" + resourceID + "&g=yes&u=yes";
		if(mVerbose) System.out.print("Service call: " + buffer );
   	
		Document doc = XMLGrep.parse(buffer);
		ArrayList<Pair> docIndex = XMLGrep.makeIndex(doc, "");
 		ArrayList<String>	values = XMLGrep.getValues(docIndex, ".*/URL");
 		try {
			for(String value : values) {
				URI tempUri = new URI(value);
				String baseName = tempUri.getPath().substring(1).replaceAll("/", "-");
				if(mVerbose) System.out.println("Downloading: " + value + " ... ");			
				zipStream.add("", "", value);
				if(mVerbose) System.out.println("done.");			
			}
		} catch(Exception e) {
			// Do nothing
			e.printStackTrace();
		}
   }	

	public Option getAppOption(String opt) {	return mAppOptions.getOption(opt);	}
   
   private void setVerbose(String value) { mVerbose = igpp.util.Text.isTrue(value); }
	private void setVerbose(boolean value) { mVerbose = value; }
	private Boolean getVerbose() { return mVerbose; }
	
	public void setI(String value) { setIdentifier(value); }
	public void setIdentifier(String value) { mIdentifier = value; }
	public String getIdentifier() { return mIdentifier; }
	
	public void setB(String value) { setStartDate(value); }
	public void setStartDate(String value) { mStartDate = value; }
	public String getStartDate() { return mStartDate; }
	
	public void setE(String value) { setStopDate(value); }
	public void setStopDate(String value) { mStopDate = value; }
	public String getStopDate() { return mStopDate; }
	
	private void setResolver(String value) { mResolver = value; }
	public String getResolver() { return mResolver; }
	
	private void setProvider(String value) { mProvider = value; }
	public String getProvider() { return mProvider; }
	
   	
}
   
