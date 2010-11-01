<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:vmo="http://vmo.nasa.gov/xslt" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
    <!-- Print a value with units and make it properly plural/singular -->
  <xsl:function name="vmo:print-value" as="xs:string" use-when="system-property('xsl:version')='2.0'">
    <xsl:param name="val" as="xs:decimal"/>
    <xsl:param name="unit" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="abs($val) eq 1">
        <xsl:copy-of select="concat($val,' ',$unit,' ')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="concat($val,' ',$unit,'s ')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  <!-- Format duration type values -->
  <xsl:function name="vmo:format-duration" as="xs:string" use-when="system-property('xsl:version')='2.0'">
    <xsl:param name="d" as="xs:duration"/>
    <xsl:choose>
      <xsl:when test="function-available('years-from-duration')">
        <xsl:variable name="yr" select="years-from-duration($d)"/>
        <xsl:variable name="mo" select="months-from-duration($d)"/>
        <xsl:variable name="da" select="days-from-duration($d)"/>
        <xsl:variable name="hh" select="hours-from-duration($d)"/>
        <xsl:variable name="mm" select="minutes-from-duration($d)"/>
        <xsl:variable name="sec" select="seconds-from-duration($d)"/>
        <xsl:variable name="res">
          <xsl:if test="abs($yr) gt 0">
            <xsl:value-of select="vmo:print-value(abs($yr),'year')"/>
          </xsl:if>
          <xsl:if test="abs($mo) gt 0">
            <xsl:value-of select="vmo:print-value(abs($mo),'month')"/>
          </xsl:if>
          <xsl:if test="abs($da) gt 0">
            <xsl:value-of select="vmo:print-value(abs($da),'day')"/>
          </xsl:if>
          <xsl:if test="abs($hh) gt 0">
            <xsl:value-of select="vmo:print-value(abs($hh),'hour')"/>
          </xsl:if>
          <xsl:if test="abs($mm) gt 0">
            <xsl:value-of select="vmo:print-value(abs($mm),'minute')"/>
          </xsl:if>
          <xsl:if test="abs($sec) gt 0">
            <xsl:value-of select="vmo:print-value(abs($sec),'second')"/>
          </xsl:if>
          <xsl:if test="$yr + $mo + $da + $hh + $mm + $sec lt 0">
            <xsl:text>ago</xsl:text>
          </xsl:if>
        </xsl:variable>
        <xsl:value-of select="$res"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$d"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
</xsl:stylesheet>
