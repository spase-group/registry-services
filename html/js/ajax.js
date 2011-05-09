/**
 * Functions for Asynchronous Javascript and XML (AJAX) support.
 * 
 * Process XML and support dynamic pages.
 * 
 * Author: Todd King
**/

var mAjaxXMLHttp = null;	// HTTP Connection that persists across functions
var mAjaxStateHandler = ajaxStateChangedElement;
var mAjaxResultElement = "results";	// The name of the element for the results;
var mAjaxStatusElement = "status";	// The name of the element for status information;
var mAjaxProgressImage = "./images/progress.gif";	// The image to display while waiting.

window.onerror = ajaxHandleError; // safety net to trap all errors

function ajaxHandleError(message, URI, line) {
	alert("Error\n\nLine: " + line + ": " + message + "\nin: " + URI);
	// alert the user that this page may not respond properly
	return true; // this will stop the default message
}

function ajaxGetXmlHttpObject()
{
	var xmlHttp=null;
	try { // Firefox, Opera 8.0+, Safari
		xmlHttp=new XMLHttpRequest();
	} catch (e) { // Internet Explorer
		try {
			xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
		}
	}
	if(xmlHttp == null) alert ("Your browser does not support AJAX!");
	
	return xmlHttp;
}

// Toggle the visibility of an element
function ajaxShowHide(id)
{ 
	if (document.getElementById){ 
		obj = document.getElementById(id); 
		if(!obj) alert("object '" + id + "' not found.");
		if (obj.style.display == "none"){ 
			obj.style.display = ""; 
		} else { 
			obj.style.display = "none"; 
		} 
	} 
}

// Set the content (innerHTML) of element to a value.
function ajaxSetContent(element, value)
{
	if(element == null) return;
	if(!document.getElementById(element)) return;
	document.getElementById(element).innerHTML = value;
}

// Sends an HTTP request and processes the results
// Arguments:
//  1: The URL to send the request
//  *: Arguments to pass to the request.
function ajaxGet()
{
	if(arguments.length < 1) {
		ajaxSetContent(mAjaxStatusElement, "Insufficient arguments to ajxGet()");
		return;
	}
	
	// Process required arguments
	var serviceURL = arguments[0];
	
	// If no words - don't submit
	/*
	if (arguments.length == 1) { 
		ajaxSetContent(mAjaxResultElement, "");
		return;
	}
	*/
	
	// Send request and process asynchonously
	mAjaxXMLHttp = ajaxGetXmlHttpObject()
	
	if (mAjaxXMLHttp == null) return;	// AJAX not supported
	
	// Build up URL
	var buffer = "?CID=" + Math.random();	// Avoids hitting cache
			
   for(var i=1; i<arguments.length; i++) {
   	if(arguments[i].length > 0) buffer += "&" + arguments[i];
   }

	mAjaxXMLHttp.onreadystatechange = mAjaxStateHandler;
	mAjaxXMLHttp.open('GET', serviceURL + buffer, true);
	mAjaxXMLHttp.send(null);
	
	return;
} 

// Sends an HTTP request and processes the results
// Arguments:
//  0: Request processor
//  1: The URL to send the request
//  *: Arguments to pass to the request.
function ajaxPost()
{
	if(arguments.length < 1) {
		ajaxSetContent(mAjaxStatusElement, "Insufficient arguments to ajaxPost()");
		return;
	}
	
	var serviceURL = arguments[0];
	
	// If no words - don't submit
	if (arguments.length == 1) { 
		ajaxSetContent(mAjaxResultElement, "");
		return;
	}
	
	// Send request and process asynchonously
	mAjaxXMLHttp = ajaxGetXmlHttpObject()
	if (mAjaxXMLHttp == null) return;	// AJAX not supported
	
	// Build up URL
	var buffer = "CID=" + Math.random();	// Avoids hitting cache
			
   for(var i=1; i<arguments.length; i++) buffer += "&" + arguments[i];

	mAjaxXMLHttp.onreadystatechange = mAjaxStateHandler;
	mAjaxXMLHttp.open('POST', serviceURL, true);
	mAjaxXMLHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	mAjaxXMLHttp.setRequestHeader("Content-length", buffer.length);
	mAjaxXMLHttp.setRequestHeader("Connection", "close");
	mAjaxXMLHttp.send(buffer);
	
	return;
} 

// Navigates to a new URL by posting arguments then jumping to link.
// Used to hide the arguments from the URL the browser displays.
// Useful when filling all or part of a form.
// First argument is URL and subsequent arguments are keyword=value.
function ajaxLinkTo()
{
	if(arguments.length < 1) {
		alert("Insufficient arguments to showResults()");
		return;
	}
	
	// Create a form
	var myForm = document.createElement("form");
	myForm.style.display = 'none';	// Hide it
	document.body.appendChild(myForm);
	
	// Add it to the document body
	
	// Add action and method attributes
	myForm.action = arguments[0];
	myForm.method = "POST"
	
	// Create a text input for each argument
	for(var i = 1; i < arguments.length; i++) 
	{
		if(arguments[i].length == 0) continue;
		var elem = document.createElement("input");
		elem.type = "text";
		var part = arguments[i].split("=", 2);
		elem.name = part[0];
		elem.value = part[1];
		myForm.appendChild(elem);
	}
	// Call the form's submit method
	myForm.submit();
} 

// Sends an HTTP request and processes the results
// Arguments:
//  0: The URL to send the request
//  *: Arguments to pass to the request.
function ajaxUpdatePage()
{
	if(arguments.length < 1) {
		ajaxSetContent(mAjaxStatusElement, "Insufficient arguments to ajaxUpdatePage()");
		return;
	}
	
	// Process required arguments
	var serviceURL = arguments[0];
	
	// Send request and process asynchonously
	mAjaxXMLHttp = ajaxGetXmlHttpObject()
	
	if(mAjaxXMLHttp == null) return;	// AJAX not supported
	
	// Build up URL
	var ref = serviceURL	+ "?CID=" + Math.random();	// Avoids hitting cache
			
   for(var i=1; i<arguments.length; i++) {
   	if(arguments[i].length > 0) ref += "&" + arguments[i] 
   }

	mAjaxXMLHttp.onreadystatechange = ajaxStateChangedPage;
	mAjaxXMLHttp.open("GET", ref, true);
	mAjaxXMLHttp.send(null);
	
	return;
} 

// Sends all the elements of a form to a URL.
// Always returns "false" to supress further processing
// when used in "onclick".
//
// Arguments:
//  formID: The identifier assigned to the form object to send
//  serverURL: The URL to send the request
function ajaxPostForm(formID, serviceURL)
{
	// Send request and process asynchonously
	mAjaxXMLHttp = ajaxGetXmlHttpObject()
	
	if(mAjaxXMLHttp == null) return false;	// AJAX not supported
	
	// Build up URL
	var delim = "";
	var buffer = "";
	var param = "";

	form = document.getElementById(formID);
	if(form == null) { alert("Form with ID \"" + formID + "\" not found."); return false;	} // Invalid form
	
   for(var i=0; i < form.elements.length; i++) {
   	param = ajaxGetElementValue(form.elements[i]);
   	if(param.length > 0) {
   		buffer += delim + param;
   		delim = "&";
   	}
   }

	if(arguments.length < 2) {
		ajaxSetContent(mAjaxStatusElement, "Insufficient arguments to ajaxPostForm()");
		return;
	}
	
	mAjaxXMLHttp.onreadystatechange = ajaxStateChangedPage;
	mAjaxXMLHttp.open('POST', serviceURL, true);
	mAjaxXMLHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	mAjaxXMLHttp.setRequestHeader("Content-length", buffer.length);
	mAjaxXMLHttp.setRequestHeader("Connection", "close");
	mAjaxXMLHttp.send(buffer);
	
	return false;
} 

function ajaxGetElementValue(elem)
{
	var buffer = '';
	
	switch(ajaxGetUpdateType(elem)) {
	case 'radio': // set checked
	case 'checkbox': // set checked
		if(elem.checked) buffer = elem.name + "=" + encodeURI(elem.value);
	   break;
	case 'select': // set selected
		buffer = elem.name + "=" + encodeURI(elem.options[elem.selectedIndex].value);
	   break;
	case 'password':	// Encrypt text
		buffer = elem.name + "=" + encodeURI(MD5(elem.value));
	   break;
	case 'html': // set innerHTML
		buffer = elem.name + "=" + encodeURI(elem.innerHTML);
	   break;
	case 'text': // set value
		buffer = elem.name + "=" + encodeURI(elem.value);
	   break;
	}
	
	return buffer;
}

// Cancels the current HTTP transaction
function ajaxCancel()
{
	if(mAjaxXMLHttp != null) mAjaxXMLHttp.abort();
}

// Defines the default HTTP Request state handler
function ajaxSetStateHangler(handler)
{
	mAjaxStateHandler = handler;
}

// Reacts when the state changes on the current HTTP connection
// The current page is updated with the content of the response.
// Elements on the page with names corresponding to the dot-notation
// for each XML elements are altered.
function ajaxStateChangedPage() 
{ 
	if(mAjaxXMLHttp == null) return;
	
	switch(mAjaxXMLHttp.readyState) {
		case 0:	// The request is not initialized
			// document.getElementById("results").innerHTML="";
			break;
		case 1:	// The request has been set up
			// document.parameters.words.innerText = mWords;
			ajaxSetContent(mAjaxStatusElement, '<center>Searching<br><img src="' + mAjaxProgressImage + '"><br><button onclick="cancel();">Cancel</button><br></center>');
			break;
		case 2:	// The request has been sent
		case 3:	// The request is in process
			break;
		case 4:	// The request is complete
			ajaxSetContent(mAjaxStatusElement, mAjaxXMLHttp.statusText);
         if (mAjaxXMLHttp.status == 200) {	// Success
				ajaxUpdatePageFromDoc(mAjaxXMLHttp.responseXML);
			} else {	// Error
				ajaxSetContent(mAjaxStatusElement, "Error: " + mAjaxXMLHttp.status + "<br>" + "Unable to connect to: " + mAjaxXMLHttp.statusText);
			}
			break;
	}
}

// Reacts when the state changes on the current HTTP connection
// Results are displayed in the mAjaxResultElement tag set when the
// connection is established with function that sends a POST or GET;
function ajaxStateChangedElement() 
{ 
	if(mAjaxXMLHttp == null) return;
	if(! document.getElementById(mAjaxResultElement)) return;
	
	switch(mAjaxXMLHttp.readyState) {
		case 0:	// The request is not initialized
			// document.getElementById("results").innerHTML="";
			break;
		case 1:	// The request has been set up
			// document.parameters.words.innerText = mWords;
			ajaxSetContent(mAjaxResultElement, '<center>Searching<br><img src="' + mAjaxProgressImage + '"><br><button onclick="cancel();">Cancel</button><br></center>');
			break;
		case 2:	// The request has been sent
		case 3:	// The request is in process
			break;
		case 4:	// The request is complete
			ajaxSetContent(mAjaxResultElement, mAjaxXMLHttp.statusText);
         if (mAjaxXMLHttp.status == 200) {
				ajaxSetContent(mAjaxResultElement, mAjaxXMLHttp.responseText);
			} else {
				ajaxSetContent(mAjaxResultElement, "Error: " + mAjaxXMLHttp.status + "<br>" + "Unable to connect to: " + mAjaxXMLHttp.statusText);
			}
			break;
	}
}

// Parse a string into an XML DOM object
function ajaxParseText(text)
{
   // code for IE
   if (window.ActiveXObject) {
     var doc=new ActiveXObject("Microsoft.XMLDOM");
     doc.async="false";
     doc.loadXML(text);
   } else { // code for Mozilla, Firefox, Opera, etc.
     var parser=new DOMParser();
     var doc=parser.parseFromString(text, "text/xml");
   }
   
   return doc;
}
	
// Parse a string into an XML DOM object
function ajaxParseText(text)
{
   // code for IE
   if (window.ActiveXObject) {
     var doc=new ActiveXObject("Microsoft.XMLDOM");
     doc.async="false";
     doc.loadXML(text);
   } else { // code for Mozilla, Firefox, Opera, etc.
     var parser=new DOMParser();
     var doc=parser.parseFromString(text, "text/xml");
   }
   
   return doc;
}
	
// Sets all elements in the current page with the content
// of the corresponding elements in the XML document.
function ajaxUpdatePageFromDoc(doc)
{
   ajaxSetElements(doc.documentElement.nodeName, doc.documentElement.childNodes);
}

// Set the progress image.
function ajaxSetProgressImage(path)
{
	mAjaxProgressImage = path;
}

// Set the name of the element for the results
function ajaxSetResultElement(name)
{
   mAjaxResultElement = name;	
}

// Set the name of the element for status information;
function ajaxSetStatusElement(name)
{
   mAjaxStatusElement = name;
}

// Sets all form elements with a name that corresponds to an XPath syntax node reference.
// With XPath node names are delimited with a "/"
function ajaxSetElements(prefix, nodes)
{
   if(nodes == null) return;
   
   var cleared = new Array(0);
   
   for(var i = 0; i < nodes.length; i++) {
      var base = prefix + "/" + nodes[i].nodeName;
      var name = base + ajaxGetPeerIndex(nodes[i]);
      var realName = name;
      if(nodes[i].nodeType == 1) {	// 1=Element
         var obj = document.getElementsByName(name);
         // If not found try the base name.
         if(obj.length == 0) { obj = document.getElementsByName(base); realName = base; }
         // Clear all checkboxes or selections
         if(! ajaxIsInList(realName, cleared)) {
         	cleared.push(realName);
	         for(var j = 0; j < obj.length; j++) {
	            switch(ajaxGetUpdateType(obj[j])) {
					case 'checkbox': 
						obj[j].checked = false;
						break;
					case 'select':
						obj[j].selected = false;
						break;
					}
	         }
	      }
         // Set field value
         for(var j = 0; j < obj.length; j++) {
            var value = ajaxGetNodeText(nodes[i]);
            switch(ajaxGetUpdateType(obj[j])) {
            case 'radio': // set checked
               obj[j].checked = ajaxIsMatch(obj[j].value, value);
               break;
            case 'checkbox': // set checked
               if(ajaxIsMatch(obj[j].value, value)) obj[j].checked = true;
               break;
            case 'select': // set selected
               obj[j].selected = ajaxSelectOption(obj[j], value);
               break;
            case 'html': // set innerHTML
               obj[j].innerHTML = value;
               break;
            case 'text': // set value
               obj[j].value = value; // Text
               break;
            case 'password': // set value
               obj[j].value = ''; // Password
               break;
            }
         }
         if(ajaxHasChildElement(nodes[i])) { ajaxSetElements(name, nodes[i].childNodes); }
      }
   }
}

// Select the option tag with the a specific value
function ajaxSelectOption(node, value)
{
   for(var i = 0; i < node.options.length; i++) {
     	if(ajaxIsMatch(node.options[i].value, value)) {
     		node.selectedIndex = i;
      }
   }
}

// Determines if a vlaue is in a list
function ajaxIsInList(value, list)
{
	for(i = 0; i < list.length; i++) {
		if(list[i] == value) return true;
	}
	return false;
}

// Determines if two values match
function ajaxIsMatch(value1, value2)
{
	if(value1 == null && value2 == null) return true;
	if(value1 == null) return false;
	if(value2 == null) return false;
   if(value1.toUpperCase() == value2.toUpperCase()) return true;
   return false;
}

// Determine the type update for a node. Looks at the node type to determine type of update
// There are the following update types
// 0: unknown
// 1: set checked
// 2: set selected
// 3: set innerHTML
// 4: set value
function ajaxGetUpdateType(node)
{
   if(node == null) return '';

	switch(node.type) {
	case 'radio':
		return 'radio';
	case 'checkbox':
		return 'checkbox';
	case 'select-one':
		return 'select';
	case 'textarea':
	case 'button':
		return 'html';
	case 'text':
	case 'hidden':
	case 'button':
	case 'submit':
		return 'text';
	case 'password':
		return 'password';
	}
	
	return '';
}

// Determine the index reference of a node. Searches all peer nodes
// of the same tag name and determines the relative index. The index
// if returned as the string "[index]". If there are no peers then
// an empty string is returned. 
function ajaxGetPeerIndex(node)
{
   var nodeParent = node.parentNode;
   
   var cnt = 0;
   var myIndex = 0;
   
   for(var i = 0; i < nodeParent.childNodes.length; i++) {
      if(nodeParent.childNodes[i].nodeType == 1) { // An element
         if(nodeParent.childNodes[i].nodeName == node.nodeName) {
            if(nodeParent.childNodes[i] == node) myIndex = cnt;
            cnt++;
         }
      }
   }
   // If more than one occurence - create index string
   if(cnt > 1) return "[" + myIndex + "]";
   
   return "";
}

// Determines if a node has children that are elements.
function ajaxHasChildElement(node)
{
   if(! node.hasChildNodes()) return false;
   
   for(var i = 0; i < node.childNodes.length; i++) {
      if(node.childNodes[i].nodeType == 1) return true;
   }
   return false;
}

// Concatenates all text nodes that are children of the node and returns the result.
// THe method for retrieving all text under a node varies by browser. This method
// is browser independent.
// Note: Performing a nomalize() on the DOM should compress all text nodes into a single
// node.
function ajaxGetNodeText(node)
{
   var buffer = "";
   if(! node.hasChildNodes()) return buffer;
   
   for(var i = 0; i < node.childNodes.length; i++) {
      if(node.childNodes[i].nodeType == 3) buffer += node.childNodes[i].nodeValue;
   }

   return buffer;
}
