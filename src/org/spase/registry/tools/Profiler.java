/**
 * Scans SPASE descriptions and generates profiles
 * which can be submitted to a solr search engine.
 *<p>
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2009-06-04
 */

package org.spase.registry.tools;

import igpp.util.Date;

import igpp.xml.XMLGrep;
import igpp.xml.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;

// import org.apache.commons.cli.*;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

public class Profiler
{
	static String	mVersion = "1.0.1";

	boolean	mVerbose = false;

	String	mExtension = ".xml";
	String	mRegistryID = "";
	boolean	mRecurse = false;
	String mLookup = "http://www.spase-group.org/registry/resolver";

	PrintStream	mOut = System.out;

	String	mFindPrefix = null;
	String	mFindReplace = null;

	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public Profiler() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "r", "recurse", false, "Recurse. Process all items in the current folder. Recurse into sub-folders.");
		mAppOptions.addOption( "f", "file", true, "File. File containing a list of file names to scan." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated profiles to {file}. Default: System.out." );
		mAppOptions.addOption( "l", "lookup", true, "Lookup. The URL to the resource lookup service to resolve resource IDs. Default: " + mLookup);
		mAppOptions.addOption( "x", "extension", true, "Extension. The file name extension for files to process (default: " + mExtension + ")" );
		mAppOptions.addOption( "i", "id", true, "ID. The registry ID to set for each resource" );
	}
   /**
	 * Command-line interface.
	 *
	 * @since		1.0
	 **/
	public static void main(String args[])
   {
   	String	filename = null;
   	String	outfile = null;
   	String	prefix = null;

		Profiler me = new Profiler();

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
			if(line.hasOption("f")) filename = line.getOptionValue("f");
			if(line.hasOption("o")) outfile = line.getOptionValue("o");
			if(line.hasOption("p")) prefix = line.getOptionValue("p");
			if(line.hasOption("i")) me.mRegistryID = line.getOptionValue("i");

			if(outfile != null) {
				me.mOut = new PrintStream(new FileOutputStream(outfile));
			}

			// Parse prefix if given
			if(prefix != null) {
				String part[] = prefix.split("::", 2);
				if(part.length != 2) {
					System.out.println("Error in prefix. Proper syntax is \"find::replace\" which will substitute \"find\" with \"replace\".");
				} else {
					me.mFindPrefix = part[0];
					me.mFindReplace = part[1];
				}
			}


			me.writeProfileHeader();

			// Process all files
			if(filename != null) {	// Process items in file
				if(me.mVerbose) System.out.println("Processing list from file: " + filename);
				me.makeProfileFromFileList(filename);
			}

			// Process other command line areguments
			for(String p : line.getArgs()) {
				if(me.mVerbose) System.out.println("Processing: " + p);
				me.makeProfileFromFile(p);
			}

			me.writeProfileFooter();

		} catch(Exception e) {
			e.printStackTrace();
		}
   }

 	/**
	 * Display help information.
    *
	 * @since           1.0
    **/
	public void showHelp()
	{
		System.out.println("");
		System.out.println(getClass().getName() + "; Version: " + mVersion);
		System.out.println("Profile generator. Create resource profiles for SPASE resource descriptions.");
		System.out.println("Profiles all have a common schema which can be used in a solr search engine.");
		System.out.println("");

		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java " + getClass().getName() + " [options] [file...]", mAppOptions);

		System.out.println("");
		System.out.println("Acknowledgements:");
		System.out.println("Development funded by NASA's VMO project at UCLA.");
		System.out.println("");
		System.out.println("Example");
		System.out.println("To create profiles for all resources in the current directory and below use the command:");
		System.out.println("");
		System.out.println("   java " + getClass().getName() + " -o /temp/vmo.xml -r *");
		System.out.println("");
		System.out.println("The profiles will be written to the file \"/temp/vmo.xml\". The profiles can then be posted the appropriate solr search engine.");
		System.out.println("");
	}


	/**
	 * Read a list of file names from a file and load each one.
	 * Lines beginning with a "#" are considered comments and ignored
    *
    * @param path		the pathname of the file to parse.
    *
	 * @since           1.0
    **/
	public void makeProfileFromFileList(String path)
		throws Exception
	{
		if(path == null) return;

		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

		String	buffer;
		while((buffer = reader.readLine()) != null) {
			if(buffer.startsWith("#")) continue;	// Comment
			makeProfileFromFile(buffer);
		}

	}

	/**
	 * Writes the profile output header to the current output stream.
    *
	 * @since           1.0
    **/
	public void writeProfileHeader()
		throws Exception
	{
		mOut.println("<!--");
		mOut.println("  SOLR \"add\" messages for SPASE resource profiles.");
		mOut.println("-->");
		mOut.println("<add>");
	}

	/**
	 * Writes the profile output footer to the current output stream.
    *
	 * @since           1.0
    **/
	public void writeProfileFooter()
		throws Exception
	{
		mOut.println("</add>");
	}

	/**
	 * Read all SPASE resource descriptions at the given path.
	 * Resource descriptions are files that have a defined extension.
	 * The path to the resources can be recusively searched.
    *
    * @param path		the pathname of the file to parse.
    *
	 * @since           1.0
    **/
	public void makeProfileFromFile(String path)
		throws Exception
	{
		if(path == null) return;

		String	resourcePath = path;

		File filePath = new File(resourcePath);

	   File[] list = null;

	   if(filePath.isFile()) {
	   	list = new File[1];
	   	list[0] = filePath;
	   } else {	// try as directory
	   	list = filePath.listFiles(new FileFilter()
	   	{
	   		public boolean accept(File pathname) { return pathname.getName().endsWith(mExtension); }
	   	}
	   	);
	   }

		if(list != null) {	// Found some files to process
			for(int y = 0; y < list.length; y++) {
				resourcePath = list[y].getCanonicalPath();
				if(mVerbose) System.out.println(resourcePath);
				makeProfile(resourcePath);
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
					makeProfileFromFile(list[y].getCanonicalPath());
				}
			}
		}
	}

	/**
	 * Read the SPASE resource descriptions at the given path or URL
	 * and output a resource profile.
    *
    * @param path		the pathname or URL to the file to parse.
    *
	 * @since           1.0
    **/
	public void makeProfile(String path)
		throws Exception
	{
		if(path == null) return;

		Document	doc;
		ArrayList<Node> resourceList;
		ArrayList<Pair> docIndex = new ArrayList<Pair>();
		ResourceProfile profile;
		String	resourcePath = path;

		try {
			doc = igpp.xml.XMLGrep.parse(resourcePath);
			docIndex = igpp.xml.XMLGrep.makeIndex(doc, "");
		} catch(Exception e) {
			System.out.println("Error parsing: " + resourcePath);
			System.out.println(e.getMessage());
			return;
		}
		String version = igpp.xml.XMLGrep.getFirstValue(docIndex, "/Spase/Version", null);
		int startAt = -1;
		int endAt = -1;

		// Load all other resources - if any
		String[] dataTags = new String[] {"NumericalData", "DisplayData", "Catalog"};
		boolean success = false;
		for(String tagName : dataTags) {
			while(true) {
				startAt = igpp.xml.XMLGrep.findFirstElement(docIndex, "/Spase/" + tagName + "/.*", startAt);
				if(startAt == -1)	break;	// All done
				endAt = igpp.xml.XMLGrep.findLastElement(docIndex, "/Spase/" + tagName + "/.*", startAt);
				profile = makeResourceProfile(docIndex, version, tagName, startAt, endAt);
				if(profile != null) {	// Valid - add it
					setInstrumentInfo(profile);	// Must preceed calling setObservatoryInfo()
					setObservatoryInfo(profile);
					profile.setAuthorityFromResourceID();
					profile.setRegistryID(mRegistryID);
					profile.setResourceType(tagName);
					profile.normalize();	// Fill in blank fields with defaults.
					profile.printSolrProfile(mOut);
					success = true;
					// storeProfile(profile);
				}
				startAt = endAt + 1;
			}
		}
		if( ! success) {
			System.out.println("Error parsing: " + resourcePath);
			System.out.println("No corresponding data resource found.");
		}
	}

	/**
	 * Creates a delete message for Solr.
	 *
	 * The expected unique key for a resource in the Solr schema is "resourceid".
    *
    * @param id		the unique key for the entry to delete.
    *
	 * @since           1.0
    **/
	public void makeDeleteItem(String id)
		throws Exception
	{
		if(id == null) return;

		mOut.println("<delete><reosurceid>" + id + "</resourceid></delete>");
	}

	/**
	 * Set the instrument information from the instrument ID
    *
    * @param profile		the {ResourceProfile} to update.
    *
	 * @since           1.0
    **/
	public void setInstrumentInfo(ResourceProfile profile)
		throws Exception
	{
		Document	doc;
		ArrayList<Pair> segment = null;
		String descURL = mLookup;	// Path to common info (Instrument/Observatory)

		if(profile.getInstrumentID().length() == 0) return;

		descURL += "?id=" + profile.getInstrumentID();

		try {
			doc = igpp.xml.XMLGrep.parse(descURL);	// Load and parse file.
			segment = igpp.xml.XMLGrep.makeIndex(doc, "");
		} catch(Exception e) {
			System.out.println("Unable to locate: " + descURL);
			return;
		}

		profile.setInstrumentName(igpp.xml.XMLGrep.getValues(segment, "/Spase/Instrument/ResourceHeader/ResourceName"));
		profile.setInstrumentType(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/Instrument/InstrumentType", ""));
		profile.setObservatoryID(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/Instrument/ObservatoryID", ""));
		profile.addWords(igpp.xml.XMLGrep.getWords(segment));
	}

	/**
	 * Get the ResourceName in a resource description.
    *
    * @param resourceID the ResourceID of the resource.
    *
	 * @since           1.0
    **/
	public String getResourceName(String resourceID)
		throws Exception
	{
		Document	doc;
		ArrayList<Pair> segment = null;
		String descURL = mLookup;	// Path to common info (Instrument/Observatory)

		if(resourceID == null) return "";
		if(resourceID.length() == 0) return "";

		descURL += "?id=" + resourceID;

		try {
			doc = igpp.xml.XMLGrep.parse(descURL);	// Load and parse file.
			segment = igpp.xml.XMLGrep.makeIndex(doc, "");
			return igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/.*/ResourceHeader/ResourceName", "");
		} catch(Exception e) {
			System.out.println("Unable to locate: " + descURL);
			return "";
		}
	}

	/**
	 * Set the observatory information from the observatory ID
    *
    * @param profile		the {ResourceProfile} to update.
    *
	 * @since           1.0
    **/
	public void setObservatoryInfo(ResourceProfile profile)
		throws Exception
	{
		Document	doc;
		ArrayList<Pair> segment = null;
		String descURL = mLookup;	// Path to common info (Instrument/Observatory)

		if(profile.getObservatoryID().length() == 0) return;

		// String part[] = profile.getObservatoryID().split("://", 2);	// Split URN. It should start with "spase:".
		// descURL += "?id=" + part[1];
		descURL += "?id=" + profile.getObservatoryID();

		try {
			doc = igpp.xml.XMLGrep.parse(descURL);	// Load and parse file.
			segment = igpp.xml.XMLGrep.makeIndex(doc, "");
		} catch(Exception e) {
			System.out.println("Unable to locate: " + descURL);
			return;
		}

		// In Version 2.2.0 of the data model ObservatoryGroup was replaced with ObservatoryGroupID
		String group = igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/Observatory/ObservatoryGroup", null);
		ArrayList<String> nameList = new ArrayList<String>();
		if(group == null) {	// Determine ObservatoryGroupID and extract resource name
			ArrayList<String> idList = new ArrayList<String>();
			idList.addAll(igpp.xml.XMLGrep.getValues(segment, "/Spase/Observatory/ObservatoryGroupID"));
			for(String id : idList) {
				nameList.add(getResourceName(id));
			}
		} else {	// Use values of ObservatoryGroup
			nameList.addAll(igpp.xml.XMLGrep.getValues(segment, "/Spase/Observatory/ObservatoryGroup"));
		}
		profile.setObservatoryGroup(nameList);
		profile.setObservatoryName(igpp.xml.XMLGrep.getValues(segment, "/Spase/Observatory/ResourceHeader/ResourceName"));
		profile.setObservatoryRegion(igpp.xml.XMLGrep.getValues(segment, "/Spase/Observatory/Location/ObservatoryRegion"));

		profile.setObservatoryType("Spacecraft");	// If Region is not specified assume to be spacecraft

		String region = igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/Observatory/Location/ObservatoryRegion", null);
		if(region != null) {	// Region specified
			if(region.compareToIgnoreCase("Earth.Surface") == 0) { // Groundbased
				profile.setObservatoryType("Groundbased");
				profile.setLatitude(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/Observatory/Location/Latitude", ""));
				profile.setLongitude(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/Observatory/Location/Longitude", ""));
			} else {	// Spacecraft
				profile.setObservatoryType("Spacecraft");
			}
		}
		profile.addWords(igpp.xml.XMLGrep.getWords(segment));
	}

	/**
	 * Extract information from a {Pair} list of values and store in a new resource profile.
    *
    * @param list		the @link{ArrayList} of @link{Pair} calues to scan.
    * @param version		the version of SPASE metadata schema the list was extracted form.
    * @param resourceTagName		the resource type to search for.
    * @param startAt		the index of the item in the list to start the search.
    * @param endAt		the index of the item in the list to end the search.
    *
    * @return          a populated @link{ResourceProfile}
    *
	 * @since           1.0
    **/
	public ResourceProfile makeResourceProfile(ArrayList<Pair> list, String version, String resourceTagName, int startAt, int endAt)
	{
		ArrayList<Pair> segment = igpp.xml.XMLGrep.getSegment(list, startAt, endAt);

		// Check that resource is well formed
		String buffer = "";
		buffer = igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/ResourceID", "");
		if(buffer == null) return null;
		if(buffer.length() ==0) return null;

		ResourceProfile profile = new ResourceProfile();

		profile.setResourceID(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/ResourceID", ""));
		profile.setResourceName(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/ResourceHeader/ResourceName", ""));
		profile.setReleaseDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/ResourceHeader/ReleaseDate", ""));

		profile.setCadence(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/Cadence", ""));

		profile.setInstrumentID(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/InstrumentID", ""));

		profile.setObservedRegion(igpp.xml.XMLGrep.getValues(segment, "/Spase/" + resourceTagName + "/ObservedRegion"));
		profile.setPhenomenonType(igpp.xml.XMLGrep.getValues(segment, "/Spase/" + resourceTagName + "/PhenomenonType"));
		profile.setMeasurementType(igpp.xml.XMLGrep.getValues(segment, "/Spase/" + resourceTagName + "/MeasurementType"));

		profile.setDescription(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/ResourceHeader/Description", ""));

		// Note: Catalog use "TimeSpan", others use "TemporalDescription"
		profile.setStartDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/StartDate", ""));

		if(version.startsWith("1.2.") || version.startsWith("1.1.")) {
			if(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/RelativeEndDate", null) != null) {
				profile.setStopDate(igpp.util.Date.getISO8601DateString(igpp.util.Date.parseISO8601Duration(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/RelativeEndDate", ""))));
			} else {
				profile.setStopDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/EndDate", ""));
			}
		} else {
			if(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/RelativeStopDate", null) != null) {
				profile.setStopDate(igpp.util.Date.getISO8601DateString(igpp.util.Date.parseISO8601Duration(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/RelativeStopDate", ""))));
			} else {
				profile.setStopDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TimeSpan/StopDate", ""));
			}
		}

		// Now try with TemporalDescription
		profile.setStartDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/StartDate", profile.getStartDate()));

		if(version.startsWith("1.2.") || version.startsWith("1.1.")) {
			if(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/RelativeEndDate", null) != null) {
				profile.setStopDate(igpp.util.Date.getISO8601DateString(igpp.util.Date.parseISO8601Duration(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/RelativeEndDate", ""))));
			} else {
				profile.setStopDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/EndDate", ""));
			}
		} else {
			if(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/RelativeStopDate", null) != null) {
				profile.setStopDate(igpp.util.Date.getISO8601DateString(igpp.util.Date.parseISO8601Duration(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/RelativeStopDate", ""))));
			} else {
				profile.setStopDate(igpp.xml.XMLGrep.getFirstValue(segment, "/Spase/" + resourceTagName + "/TemporalDescription/TimeSpan/StopDate", ""));
			}
		}

		// Words
		profile.setWords(igpp.xml.XMLGrep.getWords(segment));

		// Keywords
		profile.setKeywords(igpp.xml.XMLGrep.getValues(segment, "/Spase/.*/Keyword"));

		// Associations
		String[] idTags = new String[] {"InputResource", "Instrument", "Observatory", "Parent", "Prior", "Repository", "Registry"};
		for(String tagName : idTags) {
			startAt = 0;
			String pattern = ".*/" + tagName + "ID/.*";	// Find tags anywhere in xPath
			ArrayList<String> values = igpp.xml.XMLGrep.getValues(segment, pattern);
			profile.addAssociation(values);
		}


		return profile;
	}

	public void setOutput(PrintStream out) { mOut = out; }
	public PrintStream getOutput() { return mOut; }
}
