<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:import href="vxo-vars.xsl"/>
  <!--  <xsl:import href="vmo-functions.xsl" use-when="system-property('xsl:version')='2.0'" /> -->
  <xsl:import href="common.xsl"/>
  <xsl:import href="votable.xsl"/>
  <xsl:output doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd" doctype-public="-//W3C//DTD XHTML 1.1//EN" method="xhtml" indent="yes" omit-xml-declaration="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:key name="name-by-id" match="sp:ResourceName" use="../../sp:ResourceID"/>
  <xsl:key name="person-name-by-id" match="sp:PersonName" use="../sp:ResourceID"/>

  <xsl:template match="sp:Spase">
  	 <div class="spase">
    <xsl:apply-templates select="sp:Granule"/>
    <xsl:apply-templates select="sp:Catalog"/>
    <xsl:apply-templates select="sp:DisplayData"/>
    <xsl:apply-templates select="sp:NumericalData"/>
    <xsl:apply-templates select="sp:Instrument"/>
    <xsl:apply-templates select="sp:Observatory"/>
    <xsl:apply-templates select="sp:Person"/>
    <xsl:apply-templates select="sp:Registry"/>
    <xsl:apply-templates select="sp:Repository"/>
    <xsl:apply-templates select="sp:Service"/>
    <xsl:apply-templates select="sp:Granule/sp:Extension/vot:VOTABLE"/>
    </div>
  </xsl:template>
  <!-- Granule resource -->
  <xsl:template match="sp:Granule">
    <xsl:variable name="granule_id" select="sp:ResourceID"/>
    <xsl:variable name="hash_value" select="sp:Checksum/sp:HashValue"/>
    <xsl:variable name="bgstyle">
      <xsl:value-of select="'background-image: url('"/>
      <xsl:value-of select="$spase.webroot"/>
      <xsl:call-template name="substring-before-last">
        <xsl:with-param name="string" select="substring-after($granule_id, '://')"/>
        <xsl:with-param name="char" select="'/'"/>
      </xsl:call-template>
      <xsl:value-of select="'/'"/>
      <xsl:value-of select="$hash_value"/>
      <xsl:value-of select="'.png); background-repeat: no-repeat; background-position: right bottom; min-height: 300px;'"/>
    </xsl:variable>
    <div class="granule" style="{$bgstyle}">
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
        <dt>Data download</dt>
        <xsl:apply-templates select="sp:URL" mode="no_title"/>
        <xsl:apply-templates select="sp:Checksum"/>
        <xsl:apply-templates select="sp:DataExtent"/>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Product resources (Catalog, Display, NumericalData) -->
  <xsl:template match="sp:NumericalData|sp:DisplayData|sp:Catalog">
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
      <xsl:apply-templates select="sp:PhysicalParameter"/>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Person resource -->
  <xsl:template match="sp:Person">
    <div class="person">
      <xsl:apply-templates select="../sp:Version"/>
      <h1>
        <a>
          <xsl:attribute name="id">
            <xsl:value-of select="sp:ResourceID"/>
          </xsl:attribute>
        </a>
        <xsl:value-of select="concat('Person: ', sp:PersonName)"/>
      </h1>
      <dl>
        <xsl:apply-templates select="sp:PersonName" mode="text">
          <xsl:with-param name="title" select="'Name'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:OrganizationName" mode="text">
          <xsl:with-param name="title" select="'Organization'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Address" mode="text"/>
        <xsl:apply-templates select="sp:Email" mode="text">
          <xsl:with-param name="title" select="'Email'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:PhoneNumber" mode="text">
          <xsl:with-param name="title" select="'Phone'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Extension" mode="text"/>
        <xsl:apply-templates select="sp:FaxNumber" mode="text">
          <xsl:with-param name="title" select="'Fax number'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ResourceID">
          <xsl:with-param name="title" select="'Person ID'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ReleaseDate"/>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Instrument resource -->
  <xsl:template match="sp:Instrument">
    <div class="instrument">
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
          <xsl:with-param name="title" select="'Instrument ID'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ResourceHeader"/>
        <xsl:apply-templates select="sp:InstrumentType" mode="text">
          <xsl:with-param name="title" select="'Instrument type'"/>
          <xsl:sort/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:InvestigationName" mode="text">
          <xsl:with-param name="title" select="'Investigation name'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ObservatoryID">
          <xsl:with-param name="title" select="'Observatory'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Caveats"/>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Observatory resource -->
  <xsl:template match="sp:Observatory">
    <div class="observatory">
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
          <xsl:with-param name="title" select="'Observatory ID'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:ResourceHeader"/>
        <xsl:apply-templates select="sp:ObservatoryGroup" mode="text">
          <xsl:with-param name="title" select="'Observatory group'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Location"/>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Registry, Repository resource -->
  <xsl:template match="sp:Registry|sp:Repository">
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
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Service resource -->
  <xsl:template match="sp:Service">
    <div class="service">
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
        <dt>Access URL</dt>
        <dd>
          <dl>
            <xsl:apply-templates select="sp:AccessURL"/>
          </dl>
        </dd>
      </dl>
      <xsl:call-template name="top-arrow"/>
    </div>
  </xsl:template>
  <!-- Physical Parameter hierarchy -->
  <xsl:template match="sp:PhysicalParameter">
    <xsl:if test="position() = 1">
      <h2>Parameters</h2>
    </xsl:if>
    <div class="parameter">
      <h3>
        <xsl:text>Parameter #</xsl:text>
        <xsl:value-of select="position()"/>
      </h3>
      <dl>
        <xsl:apply-templates select="sp:Name" mode="text"/>
        <xsl:apply-templates select="sp:ParameterKey" mode="text">
          <xsl:with-param name="title" select="'Parameter key'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Description|sp:Caveats|sp:Cadence"/>
        <xsl:apply-templates select="sp:Units" mode="text"/>
        <xsl:apply-templates select="sp:UnitsConversion" mode="text">
          <xsl:with-param name="title" select="'Conversion to SI units'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:CoordinateSystem"/>
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
        <xsl:apply-templates select="sp:Measured|sp:Support"/>
      </dl>
    </div>
  </xsl:template>
  <xsl:template match="sp:Measured">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="sp:Support">
    <xsl:apply-templates select="sp:SupportQuantity" mode="text">
      <xsl:with-param name="title" select="'Parameter type'"/>
    </xsl:apply-templates>
  </xsl:template>
  <!-- Resource Header hierarchy -->
  <xsl:template match="sp:ResourceHeader">
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
    <xsl:if test="count(sp:AssociationID) &gt; 0">
      <dt>
        <xsl:text>Associated resource</xsl:text>
        <xsl:if test="count(sp:AssociationID) &gt; 1">
          <xsl:text>s</xsl:text>
        </xsl:if>
      </dt>
      <xsl:apply-templates select="sp:AssociationID" mode="no_title"/>
    </xsl:if>
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
  <!-- Access information tree -->
  <xsl:template match="sp:AccessInformation">
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
        <xsl:apply-templates select="sp:Encoding" mode="text"/>
        <xsl:apply-templates select="sp:DataExtent"/>
        <xsl:apply-templates select="sp:Acknowledgement"/>
      </dl>
    </dd>
  </xsl:template>
  <xsl:template match="sp:URL">
    <dt>Download URL</dt>
    <dd>
      <a href="{.}">
        <xsl:call-template name="url_split">
          <xsl:with-param name="file" select="."/>
        </xsl:call-template>
      </a>
    </dd>
  </xsl:template>
  <xsl:template match="sp:URL" mode="no_title">
    <dd>
      <xsl:if test="count(../sp:URL) &gt; 1">
        <xsl:value-of select="position()"/>
        <xsl:text>. </xsl:text>
      </xsl:if>
      <a href="{.}">
        <xsl:call-template name="url_split">
          <xsl:with-param name="file" select="."/>
        </xsl:call-template>
      </a>
    </dd>
  </xsl:template>
  <xsl:template name="url_split">
    <xsl:param name="file" select="."/>
    <xsl:choose>
      <xsl:when test="contains($file, '/')">
        <xsl:call-template name="url_split">
          <xsl:with-param name="file" select="substring-after($file, '/')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$file"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="spase_id">
    <dd>
      <a href="{concat($spase.render, '?id=', .)}">
        <xsl:value-of select="."/>
      </a>
      <xsl:text> </xsl:text>
      <a class="xml-logo" href="{concat($spase.resolver, '?id=', .)}">XML</a>
    </dd>
  </xsl:template>
  <xsl:template name="spase_id_xml_only">
    <dd>
      <xsl:value-of select="."/>
      <xsl:text> </xsl:text>
      <a class="xml-logo" href="{concat($spase.resolver, '?id=', .)}">XML</a>
    </dd>
  </xsl:template>
  <xsl:template name="link-name-id">
    <xsl:param name="type" select="''"/>
    <xsl:variable name="nkeys">
      <xsl:choose>
        <xsl:when test="$type='person'">
          <xsl:value-of select="count(key('person-name-by-id',.))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="count(key('name-by-id',.))"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$nkeys = 0">
        <a href="{concat($spase.render, '?id=', .)}"><xsl:value-of select="."/></a>
      </xsl:when>
      <xsl:otherwise>
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="concat('#',.)"/>
          </xsl:attribute>
          <xsl:choose>
            <xsl:when test="$type='person'">
              <xsl:value-of select="key('person-name-by-id',.)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="key('name-by-id',.)"/>
            </xsl:otherwise>
          </xsl:choose>
        </a>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <a class="xml-logo" href="{concat($spase.resolver, '?id=', .)}">XML</a>
  </xsl:template>
  <xsl:template match="sp:ParentID|sp:InstrumentID|sp:RepositoryID|sp:ObservatoryID">
    <xsl:param name="title" select="'Resource ID'"/>
    <dt>
      <xsl:value-of select="$title"/>
    </dt>
    <dd>
      <xsl:call-template name="link-name-id"/>
    </dd>
  </xsl:template>
  <xsl:template match="sp:ParentID|sp:InstrumentID|sp:RepositoryID|sp:ObservatoryID" mode="no_title">
    <dd>
      <xsl:call-template name="link-name-id"/>
    </dd>
  </xsl:template>
  <xsl:template match="sp:AssociationID|sp:InputResourceID">
    <xsl:param name="title" select="local-name()"/>
    <dt>
      <xsl:value-of select="$title"/>
    </dt>
    <xsl:call-template name="spase_id"/>
  </xsl:template>
  <xsl:template match="sp:AssociationID|sp:InputResourceID" mode="no_title">
    <xsl:call-template name="spase_id"/>
  </xsl:template>
  <xsl:template match="sp:ResourceID">
    <xsl:param name="title" select="'Resource ID'"/>
    <dt>
      <xsl:value-of select="$title"/>
    </dt>
    <xsl:choose>
      <xsl:when test="name() = 'ResourceID'">
        <xsl:call-template name="spase_id_xml_only"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="spase_id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="sp:StartDate|sp:StopDate|sp:ReleaseDate|sp:ExpirationDate|sp:EndDate">
    <dt>
      <xsl:value-of select="concat(substring-before(name(),'Date'),' date')"/>
    </dt>
    <dd>
      <xsl:choose>
        <xsl:when test="function-available('format-dateTime')">
          <xsl:value-of select="format-dateTime(., '[Y1]-[M1,2]-[D1,2] [H1,2]:[m1,2]:[s1,2]')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat(substring(.,1,10), ' ', substring(.,12,8))"/>
        </xsl:otherwise>
      </xsl:choose>
    </dd>
  </xsl:template>
  <xsl:template match="sp:RelativeEndDate|sp:RelativeStopDate|sp:Cadence|sp:Exposure|sp:DisplayCadence">
    <xsl:param name="title" select="local-name()"/>
    <dt>
      <xsl:value-of select="$title"/>
    </dt>
    <dd>
      <xsl:call-template name="format-duration"/>
    </dd>
  </xsl:template>
  <xsl:template match="sp:Per">
    <xsl:call-template name="format-duration"/>
  </xsl:template>
  <xsl:template match="sp:DataExtent">
    <dt>File size</dt>
    <dd>
      <xsl:choose>
        <xsl:when test="count(sp:Bytes) = 1 or count(sp:Quantity) = 1">
          <xsl:value-of select="sp:Bytes|sp:Quantity"/>
        </xsl:when>
        <xsl:otherwise>N/A</xsl:otherwise>
      </xsl:choose>
      <xsl:text> </xsl:text>
      <xsl:value-of select="sp:Units"/>
      <xsl:if test="sp:Per">
        <xsl:text> per </xsl:text>
        <xsl:apply-templates select="sp:Per"/>
      </xsl:if>
    </dd>
  </xsl:template>
  <xsl:template match="sp:Checksum">
    <dt>
      <xsl:value-of select="sp:HashFunction"/>
      <xsl:text> checksum</xsl:text>
    </dt>
    <dd>
      <xsl:value-of select="sp:HashValue"/>
    </dd>
  </xsl:template>
  <xsl:template match="sp:Version">
    <p class="version">
      <xsl:text>SPASE version </xsl:text>
      <xsl:value-of select="."/>
    </p>
  </xsl:template>
  <xsl:template match="sp:TemporalDescription">
    <dt>Temporal description</dt>
    <dd>
      <dl>
        <xsl:apply-templates select="*"/>
      </dl>
    </dd>
  </xsl:template>
  <xsl:template match="sp:TimeSpan">
    <xsl:apply-templates select="sp:StartDate"/>
    <!-- (Relative)EndDate were renamed in SPASE 1.3.0 -->
    <xsl:apply-templates select="sp:EndDate"/>
    <xsl:apply-templates select="sp:RelativeEndDate">
      <xsl:with-param name="title" select="'Relative end date'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:StopDate"/>
    <xsl:apply-templates select="sp:RelativeStopDate">
      <xsl:with-param name="title" select="'Relative stop date'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Note"/>
  </xsl:template>
  <xsl:template match="sp:Description|sp:Acknowledgement|sp:Caveats|sp:Note">
    <xsl:param name="print_title" select="true()"/>
    <xsl:choose>
      <xsl:when test="$print_title">
        <dt>
          <xsl:value-of select="local-name()"/>
        </dt>
        <dd>
          <!--   <xsl:apply-templates mode="copy"/> -->
          <xsl:apply-templates mode="spase-normalization"/>
        </dd>
      </xsl:when>
      <xsl:otherwise>
        <dd>
          <xsl:apply-templates mode="spase-normalization"/>
        </dd>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="sp:Contact">
    <tr>
      <td>
        <xsl:value-of select="position()"/>
        <xsl:text>. </xsl:text>
      </td>
      <td>
        <xsl:apply-templates select="sp:Role" mode="enum">
          <xsl:with-param name="break" select="true()"/>
        </xsl:apply-templates>
      </td>
      <td>
        <xsl:apply-templates select="sp:PersonID"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template match="sp:PersonID">
    <xsl:call-template name="link-name-id">
      <xsl:with-param name="type" select="'person'"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="sp:InformationURL|sp:AccessURL">
    <dt>
      <a href="{sp:URL}">
        <xsl:choose>
          <xsl:when test="count(sp:Name) = 1">
            <xsl:value-of select="sp:Name"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="sp:URL"/>
          </xsl:otherwise>
        </xsl:choose>
      </a>
    </dt>
    <xsl:apply-templates select="sp:Description">
      <xsl:with-param name="print_title" select="false()"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:Language" mode="text"/>
  </xsl:template>
  <xsl:template match="sp:Structure">
    <xsl:if test="count(*) &gt; 0">
      <dt>
        <xsl:value-of select="local-name()"/>
      </dt>
      <dd>
        <dl>
          <xsl:apply-templates select="sp:Size" mode="text"/>
          <xsl:apply-templates select="sp:Description"/>
          <xsl:if test="count(sp:Element) &gt; 0">
            <dt>Elements</dt>
            <dd>
              <table class="nested" cellspacing="0">
                <tr>
                  <th class="center">Index</th>
                  <th class="center">Name</th>
                  <xsl:if test="count(sp:Element/sp:Component) &gt; 0">
                    <th class="center">Component</th>
                  </xsl:if>
                  <xsl:if test="count(sp:Element/sp:ParameterKey) &gt; 0">
                    <th>Parameter key</th>
                  </xsl:if>
                  <xsl:if test="count(sp:Element/sp:Units) &gt; 0">
                    <th>Units</th>
                  </xsl:if>
                  <xsl:if test="count(sp:Element/sp:UnitsConversion) &gt; 0">
                    <th>Units conversion</th>
                  </xsl:if>
                  <xsl:if test="count(sp:Element/sp:ValidMin) &gt; 0">
                    <th>Valid min</th>
                  </xsl:if>
                  <xsl:if test="count(sp:Element/sp:ValidMax) &gt; 0">
                    <th>Valid max</th>
                  </xsl:if>
                  <xsl:if test="count(sp:Element/sp:FillValue) &gt; 0">
                    <th>Fill value</th>
                  </xsl:if>
                </tr>
                <xsl:apply-templates select="sp:Element"/>
              </table>
            </dd>
          </xsl:if>
        </dl>
      </dd>
    </xsl:if>
  </xsl:template>
  <xsl:template match="sp:Element">
    <tr>
      <xsl:apply-templates select="sp:Index" mode="table"/>
      <xsl:apply-templates select="sp:Name" mode="table"/>
      <xsl:if test="count(sp:Component) = 0 and count(../sp:Element/sp:Component) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:Component" mode="table"/>
      <xsl:if test="count(sp:ParameterKey) = 0 and count(../sp:Element/sp:ParameterKey) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:ParameterKey" mode="table"/>
      <xsl:if test="count(sp:Units) = 0 and count(../sp:Element/sp:Units) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:Units" mode="table"/>
      <xsl:if test="count(sp:UnitsConversion) = 0 and count(../sp:Element/sp:UnitsConversion) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:UnitsConversion" mode="table"/>
      <xsl:if test="count(sp:ValidMin) = 0 and count(../sp:Element/sp:ValidMin) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:ValidMin" mode="table"/>
      <xsl:if test="count(sp:ValidMax) = 0 and count(../sp:Element/sp:ValidMax) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:ValidMax" mode="table"/>
      <xsl:if test="count(sp:FillValue) = 0 and count(../sp:Element/sp:FillValue) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:FillValue" mode="table"/>
    </tr>
  </xsl:template>
  <xsl:template match="sp:Location">
    <dt>Location</dt>
    <dd>
      <dl>
        <xsl:apply-templates select="sp:ObservatoryRegion" mode="text">
          <xsl:with-param name="title" select="'Region'"/>
          <xsl:sort/>
        </xsl:apply-templates>
      </dl>
    </dd>
    <xsl:if test="count(sp:CoordinateSystemName|sp:Latitude|sp:Longitude|sp:Elevation) &gt; 0">
      <dd>
        <xsl:apply-templates select="sp:CoordinateSystemName" mode="text">
          <xsl:with-param name="title" select="'Coordinate system'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Latitude|sp:Longitude|sp:Elevation"/>
      </dd>
    </xsl:if>
  </xsl:template>
  <xsl:template match="sp:CoordinateSystem">
    <dt>Coordinate system</dt>
    <dd>
      <xsl:value-of select="sp:CoordinateRepresentation"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="sp:CoordinateSystemName"/>
    </dd>
  </xsl:template>
  <xsl:template match="sp:Latitude|sp:Longitude|sp:Elevation">
    <dt>
      <xsl:value-of select="local-name()"/>
    </dt>
    <dd>
      <xsl:value-of select="."/>
      <xsl:choose>
        <xsl:when test="name() = 'Elevation'">
          <xsl:text> m</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text> deg</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </dd>
  </xsl:template>
  <!-- Mixed parameter -->
  <xsl:template match="sp:Mixed">
    <dt>Mixed measurement</dt>
    <dd>
      <xsl:value-of select="text()"/>
    </dd>
  </xsl:template>
  <!-- Field parameter -->
  <xsl:template match="sp:Field">
    <xsl:apply-templates select="sp:FieldQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:FieldQualifier" mode="text">
      <xsl:with-param name="title" select="'Qualifier'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:FrequencyRange">
      <xsl:with-param name="title" select="'Frequency range'"/>
      <xsl:with-param name="label" select="'frequency'"/>
    </xsl:apply-templates>
  </xsl:template>
  <!-- Particle parameter -->
  <xsl:template match="sp:Particle">
    <xsl:apply-templates select="sp:ParticleType" mode="text">
      <xsl:with-param name="title" select="'Particle type'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:ParticleQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:ParticleQualifier" mode="text">
      <xsl:with-param name="title" select="'Qualifier'"/>
    </xsl:apply-templates>
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
  <xsl:template match="sp:Photon">
    <xsl:apply-templates select="sp:PhotonQuantity" mode="text">
      <xsl:with-param name="title" select="'Quantity'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="sp:PhotonQualifier" mode="text">
      <xsl:with-param name="title" select="'Qualifier'"/>
    </xsl:apply-templates>
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
  <xsl:template match="sp:EnergyRange|sp:FrequencyRange|sp:AzimuthalAngleRange|sp:PolarAngleRange|sp:WavelengthRange">
    <xsl:param name="title" select="local-name()"/>
    <xsl:param name="label" select="''"/>
    <dt>
      <xsl:value-of select="$title"/>
    </dt>
    <dd>
      <dl>
        <xsl:apply-templates select="sp:SpectralRange" mode="text">
          <xsl:with-param name="title" select="'Spectral range'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Low" mode="text">
          <xsl:with-param name="title" select="concat('Low ', $label)"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:High" mode="text">
          <xsl:with-param name="title" select="concat('High ', $label)"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="sp:Units" mode="text"/>
        <xsl:if test="count(sp:Bin) &gt; 0">
          <dt>Bins</dt>
          <dd>
            <table class="nested" cellspacing="0">
              <thead>
                <tr>
                  <th>Bin</th>
                  <th>Band name</th>
                  <th>
                    <xsl:value-of select="concat('Low ', $label)"/>
                  </th>
                  <th>
                    <xsl:value-of select="concat('High ', $label)"/>
                  </th>
                </tr>
              </thead>
              <tbody>
                <xsl:apply-templates select="sp:Bin"/>
              </tbody>
            </table>
          </dd>
        </xsl:if>
      </dl>
    </dd>
  </xsl:template>
  <xsl:template match="sp:Bin">
    <tr>
      <td>
        <xsl:value-of select="position()"/>
      </td>
      <xsl:if test="count(sp:BandName) = 0 and count(../sp:Bin/sp:BandName) &gt; 0">
        <xsl:call-template name="empty-cell"/>
      </xsl:if>
      <xsl:apply-templates select="sp:BandName" mode="table"/>
      <xsl:apply-templates select="sp:Low" mode="table"/>
      <xsl:apply-templates select="sp:High" mode="table"/>
    </tr>
  </xsl:template>
  <!-- SPASE enumerations -->
  <!-- Quantity enumeration-->
  <!-- FieldQuantity enumeration -->
  <xsl:template match="text()[.='CrossSpectrum']">
    <xsl:text>Cross spectrum</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Electric']">
    <xsl:text>Electric field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Magnetic']">
    <xsl:text>Magnetic field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Potential']">
    <xsl:text>Potential field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PoyntingFlux']">
    <xsl:text>Poynting flux</xsl:text>
  </xsl:template>
  <!-- Particle enumeration -->
  <xsl:template match="text()[.='AlfvenMachNumber']">
    <xsl:text>Alfven Mach number</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='AverageChargeState']">
    <xsl:text>Average charge state</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='CountRate']">
    <xsl:text>Count rate</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='EnergyDensity']">
    <xsl:text>Energy density</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='EnergyFlux']">
    <xsl:text>Energy flux</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='FlowSpeed']">
    <xsl:text>Flow speed</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='FlowVelocity']">
    <xsl:text>Flow velocity</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='HeatFlux']">
    <xsl:text>Heat flux</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='MassDensity']">
    <xsl:text>Mass density</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='NumberDensity']">
    <xsl:text>Number density</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='NumberFlux']">
    <xsl:text>Number flux</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='Phase-SpaceDensity']">
    <xsl:text>Phase-space density</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PlasmaBeta']">
    <xsl:text>Plasma beta</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PlasmaFrequency']">
    <xsl:text>Plasma frequency</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='SonicMachNumber']">
    <xsl:text>Sonic Mach number</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ThermalSpeed']">
    <xsl:text>Thermal speed</xsl:text>
  </xsl:template>
  <!-- Photon enumeration -->
  <xsl:template match="text()[.='EquivalentWidth']">
    <xsl:text>Equivalent width</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='LineDepth']">
    <xsl:text>Line depth</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ModeAmplitude']">
    <xsl:text>Mode amplitude</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='StokesParameters']">
    <xsl:text>Stoke's parameters</xsl:text>
  </xsl:template>
  <!-- Qualifier enumeration -->
  <xsl:template match="text()[.='StandardDeviation']">
    <xsl:text>Standard deviation</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='LineofSight']">
    <xsl:text>Line of sight</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='StokesParameter']">
    <xsl:text>Stoke's parameter</xsl:text>
  </xsl:template>
  <!-- Measurement Type enumeration -->
  <xsl:template match="text()[.='ActivityIndex']">
    <xsl:text>Activity index</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ChargedParticleFlux']">
    <xsl:text>Charged particle flux</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ElectricField']">
    <xsl:text>Electric field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='EnergeticParticles']">
    <xsl:text>Energetic particles</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ImageIntensity']">
    <xsl:text>Image intensity</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='InstrumentStatus']">
    <xsl:text>Instrument status</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='IonComposition']">
    <xsl:text>Ion composition</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='MagneticField']">
    <xsl:text>Magnetic field</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='NeutralAtomImages']">
    <xsl:text>Neutral atom images</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='NeutralGas']">
    <xsl:text>Neutral gas</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='RadioandPlasmaWaves']">
    <xsl:text>Radion and plasma waves</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='RadioSoundings']">
    <xsl:text>Radio soundings</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ThermalPlasma']">
    <xsl:text>Thermal plasma</xsl:text>
  </xsl:template>
  <!-- Phenomenon type enumeration -->
  <xsl:template match="text()[.='ActiveRegion']">
    <xsl:text>Active solar region</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='BowShockCrossing']">
    <xsl:text>Bow shock crossing</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='CoronalHole']">
    <xsl:text>Coronal hole</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='CoronalMassEjection']">
    <xsl:text>Coronal mass ejection (CME)</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='EITWave']">
    <xsl:text>EIT wave</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='EnergeticSolarParticleEvent']">
    <xsl:text>Energetic solar particle event</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='ForbushDecrease']">
    <xsl:text>Forbush decrease</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='GeomagneticStorm']">
    <xsl:text>Geomagnetic storm</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='InterplanetaryShock']">
    <xsl:text>Interplanetary shock</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='MagneticCloud']">
    <xsl:text>Magnetic cloud</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='MagnetopauseCrossing']">
    <xsl:text>Magnetopause crossing</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='RadioBurst']">
    <xsl:text>Radio burst</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='SolarFlare']">
    <xsl:text>Solar flare</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='SolarWindExtreme']">
    <xsl:text>Solar wind extreme</xsl:text>
  </xsl:template>
  <!-- Role enumeration -->
  <xsl:template match="text()[.='DataProducer']">
    <xsl:text>Data producer</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='GeneralContact']">
    <xsl:text>General contact</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'MetadataContact']">
    <xsl:text>Metadata contact</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='PrincipalInvestigator']">
    <xsl:text>Principal investigator</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ProjectScientist']">
    <xsl:text>Project scientist</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'TeamLeader']">
    <xsl:text>Team leader</xsl:text>
  </xsl:template>
  <xsl:template match="text()[.='TeamMember']">
    <xsl:text>Team member</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'TechnicalContact']">
    <xsl:text>Technical contact</xsl:text>
  </xsl:template>
  <!-- Instrument type enumeration -->
  <xsl:template match="text()[. = 'DoubleSphere']">
    <xsl:text>Double Sphere</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'DustDetector']">
    <xsl:text>Dust Detector</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ElectronDriftInstrument']">
    <xsl:text>Electron Drift Instrument</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ElectrostaticAnalyser']">
    <xsl:text>Electrostatic Analyser</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'EnergeticParticleInstrument']">
    <xsl:text>Energetic Particle Instrument</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'FaradayCup']">
    <xsl:text>Faraday Cup</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'FluxFeedback']">
    <xsl:text>Flux Feedback</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'FourierTransformSpectrograph']">
    <xsl:text>Fourier Transform Spectrograph</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'Geiger-MuellerTube']">
    <xsl:text>Geiger-Mueller Tube</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ImagingSpectrometer']">
    <xsl:text>Imaging Spectrometer</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'IonDrift']">
    <xsl:text>Ion Drift</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'LangmuirProbe']">
    <xsl:text>Langmuir Probe</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'LongWire']">
    <xsl:text>Long Wire</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'MassSpectrometer']">
    <xsl:text>Mass Spectrometer</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'MicrochannelPlate']">
    <xsl:text>Microchannel Plate</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'MultispectralImager']">
    <xsl:text>Multispectral Imager</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'NeutralAtomImager']">
    <xsl:text>Neutral Atom Imager</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'NeutralParticleDetector']">
    <xsl:text>Neutral Particle Detector</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ParticleCorrelator']">
    <xsl:text>Particle Correlator</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ParticleDetector']">
    <xsl:text>Particle Detector</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ProportionalCounter']">
    <xsl:text>Proportional Counter</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'QuadrisphericalAnalyser']">
    <xsl:text>Quadrispherical Analyser</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ResonanceSounder']">
    <xsl:text>Resonance Sounder</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'RetardingPotentialAnalyser']">
    <xsl:text>Retarding Potential Analyser</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'ScintillationDetector']">
    <xsl:text>Scintillation Detector</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'SearchCoil']">
    <xsl:text>Search Coil</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'SpacecraftPotential Control']">
    <xsl:text>Spacecraft Potential Control</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'SpectralPowerReceiver']">
    <xsl:text>Spectral Power Receiver</xsl:text>
  </xsl:template>
  <xsl:template match="text()[. = 'WaveformReceiver']">
    <xsl:text>Waveform Receiver</xsl:text>
  </xsl:template>
</xsl:stylesheet>
