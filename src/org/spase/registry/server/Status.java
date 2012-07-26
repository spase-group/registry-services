/**
 * Determines the status of a registry.
 * It can return lists a resources modified during
 * a specified time period and the number of
 * resources in each category.
 * <p>
 * When run as a servlet the initialization parameters
 * <br>RootPath indicate where the location to start searching for
 * files containing resource descriptions.
 * <br>Extension indicate the file name extension filter (default: .xml).
 * <p>
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2009-06-25
 */

package org.spase.registry.server;

import igpp.servlet.MultiPrinter;
import igpp.servlet.SmartHttpServlet;
import igpp.util.Encode;
import igpp.util.Text;
import igpp.util.Process;

import igpp.xml.XMLParser;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Calendar;

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

// import org.apache.commons.cli.*;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

public class Status extends SmartHttpServlet
{
	private String	mVersion = "1.0.1";
	private String mOverview = "Status retrieves information about a registry.";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";
	
	// Service configuration
	Boolean	mVerbose = false;
	String	mRootPath = null;
	String	mExtension = ".xml";
	
	// Task Options
	String	mAuthority = null;
	String	mStartDate = null;
	String	mStopDate = null;
	String	mInventory = null;
	Boolean	mUpdated = false;
	Boolean	mDetails = false;
	
	// Authority map
	HashMap<String, String> mAuthorityMap = new HashMap<String, String>();

	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public Status() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "a", "authority", true, "Authority. The authority name to report on." );
		mAppOptions.addOption( "l", "lookup", true, "Lookup. File name of the the authority lookup table." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated profiles to {file}. Default: System.out." );
		mAppOptions.addOption( "b", "startdate", true, "Start Date. The start date if the interval of interest." );
		mAppOptions.addOption( "e", "stopdate", true, "Stop Date. The stop date if the interval of interest." );
		mAppOptions.addOption( "u", "updated", false, "Updated. Return a list of resources that have been updated (new or modified) during the specified time period." );
		mAppOptions.addOption( "i", "inventory", true, "Inventory. Return a list of known resources at a given level." );
		mAppOptions.addOption( "d", "details", false, "Details. Return a detailed list of matching resources." );
	}	
	
   /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
   	String	outfile = null;
   	
		Status me = new Status();

		me.mOut.setOut(System.out);
		
		if (args.length < 1) {
			System.out.println("Version: " + me.mVersion);
			me.showHelp();
			System.exit(1);
		}

		CommandLineParser parser = new PosixParser();
		try { // parse the command line arguments
         CommandLine line = parser.parse(me.mAppOptions, args);

			if(line.hasOption("h")) me.showHelp();
			if(line.hasOption("v")) me.mVerbose = true;
			if(line.hasOption("o")) outfile = line.getOptionValue("o");
			if(line.hasOption("a")) me.mAuthority = line.getOptionValue("a");
			if(line.hasOption("l")) me.loadAuthority(line.getOptionValue("l"));
			if(line.hasOption("b")) me.mStartDate = line.getOptionValue("b");
			if(line.hasOption("e")) me.mStopDate = line.getOptionValue("e");
			if(line.hasOption("u")) me.mUpdated = true;
			if(line.hasOption("i")) me.mInventory =  line.getOptionValue("i");
			if(line.hasOption("d")) me.mDetails = true;
			
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
		String param[] = {"help", "authority", "startdate", "stopdate", "updated", "inventory", "details"};
		ArrayList<String> aware = new ArrayList<String>();
		
		aware.add("This service knows about the following authorities:");
		Set<String> keyset = mAuthorityMap.keySet();
		for(String key : keyset) {
			aware.add(key);
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
 		
 		setVersion(mVersion);
   	
   	String value = getServletConfig().getInitParameter("RootPath");	// Where files are stored
   	if(value != null) setRootPath(value);

   	value = getServletConfig().getInitParameter("AuthorityList");	// Authority list
   	if(value != null) loadAuthority(value);
   }
   

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mAuthority = null;
		mStartDate = null;
		mStopDate = null;
		mInventory = null;
		mUpdated = false;
		mDetails = false;
	}

	/** 
	 * Load options from HTTP request.
	 *
    * @param request	the {@link HttpServletRequest} with request information.
	 **/
	public void setFromRequest(HttpServletRequest request) 
	{
		reset();	// Clear query parameters

		setAuthority(igpp.util.Text.getValue(request.getParameter("a"), getAuthority()));
		setAuthority(igpp.util.Text.getValue(request.getParameter("authority"), getAuthority()));
		
		setStartDate(igpp.util.Text.getValue(request.getParameter("b"), getStartDate()));
		setStartDate(igpp.util.Text.getValue(request.getParameter("startDate"), getStartDate()));
		
		setStopDate(igpp.util.Text.getValue(request.getParameter("e"), getStopDate()));
		setStopDate(igpp.util.Text.getValue(request.getParameter("stopDate"), getStopDate()));

		setUpdated(igpp.util.Text.getValue(request.getParameter("u"), getUpdated()));
		setUpdated(igpp.util.Text.getValue(request.getParameter("updated"), getUpdated()));
		
		setInventory(igpp.util.Text.getValue(request.getParameter("i"), getInventory()));
		setInventory(igpp.util.Text.getValue(request.getParameter("inventory"), getInventory()));
		
		setDetails(igpp.util.Text.getValue(request.getParameter("d"), getDetails()));
		setDetails(igpp.util.Text.getValue(request.getParameter("details"), getDetails()));
	}	
		
	/** 
	 * Load an Authority lookup table. A table consists of rows composed of
	 * authority name and file path seperated by whitespace. Lines beginning with "#" are considered comments.
	 **/
	public void loadAuthority(String pathname)
	{
		try {
			pathname = getRealPath("conf", pathname);
			
			File file = new File(pathname);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String	buffer;
			
			// Search for word
			while((buffer = reader.readLine()) != null) {
				if(buffer.startsWith("#")) continue;
				String[] part = buffer.split("[ \t]", 2);
				if(part.length < 2) continue;
				mAuthorityMap.put(part[0].trim(), getRealPath("", part[1].trim()));
			}
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
			
		// get ready to write response
		mOut.setOut(response.getWriter());
		
		if(request.getParameter("h") != null) { // Send self documentation
			response.setContentType("text/html");
			sendCapabilities(request.getRequestURI()); 
			return; 
		}
		
		response.setContentType("text/xml");
		
		doAction();
	}
	
	/**
	 * Find all matching resource descriptions. Return as a single stream.
	 **/
	public void doAction()
   	throws Exception
	{
		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		mOut.println("<Response>");
		
		mOut.println("<Query>");
		if(mAuthority != null) mOut.println("   <Option name=\"authority\">" + mAuthority + "</Option>");
		if(mStartDate != null) mOut.println("   <Option name=\"startdate\">" + mStartDate + "</Option>");
		if(mStopDate != null) mOut.println("   <Option name=\"stopdate\">" + mStopDate + "</Option>");
		if(mInventory != null) mOut.println("   <Option name=\"inventory\">" + mInventory + "</Option>");
		if(mUpdated) mOut.println("   <Option name=\"updated\">Yes</Option>");
		if(mDetails) mOut.println("   <Option name=\"details\">Yes</Option>");
		mOut.println("</Query>");
		
		if(mAuthority != null) { 
			if(mUpdated) sendNewModified(mAuthority, mStartDate, mStopDate, mDetails); 
			if(mInventory != null) sendInventory(mAuthority, mInventory); 
		} else {
			mOut.println("<Message>");
			mOut.println("   <Info>No action requested.</Info>");
			mOut.println("</Message>");
		}
		
		mOut.println("</Response>");
	}		

	/** 
	 * Send the update status information of the respositry.
	 *
	 * Scans a registry maintained by the passed authority and determines
	 * the changes to the registry which occured between the given start and
	 * stop date. 
	 * Output is packaged in an XML formatted response document.
	 *
	 * @param authority the name of the registry authority (registry) to scan.
	 * @param startDate the start date to use for the scan. May be absolute of relative. 
	 *        If null then the date is uncontrained.
	 * @param stopDate the stop date to use for the scan. May be absolute or relative.
	 *        If null then the date is uncontrained.
	 * @param details send detail information only.
	 **/
	public void sendNewModified(String authority, String startDate, String stopDate, Boolean details)
   	throws Exception
	{
		if( ! mAuthorityMap.containsKey(authority)) {
			mOut.println("<Message>");
			mOut.println("   <Error>Unknown authority: " + authority + "</Error>");
			mOut.println("</Message>");
			return;
		}
		
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		counts.put("New", 0);
		counts.put("Modified", 0);
		counts.put("Deleted", 0);
		
		String path = mAuthorityMap.get(mAuthority);
		
		mOut.println("<Results>");
		
		if(igpp.util.File.isDirectory(igpp.util.Text.concatPath(path, ".git"))) {	// git managed
			sendNewModifiedGit(authority, startDate, stopDate, details, counts);
		} else {	// Simple file system
			sendNewModifiedFile(authority, path, igpp.util.Date.interpret(startDate), igpp.util.Date.interpret(stopDate), details, counts);
		}

		// Summary 
		mOut.println("<Summary>");
		if(counts.get("New") > 0) mOut.println("   <New>" + counts.get("New") + "</New>");
		if(counts.get("Modified") > 0) mOut.println("   <Modified>" + counts.get("Modified") + "</Modified>");
		if(counts.get("Deleted") > 0) mOut.println("   <Deleted>" + counts.get("Deleted") + "</Deleted>");
		mOut.println("</Summary>");
			
		mOut.println("</Results>");
	}

	/** 
	 * Send the update status information of the respositry that is managed by "git".
	 *
	 * Scans a registry maintained by the passed authority and determines
	 * the changes to the registry which occured between the given start and
	 * stop date. 
	 * Output is packaged in an XML formatted response document.
	 *
	 * @param authority the name of the registry authority (registry) to scan.
	 * @param startDate the start date to use for the scan. May be absolute of relative. 
	 *        If null then the date is uncontrained.
	 * @param stopDate the stop date to use for the scan. May be absolute or relative.
	 *        If null then the date is uncontrained.
	 * @param details send detail information only.
	 * @param counts a {@link HashMap} with the elements "New", "Modified" and "Deleted" to store transaction counts.
	 **/
	public void sendNewModifiedGit(String authority, String startDate, String stopDate, Boolean details, HashMap<String, Integer> counts)
   	throws Exception
	{
		Process process = new Process();
		String	type = "";

		String path = mAuthorityMap.get(mAuthority);
		
		String options = "";
		if(startDate != null) options += " --since=\"" + startDate + "\"";
		if(stopDate != null) options += " --until=\"" + stopDate + "\"";
		
		if(process.run("git whatchanged" + options, path) == -1) {// Failed to run
			mOut.println("<Message>");
			mOut.println("   <Error>Unable to determine status of respository</Error>");
			mOut.println("</Message>");
			return;
		}
		
		// Success - show results
		ArrayList<String> list = process.getOutput();
		
		// Extract the actual file names
		ArrayList<String> slist = new ArrayList<String>();
		for(String item : list) {
			if( ! item.startsWith(":")) continue;
			String[] part = item.split("[ \t]");
			if(part.length > 5) {
				slist.add(part[4] + " " + part[5]);
			}
		}
		
		// Sort list and send details is requested
		Collections.sort(slist);
		
		// Remove duplicates
		ArrayList<String> ulist = new ArrayList<String>();
		String	lastItem = "";
		for(String item : slist) {
			if(item.compareTo(lastItem) != 0) {	// Unique item
				ulist.add(item);
				lastItem = item;
			}
		}
		
		// Output unique list
		int newCount = 0;
		int modifiedCount = 0;
		int deleteCount = 0;
		
		String category = "";
		for(String item : ulist) {
			String[] part = item.split("[ \t]");
			
			type = "New";
			if(part[0].compareTo("A") == 0) { type = "New"; newCount++; }
			if(part[0].compareTo("M") == 0) { type = "Modified"; modifiedCount++; }
			if(part[0].compareTo("D") == 0) { type = "Deleted"; deleteCount++; }
			
			if(details) {
				mOut.println(igpp.xml.XMLParser.getTaggedValue(1, type, "spase://" + authority + "/" + igpp.util.Text.getFileBase(part[1])));
			}
		}
		
		counts.put("New", Integer.valueOf(newCount));
		counts.put("Modified", Integer.valueOf(modifiedCount));
		counts.put("Deleted", Integer.valueOf(deleteCount));
	}
	
	/** 
	 * Send the update status information of the respositry that is managed by "git".
	 *
	 * Scans a registry maintained by the passed authority and determines
	 * the changes to the registry which occured between the given start and
	 * stop date. 
	 * Output is packaged in an XML formatted response document.
	 *
	 * @param authority the name of the registry authority (registry) to scan.
	 * @param path the path to the directory to scan.
	 * @param startDate the start date to use for the scan. 
	 * @param stopDate the stop date to use for the scan. 
	 * @param details send detail information only.
	 * @param counts a {@link HashMap} with the elements "New", "Modified" and "Deleted" to store transaction counts.
	 **/
	public void sendNewModifiedFile(String authority, String path, Calendar startDate, Calendar stopDate, Boolean details, HashMap<String, Integer> counts)
   	throws Exception
	{
		String	type = "";
		
		int newCount = 0;
		int modifiedCount = 0;
		int deleteCount = 0;
		
		String prepath = mAuthorityMap.get(mAuthority);
		
		File scan = new File (path);
		
		File[] list = scan.listFiles();

		if(list == null) {
			mOut.println("<Message>");
			mOut.println("   <Error>Unable to determine files at: " + path + "</Error>");
			mOut.println("</Message>");
			return;
		}		
			
		for(File item : list) {
			if(item.getName().startsWith(".")) continue;	// skip hidden and relative
			if(item.isHidden()) continue;	// skip hidden
			if(item.isDirectory()) {	// Scan directory
				sendNewModifiedFile(authority, igpp.util.Text.concatPath(path, item.getName()), startDate, stopDate, details, counts);
				continue;
			}
			
			Calendar cal = igpp.util.Date.getCalendar(item.lastModified());
			
			if(igpp.util.Date.isInSpan(cal, startDate, stopDate)) { 
				type = "Modified"; 
				modifiedCount++;

				if(details) {
					mOut.println(igpp.xml.XMLParser.getTaggedValue(1, type, "spase://" + authority +
						path.substring(prepath.length()) + "/" + igpp.util.Text.getFileBase(item.getName()) ));
				}
			}
		}
		
		counts.put("New", Integer.valueOf(newCount + counts.get("New")));
		counts.put("Modified", Integer.valueOf(modifiedCount + counts.get("Modified")));
		counts.put("Deleted", Integer.valueOf(deleteCount + counts.get("Deleted")));
	}
	
	/** 
	 * Send the inventory information of an authority within the respositry.
	 *
	 * Scans a registry maintained by the passed authority and determines
	 * how many items for each top-level category are maintained by the registry. 
	 * Output is packaged in an XML formatted response document.
	 *
	 * @param authority the name of the registry authority (registry) to scan.
	 * @param level the index of the level to stop.
	 **/
	public void sendInventory(String authority, String level)
   	throws Exception
	{
		if( ! mAuthorityMap.containsKey(authority)) {
			mOut.println("<Message>");
			mOut.println("   <Error>Unknown authority: " + authority + "</Error>");
			mOut.println("</Message>");
			return;
		}
		
		int endLevel = 1;	// Default
		try {
			endLevel = Integer.parseInt(level);
		} catch(Exception e) {
			endLevel = 1;
		} 
		
		mOut.println("<Results>");
		
		String path = mAuthorityMap.get(mAuthority);
		sendInventory(path, 0, endLevel);
			
		mOut.println("</Results>");
	}
	
	/** 
	 * Send the inventory information of an authority within the repository.
	 *
	 * Scans a registry maintained by the passed authority and determines
	 * how many items for each top-level category are maintained by the registry.
	 * The method is called for each level beginning with the "atLevel" and ending
	 * at the "endLevel". 
	 * Output is packaged in an XML formatted response document.
	 *
	 * @param path the path to the directory to scan.
	 * @param atLevel the starting level for the inventory list.
	 * @param endLevel the ending level for the inventory list.
	 * 
	 **/
	public void sendInventory(String path, int atLevel, int endLevel)
   	throws Exception
	{
		if(atLevel >= endLevel) return;
		
		File start = new File(path);
		
		String indent = "";
		for(int i = 0; i < atLevel; i++) indent += "   ";
		
		File[] folders = igpp.util.Listing.getFolderList(start);
		if(folders != null) {
			for(File folder : folders) {
				mOut.println(indent + "<Folder name=\"" + folder.getName() + "\">");
				mOut.println(indent + "   <Count>" + countFiles(igpp.util.Text.concatPath(path, folder.getName()), ".xml", true) + "</Count>");
				sendInventory(igpp.util.Text.concatPath(path, folder.getName()), atLevel+1, endLevel);
				mOut.println(indent + "</Folder>");
			}
		}
	}
	
	/** 
	 * Count the number of files with the given extension.
	 *
	 * @param path the path to file folder to inspect.
	 * @param extension the extension of the filesnames to count.
	 * @param recurse if true the path will be recusively scanned.
	 *
	 * @return the number of files matching the given extension.
	 **/
	public long countFiles(String path, String extension, boolean recurse)
   	throws Exception
	{
		long count = 0;
		File[] fileList = igpp.util.Listing.getFileListByExtension(path, extension);
		if(fileList != null) count = fileList.length;
		if(recurse) {
			File[] list = igpp.util.Listing.getFolderList(path);
			if(list != null) {
				for(File item : list) {
					count += countFiles(igpp.util.Text.concatPath(path, item.getName()), extension, recurse);
				}
			}
		}
		
		return count;
	}
	
	public Option getAppOption(String opt) {	return mAppOptions.getOption(opt);	}
	
	public void setB(String value) { setStartDate(value); }
	public void setStartDate(String value) { if(igpp.util.Text.isEmpty(value)) return; mStartDate = value; }
	public String getStartDate() { return mStartDate; }
	
	public void setE(String value) { setStopDate(value); }
	public void setStopDate(String value) { if(igpp.util.Text.isEmpty(value)) return; mStopDate = value; }
	public String getStopDate() { return mStopDate; }
	
	private void setRootPath(String value) { mRootPath = value; }
	public String getRootPath() { return mRootPath; }
	
	private void setAuthority(String value) { mAuthority = value; }
	public String getAuthority() { return mAuthority; }
	
	public void setU(String value) { setUpdated(value); }
	public void setUpdated(String value) { mUpdated = igpp.util.Text.isTrue(value); }
	public void setUpdated(boolean value) { mUpdated = value; }
	public Boolean getUpdated() { return mUpdated; }
	
	public void setI(String value) { setInventory(value); }
	public void setInventory(String value) { mInventory = value; }
	public String getInventory() { return mInventory; }
	
	public void setD(String value) { setDetails(value); }
	public void setDetails(String value) { mDetails = igpp.util.Text.isTrue(value); }
	public void setDetails(boolean value) { mDetails = value; }
	public Boolean getDetails() { return mDetails; }
	
}
