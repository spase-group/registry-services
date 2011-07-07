/**
 * Reads all SPASE resource description in a directory
 * and searches each for the existance of one or more words.
 * Returns the descriptions for each matching
 * description packed as a single SPASE stream.
 * <p>
 * When run as a servlet it the initialization parameters
 * <br>RootPath indicate where the location to start searching for
 * files containing resource descriptions.
 * <br>Extension indicate the file name extension filter (default: .xml).
 * <br>Category is the default category (default: Person)
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2009-06-25
 */

package org.spase.registry.server;

import igpp.servlet.MultiPrinter;
import igpp.servlet.SmartHttpServlet;

import igpp.util.Date;
import igpp.util.Encode;
import igpp.util.Text;
import igpp.util.StringListComparator;

import igpp.xml.XMLGrep;
import igpp.xml.Pair;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Calendar;

// import java.net.*;
import java.net.URL;
import java.net.URLConnection;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// import org.apache.commons.cli.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

public class Resolver extends SmartHttpServlet
{
	private String	mVersion = "1.0.2";
	private String mOverview = "Resolver retrieves a resource description for a given resource ID \n"
									 + "or generates a list of resources at a given partial reosurce ID location.";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";

	// Service configuration
	Boolean	mVerbose = false;
	String	mRootPath = null;
	String	mExtension = ".xml";
	String	mAuthority = null;

	// Task options
	String	mIdentifier = null;
	String	mStartDate = null;
	String	mStopDate = null;
	String	mHigherAuthority = null;
	Boolean	mTree = false;
	Boolean	mGranules = false;
	Boolean	mRecursive = false;
	Boolean	mSizeOnly = false;
	Boolean	mURLOnly = false;
	Boolean	mCheck = false;
	Boolean	mScan = false;

	// Authority map
	HashMap<String, String> mAuthorityMap = new HashMap<String, String>();

	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public Resolver() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated profiles to {file}. Default: System.out." );
		mAppOptions.addOption( "l", "list", true, "Authority List Table. The path to an authority list table." );
		mAppOptions.addOption( "d", "higher", true, "Higher Authority. The URL of the authority to pass request for unkown authorities." );
		mAppOptions.addOption( "p", "path", true, "Path. The file system path the authority tag is converted to." );
		mAppOptions.addOption( "a", "authority", true, "Authority. The default authority name." );

		mAppOptions.addOption( "i", "id", true, "ID. The resource ID to locate." );
		mAppOptions.addOption( "t", "tree", false, "Tree. Show the items in tree mark-up at given resource ID prefix." );
		mAppOptions.addOption( "g", "granules", false, "Granules. Return a list of URLs for Granules associated with the resource. ID" );
		mAppOptions.addOption( "b", "startdate", true, "Start Date. The start date of the interval of interest." );
		mAppOptions.addOption( "e", "stopdate", true, "Stop Date. The stop date of the interval of interest." );
		mAppOptions.addOption( "s", "size", false, "Size. Determine the size only of a granule look-up." );
		mAppOptions.addOption( "u", "URL", false, "URL. Return only the URL for each resource." );
		mAppOptions.addOption( "r", "recursive", false, "Recursive. Retrieve the description for the given resource ID and for all resources referenced in the description." );
		mAppOptions.addOption( "c", "check", true, "Check. Check if the resource ID is known." );
		mAppOptions.addOption( "n", "scan", true, "Scan. Scan for a list of identifiers which begin with the given prefix." );
	}

   /**
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
   	String	outfile = null;

		Resolver me = new Resolver();

		me.mOut.setOut(System.out);

		System.out.println("Version: " + me.mVersion);

		if (args.length < 1) {
			me.showHelp();
			System.exit(1);
		}


		CommandLineParser parser = new PosixParser();
		try { // parse the command line arguments
         CommandLine line = parser.parse(me.mAppOptions, args);

			if(line.hasOption("h")) me.showHelp();
			if(line.hasOption("v")) me.mVerbose = true;
			if(line.hasOption("o")) outfile = line.getOptionValue("o");
			if(line.hasOption("p")) me.mRootPath = line.getOptionValue("p");
			if(line.hasOption("l")) me.loadAuthority(line.getOptionValue("l"));
			if(line.hasOption("d")) me.mHigherAuthority = line.getOptionValue("d");
			if(line.hasOption("a")) me.mAuthority = line.getOptionValue("a");

			if(line.hasOption("i")) me.mIdentifier = line.getOptionValue("i");
			if(line.hasOption("t")) me.mTree = true;
			if(line.hasOption("g")) me.mGranules = true;
			if(line.hasOption("b")) me.mStartDate = line.getOptionValue("b");
			if(line.hasOption("e")) me.mStopDate = line.getOptionValue("e");
			if(line.hasOption("s")) me.mSizeOnly = true;
			if(line.hasOption("u")) me.mURLOnly = true;
			if(line.hasOption("r")) me.mRecursive = true;
			if(line.hasOption("c")) me.mCheck = true;
			if(line.hasOption("n")) me.mScan = true;

			if(outfile != null) {
				me.mOut.setOut(new PrintStream(outfile));
			}
			if(me.mAuthority != null && me.mRootPath != null) me.mAuthorityMap.put(me.mAuthority, me.mRootPath);

			me.doAction();

			// Process other command line areguments
			for(String p : line.getArgs()) {
				if(me.mVerbose) System.out.println("Processing: " + p);
			}

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
		System.out.println("Usage: java " + getClass().getName() + " [options] [file...]");
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
		String param[] = {"help", "id", "tree", "granules", "startdate", "stopdate", "recursive", "size"};
		ArrayList<String> aware = new ArrayList<String>();

		aware.add(":This service knows about the following authorities:");
		Set<String> keyset = mAuthorityMap.keySet();
		for(String key : keyset) {
			aware.add(key);
		}
		if(mHigherAuthority != null) {
			aware.add(":and passes unknown authorities to:");
			aware.add(mHigherAuthority);
		}

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

   	String value = getServletConfig().getInitParameter("RootPath");	// Where files are stored
   	if(value != null) setRootPath(value);

   	value = getServletConfig().getInitParameter("AuthorityList");	// Authority list - handled locally
   	if(value != null) loadAuthority(value);

   	value = getServletConfig().getInitParameter("HigherAuthority");	// Where to pass unknown authorities to
   	if(value != null) setHigherAuthority(value);

   	value = getServletConfig().getInitParameter("Authority");	// Authority name (default)
   	if(value != null) setAuthority(value);
   }


	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mIdentifier = null;
		mStartDate = null;
		mStopDate = null;
		mTree = false;
		mGranules = false;
		mSizeOnly = false;
		mRecursive = false;
		mURLOnly = false;
		mCheck = false;
		mScan = false;
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

		setTree(igpp.util.Text.getValue(request.getParameter("t"), getTree()));
		setTree(igpp.util.Text.getValue(request.getParameter("tree"), getTree()));

		setGranules(igpp.util.Text.getValue(request.getParameter("g"), getGranules()));
		setGranules(igpp.util.Text.getValue(request.getParameter("granules"), getGranules()));

		setStartDate(igpp.util.Text.getValue(request.getParameter("b"), getStartDate()));
		setStartDate(igpp.util.Text.getValue(request.getParameter("startdate"), getStartDate()));

		setStopDate(igpp.util.Text.getValue(request.getParameter("e"), getStopDate()));
		setStopDate(igpp.util.Text.getValue(request.getParameter("stopdate"), getStopDate()));

		setSizeOnly(igpp.util.Text.getValue(request.getParameter("s"), getSizeOnly()));
		setSizeOnly(igpp.util.Text.getValue(request.getParameter("size"), getSizeOnly()));

		setURLOnly(igpp.util.Text.getValue(request.getParameter("u"), getURLOnly()));
		setURLOnly(igpp.util.Text.getValue(request.getParameter("url"), getURLOnly()));

		setRecursive(igpp.util.Text.getValue(request.getParameter("r"), getRecursive()));
		setRecursive(igpp.util.Text.getValue(request.getParameter("recursive"), getRecursive()));

		setCheck(igpp.util.Text.getValue(request.getParameter("c"), getCheck()));
		setCheck(igpp.util.Text.getValue(request.getParameter("check"), getCheck()));

		setScan(igpp.util.Text.getValue(request.getParameter("n"), getScan()));
		setScan(igpp.util.Text.getValue(request.getParameter("scan"), getScan()));
	}

	/**
	 * Create a URL to match the current options.
	 *
	 * URL parameters do not include recusion flags.
	 *
	 * @param id the resource identifier value.
	 *
	 * @return options formatted as URL parameters
	 **/
	 public String getURLParameters(String id)
	 {
	 	String	delim = "?";
	 	String	param = "";

	 	if(id != null) { param += delim + "i=" + id; delim = "&"; }
	 	if(mTree) { param += delim + "t=yes"; delim = "&"; }
	 	if(mGranules) { param += delim + "g=yes"; delim = "&"; }
	 	if(mStartDate != null) { param += delim + "b=" + mStartDate; delim = "&"; }
	 	if(mStopDate != null) { param += delim + "e=" + mStopDate; delim = "&"; }
	 	if(mSizeOnly) { param += delim + "s=yes"; delim = "&"; }
	 	if(mCheck) { param += delim + "c=yes"; delim = "&"; }
	 	if(mScan) { param += delim + "n=yes"; delim = "&"; }

	 	// Add random value parameter to avoid web caching
	 	param += delim + "cid=" + getCID(); delim = "&";

	 	return param;
	 }

	/**
	 * Load an Authority lookup table.
	 *
	 * A table consists of rows composed of
	 * authority name and file path seperated by whitespace.
	 * Lines beginning with "#" are considered comments.
	 **/
	public void loadAuthority(String pathname)
	{
		try {
			pathname = getRealPath("conf", pathname);

			File file = new File(pathname);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String	buffer;

			// Load authority info
			while((buffer = reader.readLine()) != null) {
				if(mVerbose) System.out.println(buffer);
				if(buffer.startsWith("#")) continue;
				String[] part = buffer.split("[ \t]", 2);
				if(part.length < 2) continue;
				mAuthorityMap.put(part[0].trim(), getRealPath("", part[1].trim()));
			}

			reader.close();
		} catch(Exception e) {
			System.out.println(getClass().getName() + ":" + e.getMessage());
		}
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

		// response.setCharacterEncoding("UTF-8");
		response.setContentType("text/xml; charset=UTF-8");

		// get ready to write response
		mOut.setOut(response.getWriter());

		if(request.getParameter("h") != null) { // Send self documentation
			response.setContentType("text/html");
			sendCapabilities(request.getRequestURI());
			return;
		}

		doAction();
	}

   /**
    * Process a HTTP request.
    *
    * Unifies the handling of HTTP get and post events.
    * Processes passed parameters and performs the appropriate tasks.
    **/
	public void doAction()
   	throws Exception
	{
		if(mTree) { getTreeInfo(mIdentifier); return; }
		if(mGranules) { getGranules(mIdentifier, mStartDate, mStopDate, mRecursive, mURLOnly, mSizeOnly); return; }
		if(mIdentifier != null) {
			if(mCheck) { checkID(mIdentifier); }
			else {
				if(mScan) { getIDList(mIdentifier); }
			   else { getDescription(mIdentifier); }
			}
		}
	}

	/**
	 * Determine of a resource ID is known.
	 *
	 * Performs a quick check for the existance of a resource with the passed ID.
	 *
	 * @param id	the identifier of the resource description to retrieve.
	 **/
	public void checkID(String id)
   	throws Exception
	{
		ArrayList<String> processed = new ArrayList<String>();

		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		String path = translate(id);

		mOut.println("<Response>");
		if(path != null) {	// Found resource and transform if requested
		   path += ".xml";	// File must have ".xml" extension
		   File file = new File(path);
			if(file.exists()) {
				mOut.println("<Known>" + id + "</Known>");
			} else {
				mOut.println("<Unknown>" + id + "</Unknown>");
				mOut.println("<Message>Unable to locate within known authority.</Message>");
			}
		} else {
			if(mHigherAuthority != null) {	// Pass request on
				String	buffer = "";
			   String url = mHigherAuthority + getURLParameters(id);
				URL urlSource = new URL(url);
				URLConnection con = urlSource.openConnection();
				InputStream stream = con.getInputStream();
				streamContent(stream, false);
			} else {
				mOut.println("<Message>Unable to locate authority.</Message>");
			}
		}
		mOut.println("</Response>");
	}

	/**
	 * Retrieve and stream a full resource description from the repository.
	 *
	 * @param id	the identifier of the resource description to retrieve.
	 **/
	public void getDescription(String id)
   	throws Exception
	{
		ArrayList<String> processed = new ArrayList<String>();

		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if(mRecursive) mOut.println("<Package>");	// Multiple SPASE segments
		try {
			getDescription(id, mRecursive, processed);
		} catch(Exception e) {
			mOut.println("<Message>Unable to resolve id " + id + "</Message>");
		}
		if(mRecursive) mOut.println("</Package>");

		processed.clear();
		processed = null;
	}

	/**
	 * Retrieve and stream a full resource description from the repository.
	 *
	 * @param id	the identifier of the resource description to retrieve.
	 **/
	public void getDescription(String id, boolean recursive, ArrayList<String> processed)
   	throws Exception
	{
		// Resource Description
		String path = translate(id);
		ArrayList<String>	idList = null;

		processed.add(id);

		if(path != null) {	// Found resource and transform if requested
		   path += ".xml";	// File must have ".xml" extension
		   File file = new File(path);
			if(file.exists()) {
				idList = streamContent(path, recursive);
			} else {
				mOut.println("<Message>Unable to locate resource within known authority. Looking for: " + id + "</Message>");
			}
		} else {
			if(mHigherAuthority != null) {	// Pass request on
				String	buffer = "";
			   String url = mHigherAuthority + getURLParameters(id);
				URL urlSource = new URL(url);
				URLConnection con = urlSource.openConnection();
				InputStream stream = con.getInputStream();
				idList = streamContent(stream, recursive);
			} else {
				mOut.println("<Message>Unable to locate authority for: " + id + "</Message>");
			}
		}
		// Process items in idList
		if(idList != null) {
			for(String item : idList) {
				mOut.println("");
				if( ! igpp.util.Text.isInList(item, processed)) { getDescription(item, recursive, processed);  }
			}
		}
	}

	/**
	 * Get the list of granules associated with the given reource ID.
	 * which overlap a time interval.
	 *
	 * The retrieved information is sent to the output stream and formatted
	 * as XML. If both sendURL and sendSize are false then the full Granule description
	 * is sent for each matching granule.
	 *
	 * @param id	the identifier of the resource description to retrieve.
	 * @param startDate	the start date of the internval expressed as an ISO-8601 data/time.
	 * @param stopDate	the stop date of the internval expressed as an ISO-8601 data/time.
	 * @param sendURL	if true will send URL of each granule.
	 * @param sendSize	if true will send size of each granule.
	 **/
	public void getGranules(String id, String startDate, String stopDate, Boolean sendDesc, Boolean sendURL, Boolean sendSize)
   	throws Exception
	{
		//Fix-up dates
		if(startDate != null) {
			Calendar startCal = igpp.util.Date.parseISO8601(startDate, false);
			startDate = igpp.util.Date.getISO8601DateString(startCal);
		}
		if(stopDate != null) {
			Calendar stopCal = igpp.util.Date.parseISO8601(stopDate, true);
			stopDate = igpp.util.Date.getISO8601DateString(stopCal);
		}

		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		mOut.println("<Response>");

		// Description
		ArrayList<String> processed = new ArrayList<String>();

		// Granules related to a resource
		String zipMessage = "";
		ArrayList<String> ackList = new ArrayList<String>();

		String path = translate(id);
		if(path == null) {
			mOut.println("<Message>Unable to resolve id: " + id + "</Message>");
			mOut.println("</Response>");
			return;
		}
	   path += ".xml";	// File must have ".xml" extension
		Document doc = XMLGrep.parse(path);
		ArrayList<Pair> docIndex = XMLGrep.makeIndex(doc, "");
		ackList = XMLGrep.getValues(docIndex, ".*/Acknowledgement");

		// Switch data resource types to "Granule"
		String granuleID = id;
		granuleID = granuleID.replace("/NumericalData/", "/Granule/");
		granuleID = granuleID.replace("/DisplayData/", "/Granule/");
		granuleID = granuleID.replace("/Catalog/", "/Granule/");

		path = translate(granuleID);
		if(path == null) {
			mOut.println("<Message>Invalid resource id: " + id + "</Message>");
		} else {	// Find resource and transform if requested
			mOut.println("<StartDate>" + (startDate == null ? "" : startDate) + "</StartDate>");
			mOut.println("<StopDate>" + (stopDate == null ? "" : stopDate) + "</StopDate>");

			for(String item : ackList) mOut.println("<Acknowledgement>" + item + "</Acknowledgement>");

			if(sendDesc) getDescription(id, true, processed);

			long bytes = 0;
			long totalBytes = 0;
			ArrayList<ArrayList<String>> granules = new ArrayList<ArrayList<String>>();
			ArrayList<String> matches = new ArrayList<String>();
			scanDeepPath(path, matches);
			if(matches.size() == 0) mOut.println("<Message>No granules are associated with resource.</Message>");
			for(String item : matches) {
				if(igpp.util.File.isDirectory(item)) continue;	// Skip folders
				doc = XMLGrep.parse(item);
				docIndex = XMLGrep.makeIndex(doc, "");

				// Check if correct parent
				String gParentID = XMLGrep.getFirstValue(docIndex, ".*/ParentID", "");
				System.out.println("ParentID: " + gParentID);
				System.out.println("id: " + id);

				if( ! igpp.util.Text.isMatch(gParentID, id)) continue;	// Does not share parent

				// Get attributes
				String gStartDate = XMLGrep.getFirstValue(docIndex, ".*/StartDate", null);
				String gStopDate = XMLGrep.getFirstValue(docIndex, ".*/StopDate", null);
				if(igpp.util.Date.isInSpan(gStartDate, gStopDate, startDate, stopDate)) {
			 		ArrayList<String>	values = XMLGrep.getValues(docIndex, ".*/URL");
			 		String delim = "";
			 		String urlList = "";
			 		// Pack URL in a single string
					for(String value : values) { urlList += delim + value; delim = "\n"; }

			 		// Calculate total size
			 		bytes = 0;
			 		// Prior to version 1.3.0 DataExtent used Bytes for quantity
			 		values = XMLGrep.getValues(docIndex, ".*/DataExtent/Bytes");
					for(String value : values) {
						try {
							bytes += (long) Double.parseDouble(value);
						} catch(Exception e) {
							// Value is not in properly format - ignore.
						}
					}
					// Since version 1.3.0 DataExtent is expressed as a Quantity with units (for now assume bytes)
			 		values = XMLGrep.getValues(docIndex, ".*/DataExtent/Quantity");
					for(String value : values) {
						try {
							bytes += (long) Double.parseDouble(value);
						} catch(Exception e) {
							// Value is not in properly format - ignore.
						}
					}

					totalBytes += bytes;
					// Add record that consists of: StartDate, StopDate, PathName, URL, Bytes
					ArrayList<String> record = new ArrayList<String>();
					record.add(gStartDate);
					record.add(gStopDate);
					record.add(item);
					record.add(urlList);
					record.add(igpp.util.Text.toUnitizedBytes(bytes));
					granules.add(record);
				}
			}

			// Sort results on StartDate (Field = 0)
			Collections.sort(granules, new StringListComparator(0, StringListComparator.SortAscending));

			// Write sorted list
			if(sendSize) mOut.println("<Size>" + igpp.util.Text.toUnitizedBytes(totalBytes) + "</Size>");
			for(ArrayList<String> item : granules) {
				if( sendSize || sendURL) {
					if(sendURL) {
				 		String[]	values = item.get(3).split("\n");
						for(String value : values) mOut.println("   <URL>" + value + "</URL>");
					}
					// if(sendSize) mOut.println("<Size>" + item.get(4) + "</Size>");
				} else {	// Send entire Granule description
					streamContent(item.get(2));
				}
			}
		}
			long totalBytes = 0;
		mOut.println("</Response>");
	}

	/**
	 * Get the tree information at the Resource ID location.
	 * The Resource ID can be a partial ID. All items at that
	 * location in the path will be returned.
	 *
	 * The retrieved information is sent to the output stream and formatted
	 * as XML.
	 *
	 * @param id	the identifier of the resource description to retrieve.
	 **/
	public void getTreeInfo(String id)
   	throws Exception
	{
		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		mOut.println("<Response>");

		if(id == null) { 	// Send known authority list
			Set<String> keyset = mAuthorityMap.keySet();
			ArrayList<String> authList = new ArrayList<String>();
			for(String key : keyset) { authList.add(key); }
			Collections.sort(authList);
			for(String key : authList) {
				mOut.println("<node "
					+ " id=\"" + "spase://" + key + "\""
					+ " text=\"" + key + "\""
					+ " name=\"" + key + "\""
					+ " />");
			}
			mOut.println("</Response>");
			return;
		}

		// Translate ID

		String path = translate(id);
		if(path == null) {	// Send known authority list
			mOut.println("<Message>Invalid resource id: " + id + "</Message>");
		} else {	// Send file lists
			ArrayList<String> matches = scanPath(path);
			Collections.sort(matches);
			for(String name : matches) {
				File test = new File(name);
				if(test.isDirectory()) {	// Node
					mOut.println("<node "
						+ " id=\"" + igpp.util.Text.concatPath(id, test.getName(), "/") + "\""
						+ " text=\"" + test.getName() + "\""
						+ " name=\"" + test.getName() + "\""
						+ " />");
				} else {	// Leaf
					mOut.println("<leaf "
						+ " id=\"" + igpp.util.Text.concatPath(id, igpp.util.Text.getFileBase(test.getName()), "/") + "\""
						+ " text=\"" + igpp.util.Text.getFileBase(test.getName()) + "\""
						+ " name=\"" + igpp.util.Text.getFileBase(test.getName()) + "\""
						+ " />");
				}
			}
		}
		mOut.println("</Response>");
	}

	/**
	 * Get the identifiers of every resource starting at a Resource ID location.
	 * The Resource ID can be a partial ID. All items at that
	 * location in the path and below will be returned.
	 *
	 * The retrieved information is sent to the output stream and formatted
	 * as XML.
	 *
	 * @param id	the identifier of the resource description to retrieve.
	 **/
	public void getIDList(String id)
   	throws Exception
	{
		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		mOut.println("<Response>");

		// Translate ID

		String path = translate(id);
		if(path == null) {	// Send known authority list
			mOut.println("<Message>Invalid resource id: " + id + "</Message>");
		} else {	// Send file lists
			ArrayList<String> matches = new ArrayList<String>();
			scanDeepPath(path, matches);
			Collections.sort(matches);
			for(String name : matches) {
				   if(name.endsWith(mExtension)) { // It's a resource
				   	String resourcePath = igpp.util.Text.getFileBase(name);
				   	resourcePath = resourcePath.substring(path.length());	// String filesystem path.
						mOut.println("<ResourceID>"
							+ igpp.util.Text.concatPath(id, resourcePath, "/")
							+ "</ResourceID>");
				   }
			}
		}
		mOut.println("</Response>");
	}

	/**
	 * Translate a resource ID into a local path reference.
	 * If the path can not be translated then null is returned.
	 *
	 * @param id	the identifier of the resource description to translate.
	 *
	 * @return the pathname to the file appropriate for the context.
	 **/
	public String translate(String id)
	{
		String path = null;
		if(id == null) return null;

		Set<String> keyset = mAuthorityMap.keySet();
		for(String key : keyset) {
			if(id.startsWith("spase://" + key)) {	// Replace with path
				path = mAuthorityMap.get(key) + id.substring(8 + key.length());
				break;
			}
		}

		return path;
	}

	/**
	 * Recusively determine all named files and folders at a path.
	 *
	 * If the path points to a folder, then all subfolders and files
	 * are determined. If the path is to a file, then the filename is returned.
	 *
	 * @param path the path to scan.
	 *
	 * @return an {@link ArrayList} of {@link String} values of paths to the files and folders found.
	 * if the path is a folder, then an empty list is returned.
	 **/
	public int scanDeepPath(String path, ArrayList<String> list)
		throws Exception
	{
		ArrayList<String> matches = new ArrayList<String>();

		matches = scanPath(path);
		if(matches.isEmpty()) return list.size();	// all done

		list.addAll(matches);

		// Scan matches which are directories
		for(String item : matches) {
		   File filePath = new File(item);
		   if(filePath.isDirectory()) { scanDeepPath(item, list); }
		}

		return list.size();
	}

	/**
	 * Determine all named files and folders at a path.
	 *
	 * If the path points to a folder, then all subfolders and files
	 * are determined. If the path is to a file, then the filename is returned.
	 *
	 * @param path the path to scan.
	 *
	 * @return an {@link ArrayList} of {@link String} values of paths to the files and folders found.
	 * if the path is a folder, then an empty list is returned.
	 **/
	public ArrayList<String> scanPath(String path)
		throws Exception
	{
		ArrayList<String> matches = new ArrayList<String>();

		if(path == null) return matches;

		// File name filter
	   String[] list = new String[1];
	   list[0] = null;

	   File filePath = new File(path);
	   if(filePath.isDirectory()) {
			list = filePath.list(new FilenameFilter()
		   	{
		   		// Accept all files that do not begin with a "."
		   		public boolean accept(File path, String name) { return ! name.startsWith("."); }
		   	}
		   	);
		   for(String item: list) {
		   	if(item != null) matches.add(igpp.util.Text.concatPath(path, item));
		   }
	   } else {
	   	if(filePath.exists()) matches.add(path);
	   }

	   return matches;
	}

	/**
	 * Stream content between the <Spase>/</Spase> tags
	 *
	 * The content of a SPASE XML document is sent to the output stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
	 **/
	public void streamContent(String pathname)
		throws Exception
	{
		streamContent(pathname, false);
	}

	/**
	 * Stream content between the <Spase>/</Spase> tags
	 *
	 * The content of a SPASE XML document is sent to the output stream.
	 * The version tag can optionally be striped from the stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
	 * @param getID if true all resource IDs in the stream will be collected.
	 *
	 * @return an {@link ArrayList} of resource IDs or null if none collected.
	 **/
	public ArrayList<String> streamContent(String pathname, boolean getID)
		throws Exception
	{
		ArrayList<String>	idList = null;

		try {
			idList = streamContent(new FileInputStream(pathname), getID);
		} catch(Exception e) {
			// mOut.println("<Message>" + e.getMessage() + "</Message>");
		}

		return idList;
	}

	/**
	 * Stream content between the <Spase>/</Spase> tags
	 *
	 * The content of a SPASE XML document is sent to the output stream.
	 * Only the content enclosed in the <Spase> tags are streamed.
	 *
	 * @param stream a pre-opened {@link InputStream} to the file to scan and stream.
	 * @param getID if true all resource IDs in the stream will be collected.
	 *
	 * @return an {@link ArrayList} of resource IDs. An empty list is return if IDs are not collected.
	 **/
	public ArrayList<String>  streamContent(InputStream stream, boolean getID)
		throws Exception
	{
		boolean on = false;
		String	buffer;
		BufferedReader reader = null;
		ArrayList<String>	idList = new ArrayList<String>();

		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

			// Search for word
			while((buffer = reader.readLine()) != null) {
				if(on) {	// Check for end tag
					if(buffer.indexOf("</Spase") != -1) { on = false; }
					if(getID) {
						if(buffer.indexOf("ID>") != -1) { idList.add(getTagContent(buffer)); }
					}
					mOut.println(buffer);
				}
				if(buffer.indexOf("<Spase") != -1) { mOut.println(buffer); on = true; }
			}
		} catch(Exception e) {
		} finally {
			if(reader != null) reader.close();
		}

		return idList;
	}

	/**
	 * Read content between the <Spase>/</Spase> tags into a String
	 *
	 * The content of a SPASE XML document is processed and streamed to
	 * a String. The version tag can optionally be striped from the stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
	 *
	 * @return {@link String} containing the extracted content.
	 **/
	public String streamToString(String pathname)
		throws Exception
	{
		boolean on = false;
		String	buffer;
		BufferedReader reader = null;
		StringBuffer	content = new StringBuffer();

		try {
			File file = new File(pathname);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			// Search for word
			while((buffer = reader.readLine()) != null) {
				if(on) {	// Check for end tag
					if(buffer.indexOf("</Spase") != -1) { on = false; }
					content.append(buffer);
				}
				if(buffer.indexOf("<Spase") != -1) { content.append(buffer); on = true; }
			}
		} catch(Exception e) {
		} finally {
			if(reader != null) reader.close();
		}

		return new String(content);
	}


	/**
	 * Stream file contents to output.
	 *
	 * Read a file and send to the current output stream.
	 *
	 * @param pathname the path name of the file to stream.
	 **/
	public void streamFile(String pathname)
		throws Exception
	{
		String	buffer;
		BufferedReader reader = null;

		try {
			File file = new File(pathname);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			// Search for word
			while((buffer = reader.readLine()) != null) {
				mOut.println(buffer);
			}
		} catch(Exception e) {
		} finally {
			if(reader != null) reader.close();
		}
	}

	/**
	 * Extract content between XML tags.
	 *
	 * @param tagline the line of text containing content between XML tags.
	 *
	 * @return the content between the tags.
	 **/
	public String getTagContent(String tagline)
	{
		int n;
		n = tagline.indexOf('>');
		if(n != -1) tagline = tagline.substring(n+1);
		n = tagline.lastIndexOf('<');
		if(n != -1) tagline = tagline.substring(0, n);

		return tagline;
	}

	public Option getAppOption(String opt) {	return mAppOptions.getOption(opt);	}

	public void setI(String value) { setIdentifier(value); }
	public void setIdentifier(String value) { mIdentifier = value; }
	public String getIdentifier() { return mIdentifier; }

	public void setT(String value) { setTree(value); }
	public void setTree(String value) { mTree = igpp.util.Text.isTrue(value); }
	public void setTree(boolean value) { mTree = value; }
	public Boolean getTree() { return mTree; }

	public void setG(String value) { setGranules(value); }
	public void setGranules(String value) { mGranules = igpp.util.Text.isTrue(value); }
	public void setGranules(boolean value) { mGranules = value; }
	public Boolean getGranules() { return mGranules; }

	public void setB(String value) { setStartDate(value); }
	public void setStartDate(String value) { if(igpp.util.Text.isEmpty(value)) return; mStartDate = value; }
	public String getStartDate() { return mStartDate; }

	public void setE(String value) { setStopDate(value); }
	public void setStopDate(String value) { if(igpp.util.Text.isEmpty(value)) return; mStopDate = value; }
	public String getStopDate() { return mStopDate; }

	public void setS(String value) { setSizeOnly(value); }
	public void setSizeOnly(String value) { mSizeOnly = igpp.util.Text.isTrue(value); }
	public void setSizeOnly(boolean value) { mSizeOnly = value; }
	public Boolean getSizeOnly() { return mSizeOnly; }

	public void setU(String value) { setSizeOnly(value); }
	public void setURLOnly(String value) { mURLOnly = igpp.util.Text.isTrue(value); }
	public void setURLOnly(boolean value) { mURLOnly = value; }
	public Boolean getURLOnly() { return mURLOnly; }

	public void setR(String value) { setRecursive(value); }
	public void setRecursive(String value) { mRecursive = igpp.util.Text.isTrue(value); }
	public void setRecursive(boolean value) { mRecursive = value; }
	public Boolean getRecursive() { return mRecursive; }

	public void setC(String value) { setCheck(value); }
	public void setCheck(String value) { mCheck = igpp.util.Text.isTrue(value); }
	public void setCheck(boolean value) { mCheck = value; }
	public Boolean getCheck() { return mCheck; }

	public void setN(String value) { setScan(value); }
	public void setScan(String value) { mScan = igpp.util.Text.isTrue(value); }
	public void setScan(boolean value) { mScan = value; }
	public Boolean getScan() { return mScan; }

	private void setRootPath(String value) { mRootPath = value; }
	public String getRootPath() { return mRootPath; }

	private void setAuthority(String value) { mAuthority = value; }
	public String getAuthority() { return mAuthority; }

	private void setHigherAuthority(String value) { mHigherAuthority = value; }
	public String getHigherAuthority() { return mHigherAuthority; }
}
