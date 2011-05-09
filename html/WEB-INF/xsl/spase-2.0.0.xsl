<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vxo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:import href="spase-1.3.5.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <!-- Updated hierarchy -->
  <!-- Mixed parameter -->
  <xsl:template match="sp:Mixed[ancestor::sp:Spase/sp:Version ge '1.3.6']">
    <xsl:apply-templates select="sp:MixedQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Qualifier" mode="text"/>
    <xsl:apply-templates select="sp:PaticleType" mode="text"/>
  </xsl:template>
  <!-- Association type enumeration -->
  <xsl:template match="text()[.='ChildEventOf']">
    <xsl:text>Child event of</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ObservedBy']">
    <xsl:text>Observed by</xsl:text>
  </xsl:template>
  <!-- Qualifier enumeration -->
  <xsl:template match="text()[.='Integral.Area']">
    <xsl:text>Area integral</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Integral.Bandwidth']">
    <xsl:text>Integral over the width a frequency band</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Integral.SolidAngle']">
    <xsl:text>Integral over the angle in three-dimensional space</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Field-Aligned']">
    <xsl:text>Field-aligned</xsl:text>
  </xsl:template>
</xsl:stylesheet>
