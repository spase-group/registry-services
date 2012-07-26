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

import igpp.servlet.SmartHttpServlet;

import java.util.ArrayList;

import java.io.FileOutputStream;

import org.spase.registry.client.Search;

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

public class QRCode extends SmartHttpServlet
{
	private String	mVersion = "1.0.0";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";
	private String mOverview = "Generate a QR Code image which points to a registry service.\n"
				+ "The action, identifier, start date and stop date options are included in QR Code. \n"
				+ "Any valid registry service is a valid action. \n"
				;
	
	Boolean	mVerbose = false;
	String	mAction = "downloader";
	String  mOutfile = "qrcode.png";
	String	mIdentifier = null;
	String	mStartDate = null;
	String	mStopDate = null;
	String	mWidth = "150";
	String	mHeight = "150";
	
	String	mOriginalRequest = "";
	
	// create the Options
	Options mAppOptions = new Options();

	public QRCode() 
	{
		mAppOptions.addOption( "h", "help", false, "dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step" );
		mAppOptions.addOption( "a", "action", true, "The registry action (service) for use in the QR Code (default: " + mAction + ").");
		mAppOptions.addOption( "i", "id", true, "Identifier for the desired resource.");
		mAppOptions.addOption( "b", "startdate", true, "Start Date. The start date if the interval of interest." );
		mAppOptions.addOption( "e", "stopdate", true, "Stop Date. The stop date if the interval of interest." );
		mAppOptions.addOption( "q", "query", true, "Query. Lucene style query string." );
		mAppOptions.addOption( "x", "width", true, "Width. The width (x) in pixels for the QR Code image." );
		mAppOptions.addOption( "y", "height", true, "Height. The height (y) in pixels for the QR Code image." );
		mAppOptions.addOption( "o", "output", true, "Output filename (default: " + mOutfile + ").");
	}
		
   /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
		QRCode me = new QRCode();
		String  outfile = "qrcode.png";

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
			if(line.hasOption("a")) me.mAction = line.getOptionValue("a");
			if(line.hasOption("o")) me.mOutfile = line.getOptionValue("o");
			if(line.hasOption("b")) me.mStartDate = line.getOptionValue("b");
			if(line.hasOption("e")) me.mStopDate = line.getOptionValue("e");
			if(line.hasOption("x")) me.mWidth = line.getOptionValue("x");
			if(line.hasOption("y")) me.mHeight = line.getOptionValue("y");
			
			if(line.getArgs().length == 0) {
				System.out.println("At least one resource ID must be passed.");
				System.exit(1);
			}
			
			igpp.barcode.QRCreate qr = new igpp.barcode.QRCreate();
			
			qr.setOutput(new FileOutputStream(outfile));
			
			String buffer = "/registry/" + me.mAction + "?";
			buffer += "i=" + line.getArgs()[0];
			if(me.mStartDate != null) buffer += "&b=" + me.mStartDate;
			if(me.mStopDate != null) buffer += "&e=" + me.mStopDate;
			
		    qr.setText(buffer);
		    qr.setWidth(me.mWidth);
		    qr.setHeight(me.mHeight);
		    
			qr.doAction();
			
			if(me.mVerbose) System.out.print("Output written to: " + outfile);
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
		String param[] = {"help", "action", "id", "startdate", "stopdate", "width", "height"};
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
   }

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mAction = "downloader";
		mIdentifier = null;
		mStartDate = null;
		mStopDate = null;
		mWidth = "150";
		mHeight = "150";
	}

	/** 
	 * Load options from HTTP request.
	 *
    * @param request	the {@link HttpServletRequest} with request information.
	 **/
	public void setFromRequest(HttpServletRequest request) 
	{
		reset();	// Clear query parameters

		setAction(igpp.util.Text.getValue(request.getParameter("a"), getAction()));
		setAction(igpp.util.Text.getValue(request.getParameter("action"), getAction()));
		
		setIdentifier(igpp.util.Text.getValue(request.getParameter("i"), getIdentifier()));
		setIdentifier(igpp.util.Text.getValue(request.getParameter("id"), getIdentifier()));
		
		setStartDate(igpp.util.Text.getValue(request.getParameter("b"), getStartDate()));
		setStartDate(igpp.util.Text.getValue(request.getParameter("strtDate"), getStartDate()));
		
		setStopDate(igpp.util.Text.getValue(request.getParameter("e"), getStopDate()));
		setStopDate(igpp.util.Text.getValue(request.getParameter("stopDate"), getStopDate()));
		
		setWidth(igpp.util.Text.getValue(request.getParameter("x"), getWidth()));
		setWidth(igpp.util.Text.getValue(request.getParameter("width"), getWidth()));
		
		setHeight(igpp.util.Text.getValue(request.getParameter("y"), getHeight()));
		setHeight(igpp.util.Text.getValue(request.getParameter("height"), getHeight()));
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
		
		if(mIdentifier == null) return;
		
		String query = igpp.util.Text.getValue(request.getParameter("q"), null);
		if(query != null) {
			Search search = new Search();
			search.parseQuery(query);	// Parse lucene query string
			setStartDate(search.getStartDate());
			setStopDate(search.getStopDate());
		}
		
		// Construct download request.
		String url = request.getRequestURL().toString();
		url = igpp.util.Text.getPath(url) + "/" + mAction;	// Swap action for the name of this servlet.
		String delim = "?";
		if(mIdentifier != null) { url += delim + "i=" + mIdentifier; delim = "&"; }
		if(mStartDate != null) { url += delim + "b=" + mStartDate; delim = "&"; }
		if(mStopDate != null) { url += delim + "e=" + mStopDate; delim = "&"; }
		
		igpp.barcode.QRCreate qr = new igpp.barcode.QRCreate();
		
		qr.setText(url);
		qr.setWidth(mWidth);
		qr.setHeight(mHeight);
		
	    qr.doAction(response);
	}
	

	public Option getAppOption(String opt) {	return mAppOptions.getOption(opt);	}
   
    private void setVerbose(String value) { mVerbose = igpp.util.Text.isTrue(value); }
	private void setVerbose(boolean value) { mVerbose = value; }
	private Boolean getVerbose() { return mVerbose; }
	
	public void setA(String value) { setAction(value); }
	public void setAction(String value) { mAction = value; }
	public String getAction() { return mAction; }
	
	public void setI(String value) { setIdentifier(value); }
	public void setIdentifier(String value) { mIdentifier = value; }
	public String getIdentifier() { return mIdentifier; }
	
	public void setB(String value) { setStartDate(value); }
	public void setStartDate(String value) { mStartDate = value; }
	public String getStartDate() { return mStartDate; }
	
	public void setE(String value) { setStopDate(value); }
	public void setStopDate(String value) { mStopDate = value; }
	public String getStopDate() { return mStopDate; }
	
	public void setX(String value) { mWidth = value; }
	public void setWidth(String value) { mWidth = value; }
	public String getWidth() { return mWidth; }
	
	public void setY(String value) { mHeight = value; }
	public void setHeight(String value) { mHeight = value; }
	public String getHeight() { return mHeight; }
}
   
