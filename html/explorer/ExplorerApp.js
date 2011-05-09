ExplorerApp = {};

Ext.onReady(function() {
   Ext.QuickTips.init();
   
   Ext.state.Manager.setProvider(new Ext.state.SessionProvider({state: Ext.appState}));

   var tpl = new Ext.Template(
        '<h2 class="title">{term}</h2>',
        '<p><b>Occurence</b>: {occur}',
        '<p><b>New</b>: {new}</p>',
        '<p><b>Type</b>: {type}</p>'
   );
   tpl.compile();

    ExplorerApp.getTemplate = function(){
        return tpl;
    }
   
    var el = Ext.get('explorer');
    var urn = el.dom.title; // passed parameter on 'explorer' tag
    
    ExplorerApp.getUrn = function(){
        return urn;
    }
   
    var tree = new ExplorerTree();
    ExplorerApp.getTree = function(){
        return tree;
    }
    tree.getRootNode().setText(urn);
    tree.getRootNode().attributes.urn = urn;
    tree.getRootNode().attributes.pattern = '-';
    
    var view = new ExplorerView();
    ExplorerApp.getView = function(){
        return view;
    }
    
    // Viewport occcupies entire window
    var viewport = new Ext.Viewport({
        layout:'border',
        items:[
            new Ext.BoxComponent({ // Open area
                region:'north',
                el: 'explorer',
                height:75
            }),
            tree,
            view
         ]
    });    
    
});

// This is a custom event handler passed to preview panels so 
// links open in a new window
ExplorerApp.LinkInterceptor = {
    render: function(p){
        p.body.on({
            'mousedown': function(e, t){ // try to intercept the easy way
            	if(t.href.indexOf('spase://', 0) == 0) {	// Starts with "spase://"
	           		part = t.href.split('spase://');
            		part[1] = part[1].replace("')", '');
            		n = part[1].lastIndexOf('/');
            		tabName = part[1];
            		if(n != -1) tabName = part[1].substring(n+1);
                  ExplorerApp.getView().openTab(tabName, part[1] + ".xml");
            	} else {
            		window.open(t.href);
            	}
            },
            'click': function(e, t){ // if they tab + enter a link, need to do it old fashioned way
                if(String(t.target).toLowerCase() != '_blank'){
                    e.stopEvent();
                    window.open(t.href);
                }
            },
            delegate: 'a'
        });
    }
};
