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
        <style>
           /* http://meyerweb.com/eric/tools/css/reset/ */
            /* v1.0 | 20080212 */
            
            html, body, div, span, applet, object, iframe,
            h1, h2, h3, h4, h5, h6, p, blockquote, pre,
            a, abbr, acronym, address, big, cite, code,
            del, dfn, em, font, img, ins, kbd, q, s, samp,
            small, strike, strong, sub, sup, tt, var,
            b, u, i, center,
            dl, dt, dd, ol, ul, li,
            fieldset, form, label, legend,
            table, caption, tbody, tfoot, thead, tr, th, td {
               margin: 0;
               padding: 0;
               border: 0;
               outline: 0;
               font-size: 100%;
               vertical-align: baseline;
               background: transparent;
            }
            body {
               line-height: 1;
            }
            ol, ul {
               list-style: none;
            }
            blockquote, q {
               quotes: none;
            }
            blockquote:before, blockquote:after,
            q:before, q:after {
               content: '';
               content: none;
            }
            
            /* remember to define focus styles! */
            :focus {
               outline: 0;
            }
            
            /* remember to highlight inserts somehow! */
            ins {
               text-decoration: none;
            }
            del {
               text-decoration: line-through;
            }
            
            /* tables still need 'cellspacing="0"' in the markup */
            table {
               border-collapse: collapse;
               border-spacing: 0;
            }
            
            /* SPASE CSS */
            :focus {
                outline-color: invert;
                outline-style: dotted;
                outline-width: thin;
            }
            
            body {
                background-color: #e9f0f5;/*#277bc0;/*#d3d3f9; */
                color: black;
                font-family: Verdana, Arial, sans-serif; 
                font-size:10px; 
                line-height: 1.2;
                padding: 10px 3% 10px 3%;
            }
             
            h1,h2,h3,h4,h5,h6 {
                margin-top: 10px;
                margin-bottom: 10px;
                margin-left: 2ex;
                margin-right: 2ex;
                font-weight:bold;
            }
            
            h1 {
                font-size: 140%;
            }
            
            h2 {
                font-size: 120%;
            }
            
            h3 {
                font-size: 110%;
                font-style: oblique;
            }
            
            p {
                margin-bottom: 0.75ex;
            }
            
            dt {
                margin-top: 5px;
                padding-left: 5px;
                border-top: 1px solid #DDD;
                font-weight: bold;
            }
            
            dd {
                margin-left: 5ex;
            }
            
            dd:hover {
                background-color: white;
            }
            
            a:link,
            a:visited {
               color: #277bc0;/* #339;*/
               font-weight:bolder; 
               text-decoration:none; 
            }
            
            a:hover {
               color: blue;
               font-weight:bolder; 
               text-decoration:underline; 
            }
            
            .toparrow {
                position: relative;
                float: right;
                bottom: 2.5ex;
                right: 0.5ex;
            }
            
            ul {
                list-style: square inside ;
                margin-bottom: 0.75ex;
            }
            
            table {
               border: thin solid #666;
                margin-top: 5px;
                margin-bottom: 10px;
            }
            
            thead,tbody {
               border: thin solid #666;
            }
            
            td, th {
               margin: 0;
                padding: 2px 2px 2px 2px;
            }
            
            th {
                font-style: oblique;
            }
            
            div.product, div.instrument, div.observatory, div.repository, div.person {
                background-color: white;
                border: thin solid #333;
                padding: 10px 15px 10px 15px;
                margin-top: 10px;
            }
            
            div.parameter {
                margin-top: 10px;
                padding: 5px 10px 10px 10px;
                border: thin dotted #333;
                background-color: #ebebeb;
            }
            
            #granule {
                background-color: white;
                border: thin solid #333;
                padding: 10px 15px 10px 15px;
                margin-top: 10px;
            }
            
            #granule dt {
                border-top-style: none;
            }
            
            #toc {
               border: thin solid #333;
               background-color: #F5F5F5; 
               padding: 10px 15px 10px 15px;
               margin-left: 5%;
                margin-right: 5%;
               margin-bottom: 30px;
            }
            
            #toc ol, ul, li {
                padding-left: 5ex;
            }
            
            #toc ol {
                list-style-type: decimal;
                list-style-position: inside; 
            }
            
            #toc ul {
                list-style-type: square;
                list-style-position: inside; 
            }
            
            p.version {
              float: right;
              width: 100%;
              margin-top: 5px;
              text-align: right;
              font-size: x-small;
            }
            a.xml-logo:link,
            a.xml-logo:visited {
               background: #ff6600;
               color: #ffffff;
               font-weight:bolder; 
               text-decoration:none; 
               padding-left:2px;
               padding-right:2px;
            }
            a.xml-logo:hover {
               text-decoration:underline; 
            }
      </style>
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
