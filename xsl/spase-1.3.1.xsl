<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vmo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:import href="spase-1.3.0.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <!-- Access information tree -->
  <xsl:template match="sp:AccessInformation[ancestor::sp:Spase/sp:Version ge '1.3.1']">
    <xsl:choose>
      <xsl:when test="count(../sp:AccessInformation) = 1">
        <dt>Repository</dt>
      </xsl:when>
      <xsl:otherwise>
        <dt>
          <xsl:text>Repository #</xsl:text>
          <xsl:value-of select="position()"/>
        </dt>
      </xsl:otherwise>
    </xsl:choose>
    <dd>
      <dl>
        <xsl:apply-templates select="sp:RepositoryID">
          <xsl:with-param name="title" select="'Name'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Availability" mode="text"/>
        <xsl:apply-templates select="sp:AccessRights" mode="text">
          <xsl:with-param name="title" select="'Access rights'"/>
        </xsl:apply-templates>
        <dt>URL</dt>
        <dd>
          <dl>
            <xsl:apply-templates select="sp:AccessURL"/>
          </dl>
        </dd>
        <xsl:apply-templates select="sp:Format" mode="text"/>
        <xsl:apply-templates select="sp:Encoding" mode="text"/>
        <xsl:apply-templates select="sp:DataExtent"/>
        <xsl:apply-templates select="sp:Acknowledgement"/>
      </dl>
    </dd>
  </xsl:template>
  <!-- Product resources (Catalog, Display, NumericalData) -->
  <xsl:template match="sp:NumericalData[../sp:Version ge '1.3.1']|sp:DisplayData[../sp:Version ge '1.3.1']|sp:Catalog[../sp:Version ge '1.3.1']">
    <xsl:variable name="resource_name">
      <xsl:choose>
        <xsl:when test="name() = 'NumericalData'">
          <xsl:text>Numerical Data</xsl:text>
        </xsl:when>
        <xsl:when test="name() = 'DisplayData'">
          <xsl:text>Display Data</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="name()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div class="product">
      <xsl:apply-templates select="../sp:Version"/>
      <h1>
        <a>
          <xsl:attribute name="id">
            <xsl:value-of select="sp:ResourceID"/>
          </xsl:attribute>
        </a>
        <xsl:value-of select="concat($resource_name, ' Product: ', sp:ResourceHeader/sp:ResourceName)"/>
      </h1>
      <dl>
        <xsl:apply-templates select="sp:ResourceID"/>
        <xsl:apply-templates select="sp:ResourceHeader"/>
        <xsl:apply-templates select="sp:AccessInformation"/>
        <xsl:apply-templates select="sp:ProcessingLevel" mode="text">
          <xsl:with-param name="title" select="'Processing level'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ProviderProcessingLevel" mode="text">
          <xsl:with-param name="title" select="'Provider processing level'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ProviderResourceName" mode="text">
          <xsl:with-param name="title" select="'Provider resource name'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ProviderVersion" mode="text">
          <xsl:with-param name="title" select="'Provider version'"/>
        </xsl:apply-templates>
        <xsl:if test="sp:InstrumentID">
          <dt>
            <xsl:text>Instrument</xsl:text>
            <xsl:if test="count(sp:InstrumentID) &gt; 1">
              <xsl:text>s</xsl:text>
            </xsl:if>
          </dt>
          <xsl:apply-templates select="sp:InstrumentID" mode="no_title"/>
        </xsl:if>
        <xsl:apply-templates select="sp:MeasurementType" mode="text">
          <xsl:with-param name="title" select="'Measurement type'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:PhenomenonType" mode="text">
          <xsl:with-param name="title" select="'Phenomenon type'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:TimeSpan"/>
        <xsl:apply-templates select="sp:TemporalDescription"/>
        <xsl:apply-templates select="sp:SpectralRange" mode="text">
          <xsl:with-param name="title" select="'Spectral range'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:DisplayCadence">
          <xsl:with-param name="title" select="'Display cadence'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ObservedRegion" mode="text">
          <xsl:with-param name="title" select="'Observed regions'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Caveats"/>
        <xsl:apply-templates select="sp:Keyword" mode="text">
          <xsl:with-param name="title" select="'Keywords'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:InputResourceID" mode="text">
          <xsl:with-param name="title" select="'Input resource ID'"/>
        </xsl:apply-templates>
      </dl>
      <xsl:call-template name="top-arrow"/>
      <xsl:apply-templates select="sp:Parameter"/>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Parameter hierarchy -->
  <xsl:template match="sp:Parameter">
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
        <xsl:apply-templates select="sp:Photon"/>
        <xsl:apply-templates select="sp:Mixed"/>
        <xsl:apply-templates select="sp:Support"/>
      </dl>
    </div>
  </xsl:template>
  <xsl:template match="sp:RenderingHints">
    <dt>Rendering hints</dt>
    <dd>
      <dl>
        <xsl:apply-templates select="sp:DisplayType" mode="text">
          <xsl:with-param name="title" select="'Display type'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:AxisLabel" mode="text">
          <xsl:with-param name="title" select="'Axis label'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Format" mode="text"/>
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
  <!-- Field parameter -->
  <xsl:template match="sp:Field[ancestor::sp:Spase/sp:Version ge '1.3.1']">
    <xsl:apply-templates select="sp:FieldQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Qualifier" mode="text"/>
    <xsl:apply-templates select="sp:FrequencyRange">
      <xsl:with-param name="title" select="'Frequency range'"/>
      <xsl:with-param name="label" select="'frequency'"/>
    </xsl:apply-templates>
  </xsl:template>
  <!-- Particle parameter -->
  <xsl:template match="sp:Particle[ancestor::sp:Spase/sp:Version ge '1.3.1']">
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
  <!-- Photon parameter -->
  <xsl:template match="sp:Photon[ancestor::sp:Spase/sp:Version ge '1.3.1']">
    <xsl:apply-templates select="sp:PhotonQuantity" mode="text">
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
  <!-- Support parameter -->
  <xsl:template match="sp:Support">
    <xsl:apply-templates select="sp:SupportQuantity" mode="text">
      <xsl:with-param name="title" select="'Parameter type'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Qualifier" mode="text"/>
  </xsl:template>
  <!-- Granule resource -->
  <xsl:template match="sp:Granule[../sp:Version ge '1.3.1']">
    <xsl:variable name="granule_id" select="sp:ResourceID"/>
    <xsl:variable name="hash_value" select="sp:Checksum/sp:HashValue"/>
    <xsl:variable name="bgstyle">
      <xsl:value-of select="'background-image: url('"/>
      <xsl:value-of select="$vmo:spase_web_root"/>
      <xsl:call-template name="substring-before-last">
        <xsl:with-param name="string" select="substring-after($granule_id, '://')"/>
        <xsl:with-param name="char" select="'/'"/>
      </xsl:call-template>
      <xsl:value-of select="'/'"/>
      <xsl:value-of select="$hash_value"/>
      <xsl:value-of select="'.png); background-repeat: no-repeat; background-position: right bottom; min-height: 300px;'"/>
    </xsl:variable>
    <div style="{$bgstyle}" id="granule">
      <xsl:apply-templates select="../sp:Version"/>
      <h1><a id="{$granule_id}"/>File Information (Granule)
      </h1>
      <dl>
        <xsl:apply-templates select="sp:ResourceID">
          <xsl:with-param name="title" select="'Granule ID'"/>
        </xsl:apply-templates>
        <xsl:if test="count(sp:PriorID) &gt; 0">
          <dt>
            <xsl:text>Prior ID</xsl:text>
            <xsl:if test="count(sp:PriorID) &gt; 1">
              <xsl:text>s</xsl:text>
            </xsl:if>
          </dt>
          <xsl:apply-templates select="sp:PriorID" mode="no_title"/>
        </xsl:if>
        <xsl:apply-templates select="sp:ParentID">
          <xsl:with-param name="title" select="'Product name'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ReleaseDate|sp:ExpirationDate|sp:StartDate|sp:StopDate"/>
        <xsl:apply-templates select="sp:Source"/>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Source tree -->
  <xsl:template match="sp:Source">
    <dt>Source</dt>
    <dd>
      <dl>
        <xsl:apply-templates select="sp:SourceType" mode="text">
          <xsl:with-param name="title" select="'Type'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:URL"/>
        <dt>Mirror URL</dt>
        <xsl:apply-templates select="sp:MirrorURL" mode="no_title"/>
        <xsl:apply-templates select="sp:Checksum"/>
        <xsl:apply-templates select="sp:DataExtent"/>
      </dl>
    </dd>
  </xsl:template>
  <xsl:template match="sp:MirrorURL" mode="no_title">
    <dd>
      <a href="{.}">
        <xsl:call-template name="url_split">
          <xsl:with-param name="file" select="."/>
        </xsl:call-template>
      </a>
    </dd>
  </xsl:template>
  <xsl:template match="text()[.='CountRate']">
    <xsl:text>Count rate</xsl:text>
  </xsl:template>
</xsl:stylesheet>
