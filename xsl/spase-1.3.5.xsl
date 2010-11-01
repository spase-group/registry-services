<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vmo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:import href="spase-1.3.4.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <!-- Updated hierarchy -->
  <!-- AccessURL is new in Registry and Repository -->
  <xsl:template match="sp:Registry[ancestor::sp:Spase/sp:Version ge '1.3.5']|sp:Repository[ancestor::sp:Spase/sp:Version ge '1.3.5']">
    <div class="repository">
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
          <xsl:with-param name="title" select="concat(name(),' ID')"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ResourceHeader"/>
      </dl>
      <dt>Access URL</dt>
      <dd>
        <dl>
          <xsl:apply-templates select="sp:AccessURL"/>
        </dl>
      </dd>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- ImageURL is new in Annotation -->
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
        <xsl:apply-templates select="sp:ImageURL"/>
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
  <!-- Parameter hierarchy -->
  <xsl:template match="sp:Parameter[ancestor::sp:Spase/sp:Version ge '1.3.5']">
    <xsl:if test="position() = 1">
      <h2>Parameters</h2>
    </xsl:if>
    <div class="parameter">
      <h3>
        <a id="{concat('par',position())}"/>
        <xsl:text>Parameter #</xsl:text>
        <xsl:value-of select="position()"/>
      </h3>
      <dl>
        <xsl:apply-templates select="sp:Name" mode="text"/>
        <xsl:apply-templates select="sp:Set" mode="text"/>
        <xsl:apply-templates select="sp:ParameterKey" mode="text">
          <xsl:with-param name="title" select="'Parameter key'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Description|sp:Caveats|sp:Cadence"/>
        <xsl:apply-templates select="sp:Units" mode="text"/>
        <xsl:apply-templates select="sp:UnitsConversion" mode="text">
          <xsl:with-param name="title" select="'Conversion to SI units'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:CoordinateSystem"/>
        <xsl:apply-templates select="sp:RenderingHints"/>
        <xsl:apply-templates select="sp:Structure"/>
        <xsl:apply-templates select="sp:ValidMin" mode="text">
          <xsl:with-param name="title" select="'Valid minimum'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ValidMax" mode="text">
          <xsl:with-param name="title" select="'Valid maximum'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:FillValue" mode="text">
          <xsl:with-param name="title" select="'Fill value'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Field"/>
        <xsl:apply-templates select="sp:Particle"/>
        <xsl:apply-templates select="sp:Wave"/>
        <xsl:apply-templates select="sp:Mixed"/>
        <xsl:apply-templates select="sp:Support"/>
      </dl>
    </div>
  </xsl:template>
  <!-- Mixed parameter -->
  <xsl:template match="sp:Mixed[ancestor::sp:Spase/sp:Version ge '1.3.5']">
    <xsl:apply-templates select="sp:MixedQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Qualifier" mode="text"/>
  </xsl:template>
  <!-- New SPASE elements -->
  <!-- Photon parameter -->
  <xsl:template match="sp:Wave">
    <xsl:apply-templates select="sp:WaveType" mode="text">
      <xsl:with-param name="title" select="'Wave type'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:WaveQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Qualifier" mode="text"/>
    <xsl:apply-templates select="sp:EnergyRange">
      <xsl:with-param name="title" select="'Energy range'"/>
      <xsl:with-param name="label" select="'energy'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:WavelengthRange">
      <xsl:with-param name="title" select="'Wavelength range'"/>
      <xsl:with-param name="label" select="'wavelength'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:FrequencyRange">
      <xsl:with-param name="title" select="'Frequency range'"/>
      <xsl:with-param name="label" select="'frequency'"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="sp:ImageURL">
    <xsl:element name="img">
      <xsl:attribute name="src" select="."/>
      <xsl:attribute name="alt" select="ImageURL"/>
      <!-- Height and width is unknown so we cannot use the following, for example:
      <xsl:attribute name="height" value="100"/>
      <xsl:attribute name="width" value="100"/>
      -->
    </xsl:element>
  </xsl:template>
  <!-- New enumerations -->
  <!-- MixedQuantity enumeration -->
  <xsl:template match="text()[.='AlfvenMachNumber']">
    <xsl:text>Alfven Mach number</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='AlfvenVelocity']">
    <xsl:text>Alfven speed</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='MagnetosonicMachNumber']">
    <xsl:text>Magnetosonic Mach number</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PlasmaFrequency-To-GyrofrequencyRatio']">
    <xsl:text>Plasma frequency-to-gyrofrequency ratio</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ThermalPressure']">
    <xsl:text>Thermal pressure</xsl:text>
  </xsl:template>
  <!-- WaveType enumeration -->
  <xsl:template match="text()[.='PlasmaWaves']">
    <xsl:text>Plasma waves</xsl:text>
  </xsl:template>
  <!-- WaveQuantity enumeration -->
  <xsl:template match="text()[.='AC-ElectricField']">
    <xsl:text>AC electric field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='AC-MagneticField']">
    <xsl:text>AC magnetic field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='DopplerFrequency']">
    <xsl:text>Doppler frequency</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PropagationTime']">
    <xsl:text>Propagation time</xsl:text>
  </xsl:template>
  <!-- SpectralRange enumeration -->
  <xsl:template match="text()[.='FarUltraviolet']">
    <xsl:text>Far ultraviolet</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='HE-304']">
    <xsl:text>HE-304</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='LBHBand']">
    <xsl:text>LBH band</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='SoftX-Rays']">
    <xsl:text>Soft X-rays</xsl:text>
  </xsl:template>
</xsl:stylesheet>
