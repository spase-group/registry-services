<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:sp="http://www.spase-group.org/data/schema" xmlns:vot="http://www.ivoa.net/xml/VOTable/VOTable/v1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" version="2.0" exclude-result-prefixes="sp vot vmo">
  <xsl:template name="top-arrow">
    <a href="#top" class="toparrow">&#8593;</a>
  </xsl:template>
  <xsl:template name="substring-before-last">
    <xsl:param name="string"/>
    <xsl:param name="char" select="' '"/>
    <xsl:if test="contains($string, $char)">
      <xsl:value-of select="substring-before($string, $char)"/>
      <xsl:variable name="rest" select="substring-after($string, $char)"/>
      <xsl:if test="contains($rest, $char)">
        <xsl:value-of select="$char"/>
        <xsl:call-template name="substring-before-last">
          <xsl:with-param name="string" select="$rest"/>
          <xsl:with-param name="char" select="$char"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <xsl:template name="print-value">
    <xsl:param name="val"/>
    <xsl:param name="unit"/>
    <xsl:choose>
      <xsl:when test="abs($val) = 1">
        <xsl:copy-of select="concat($val,' ',$unit,' ')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="concat($val,' ',$unit,'s ')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="format-duration">
    <xsl:variable name="d" select="."/>
    <xsl:choose>
      <xsl:when test="function-available('years-from-duration')">
        <xsl:variable name="yr" select="years-from-duration($d)"/>
        <xsl:variable name="mo" select="months-from-duration($d)"/>
        <xsl:variable name="da" select="days-from-duration($d)"/>
        <xsl:variable name="hh" select="hours-from-duration($d)"/>
        <xsl:variable name="mm" select="minutes-from-duration($d)"/>
        <xsl:variable name="sec" select="seconds-from-duration($d)"/>
        <xsl:variable name="res">
          <xsl:if test="abs($yr) &gt; 0">
            <xsl:call-template name="print-value">
              <xsl:with-param name="val" select="abs($yr)"/>
              <xsl:with-param name="unit" select="'year'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="abs($mo) &gt; 0">
            <xsl:call-template name="print-value">
              <xsl:with-param name="val" select="abs($mo)"/>
              <xsl:with-param name="unit" select="'month'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="abs($da) &gt; 0">
            <xsl:call-template name="print-value">
              <xsl:with-param name="val" select="abs($da)"/>
              <xsl:with-param name="unit" select="'day'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="abs($hh) &gt; 0">
            <xsl:call-template name="print-value">
              <xsl:with-param name="val" select="abs($hh)"/>
              <xsl:with-param name="unit" select="'hour'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="abs($mm) &gt; 0">
            <xsl:call-template name="print-value">
              <xsl:with-param name="val" select="abs($mm)"/>
              <xsl:with-param name="unit" select="'minute'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="abs($sec) &gt; 0">
            <xsl:call-template name="print-value">
              <xsl:with-param name="val" select="abs($sec)"/>
              <xsl:with-param name="unit" select="'second'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="$yr + $mo + $da + $hh + $mm + $sec &lt; 0">
            <xsl:text>ago</xsl:text>
          </xsl:if>
        </xsl:variable>
        <xsl:value-of select="$res"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$d"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="empty-cell">
    <td/>
  </xsl:template>
  <xsl:template match="*" mode="enum">
    <xsl:param name="break" select="false()"/>
    <xsl:apply-templates select="text()"/>
    <xsl:if test="$break and last() != position()">
      <br/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*" mode="text">
    <xsl:param name="title" select="local-name()"/>
    <xsl:if test="position() = 1">
      <dt>
        <xsl:value-of select="$title"/>
      </dt>
    </xsl:if>
    <dd>
      <xsl:apply-templates select="text()"/>
    </dd>
  </xsl:template>
  <xsl:template match="*" mode="no_title">
    <dd>
      <xsl:apply-templates select="text()"/>
    </dd>
  </xsl:template>
  <xsl:template match="*" mode="table">
    <td>
      <xsl:apply-templates select="text()"/>
    </td>
  </xsl:template>
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="*" mode="copy" priority="0">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="node()" mode="spase-normalization">
    <xsl:analyze-string select="." regex="(\n\s*\n)">
      <xsl:non-matching-substring>
        <!-- Find tables -->
        <xsl:analyze-string select="." regex="(\+-+\+.*?\+-+\+)" flags="s">
          <xsl:matching-substring>
            <xsl:call-template name="htmltable"/>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <!-- Find lists -->
            <xsl:analyze-string select="." regex="\s*(^\s*\*\s.*)" flags="ms">
              <xsl:matching-substring>
                <xsl:call-template name="htmllist"/>
              </xsl:matching-substring>
              <xsl:non-matching-substring>
                <p>
                  <xsl:value-of select="."/>
                </p>
              </xsl:non-matching-substring>
            </xsl:analyze-string>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>
  <xsl:template name="htmllist">
    <ul>
      <xsl:for-each select="tokenize(regex-group(1),'^\s*\* ','m')">
        <xsl:analyze-string select="." regex="^\s*([^-\.\s].*?)\n\s*(-\s.*)$" flags="s">
          <xsl:matching-substring>
            <li>
              <xsl:value-of select="regex-group(1)"/>
              <xsl:if test="string-length(regex-group(2)) gt 0">
                <xsl:call-template name="htmllist-level2"/>
              </xsl:if>
            </li>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <li>
              <xsl:value-of select="."/>
            </li>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:for-each>
    </ul>
  </xsl:template>
  <xsl:template name="htmllist-level2">
    <ul>
      <xsl:for-each select="tokenize(regex-group(2),'^\s*-\s','m')">
        <xsl:analyze-string select="." regex="^\s*([^\.\s].*?)\n\s*(\.\s.*)$" flags="s">
          <xsl:matching-substring>
            <li>
              <xsl:value-of select="regex-group(1)"/>
              <xsl:if test="string-length(regex-group(2)) gt 0">
                <xsl:call-template name="htmllist-level3"/>
              </xsl:if>
            </li>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <li>
              <xsl:value-of select="."/>
            </li>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:for-each>
    </ul>
  </xsl:template>
  <xsl:template name="htmllist-level3">
    <ul>
      <xsl:for-each select="tokenize(.,'^\s*\.\s','m')">
        <xsl:if test="string-length(.) != 0">
          <li>
            <xsl:value-of select="."/>
          </li>
        </xsl:if>
      </xsl:for-each>
    </ul>
  </xsl:template>
  <xsl:template name="list-item">
    <xsl:analyze-string select="." regex="^\s*$" flags="m">
      <xsl:non-matching-substring>
        <li>
          <xsl:value-of select="."/>
        </li>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>
  <xsl:template name="htmltable">
    <table>
      <xsl:attribute name="class" select="'spase-desc'"/>
      <xsl:analyze-string select="." regex="\+-+\+(.*?)\n\s*\|[\|\-]+\|\s*\n(.*?)\+-+\+\s*" flags="s">
        <!-- Assuming that the header ends at the first visual row separator -->
        <!-- If no visual separator is present, no header is created -->
        <xsl:matching-substring>
          <!-- Process table header -->
          <thead>
            <xsl:analyze-string select="regex-group(1)" regex="\n">
              <xsl:non-matching-substring>
                <tr>
                  <xsl:call-template name="table-cells">
                    <xsl:with-param name="tag" select="'th'"/>
                  </xsl:call-template>
                </tr>
              </xsl:non-matching-substring>
            </xsl:analyze-string>
          </thead>
          <xsl:analyze-string select="regex-group(2)" regex="\|[-\|]+\|" flags="s">
            <xsl:non-matching-substring>
              <xsl:call-template name="table-body"/>
            </xsl:non-matching-substring>
          </xsl:analyze-string>
        </xsl:matching-substring>
        <xsl:non-matching-substring>
          <!-- No header -->
          <xsl:analyze-string select="." regex="\+-+\+(.*?)\+-+\+" flags="s">
            <xsl:matching-substring>
              <xsl:call-template name="table-body">
                <xsl:with-param name="body" select="regex-group(1)"/>
              </xsl:call-template>
            </xsl:matching-substring>
          </xsl:analyze-string>
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </table>
  </xsl:template>
  <xsl:template name="table-body">
    <xsl:param name="body" select="."/>
    <tbody>
      <xsl:analyze-string select="$body" regex="\s*\n\s*">
        <xsl:non-matching-substring>
          <tr>
            <xsl:call-template name="table-cells"/>
          </tr>
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </tbody>
  </xsl:template>
  <xsl:template name="table-cells">
    <xsl:param name="tag" select="'td'"/>
    <xsl:analyze-string select="." regex="\|(\s*[^\|]+)" flags="m">
      <xsl:matching-substring>
        <xsl:element name="{$tag}">
          <xsl:value-of select="regex-group(1)"/>
        </xsl:element>
      </xsl:matching-substring>
    </xsl:analyze-string>
  </xsl:template>
  <xsl:template name="test">
    <xsl:element name="p">
      <xsl:attribute name="style" select="'color:red;'"/>
      <xsl:value-of select="concat('Match:', .)"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
