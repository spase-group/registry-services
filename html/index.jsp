<html>
<head>
<style>
body {
 font-family: "Trebuchet MS";
 color: #0F0F0F
}
h1 {
  font-size: 120%;
  color: #4F4F4F;
  border-bottom-width: 2px;
  border-bottom-style: solid;
  border-bottom-color: #CCCCCC;
}
h2 {
  font-size: 110%;
  margin-bottom: 4px;
  color: #4F4F4F;
}
div.right {
   float: right;
}
</style>
</head>
<body>
<center><h1>SPASE Registry Services</h1></center>

<h1>Registry Explorer</h1>
<table>
<tr>
<td valign="top">A graphical tree based interface to the registries. Click <a href="explorer">here</a> to try it.</td>
<td valign="top"><img src="explorer.gif" /></td>
</tr>
</table>

<h1>Status</h1>
Status retrieves information about a registry. 
<ul>
<li>Help: <a href="status?h=yes">status?h=yes</a></li>
<li>Inventory:
	<ul>
		<li>Resource Class (1): <a href="status?a=Example&i=1">status?a=Example&i=1</a></li>
		<li>Observatory (2): <a href="status?a=Example&i=2">status?a=Example&i=2</a></li>
		<li>Instrument (3): <a href="status?a=Example&i=3">status?a=Example&i=3</a></li>
	</ul>
</li>
<li>Updated:
	<ul>
		<li>In last 30 days<br>
			<ul>
				<li>Summary: <a href="status?a=Example&u=yes&b=30+days">status?a=Example&u=yes&b=30+days</a></li>
				<li>Details: <a href="status?a=Example&u=yes&d=yes&b=30+days">status?a=Example&u=yes&d=yes&b=30+days</a></li>
			</ul>
		</li>
		<li>Since 2009-01-01<br>
			<ul>
				<li>Summary: <a href="status?a=Example&u=yes&b=2009-01-01">status?a=Example&u=yes&b=2009-01-01</a></li>
				<li>Details: <a href="status?a=Example&u=yes&d=yes&b=2009-01-01">status?a=Example&u=yes&d=yes&b=2009-01-01</a></li>
			</ul>
		</li>
	</ul>
</ul>

<h1>Search</h1>

Scan a registry for resources containing one or more words.
<ul>
<li>Help: <a href="search?h=yes">search?h=yes</a></li>
<li>Render:
	<ul>
		<li>Geotail related: <a href="search?words=Geotail">search?words=Geotail</a></li>
   </ul>
</ul>			


<h1>Resolver</h1>
Resolver retrieves a resource description for a given resource ID or generates a list 
of resources at a given partial reosurce ID location. 

<ul>
<li>Help: <a href="resolver?h=yes">resolver?h=yes</a></li>
<li>Tree: <a href="resolver?i=spase://Example/NumericalData&t=yes">resolver?i=spase://Example/NumericalData&amp;t=yes</a></li>
<li>Resource Description:<br>
	<ul>
		<li>Just resource: <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S</a></li>
		<li>Full (recursive): <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&r=yes">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&r=yes</a></li>
	</ul>
</ul>
<ul>
<li>Granule:<br>
	<ul>
		<li>All: <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes</a></li>
		<li>All (URL): <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&u=yes">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&u=yes</a></li>
		<li>All (size): <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&s=yes">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&s=yes</a></li>
		<li>For August 2004: <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&b=2004-08-01&e=2004-08-31">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&b=2004-08-01&e=2004-08-31</a></li>
		<li>For August 2004 (URL): <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&b=2004-08-01&e=2004-08-31&u=yes">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&b=2004-08-01&e=2004-08-31&u=yes</a></li>
		<li>For August 2004 (size): <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&b=2004-08-01&e=2004-08-31&s=yes">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&b=2004-08-01&e=2004-08-31&s=yes</a></li>
		<li>For August 2004 (with parent): <a href="resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&r=yes&b=2004-08-01&e=2004-08-31">resolver?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&g=yes&r=yes&b=2004-08-01&e=2004-08-31</a></li>
	</ul>
</ul>

<h1>Download</h1>
Obtains a list of URLs associated with a resource by quering a registry 
server, then downloads and packages all the source files. The collection 
of files is packaged into a zip file and written to the output stream. 

<ul>
<li>Help: <a href="download?h=yes">download?h=yes</a></li>
<li>Granule Lists:
	<ul>
		<li>All granules: <a href="download?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S">download?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S</a></li>
		<li>Granules for August 2004: <a href="download?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&b=2004-08-01&e=2004-08-31">download?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&b=2004-08-01&e=2004-08-31</a></li>
	</ul>
</ul>

<h1>Render</h1>
Render a resource using a stylesheet. 
<ul>
<li>Help: <a href="render?h=yes">render?h=yes</a></li>
<li>Render:
	<ul>
		<li>Just resource: <a href="render?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S">render?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S</a></li>
		<li>Full (recursive): <a href="render?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&f=yes">render?id=spase://Example/NumericalData/Geotail/LEP/EDA.PT12S&f=yes</a></li>
	</ul>
</ul>

</body>
