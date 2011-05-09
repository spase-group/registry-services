//
// Extend the XmlTreeLoader to set some custom features
//
Ext.app.ExplorerTreeLoader = Ext.extend(Ext.ux.XmlTreeLoader, {
    processAttributes : function(attr){
         // Set the node text that will show in the tree 
         attr.text = attr.name;
       //  alert('pattern: ' + attr.pattern);
         attr.pattern = attr.id;
         
         attr.iconCls = attr.tagName;
         attr.cls = 'item';
         
         // Indicate that the tree is fully loaded.
         // Otherwise each node will be loaded asynchronously
         attr.loaded = false;

         // Expand the node
         attr.expanded = false;
    }
});

ExplorerTree = function() {

    this.loadMessage = new Ext.LoadMask(Ext.getBody(), {msg:"Loading..."});

    this.treeLoader = new Ext.app.ExplorerTreeLoader({
               dataUrl: ExplorerResolver
        });
    this.treeLoader.on('beforeload', function(treeLoader, node) {
        this.treeLoader.baseParams.t = "yes";	// Tree listing
		  if(node.attributes.pattern === null || node.attributes.pattern === '-') {
			} else {	// Each "baseParams" is passed as a parameter in the URL.
        		this.treeLoader.baseParams.i = node.attributes.pattern;
      	}
    }, this);
    
   ExplorerTree.superclass.constructor.call(this, {
        id: 'tree-panel',
        region: 'west',
        title: 'Resource',
        split: true,
        width: 165,
        minSize: 150,
        maxSize: 400,
        collapsible: true,
        margins: '0 0 5 5',
        cmargins: '0 5 5 5',
        autoScroll: true,
        collapseFirst: false,
        rootVisible: true,
        root: new Ext.tree.AsyncTreeNode(),
        loader: this.treeLoader,
        scope: this
    });

    this.getSelectionModel().on({
             'selectionchange': function(model, node){
                    if(node.isLoaded !== undefined && ! node.isLoaded() && ! node.leaf) { node.expand(); }
                    if(node.leaf) {
                       ExplorerApp.getView().openTab(node.attributes.name, node.attributes.pattern);
                     } else {
                       node.getOwnerTree().expandPath(node.getPath());
                     }
                  },
                  scope: this
               });
};

Ext.extend(ExplorerTree, Ext.tree.TreePanel, {
});
