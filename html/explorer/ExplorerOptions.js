// Global Options
var ExplorerResolver = './resolver';
var ExplorerRender = './render';
var ExplorerOverview = 'overview.htx';

var ExplorerService = window.location.pathname;

// Fix-up Service location - Strip last two nodes
n = ExplorerService.lastIndexOf('/');
if(n != -1) ExplorerService = ExplorerService.substring(0, n);
n = ExplorerService.lastIndexOf('/');
if(n != -1) ExplorerService = ExplorerService.substring(0, n);

// Fix-up individual service paths
if(ExplorerResolver.charAt(0) == '.') {
	ExplorerResolver = ExplorerService + ExplorerResolver.substring(1);
}

if(ExplorerRender.charAt(0) == '.') {
	ExplorerRender = ExplorerService + ExplorerRender.substring(1);
}

