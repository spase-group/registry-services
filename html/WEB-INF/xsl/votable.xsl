<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vmo vot">
  <xsl:template name="ResourceID">
    <xsl:param name="id" select="''"/>
    <xsl:param name="title" select="'Resource ID'"/>
    <xsl:param name="class" select="''"/>
    <tr class="{$class}">
      <th>
        <xsl:value-of select="$title"/>
      </th>
      <td>
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="concat('#',$id)"/>
          </xsl:attribute>
          <xsl:choose>
            <xsl:when test="count(/sp:Spase/sp:NumericalData[sp:ResourceID=$id]) &gt; 0">
              <xsl:value-of select="/sp:Spase/sp:NumericalData[sp:ResourceID=$id]/sp:ResourceHeader/sp:ResourceName"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$id"/>
            </xsl:otherwise>
          </xsl:choose>
        </a>
        <xsl:text> </xsl:text>
        <a class="xml-logo" href="{concat($spase.webroot, substring-after($id, '://'), '.xml')}">XML</a>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="sp:Granule/sp:Extension/vot:VOTABLE">
    <div class="votable">
      <a id="vot">
        <h1>Summary of Parameter Values in the Data File</h1>
      </a>
      <xsl:apply-templates select="vot:RESOURCE"/>
      <p>
        <xsl:text>VOTABLE version </xsl:text>
        <xsl:value-of select="@version"/>
      </p>
    </div>
  </xsl:template>
  <xsl:template match="vot:RESOURCE">
    <table class="noborder">
      <tbody>
        <xsl:call-template name="ResourceID">
          <xsl:with-param name="id" select="../../../sp:ParentID"/>
          <xsl:with-param name="title" select="'Product'"/>
          <xsl:with-param name="class" select="'noborder'"/>
        </xsl:call-template>
        <xsl:call-template name="ResourceID">
          <xsl:with-param name="id" select="../../../sp:ResourceID"/>
          <xsl:with-param name="title" select="'File/Granule ID'"/>
          <xsl:with-param name="class" select="'noborder'"/>
        </xsl:call-template>
        <xsl:apply-templates select="vot:COOSYS">
          <xsl:with-param name="class" select="'noborder'"/>
        </xsl:apply-templates>
      </tbody>
    </table>
    <br/>
    <xsl:apply-templates select="vot:TABLE"/>
  </xsl:template>
  <xsl:template match="vot:COOSYS">
    <xsl:param name="class" select="''"/>
    <tr class="{$class}">
      <th>
        <xsl:text>Coordinate system</xsl:text>
      </th>
      <td>
        <xsl:value-of select="@ID"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="vot:TABLE">
    <table>
      <thead>
        <tr>
          <xsl:apply-templates select="vot:FIELD"/>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="vot:DATA/vot:TABLEDATA/vot:TR"/>
      </tbody>
    </table>
  </xsl:template>
  <xsl:template match="vot:FIELD">
    <th class="center">
      <xsl:value-of select="@name"/>
      <xsl:apply-templates select="@unit"/>
    </th>
  </xsl:template>
  <xsl:template match="@unit">
    <xsl:value-of select="concat(' [', ., ']')"/>
  </xsl:template>
  <xsl:template match="vot:DATA/vot:TABLEDATA/vot:TR">
    <tr>
      <xsl:apply-templates select="vot:TD"/>
    </tr>
  </xsl:template>
  <xsl:template match="vot:TD">
    <td>
      <xsl:value-of select="."/>
    </td>
  </xsl:template>
</xsl:stylesheet>
