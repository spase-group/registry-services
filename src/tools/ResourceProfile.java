/**
 * Resource profile class. 
 * Data and methods to manage resource information.
 *<p>
 * Development funded by NASA's VMO project at UCLA.
 *
 * @author Todd King
 * @version 1.00 2007-01-05
 */

package org.spase.registry.tools;

import java.util.ArrayList;
import java.io.PrintStream;

import java.net.URI;

public class ResourceProfile
{
	private String	mVersion = "1.0.0";
	
	String mRegistryID = "";
	String mResourceType = "";
	String mResourceID = "";
	String mResourceName = "";
	String mReleaseDate = "";
	String mStartDate = "";
	String mStopDate = "";
	String mCadence = "";
	ArrayList<String>  mMeasurementType  = new ArrayList<String>();
	ArrayList<String>  mPhenomenonType  = new ArrayList<String>();
	ArrayList<String>  mObservedRegion  = new ArrayList<String>();
	String mObservatoryName = "";
	String mObservatoryID = "";
	ArrayList<String> mObservatoryGroup  = new ArrayList<String>();
	String mObservatoryType = "";
	String mInstrumentID = "";
	String mInstrumentName = "";
	String mInstrumentType = "";
	String mDescription = "";
	String mAuthority = "";
	String mLatitude = "";
	String mLongitude = "";
	ArrayList<String> mAssociation = new ArrayList<String>();
   ArrayList<String> mWords = new ArrayList<String>();
   ArrayList<String> mKeywords = new ArrayList<String>();

    /** 
	 * Command-line interface.
	 *
	 * @since		1.0
	 */
	public static void main(String args[])
   {
		ResourceProfile me = new ResourceProfile();
		
		System.err.println("Version: " + me.mVersion);
   }
 
	/**
	 * Print an XML document suitable for loading into a solr search engine.
    *
    * @param out		the @link{PrintStream} to emit the XML document.
    *
	 * @since           1.0
    **/
   public void printSolrProfile(PrintStream out)
   {
   	out.println("<doc>");
		out.println("   <field name=\"registryid\">" + mRegistryID + "</field>");
		out.println("   <field name=\"resourcetype\">" + mResourceType + "</field>");
		out.println("   <field name=\"resourceid\">" + mResourceID + "</field>");
		out.println("   <field name=\"resourcename\">" + mResourceName + "</field>");
		for(String buffer : mMeasurementType) { out.println("   <field name=\"measurementtype\">" + buffer + "</field>"); }
		for(String buffer : mPhenomenonType) { out.println("   <field name=\"phenomenontype\">" + buffer + "</field>"); }
		for(String buffer : mObservedRegion) { out.println("   <field name=\"observedregion\">" + buffer + "</field>"); }
		out.println("   <field name=\"observatoryid\">" + mObservatoryID + "</field>");
		out.println("   <field name=\"observatoryname\">" + mObservatoryName + "</field>");
		out.println("   <field name=\"observatorytype\">" + mObservatoryType + "</field>");
		for(String buffer : mObservatoryGroup) {	out.println("   <field name=\"observatorygroup\">" + buffer + "</field>"); }
		out.println("   <field name=\"instrumentid\">" + mInstrumentID + "</field>");
		out.println("   <field name=\"instrumentname\">" + mInstrumentName + "</field>");
		out.println("   <field name=\"instrumenttype\">" + mInstrumentType + "</field>");
		out.println("   <field name=\"releasedate\">" + mReleaseDate + "</field>");
		out.println("   <field name=\"startdate\">" + mStartDate + "</field>");
		out.println("   <field name=\"stopdate\">" + mStopDate + "</field>");
		out.println("   <field name=\"cadence\">" + mCadence + "</field>");
		out.println("   <field name=\"latitude\">" + mLatitude + "</field>");
		out.println("   <field name=\"longitude\">" + mLongitude + "</field>");
		out.println("   <field name=\"description\">" + mDescription + "</field>");
		out.println("   <field name=\"authority\">" + mAuthority + "</field>");
		
		for(String buffer : mAssociation) {	out.println("   <field name=\"association\">" + buffer + "</field>"); }
		for(String buffer : mWords) {	out.println("   <field name=\"word\">" + buffer + "</field>"); }
		for(String buffer : mKeywords) {	out.println("   <field name=\"keyword\">" + buffer + "</field>"); }
		
   	out.println("</doc>");
   }

	/** 
	 * Set undefined values to normalized states.
	 *
	 * @since           1.0
    **/
	public void normalize()
	{
		if(igpp.util.Text.isEmpty(mStartDate)) mStartDate = "0000-00-00T00:00:00Z";
		if(igpp.util.Text.isEmpty(mStopDate)) mStopDate = "9999-12-31T24:00:00Z";
		if(igpp.util.Text.isEmpty(mReleaseDate)) mReleaseDate = igpp.util.Date.now();
	
		mStartDate = fixTime(mStartDate);	
		mStopDate = fixTime(mStopDate);
		mReleaseDate = fixTime(mReleaseDate);
		
		// A cludge of a fix. Sometimes text has XML entities which are not properly coded. "&" is the only one handled
		mResourceName = org.apache.commons.lang.StringEscapeUtils.escapeXml(mResourceName);
		mObservatoryName = org.apache.commons.lang.StringEscapeUtils.escapeXml(mObservatoryName);
		mInstrumentName = org.apache.commons.lang.StringEscapeUtils.escapeXml(mInstrumentName);
		mDescription = org.apache.commons.lang.StringEscapeUtils.escapeXml(mDescription);
	}
   
	/** 
	 * Modify a time string to be ISO 8601 compliant.
	 * The string is assumed to be nearly ISO 8601 compliant and
	 * to have the nominal format of "YYYY-MM-DD HH:MM:SS.sss"
	 * This will replace all spaces with a "T" and ensure
	 * that the string ends with a "Z".
	 *
	 * @since           1.0
    **/
   public String fixTime(String time) {
   	time = time.trim();
   	time = time.replace(" ", "T");
   	if( ! time.endsWith("Z")) time += "Z";
   	
   	return time;
   }
   
	public void setRegistryID(String value) { mRegistryID = value; }
	public void setRegistryID(ArrayList<String> value) { if(value.size() > 0) mRegistryID = value.get(0); }
	public String getRegistryID() { return mRegistryID; }
	
	public void setResourceType(String value) { mResourceType = value; }
	public void setResourceType(ArrayList<String> value) { if(value.size() > 0) mResourceType = value.get(0); }
	public String getResourceType() { return mResourceType; }
	
	public void setResourceID(String value) { mResourceID = value; }
	public void setResourceID(ArrayList<String> value) { if(value.size() > 0) mResourceID = value.get(0); }
	public String getResourceID() { return mResourceID; }
	
	public void setResourceName(String value) { mResourceName = value; }
	public void setResourceName(ArrayList<String> value) { if(value.size() > 0) mResourceName = value.get(0); }
	public String getResourceName() { return mResourceName; }
	
	public void setReleaseDate(String value) { mReleaseDate = value; }
	public void setReleaseDate(ArrayList<String> value) { if(value.size() > 0) mReleaseDate = value.get(0); }
	public String getReleaseDate() { return mReleaseDate; }

	public void setStartDate(String value) { mStartDate = value; }
	public void setStartDate(ArrayList<String> value) { if(value.size() > 0) mStartDate = value.get(0); }
	public String getStartDate() { return mStartDate; }

	public void setStopDate(String value) { mStopDate = value; }
	public void setStopDate(ArrayList<String> value) { if(value.size() > 0) mStopDate = value.get(0); }
	public String getStopDate() { return mStopDate; }

	public void setCadence(String value) { mCadence = igpp.util.Date.translateISO8601Duration(value); }
	public void setCadence(ArrayList<String> value) { if(value.size() > 0) setCadence(value); }
	public String getCadence() { return mCadence; }

	public void addMeasurementType(String value) { mMeasurementType.add(value); }
	public void addMeasurementType(ArrayList<String> value) { mMeasurementType.addAll(value); }
	public void setMeasurementType(String value) { mMeasurementType.clear(); addMeasurementType(value); }
	public void setMeasurementType(ArrayList<String> value) { mMeasurementType.clear(); addMeasurementType(value); }
	public ArrayList<String>  getMeasurementType() { return mMeasurementType; }
	
	public void addPhenomenonType(String value) { mPhenomenonType.add(value); }
	public void addPhenomenonType(ArrayList<String> value) { mPhenomenonType.addAll(value); }
	public void setPhenomenonType(String value) { mPhenomenonType.clear(); addPhenomenonType(value); }
	public void setPhenomenonType(ArrayList<String> value) { mPhenomenonType.clear(); addPhenomenonType(value); }
	public ArrayList<String>  getPhenomenonType() { return mPhenomenonType; }
	
	public void addObservedRegion(String value) { mObservedRegion.add(value); }
	public void addObservedRegion(ArrayList<String> value) { mObservedRegion.addAll(value); }
	public void setObservedRegion(String value) { mObservedRegion.clear(); addObservedRegion(value); }
	public void setObservedRegion(ArrayList<String> value) { if(value.size() > 0) addObservedRegion(value); }
	public ArrayList<String>  getObservedRegion() { return mObservedRegion; }
	
	public void setObservatoryName(String value) { mObservatoryName = value; }
	public void setObservatoryName(ArrayList<String> value) { if(value.size() > 0) mObservatoryName = value.get(0); }
	public String getObservatoryName() { return mObservatoryName; }
	
	public void setObservatoryID(String value) { mObservatoryID = value; }
	public void setObservatoryID(ArrayList<String> value) { if(value.size() > 0) mObservatoryID = value.get(0); }
	public String getObservatoryID() { return mObservatoryID; }
	
	public void addObservatoryGroup(String value) { mObservatoryGroup.add(value); }
	public void addObservatoryGroup(ArrayList<String> value) { mObservatoryGroup.addAll(value); }
	public void setObservatoryGroup(String value) { mObservatoryGroup.clear(); mObservatoryGroup.add(value); }
	public void setObservatoryGroup(ArrayList<String> value) { mObservatoryGroup.clear(); mObservatoryGroup.addAll(value); }
	public ArrayList<String> getObservatoryGroup() { return mObservatoryGroup; }

	public void setObservatoryType(String value) { mObservatoryType = value; }
	public void setObservatoryType(ArrayList<String> value) { if(value.size() > 0) mObservatoryType = value.get(0); }
	public String getObservatoryType() { return mObservatoryType; }
	
	public void setInstrumentID(String value) { mInstrumentID = value; }
	public void setInstrumentID(ArrayList<String> value) { if(value.size() > 0) mInstrumentID = value.get(0); }
	public String getInstrumentID() { return mInstrumentID; }
	
	public void setInstrumentType(String value) { mInstrumentType = value; }
	public void setInstrumentType(ArrayList<String> value) { if(value.size() > 0) mInstrumentType = value.get(0); }
	public String getInstrumentType() { return mInstrumentType; }
	
	public void setInstrumentName(String value) { mInstrumentName = value; }
	public void setInstrumentName(ArrayList<String> value) { if(value.size() > 0) mInstrumentName = value.get(0); }
	public String getInstrumentName() { return mInstrumentName; }
	
	public void setLatitude(String value) { mLatitude = value; }
	public void setLatitude(ArrayList<String> value) { if(value.size() > 0) mLatitude = value.get(0); }
	public String getLatitude() { return mLatitude; }
	
	public void setLongitude(String value) { mLongitude = value; }
	public void setLongitude(ArrayList<String> value) { if(value.size() > 0) mLongitude = value.get(0); }
	public String getLongitude() { return mLongitude; }
	
	public void setDescription(String value) { mDescription = value; }
	public void setDescription(ArrayList<String> value) { if(value.size() > 0) mDescription = value.get(0); }
	public String getDescription() { return mDescription; }
	
	public String getAuthority() { return mAuthority; }
	public void setAuthority(String value) { mAuthority = value; }
	public void setAuthorityFromResourceID() { setAuthorityFromResourceID(mResourceID); }
	public void setAuthorityFromResourceID(String resourceID) 	{ 
		try {
			URI uri = new URI(resourceID);
			mAuthority = uri.getAuthority();
		} catch(Exception e) {
				// Do nothing
		}
	}

	public void addAssociation(String value) { mAssociation.add(value); }
	public void addAssociation(ArrayList<String> value) { mAssociation.addAll(value); }
	public void setAssociation(String value) { mAssociation.clear(); mAssociation.add(value); }
	public void setAssociation(ArrayList<String> value) { mAssociation.clear(); mAssociation.addAll(value); }
	public ArrayList<String> getAssociation() { return mAssociation; }

	public void addWords(String value) { mWords.add(value); }
	public void addWords(ArrayList<String> value) { mWords.addAll(value); }
	public void setWords(String value) { mWords.clear(); mWords.add(value); }
	public void setWords(ArrayList<String> value) { mWords.clear(); mWords.addAll(value); }
	public ArrayList<String> getWords() { return mWords; }

	public void addKeywords(String value) { mKeywords.add(value); }
	public void addKeywords(ArrayList<String> value) { mKeywords.addAll(value); }
	public void setKeywords(String value) { mKeywords.clear(); mKeywords.add(value); }
	public void setKeywords(ArrayList<String> value) { mKeywords.clear(); mKeywords.addAll(value); }
	public ArrayList<String> getKeywords() { return mKeywords; }
}
