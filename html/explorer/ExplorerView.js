ExplorerView = function() {
    ExplorerView.superclass.constructor.call(this, {
           id:'main-tabs',
           activeTab:0,
           region:'center',
           margins:'0 5 5 0',
           resizeTabs:true,
           tabWidth:150,
           minTabWidth: 120,
           enableTabScroll: true,
           plugins: new Ext.ux.TabCloseMenu(),
           items: [{
               id:'main-view',
               layout:'border',
               title:'Overview',
               hideMode:'offsets',
               items: [{
                  id:'item-preview',
                  region: 'center',
                  layout:'fit',
                  border:false,
                  autoScroll:true,
                  autoLoad: ExplorerOverview
                 }]
               }]
         });
}

Ext.extend(ExplorerView, Ext.TabPanel, {
    openTab : function(term, pattern) {
        var id = !term ? Ext.id() : pattern

        var tab;
        if(!(tab = this.getItem(id))) {
            tab = new Ext.Panel({
               id: id,
               cls:'preview single-preview',
               title: term,
               tabTip: term,
               layout: 'anchor',
               closable: true,
               defaults: {bodyStyle: 'padding:15px'},
               listeners: ExplorerApp.LinkInterceptor,
               autoScroll:true,
               border:true,
               term: term,
               pattern: pattern
            });

            tab.on('activate', this.loadInfo, tab);
            this.add(tab);

        }
        this.setActiveTab(tab);
    },

    loadInfo : function() {
       this.load({
            url: ExplorerRender,
            params: {i: this.pattern, s: 'plain'}, // i = resourceID, s = stylesheet (plain)
            discardUrl: false,
            nocache: false,
            timeout: 30,
            scripts: false
         });
    },
    
    loadFromUrl : function() {
       this.load({
            url: this.infoUrl,
            discardUrl: false,
            nocache: false,
            timeout: 30,
            scripts: false
         });
    }
});