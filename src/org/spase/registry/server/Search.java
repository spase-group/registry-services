/**
 * Reads all SPASE resource description in a directory 
 * and searches each for the existance of one or more words. 
 * Returns the descriptions for each matching
 * description packed as a single SPASE stream.
 * <p>
 * When run as a servlet the initialization parameters
 * <br>RootPath indicate where the location to start searching for
 * files containing resource descriptions.
 * <br>Extension indicate the file name extension filter (default: .xml).
 * <br>Category is the default category (default: Person)
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2008-05-15
 */

package org.spase.registry.server;

import igpp.servlet.MultiPrinter;
import igpp.servlet.SmartHttpServlet;
import igpp.util.Encode;
import igpp.util.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;

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

public class Search extends SmartHttpServlet
{
	private String	mVersion = "1.0.0";
	private String mOverview = "Resolver retrieves a resource description for a given resource ID \n"
									 + "or generates a list of resources at a given partial reosurce ID location.";
	private String mAcknowledge = "Development funded by NASA's VMO project at UCLA.";

	// Service configuration
	String	mRootPath = ".";
	String	mExtension = ".xml";
	
	// Task options
	boolean	mVerbose = false;
	boolean	mRecurse = true;
	boolean	mAllWords = false;
	ArrayList<String>	mWords = new ArrayList<String>();
	String	mCategory = null;

	// Authority map
	HashMap<String, String> mAuthorityMap = new HashMap<String, String>();
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

 	public Search() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "l", "list", true, "Authority List Table. The path to an authority list table." );
		
		mAppOptions.addOption( "a", "all", false, "All. Match all words. Default: " + mAllWords );
		mAppOptions.addOption( "b", "base", true, "Base. Set the base path to resource descriptions. Default: " + mRootPath);
		mAppOptions.addOption( "x", "extension", true, "Extension. Set the file extension for file names containing resource descriptions. Default: " + mExtension);
		mAppOptions.addOption( "r", "recursive", false, "Recursive. Retrieve the description for the given resource ID and for all resources referenced in the description." );
		mAppOptions.addOption( "c", "category", true, "Category. Limit search to a category of resources." );
		mAppOptions.addOption( "w", "words", true, "Words. List of one or more words to search for in the content of the resource descriptions." );
	}	

  /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
		Search me = new Search();
		
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
			if(line.hasOption("r")) me.mRecurse = true;
			if(line.hasOption("a")) me.mAllWords = true;
			
			if(line.hasOption("b")) me.setBasePath(line.getOptionValue("b"));
			
			if(line.hasOption("l")) me.loadAuthority(line.getOptionValue("l"));
			
			if(line.hasOption("x")) me.setExtension(line.getOptionValue("x"));
			if(line.hasOption("c")) me.setCategory(line.getOptionValue("c"));
			if(line.hasOption("w")) me.setWords(line.getOptionValue("w"));

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
		String param[] = {"help", "all", "category", "word"};
		ArrayList<String> aware = new ArrayList<String>();
		
		aware.add(":This service knows about the following authorities:");
		Set<String> keyset = mAuthorityMap.keySet();
		for(String key : keyset) {
			aware.add(key);
		}
			
		sendCapabilities(title, mOverview, mAcknowledge, param, aware);
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
	 * Initialize servlet.
	 *
	 * When instantiated as a servlet the framework calls this method to 
	 * perform initialization tasks.
	 **/
   public void init()
   	throws ServletException
   {
   	super.init();
   	
   	String value = getServletConfig().getInitParameter("BasePath");	// Where files are stored
   	if(value != null) setBasePath(value);
   	
   	value = getServletConfig().getInitParameter("AuthorityList");	// Authority list - handled locally
   	if(value != null) loadAuthority(value);
   	
   	value = getServletConfig().getInitParameter("Extension");
   	if(value != null) setExtension(value);
   }
   

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mWords.clear();	
		mCategory = null;
		mAllWords = false;
	}
	
	/** 
	 * Load options from HTTP request.
	 *
    * @param request	the {@link HttpServletRequest} with request information.
	 **/
	public void setFromRequest(HttpServletRequest request) 
		throws Exception
	{
		reset();	// Clear query parameters
		
		setWords(igpp.util.Text.getValue(request.getParameter("words"), ""));
		setAllWords(igpp.util.Text.getValue(request.getParameter("all"), ""));
		setCategory(igpp.util.Text.getValue(request.getParameter("category"), null));
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
		
		// Category overrides 
		if(igpp.util.Text.isSetMatch(mCategory, "-")) mCategory = null;
		
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
	 * Find all matching resource descriptions and send to the current output stream.
	 *
	 * The output is formatted as an XML response document.
	 **/
	public void doAction()
   	throws Exception
	{
		String catPath = "";
		if(mCategory != null) catPath = File.separator + mCategory;
		ArrayList<String> matches = new ArrayList<String>();
		
		Set<String> keyset = mAuthorityMap.keySet();
		for(String key : keyset) {
			String path = mAuthorityMap.get(key);
			System.out.println("Scanning: " + path + catPath);
			ArrayList<String> matchList = search(path + catPath, mWords);
			if(matchList != null) matches.addAll(matchList);
		}

		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		
		// Stream files
		mOut.println("<Package>");
		for(String name : matches) {
			stream(name);
		}
   	mOut.println("</Package>");
	}
	
	
	/** 
	 * Scan a path and locate all files which match a pattern.
	 *
	 * @param path the path to scan. 
	 * @param pattern	the regular expression pattern for the desired files.
	 *
	 * @return an {@link ArrayList} of {@link String} values of paths to the files and folders found.
	 * if the path is a folder, then an empty list is returned.
	 **/
	public ArrayList<String> scanPath(String path, String pattern)
		throws Exception
	{
		ArrayList<String> matches = new ArrayList<String>();
		
		if(path == null) return matches;
		if(pattern == null) return matches;
		
		String endPath = pattern;
		
		String[] part = pattern.split(":", 2);
		if(part.length == 2) { endPath = part[1]; }	// Remove scheme:
		
		if(endPath.startsWith("//")) endPath = endPath.substring(2);	// Remove "//"
		if(endPath.compareTo("*") == 0) endPath = "";
		if(endPath.compareTo("-") == 0) endPath = "";
		
		String fullPath = igpp.util.Text.concatPath(path, endPath);
		
		// File name filter
	   String[] list = new String[1];
	   list[0] = null;
	 
	   File filePath = new File(fullPath);
	   if(filePath.isDirectory()) {
			list = filePath.list(new FilenameFilter()	
		   	{ 
		   		// Accept all files that do not begin with a "."
		   		public boolean accept(File path, String name) { return ! name.startsWith("."); } 
		   	} 
		   	);
	   } else {
	   	if(filePath.exists()) list[0] = fullPath;
	   }
	   
	   for(String item: list) {
	   	if(item != null) matches.add(igpp.util.Text.concatPath(fullPath, item));
	   }
	   
	   return matches;
	}
	
	/** 
	 * Scan all files in the directory and return a list of files which contain
	 * the words or identifiers.
	 *
	 * @param path the path to scan. 
	 * @param words	the {@link ArrayList} of words to search for. If null no word search is performed.
	 *
	 * @return an {@link ArrayList} of {@link String} values of paths to the files and folders found.
	 **/
	public ArrayList<String> search(String path, ArrayList<String> words)
		throws Exception
	{
		ArrayList<String> matches = new ArrayList<String>();
		
		if(path == null) return matches;
				
		// File name filter
	   File filePath = new File(path);
	   File[] list = new File[1];
	   if(filePath.isDirectory()) {
			list = filePath.listFiles(new FileFilter()	
		   	{ 
		   		public boolean accept(File pathname) { return pathname.getName().endsWith(mExtension); } 
		   	} 
		   	);
	   } else {
	   	list[0] = filePath;
	   }

		
		String resourcePath;
		if(list != null) {	// Found some files to process
			for(File item : list) {
				resourcePath = item.getCanonicalPath();
				try {
					if(scan(resourcePath, words)) matches.add(resourcePath);
				} catch(Exception e) {
					mOut.println("Error parsing: " + resourcePath);
					mOut.println(e.getMessage());
				}
			}		
		}
		
		// Now recurse if asked to
		if(mRecurse) {
		   list = filePath.listFiles(new FileFilter()	
		   	{ 
		   		// Skip hidden files (.*), relative directories (. and ..) and "Granule" folders
		   		public boolean accept(File pathname) { if(pathname.getName().compareToIgnoreCase("Granule") == 0) return false;
		   		                                       return (pathname.isDirectory() && !pathname.getName().startsWith(".")); } 
		   	} 
		   	);
			if(list != null) {	// Found some files to process
				for(int y = 0; y < list.length; y++) {
					matches.addAll(search(list[y].getCanonicalPath(), words));			
				}
			}
		}
		
		return matches;
	}

	/**
	 * Scan a file and determine if the words or identifier are present.
	 *
	 * @param path the path to scan. 
	 * @param words	the {@link ArrayList} of words to search for. If null no word search is performed.
	 *
	 * @return true of words or identifier are founde, false otherwise.
	 **/	
	public boolean scan(String path, ArrayList<String> words)
		throws Exception
	{
		boolean match = false;
		boolean done = false;
		String	buffer;
		ArrayList<String> foundWords = new ArrayList<String>();
		
		// Check for wild card
		for(String word : words) { if(word.compareTo("*") == 0) return true; }
		
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		
		// Search for word
		while((buffer = reader.readLine()) != null) {
			buffer = buffer.toUpperCase();
			for(String word : words) {
				String[] snip = buffer.split("[\\p{Punct}\\p{Blank}]");
				for(String temp : snip) {
					if(temp.equals(word)) { match = true; if(foundWords.indexOf(word) == -1) foundWords.add(word); } 
					if(match && ! mAllWords) { done = true; break; }
				}
				if(done) break;
				if(mAllWords) { // Check if all words have been found
					match = false;
					if(foundWords.size() == words.size()) { match = true; done = true; break; }
				}
			}
			if(done) break;
			if(mAllWords) { // Check if all words have been found
				match = false;
				if(foundWords.size() == words.size()) { match = true; done = true; break; }
			}
		}
		
		reader.close();
		
		return match;
	}
	
	/**
	 * Stream content between the <Spase>/</Spase> tags
	 * 
	 * The content of a SPASE XML document is sent to the output stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
	 **/
	public void stream(String pathname)
		throws Exception
	{
		boolean on = false;
		String	buffer;
		BufferedReader reader = null;
	
		try {
			File file = new File(pathname);
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			
			// Search for word
			while((buffer = reader.readLine()) != null) {
				if(buffer.indexOf("<?xml ") != -1) continue;
				mOut.println(buffer);
			}
		} catch(Exception e) {
		} finally {
			if(reader != null) reader.close();
		}	
	}
	
	/**
	 * Read the content of a SPASE XML document into a String. 
	 * The XML document tag "<xml ... ?> is striped from the stream.
	 *
	 * @param pathname the pathname of the file to scan and read.
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
				if(buffer.indexOf("<?xml ") != -1) continue;
				content.append(buffer);
			}
		} catch(Exception e) {
		} finally {
			if(reader != null) reader.close();
		}	
			
		return new String(content);
	}
	
	
	/**
	 * Send file contents to the current output stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
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
	
	private void setExtension(String value) { mExtension = value; }
	
	public void setBasePath(String value) { mAuthorityMap.put("base", value); }
	
	public void setWords(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; if(value.compareTo("*") == 0) { mWords.add(value); return; } String[] words = value.split("[\\p{Punct}\\p{Blank}]"); for(String word : words) { word = word.trim(); if(word.length() > 0) mWords.add(word.toUpperCase()); } }
	public ArrayList<String> getWords() { return mWords; }
	
	public void setCategory(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; mCategory = value; }
	public String getCategory() { return mCategory; }
	
	public void setAllWords(boolean value) { mAllWords = value; }
	public void setAllWords(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; mAllWords = igpp.util.Text.isTrue(value); }
	public boolean getAllWords() { return mAllWords; }
	
}
