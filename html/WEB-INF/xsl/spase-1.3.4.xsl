<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vxo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:import href="spase-1.3.3.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <!-- Annotation resource is new in 1.3.4 -->
  <xsl:template match="sp:Annotation">
    <div class="annotation">
      <xsl:apply-templates select="../sp:Version"/>
      <h1>
        <a>
          <xsl:attribute name="id">
            <xsl:value-of select="sp:ResourceID"/>
          </xsl:attribute>
        </a>
        <xsl:value-of select="concat(name(),': ', sp:ResourceHeader/sp:ResourceName)"/>
      </h1>
      <dl>
        <xsl:apply-templates select="sp:ResourceID">
          <xsl:with-param name="title" select="'Annotation ID'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ResourceHeader"/>
        <xsl:apply-templates select="sp:AnnotationType" mode="text">
          <xsl:with-param name="title" select="'Annotation type'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:PhenomenonType" mode="text">
          <xsl:with-param name="title" select="'Phenomenon type'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ConfidenceRating" mode="text">
          <xsl:with-param name="title" select="'Confidence rating'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:TimeSpan"/>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Particle parameter -->
  <xsl:template match="sp:Particle[ancestor::sp:Version ge '1.3.4']">
    <xsl:apply-templates select="sp:ParticleType" mode="text">
      <xsl:with-param name="title" select="'Particle type'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:ParticleQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Qualifier" mode="text"/>
    <xsl:apply-templates select="sp:AtomicNumber" mode="text"/>
    <xsl:apply-templates select="sp:EnergyRange">
      <xsl:with-param name="title" select="'Energy range'"/>
      <xsl:with-param name="label" select="'energy'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:AzimuthalAngleRange">
      <xsl:with-param name="title" select="'Azimuthal angle range'"/>
      <xsl:with-param name="label" select="'angle'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:PolarAngleRange">
      <xsl:with-param name="title" select="'Polar angle range'"/>
      <xsl:with-param name="label" select="'angle'"/>
    </xsl:apply-templates>
  </xsl:template>
  <!-- Measurement Type enumeration -->
  <xsl:template match="text()[.='Waves.Active']">
    <xsl:text>Active waves</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Waves.Passive']">
    <xsl:text>Passive waves</xsl:text>
  </xsl:template>
  <!-- ScaleType enumeration -->
  <!-- Measurement Type enumeration -->
  <xsl:template match="text()[.='LinearScale']">
    <xsl:text>Linear scale</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='LogScale']">
    <xsl:text>Log scale</xsl:text>
  </xsl:template>
  <!-- Qualifier enumeration -->
  <xsl:template match="text()[.='Component.I']">
    <xsl:text>Component I</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Component.J']">
    <xsl:text>Component J</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Component.K']">
    <xsl:text>Component K</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='DirectionAngle']">
    <xsl:text>Direction angle</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='DirectionAngle.AzimuthAngle']">
    <xsl:text>Azimuth angle</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='DirectionAngle.PolarAngle']">
    <xsl:text>Polar angle</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='DirectionAngle.ElevationAngle']">
    <xsl:text>Elevation angle</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Projection.IJ']">
    <xsl:text>Projection IJ</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Projection.IK']">
    <xsl:text>Projection IK</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Projection.JK']">
    <xsl:text>Projection JK</xsl:text>
  </xsl:template>
  <!-- Other changes -->
  <xsl:template match="text()[.='IonChamber']">
    <xsl:text>Ion chamber</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='LineOfSight']">
    <xsl:text>Line of sight</xsl:text>
  </xsl:template>
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
  <xsl:template match="text()[parent::sp:AssociationType/.='Other']">
    <xsl:text>Other association with</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PartOf']">
    <xsl:text>Part of</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='RevisionOf']">
    <xsl:text>Revision of</xsl:text>
  </xsl:template>
</xsl:stylesheet>
