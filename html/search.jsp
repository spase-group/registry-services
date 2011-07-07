<%! String PageTitle="SPASE - SMWG Registry"; %>
<%@ page 
	import="igpp.xml.Transform" 
	import="igpp.util.Encode" 
	import="igpp.util.Text" 
%>
<script type="text/javascript" src="js/ajax.js"></script>
<link rel="stylesheet" type="text/css" href="css/search.css" />

<% // Get passed parameters
   String words = igpp.util.Text.getValue(request.getParameter("words"), "").trim();
   String category = igpp.util.Text.getValue(request.getParameter("category"), "-").trim();
   String all = "yes"; 	// igpp.util.Text.getValue(request.getParameter("all"), "").trim();
%>

<script>ajaxSetProgressImage("images/progress.gif")</script>
<div class="info">
SPASE Metadata Working group (SMWG) Registry Search<br/>
Discussions at: <a href="http://groups.google.com/group/vxo-spase-smwg">http://groups.google.com/group/vxo-spase-smwg</a><br/>
Metadata Registry at: <a href="http://vmo.nasa.gov/">http://vmo.nasa.gov/</a><br/>
<br>
Registry Explorer at: <a href="http://www.spase-group.org/registry/explorer">http://www.spase-group.org/registry/explorer</a><br/>
</div>
<div class="search">
<form action="" method="post">
	<table> 
	<tr><td>Look&nbsp;for:</td><td nowrap><input type=text name=words value="<%= words %>">
		(Enter "*" to show all resources)
	</td></tr>
	<tr><td>Category:</td><td>
		       <input type=radio name=category value=- <%= igpp.util.Text.isChecked("-", category) %>> Any
             <input type=radio name=category value=Observatory <%= igpp.util.Text.isChecked("Observatory", category) %>> Observatory
		       <input type=radio name=category value=Person <%= igpp.util.Text.isChecked("Person", category) %>> Person
             <input type=radio name=category value=Instrument <%= igpp.util.Text.isChecked("Instrument", category) %>> Instrument
             <input type=radio name=category value=Repository <%= igpp.util.Text.isChecked("Repository", category) %>> Repository
            </td></tr>
</table>
</form>
<br/>
</div>
<hr>
<span id="status"></span> <!-- Area for AJAX messages -->
<div id="results"></div> <!-- Area for AJAX results -->


<% if(words.length() > 0) { %>
<% String wordList = ""; 
   if(words.compareTo("*") == 0) wordList = "*"; 
   else wordList = igpp.util.Text.makeList(words.split("[\\p{Punct}\\p{Blank}]"));
%>

<script>
	   ajaxGet("./render", "url=<%= igpp.util.Encode.urlEncode(
	       igpp.util.Text.getURLPath(request.getRequestURL().toString()) + "/search" 
	     + "?words=" + wordList
	     + "&category=" + category
	     + "&all=" + all
	     )%>"
	  );
</script>
<% } %>

