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

public class SimpleReg extends SmartHttpServlet
{
	private String	mVersion = "1.0.1";

	// Service configuration
	String	mRootPath = ".";
	String	mExtension = ".xml";
	String	mXSLPathName = "spase-combined-tk.xsl";
	
	// Task options
	boolean	mRecurse = true;
	boolean	mAllWords = true;
	ArrayList<String>	mWords = new ArrayList<String>();
	String	mIdentifier = null;
	String	mCategory = null;
	boolean	mCheck = false;
	String	mPattern = null;
	String	mContent = null;
	
   /** 
	 * Command-line interface.
	 **/
	public static void main(String args[])
   {
		SimpleReg me = new SimpleReg();
		
		System.out.println("Version: " + me.mVersion);
		
		if(args.length < 1) {
			System.out.println("Usage: " + me.getClass().getName() + "[-b {path}] [-p {pattern}] [-i {identifier}] [-x {extension}] [-c {category}] [-k] [-r] {words}");
			return;
		}

		me.mOut.setOut(System.out);
	
		try {
			for(int i = 0; i < args.length; i++) {
				if(args[i].compareTo("-b") == 0) {	// set base path
					i++;
					if(i < args.length) me.setRootPath(args[i]);
				} else if(args[i].compareTo("-x") == 0) {	// Extension
					i++;
					if(i < args.length) me.setExtension(args[i]);
				} else if(args[i].compareTo("-k") == 0) {	// check only
					me.setCheck(true);
				} else if(args[i].compareTo("-i") == 0) {	// identifier
					i++;
					if(i < args.length) me.setIdentifier(args[i]);
				} else if(args[i].compareTo("-p") == 0) {	// pattern
					i++;
					if(i < args.length) me.setPattern(args[i]);
				} else if(args[i].compareTo("-t") == 0) {	// content
					i++;
					if(i < args.length) me.setContent(args[i]);
				} else if(args[i].compareTo("-s") == 0) {	// stylsheet
					i++;
					if(i < args.length) me.setXSLPathName(args[i]);
				} else if(args[i].compareTo("-c") == 0) {	// category
					i++;
					if(i < args.length) me.setCategory(args[i]);
				} else if(args[i].compareTo("-r") == 0) {	// Recurse
					me.mRecurse = true;
				} else {	// Load resource at path
				   me.setWords(args[i]);
				}
			}
			me.doAction();
		} catch(Exception e) {
			e.printStackTrace();
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
   	
		String value = getServletConfig().getInitParameter("RootPath");
   	if(value != null) setRootPath(value);
   	
   	value = getServletConfig().getInitParameter("Extension");
   	if(value != null) setExtension(value);
   	
   	value = getServletConfig().getInitParameter("XSLPathName");
   	if(value != null) setXSLPathName(value);
   }
   

	/**
	 * Return all internal options to default values.
	 **/
	public void reset()
	{
		mWords.clear();	
		mCategory = null;
		mAllWords = true;
		mIdentifier = null;
		mCheck = false;
		mPattern = null;
		mContent = null;
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
		setIdentifier(igpp.util.Text.getValue(request.getParameter("identifier"), null));
		setPattern(igpp.util.Text.getValue(request.getParameter("pattern"), null));
		setContent(igpp.util.Text.getValue(request.getParameter("content"), null));
		setCategory(igpp.util.Text.getValue(request.getParameter("category"), null));
		setCheck(igpp.util.Text.getValue(request.getParameter("check"), ""));
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
		
		// Identifier overrides words
		if(mIdentifier != null) { mWords.clear(); mAllWords = false; }
		
		// Category overrides 
		if(igpp.util.Text.isSetMatch(mCategory, "-")) mCategory = null;
		
		// get ready to write response
		mOut.setOut(response.getWriter());
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
		
		
		String basePath = mRootPath + catPath;
		if(mPattern != null) {
			if(mContent != null) {
				if(igpp.util.Text.isMatch(mContent, "HTML")) {	// Transform to HTML
					String content = streamToString(igpp.util.Text.concatPath(basePath, mPattern), false);
					igpp.xml.Transform.perform(
						new StringReader(content),
						mXSLPathName, mOut);
				} else {	// Stream raw contents
					mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					streamFile(igpp.util.Text.concatPath(basePath, mPattern));
				}
				return;
			}
			matches = scanPath(basePath, mPattern);
			Collections.sort(matches);
		} else {
			matches = search(basePath, mWords, mIdentifier);
		}

		mOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		
		// Stream files
		if(mPattern != null) {	// Send file lists
			mOut.println("<Response>");
			for(String name : matches) {
				File test = new File(name);
				if(test.isDirectory()) {	// Node
					mOut.println("<node pattern=\"" + name.substring(basePath.length()) + "\"" 
						+ " id=\"" + name.substring(basePath.length()) + "\""
						+ " text=\"" + igpp.util.Text.getFileBase(test.getName()) + "\""
						+ " name=\"" + igpp.util.Text.getFileBase(test.getName()) + "\""
						+ " />");
				} else {	// Leaf
					mOut.println("<leaf pattern=\"" + name.substring(basePath.length()) + "\""
						+ " id=\"" + name.substring(basePath.length()) + "\""
						+ " text=\"" + igpp.util.Text.getFileBase(test.getName()) + "\""
						+ " name=\"" + igpp.util.Text.getFileBase(test.getName()) + "\""
						+ " />");
				}
			}
			mOut.println("</Response>");
		} else {	// Return content
			if(mCheck) {	// Return status tag
				mOut.println("<Check>");
				mOut.println("   <Valid>" + igpp.util.Text.getYesNo(matches.size() > 0) + "</Valid>");
				mOut.println("</Check>");
			} else {	// Stream the descriptions
				mOut.println("<Spase>");
				mOut.println("   <Version>1.2.1</Version>");
				for(String name : matches) {
					stream(name);
				}
				mOut.println("</Spase>");
			}
		}
		
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
	 * @param identifier the resource identifier to search for. If null no identifier search is performed.
	 *
	 * @return an {@link ArrayList} of {@link String} values of paths to the files and folders found.
	 **/
	public ArrayList<String> search(String path, ArrayList<String> words, String identifier)
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
					if(scan(resourcePath, words, identifier)) matches.add(resourcePath);
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
		   		public boolean accept(File pathname) { return (pathname.isDirectory() && !pathname.getName().startsWith(".")); } 
		   	} 
		   	);
			if(list != null) {	// Found some files to process
				for(int y = 0; y < list.length; y++) {
					matches.addAll(search(list[y].getCanonicalPath(), words, identifier));			
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
	 * @param identifier the resource identifier to search for. If null no identifier search is performed.
	 *
	 * @return true of words or identifier are founde, false otherwise.
	 **/	
	public boolean scan(String path, ArrayList<String> words, String identifier)
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
			if(identifier != null) {
				if(buffer.indexOf(identifier) != -1) {match = true; break; }
			}
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
		stream(pathname, true);
	}
	
	/**
	 * Stream content between the <Spase>/</Spase> tags
	 * 
	 * The content of a SPASE XML document is sent to the output stream.
	 * The version tag can optionally be striped from the stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
	 * @param stripVersion if true the version tag with be stripped, otherwise it will be included.
	 **/
	public void stream(String pathname, boolean stripVersion)
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
				if(buffer.indexOf("<Version>") != -1 && stripVersion) continue;	// Skip
				if(on) {	// Check for end tag
					if(buffer.indexOf("</Spase>") != -1) on = false;
					else mOut.println(buffer);
				}
				if(buffer.indexOf("<Spase") != -1) on = true;
			}
		} catch(Exception e) {
		} finally {
			if(reader != null) reader.close();
		}	
	}
	
	/**
	 * Read content between the <Spase>/</Spase> tags into a String
	 *
	 * The content of a SPASE XML document is processed and streamed to 
	 * a String. The version tag can optionally be striped from the stream.
	 *
	 * @param pathname the pathname of the file to scan and stream.
	 * @param stripVersion if true the version tag with be stripped, otherwise it will be included.
	 *
	 * @return {@link String} containing the extracted content.
	 **/
	public String streamToString(String pathname, boolean stripVersion)
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
				if(buffer.indexOf("<Version>") != -1 && stripVersion) continue;	// Skip
				if(on) {	// Check for end tag
					if(buffer.indexOf("</Spase>") != -1) on = false;
					content.append(buffer);
				}
				if(buffer.indexOf("<Spase") != -1) { on = true; content.append("<Spase>"); }
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
	private void setRootPath(String value) { mRootPath = value; }
	private void setXSLPathName(String value) { mXSLPathName = value; }
	
	public void setWords(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; if(value.compareTo("*") == 0) { mWords.add(value); return; } String[] words = value.split("[\\p{Punct}\\p{Blank}]"); for(String word : words) { word = word.trim(); if(word.length() > 0) mWords.add(word.toUpperCase()); } }
	public ArrayList<String> getWords() { return mWords; }
	
	public void setIdentifier(String value) { if(igpp.util.Text.isEmpty(value)) return; mIdentifier = "<ResourceID>" + value + "</ResourceID>"; }
	public String getIdentifier() { return mIdentifier; }
	
	public void setPattern(String value) { if(igpp.util.Text.isEmpty(value)) return; mPattern = value; }
	public String getPattern() { return mPattern; }
	
	public void setCategory(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; mCategory = value; }
	public String getCategory() { return mCategory; }
	
	public void setAllWords(boolean value) { mAllWords = value; }
	public void setAllWords(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; mAllWords = igpp.util.Text.isTrue(value); }
	public boolean getAllWords() { return mAllWords; }
	
	public void setCheck(boolean value) { mCheck = value; }
	public void setCheck(String value) throws Exception { if(igpp.util.Text.isEmpty(value)) return; mCheck = igpp.util.Text.isTrue(value); }
	public boolean getCheck() { return mCheck; }
	
	public void setContent(String value) { mContent = value; }
	public String getContent() { return mContent; }

}
