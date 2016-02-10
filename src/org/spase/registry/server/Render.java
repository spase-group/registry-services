/**
 * Render a resource description for viewing.
 * 
 * A resource description is converted to HTML
 * using a local XML Stylesheet.
 * <p>
 * When run as a servlet the initialization parameters
 * <br>RootPath indicate where stylesheets are stored.
 * <p>
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2009-10-19
 */

package org.spase.registry.server;

import igpp.servlet.MultiPrinter;
import igpp.servlet.SmartHttpServlet;
import igpp.util.Encode;
import igpp.util.Text;
import igpp.util.Process;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.saxon.value.StringValue;
import net.sf.saxon.trans.XPathException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import javax.xml.transform.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;




// import org.apache.commons.cli.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

public class Render extends SmartHttpServlet
{
	private String	mVersion = "1.0.1";
	private String mOverview = "Render a resource using a stylesheet.";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";

	// Service configuration
	Boolean	mVerbose = false;
	String	mRootPath = null;
	String	mResolver = "/resolver";
	String	mRender = "/render";
	String	mURLSource = null;
	String	mLocalFile = null;
   private	HashMap mCache = new HashMap(20);
	
	// Task options
	String	mStylesheet = "spase";
	String	mIdentifier = null;
	Boolean	mFull = false;
	
	// Authority map
	HashMap<String, String> mAuthorityMap = new HashMap<String, String>();

	// Context info
	ServletContext mContext = null;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public Render() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "c", "clear", false, "Clear. Clear the stylesheet cache." );
		mAppOptions.addOption( "f", "full", false, "Full. Render the full description including all referenced resources." );
		mAppOptions.addOption( "p", "path", true, "Path. The root path to the store of XML stylesheets." );
		mAppOptions.addOption( "r", "resolver", true, "Resolver. The URL to the resource identifier resolver." );
		mAppOptions.addOption( "s", "stylesheet", true, "Stylesheet. The base name of the XML Stylesheet to use." );
		mAppOptions.addOption( "i", "id", true, "ID. The registry ID to set for each resource" );
		mAppOptions.addOption( "u", "url", true, "URL. Render the XML returned from a URL" );
		mAppOptions.addOption( "l", "local", true, "Local. Render a local file." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated profiles to {file}. Default: System.out." );
	}	
	
   /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
   	String	outfile = null;
   	
		Render me = new Render();

		me.mOut.setOut(System.out);
		
		if (args.length < 1) {
			System.out.println("Version: " + me.mVersion);
			me.showHelp();
			System.exit(1);
		}

		CommandLineParser parser = new PosixParser();
		try { // parse the command line arguments
         CommandLine line = parser.parse(me.mAppOptions, args);

         // Command-line and servlet options
			if(line.hasOption("h")) me.showHelp();
			if(line.hasOption("i")) me.mIdentifier = line.getOptionValue("i");
			if(line.hasOption("s")) me.mStylesheet = line.getOptionValue("s");
			if(line.hasOption("u")) me.mURLSource = line.getOptionValue("u");
         if(line.hasOption("f")) me.mFull = true;
         
         if(line.hasOption("c")) me.clearCache();
         
			// Command-line only options
			if(line.hasOption("v")) me.mVerbose = true;
			if(line.hasOption("o")) outfile = line.getOptionValue("o");
         if(line.hasOption("l")) me.mLocalFile = line.getOptionValue("l");
			if(line.hasOption("p")) me.mRootPath = line.getOptionValue("p");
			if(line.hasOption("r")) me.mResolver = line.getOptionValue("r");
         
			
			if(outfile != null) {
				me.mOut.setOut(new PrintStream(outfile));
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
		System.out.println("Usage: java " + getClass().getName() + " [options]");
		System.out.println("");
		System.out.println("Options:");
		
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(getClass().getName(), mAppOptions);

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
		String param[] = {"help", "id", "full", "stylesheet", "url"};
		ArrayList<String> aware = null;
			
		sendCapabilities(title, mOverview, mAcknowledge, param, aware);
	}

    /**
     * getServletInfo<BR>
     * Required by Servlet interface
     **/
    public String getServletInfo() {
        return "Calls SAXON to apply a stylesheet to a source document";
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
		
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		
   	mContext = getServletContext();
   	
   	clearCache();
   	
   	String value = getServletConfig().getInitParameter("RootPath");	// Where files are stored
   	if(value != null) setRootPath(value);
   	
   	value = getServletConfig().getInitParameter("Resolver");	// URL to the resource id resolver
   	if(value != null) setResolver(value);
   }
   

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mStylesheet = "spase";
		mIdentifier = null;
		mFull = false;
		mURLSource = null;
		mLocalFile = null;
	}

	/** 
	 * Load options from HTTP request.
	 *
    * @param request	the {@link HttpServletRequest} with request information.
	 **/
	public void setFromRequest(HttpServletRequest request) 
	{
		reset();	// Clear query parameters

		if(request.getParameter("c") != null) clearCache();
		
		setIdentifier(igpp.util.Text.getValue(request.getParameter("i"), getIdentifier()));
		setIdentifier(igpp.util.Text.getValue(request.getParameter("id"), getIdentifier()));
		
		setFull(igpp.util.Text.getValue(request.getParameter("f"), getFull()));
		setFull(igpp.util.Text.getValue(request.getParameter("full"), getFull()));
		
		setStylesheet(igpp.util.Text.getValue(request.getParameter("s"), getStylesheet()));
		setStylesheet(igpp.util.Text.getValue(request.getParameter("stylesheet"), getStylesheet()));

		setURLSource(igpp.util.Text.getValue(request.getParameter("u"), getURLSource()));
		setURLSource(igpp.util.Text.getValue(request.getParameter("url"), getURLSource()));
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
		
		// Fix-up service URLs
		if(getResolver().indexOf("://") == -1) setResolver(getQualifiedURL(request, getResolver()));
		if(getRender().indexOf("://") == -1) setRender(request.getRequestURI());	// That's me
		
		// get ready to write response
		mOut.setOut(response.getWriter());
		response.setContentType("text/html");
		
		if(request.getParameter("h") != null) { // Send self documentation
			sendCapabilities(request.getRequestURI()); 
			return; 
		}
		
		doAction();
	}
	
	/**
	 * Find all matching resource descriptions. Return as a single stream.
	 **/
	public void doAction()
   	throws Exception
	{
        if(mStylesheet == null) {
            mOut.println("No stylesheet parameter supplied");
            return;
        }
        if(mIdentifier == null && mURLSource == null && mLocalFile == null) {
            mOut.println("No resource source (identifier or URL) supplied");
            return;
        }
        
        Transformer transformer = null;
        String stylePath = getRealPath(mRootPath, mStylesheet + ".xsl");
        try {
	        transformer = igpp.xml.Transform.getTransformer(stylePath, mCache);
	        // transformer.setParameter("spase.resolver", getResolver());
	        // transformer.setParameter("spase.render", getRender());
	        transformer.setParameter("spase.resolver", getResolver());
	        transformer.setParameter("spase.render", getRender());
        } catch(Exception e) {
        		mOut.println("Unable to initialize transform with defined stylesheet.<br/>");
        		mOut.println("Path: " + getRealPath(mRootPath, mStylesheet + ".xsl" + "<br/"));
        		return;
        }
        
        // Call the URL and transform the result.
        String service = "";
        try {
        		if(mLocalFile != null) {  // Use local reference
					transformer.transform(new StreamSource(mLocalFile), mOut.getStreamResult());
        		} else {
	        		if(mURLSource != null) {	// Use passed URL
	        			service = mURLSource;
	        		} 
	        		if(mIdentifier != null) {	// Use resolver
		        		String fullFlag = "";
			        	if(mFull) fullFlag = "&r=yes";
			        	service = mResolver + "?i=" + mIdentifier + fullFlag;
	        		}
					transformer.transform(igpp.xml.Transform.getURLSource(service), mOut.getStreamResult());
        		}
        } catch(Exception e) {
        		mOut.println("Unable to render resource description for viewing.<br/>");
        		mOut.println("Calling resolver: " + getResolver() + "<br/>");
        		mOut.println("Transform reported: " + e.getMessage() + "<br/>");
        		mOut.println("<p><a href=\"" + service + "\">View XML source</a></p>");
        		return;
        }
    }

    /**
    * Clear the cache. Useful if stylesheets have been modified, or simply if space is
    * running low. We let the garbage collector do the work.
    **/
   private synchronized void clearCache() {
       mCache = new HashMap(20);
   }

	/** 
	 * Get a description of an option. Called by SmartHttpServlet when help is requested.
	 * 
	 * @param name	option name (long or short)
	 */
	public Option getAppOption(String opt) {	return mAppOptions.getOption(opt);	}

	public void setI(String value) { setIdentifier(value); }
	public void setIdentifier(String value) { mIdentifier = value; }
	public String getIdentifier() { return mIdentifier; }
	
	public void setS(String value) { setStylesheet(value); }
	public void setStylesheet(String value) { mStylesheet = value; }
	public String getStylesheet() { return mStylesheet; }
	
	public void setF(String value) { setFull(value); }
	public void setFull(String value) { mFull = igpp.util.Text.isTrue(value); }
	public void setFull(boolean value) { mFull = value; }
	public boolean getFull() { return mFull; }
	
	public void setU(String value) { setURLSource(value); }
	public void setUrl(String value) { mURLSource = value; }
	public void setURLSource(String value) { mURLSource = value; }
	public String getURLSource() { return mURLSource; }
	
	private void setRootPath(String value) { mRootPath = value; }
	public String getRootPath() { return mRootPath; }
	
	private void setResolver(String value) { mResolver = value; }
	public String getResolver() { return mResolver; }
	
	private void setRender(String value) { mRender = value; }
	public String getRender() { return mRender; }
}
