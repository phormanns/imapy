var imapy = {
		loadMailbox: function() {
			return function(event) {
				var obj = JSON.parse(fs.ajax.loadSync( { url : "/IMAPy/imap" } ));
				var l = obj.items.length;
				var src = document.getElementById("sources");
				fs.dom.removeChildNodes(src);
				for (var idx = 0; idx < l; ++idx) {
					var itm = obj.items[idx];
					var nunread = itm['nunread'];
					var ntotal = itm['ntotal'];
					var hasnew = 'notnew';
					if (nunread > 0) {
						hasnew = 'hasnew';
					}
					var par = fs.dom.create("p", {'class':'btn'});
					var div = fs.dom.create("div", {'class':hasnew}, itm['title'] + " (" + nunread + "/" + ntotal + ")");
					fs.dom.append(par, div);
					fs.dom.append(src, par);
					fs.dom.addListener(par, "click", imapy.loadFolder(itm['folder']));
				}
			};
		},
		loadFolder: function(fd) {
			var _fd = fd;
			return function(event) {
				var json = fs.ajax.loadSync( { url : "/IMAPy/imap?fd=" + _fd } );
				var o = JSON.parse(json);
				var it = document.getElementById("item");
				fs.dom.removeChildNodes(it);
				var col = document.getElementById("collection");
				fs.dom.removeChildNodes(col);
				for (var idx = o.items.length-1; idx >= 0; --idx) {
					var itm = o.items[idx];
					var par = fs.dom.create("p", {'class':'btn'});
					var div = fs.dom.create("div", {'class':itm['status']}, 
							itm['title'], fs.dom.create("br"), itm['author']);
					fs.dom.append(par, div);
					fs.dom.append(col, par);
					fs.dom.addListener(par, "click", imapy.loadMessage(o.items[idx]['idx'], o.items[idx]['folder']));
				}
			}; 
		},
		loadMessage: function(idx, fd) {
			var _idx = idx;
			var _fd = fd;
			return function(event) {
				var json = fs.ajax.loadSync( { url : "/IMAPy/imap?fd=" + _fd + "&msg=" + _idx } );
				var o = JSON.parse(json);
				var it = document.getElementById("item");
				fs.dom.removeChildNodes(it);
				fs.dom.append(it, fs.dom.create("pre", {'class':'btn'}, fs.str.unescapeHTML(o.content)));
			};
		}
};