<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vmo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:import href="spase-1.3.1.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="sp:RenderingHints[ancestor::sp:Spase/sp:Version ge '1.3.2']">
    <dt>Rendering hints</dt>
    <dd>
      <dl>
        <xsl:apply-templates select="sp:DisplayType" mode="text">
          <xsl:with-param name="title" select="'Display type'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:AxisLabel" mode="text">
          <xsl:with-param name="title" select="'Axis label'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ValueFormat" mode="text">
          <xsl:with-param name="title" select="'Format'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ScaleMin" mode="text">
          <xsl:with-param name="title" select="'Scale minimum'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ScaleMax" mode="text">
          <xsl:with-param name="title" select="'Scale maximum'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ScaleType" mode="text">
          <xsl:with-param name="title" select="'Scale type'"/>
        </xsl:apply-templates>
      </dl>
    </dd>
  </xsl:template>
</xsl:stylesheet>
