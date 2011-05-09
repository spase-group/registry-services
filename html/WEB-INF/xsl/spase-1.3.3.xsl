<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vxo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:import href="spase-1.3.2.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <!-- Resource Header hierarchy -->
  <xsl:template match="sp:ResourceHeader[ancestor::sp:Version ge '1.3.3']">
    <xsl:apply-templates select="sp:ResourceName" mode="text">
      <xsl:with-param name="title" select="'Name'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:AlternateName" mode="text">
      <xsl:with-param name="title" select="'Alternate name'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Description"/>
    <xsl:if test="count(sp:InformationURL) &gt; 0">
      <dt>Additional information</dt>
      <dd>
        <dl>
          <xsl:apply-templates select="sp:InformationURL"/>
        </dl>
      </dd>
    </xsl:if>
    <xsl:apply-templates select="sp:Acknowledgement"/>
    <dt>Contact</dt>
    <dd>
      <table class="nested" cellspacing="0">
        <tbody>
          <tr>
            <th>
              <xsl:text> </xsl:text>
            </th>
            <th class="center">Role</th>
            <th class="center">Person</th>
          </tr>
          <xsl:apply-templates select="sp:Contact"/>
        </tbody>
      </table>
    </dd>
    <xsl:apply-templates select="sp:ReleaseDate|sp:ExpirationDate"/>
    <xsl:apply-templates select="sp:Association"/>
    <xsl:if test="count(sp:PriorID) &gt; 0">
      <dt>
        <xsl:text>Prior ID</xsl:text>
        <xsl:if test="count(sp:PriorID) &gt; 1">
          <xsl:text>s</xsl:text>
        </xsl:if>
      </dt>
      <xsl:apply-templates select="sp:PriorID" mode="no_title"/>
    </xsl:if>
  </xsl:template>
  <!-- Association hierarchy -->
  <xsl:template match="sp:Association">
    <dt>Association</dt>
    <dd>
      <dl>
        <dd>
          <xsl:apply-templates select="sp:AssociationType" mode="enum"/>
          <xsl:text> </xsl:text>
          <a href="{concat($spase.render, '?id=', sp:AssociationID)}">
            <xsl:value-of select="sp:AssociationID"/>
          </a>
          <xsl:text> </xsl:text>
          <a class="xml-logo" href="{concat($spase.resolver, 'id=', sp:AssociationID)}">XML</a>
        </dd>
        <xsl:apply-templates select="sp:Note"/>
      </dl>
    </dd>
  </xsl:template>
  <!-- Association type enumeration -->
  <xsl:template match="text()[.='DerivedFrom']">
    <xsl:text>Derived from</xsl:text>
  </xsl:template>
  <xsl:template match="text()[parent::sp:AssociationType.='Other']">
    <xsl:text>Other association with</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PartOf']">
    <xsl:text>Part of</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='RevisionOf']">
    <xsl:text>Revision of</xsl:text>
  </xsl:template>
</xsl:stylesheet>
