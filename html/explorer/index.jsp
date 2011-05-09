<%@ page language="java" contentType="text/html" 
   import="java.io.File"
%>
<%! String PageTitle="Registry Explorer"; %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=iso-8859-1">
    <title><%= PageTitle%></title>
    <link rel="stylesheet" type="text/css" href="./css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="./css/explorer.css" />
    <link rel="stylesheet" type="text/css" href="./css/spase.css" />
    <script type="text/javascript" src="./js/ext-base.js"></script>
    <script type="text/javascript" src="./js/ext-all.js"></script>
    <script type="text/javascript" src="./js/SessionProvider.js"></script>
    <script type="text/javascript" src="./js/TabCloseMenu.js"></script>
    <script type="text/javascript" src="XmlTreeLoader.js"></script>
    <script type="text/javascript" src="ExplorerOptions.js"></script>
    <script type="text/javascript" src="ExplorerTree.js"></script>
    <script type="text/javascript" src="ExplorerView.js"></script>
    <script type="text/javascript" src="ExplorerApp.js"></script>
<!-- Passed parameters -->
<% String urn = request.getParameter("urn"); %>
</head>

<body style="background-color:#000000" bgcolor="#000000">
    <div id="title"><img src="./images/header-title.png" /></div>
  <div id="header">
    <div id="title"></div>
    <div id="logo"></div>
    <div id="banner"></div>
  </div>
  <div id="explorer" title="Authority"></div>
</body>
</html>
