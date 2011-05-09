<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <!-- This style sheet creates a table of content for a set of SPASE resources encapsulated by a 'root' element. -->
  <!-- This XSLT should be imported after a versioned SPASE XSLT. -->
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="/">
    <html>
      <head>
        <title>SPASE Resource Description</title>
      </head>
      <body>
        <div>
          <a id="top"/>
        </div>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="Package">
  	 <div class="spase">
    <xsl:call-template name="content"/>
    <!-- Apply resource templates in order you wish to display -->
    <xsl:apply-templates select="sp:Spase/sp:Granule"/>
    <xsl:apply-templates select="sp:Spase/sp:Catalog"/>
    <xsl:apply-templates select="sp:Spase/sp:NumericalData|sp:Spase/sp:DisplayData"/>
    <xsl:apply-templates select="sp:Spase/sp:Instrument"/>
    <xsl:apply-templates select="sp:Spase/sp:Observatory"/>
    <xsl:apply-templates select="sp:Spase/sp:Person"/>
    <xsl:apply-templates select="sp:Spase/sp:Registry|sp:Spase/sp:Repository"/>
    <xsl:apply-templates select="sp:Spase/sp:Service"/>
    <xsl:apply-templates select="sp:Spase/sp:Granule/sp:Extension/vot:VOTABLE"/>
    </div>
  </xsl:template>
  <!-- Templates for creating a Table of Content in the XHTML page -->
  <xsl:template name="content">
    <h1>Data Description</h1>
    <p>This page summarizes information about the selected resource and its origin based on
  <a href="http://www.spase-group.org">SPASE</a> metadata.</p>
    <div id="toc">
      <h2>Table of Contents</h2>
      <ol>
        <xsl:apply-templates select="sp:Spase/sp:Granule" mode="toc"/>
        <xsl:apply-templates select="sp:Spase/sp:Catalog|sp:Spase/sp:DisplayData|sp:Spase/sp:NumericalData" mode="toc"/>
        <xsl:if test="sp:Spase/sp:Repository">
          <li>
             <xsl:choose>
               <xsl:when test="count(sp:Spase/sp:Repository) = 1">
                 <xsl:text>Repository</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:text>Repositories</xsl:text>
               </xsl:otherwise>
             </xsl:choose>
            <ul>
              <xsl:for-each select="sp:Spase/sp:Repository">
                <li>
                  <xsl:call-template name="content_item"/>
                </li>
              </xsl:for-each>
            </ul>
          </li>
        </xsl:if>
        <xsl:if test="sp:Spase/sp:Registry">
          <li>
             <xsl:choose>
               <xsl:when test="count(sp:Spase/sp:Registry) = 1">
                 <xsl:text>Registry</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:text>Registries</xsl:text>
               </xsl:otherwise>
             </xsl:choose>
            <ul>
              <xsl:for-each select="sp:Spase/sp:Registry">
                <li>
                  <xsl:call-template name="content_item"/>
                </li>
              </xsl:for-each>
            </ul>
          </li>
        </xsl:if>
        <xsl:if test="sp:Spase/sp:Instrument">
          <li>
            <xsl:text>Instrument</xsl:text>
            <xsl:if test="count(sp:Spase/sp:Instrument) &gt; 1">
              <xsl:text>s</xsl:text>
            </xsl:if>
            <ul>
              <xsl:for-each select="sp:Spase/sp:Instrument">
                <li>
                  <xsl:call-template name="content_item"/>
                </li>
              </xsl:for-each>
            </ul>
          </li>
        </xsl:if>
        <xsl:if test="sp:Spase/sp:Observatory">
           <li>
             <xsl:choose>
               <xsl:when test="count(sp:Spase/sp:Observatory) = 1">
                 <xsl:text>Observatory</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:text>Observatories</xsl:text>
               </xsl:otherwise>
             </xsl:choose>
             <ul>
               <xsl:for-each select="sp:Spase/sp:Observatory">
                 <li>
                   <xsl:call-template name="content_item"/>
                 </li>
               </xsl:for-each>
             </ul>
           </li>
        </xsl:if>
        <xsl:if test="sp:Spase/sp:Person">
           <li>
             <xsl:text>Person</xsl:text>
             <xsl:if test="count(sp:Spase/sp:Person) &gt; 1">
               <xsl:text>s</xsl:text>
             </xsl:if>
             <ul>
               <xsl:for-each select="sp:Spase/sp:Person">
                 <li>
                   <xsl:call-template name="content_item">
                     <xsl:with-param name="label">
                       <xsl:value-of select="sp:PersonName"/>
                     </xsl:with-param>
                   </xsl:call-template>
                 </li>
               </xsl:for-each>
             </ul>
           </li>
        </xsl:if>
      </ol>
    </div>
  </xsl:template>
  <xsl:template match="sp:Catalog|sp:DisplayData|sp:NumericalData" mode="toc">
    <li>
      <xsl:text>Product</xsl:text>
      <ul>
        <li>
          <xsl:call-template name="content_item"/>
        </li>
      </ul>
    </li>
  </xsl:template>
  <xsl:template match="sp:Granule" mode="toc">
    <li>
      <xsl:call-template name="content_item">
        <xsl:with-param name="label" select="'File (granule)'"/>
      </xsl:call-template>
      <xsl:if test="/root/sp:Spase/sp:Granule/sp:Extension/vot:VOTABLE">
        <ul>
          <li>
            <a href="#vot">Statistical summary</a>
          </li>
        </ul>
      </xsl:if>
    </li>
  </xsl:template>
  <xsl:template name="content_item">
    <xsl:param name="link">
      <xsl:value-of select="sp:ResourceID"/>
    </xsl:param>
    <xsl:param name="label">
      <xsl:value-of select="sp:ResourceHeader/sp:ResourceName"/>
    </xsl:param>
    <a href="{concat('#',$link)}">
      <xsl:value-of select="$label"/>
    </a>
  </xsl:template>
</xsl:stylesheet>
