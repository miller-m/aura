/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
({		
	//number of milliseconds to wait before navigating to the next page with arrow key
	KEY_PAGE_SELECTION_TIMEOUT_DURATION: 200,
	//number of pixels the scroller has moved before handling the scrollMove event
	SCROLL_START_THRESHOLD : 10,
	//indicates only the selected page is visible or not
    SHOW_SELECTED_PAGE_ONLY : true,
    //navContainer height, hardcode for now so it does require updating the size dynamically which causes reflow
    NAV_CONTAINER_HEIGHT : 58,
   	
	init : function(cmp) {		
		this.initSize(cmp);
		this.initPages(cmp);		
	},
				
	initSize : function(cmp) {
		var width = cmp.get('v.width'),
			height = cmp.get('v.height'),
			pageStyle = carouselStyle = width ? ('width:' + width + 'px;') : '';			
		
		cmp._width = width;
		cmp._height = height;
				
		if (height) {			
			carouselStyle += 'height:' + height + 'px;';
			pageStyle += 'height:' + (height - this.NAV_CONTAINER_HEIGHT) + 'px';
		}
		
		cmp.getAttributes().setValue('priv_carouselStyle', carouselStyle);
		cmp.getAttributes().setValue('priv_pageStyle', pageStyle);
	},
	
	initScroller: function(cmp) {		
		var pageCmps = this.getPageComponents(cmp);			
 		 
		if (pageCmps && pageCmps.length > 0) {				
			 
			var snap = this.getSnap(cmp);
			
			if (snap) {					
				cmp.getValue('v.priv_snap').setValue(snap);
			}		 

			if (cmp._width) {
				//set scroller width
				var totalWidth = cmp._width * pageCmps.length;
				cmp.getAttributes().setValue('priv_scrollerWidth',	totalWidth + 'px');
			}	
		}
	},
	
	initPages: function(cmp) {		
		var pageModels = this.getPageModels(cmp),
			pageCmps = this.getPageComponents(cmp),			
			isContinuousFlow = cmp.get('v.continuousFlow'), 
			isVisible = isContinuousFlow || !this.SHOW_SELECTED_PAGE_ONLY,
			page, snap = this.getSnap(cmp),	
			pageHeight = this.getPageSize(cmp).height,
			pages = [];	
		
		//reset current page;
		cmp.getValue('v.priv_currentPage').setValue(-1);
				
		if (pageCmps && pageCmps.length > 0) {
			//TODO: need a better solution to handle iteration inside the pageComponents
			if (pageCmps[0].isInstanceOf('aura:iteration')) {				
				//pageCmps = pageCmps[0].get('v.realBody');
				pageCmps = this.getPageComponentsFromIteration(pageCmps[0]); 				
			}
			
			for ( var i = 0; i < pageCmps.length; i++) {
				page = pageCmps[i];
				//append page components to page container body
				if ($A.util.isComponent(page) && page.isInstanceOf("ui:carouselPage")) {
					//page index starts with 1
					page.getValue('v.pageIndex').setValue(i + 1);
					page.getValue('v.parent').setValue([cmp]);
					page.getValue('v.priv_width').setValue(cmp._width);
					page.getValue('v.priv_height').setValue(pageHeight);
					page.getValue('v.priv_visible').setValue(isVisible);
					page.getValue('v.priv_snap').setValue(snap);					
					page.getValue('v.priv_continuousFlow').setValue(isContinuousFlow);
					pages.push(page);
				}
			}
			cmp.getValue('v.pageComponents').setValue(pages, true);
		} else if (pageModels.length > 0) {
			for ( var i = 0; i < pageModels.length; i++) {
				page = pageModels[i];
				//create new instance of carousePage and pass pageModel to it
				 var component=$A.componentService.newComponentDeprecated({
			            componentDef:{descriptor: 'markup://ui:carouselPage'},
			            //page index starts with 1
			            attributes:{values: {
			            	'priv_visible' : isVisible, 
			            	'pageModel' : page, 
			            	'pageIndex' : i + 1,
			            	'parent' : [cmp],
			            	'priv_snap' : snap,
			            	'priv_width' : cmp._width,
			            	'priv_height' : pageHeight,
			            	'priv_continuousFlow' : isContinuousFlow}}
			        },null,true);
				 pages.push(component);
			}			
			cmp.getValue('v.pageComponents').setValue(pages, true);
		}
		
		this.initPageIndicator(cmp);
		this.initScroller(cmp);
	},	
	
	initPageIndicator : function(cmp) {		
		var indCmp = this.getPageIndicatorsComponent(cmp);
		if (indCmp) {		
			indCmp.getValue('v.pageComponents').setValue(cmp.getValue('v.pageComponents'));
			indCmp.addHandler('pagerClicked', cmp, 'c.pagerClicked');
			indCmp.addHandler('pagerKeyed', cmp, 'c.pagerKeyed');			 
		}		 
	},
	
	getPageComponentsFromIteration : function(iterCmp) {
		var realBody = iterCmp.get('v.realBody'),
			pageCmps = [];
		
		for (var i=0; i< realBody.length; i++) {
			if (realBody[i].isInstanceOf('aura:iteration')) {
				pageCmps = pageCmps.concat(this.getPageComponentsFromIteration(realBody[i])); 
			} else if (realBody[i].isInstanceOf("ui:carouselPage")) {
				pageCmps.push(realBody[i]);
			}		
		}
		
		return pageCmps;
	},
		
	/**
	 * Handle window resize event
	 * This event is always fired after the carousel is rendered
	 */
	refresh: function(cmp, evt) {
		if (cmp.isRendered()) {		    	
			this.updateSize(cmp);
	    } 
	},
	
	/**
	 * Update carousel and page size if carousel width is not pre-defined
	 */
	updateSize: function(cmp) {		
		var origWidth = cmp.get('v.width'),
			origHeight = cmp.get('v.height');
	
		var pages = this.getPageComponents(cmp);
		//need to update size width if carousel width and height is not explicitly set
		if (pages.length > 0 && !(origWidth  && origHeight)) {
			var carouselSize = this.getCarouselSize(cmp, origWidth, origHeight);
			 	
			this.updateCarouselSize(cmp, pages, carouselSize);
			this.updatePageSize(cmp, pages, origWidth, origHeight, carouselSize);			
		}
	},
	
	updateCarouselSize: function(cmp, pages, carouselSize) {	
		cmp.getValue('v.priv_carouselStyle').setValue(this._getStyleString(carouselSize.width, carouselSize.height));		
		cmp.getValue('v.priv_scrollerWidth').setValue(carouselSize.width * pages.length + 'px'); 
	},
	
	updatePageSize: function(cmp, pages, origWidth, origHeight, carouselSize) {
		var height,
			width;
		
		 
		var navContainerHeight,
			navContainer = cmp.find('navContainer');
		
		if (navContainer) {
			navContainerHeight = navContainer.getElement().offsetHeight;
		}					
		height = carouselSize.height - navContainerHeight;
		 
		cmp.getValue('v.priv_pageStyle').setValue(this._getStyleString(carouselSize.width, height));
		
		if (!origWidth) {
			for (var i=0; i< pages.length; i++) {
				var e = pages[i].get('e.updateSize');
				//page width always same as carousel width	
				e.setParams({pageSize: {width:carouselSize.width, height:height}})
				e.fire(); 
			}
		} 
	},
	
	_getStyleString: function(width, height) {
		var style = width ? 'width:' + width + 'px;' : '';
			style += height ? 'height:' + height + 'px;' : '';
		
		return style ? style : null;		
	},
	
	getCarouselSize: function(cmp, origWidth, origHeight) {		 		
		var width, 
			height,
			navContainerHeight = 0,			 
			windowSize = $A.util.getWindowSize(),
			el = cmp.getElement();
		
		var navContainer = cmp.find('navContainer');
		if (navContainer) {
			navContainerHeight = navContainer.getElement().offsetHeight;
		}		
		
		width = origWidth ? origWidth : el ? el.offsetWidth : windowSize.width;
		height = origHeight ? this.getPageSize(cmp).height + navContainerHeight : el ? el.offsetHeight : windowSize.height;			
		
		return {width: width, height: height};
	},
	
	getPageSize: function(cmp) {
		var width = cmp.get('v.width'),
			height = cmp.get('v.height');
			
		if (height) {
			height = height - this.NAV_CONTAINER_HEIGHT;
		} else {
			var el = cmp.find('pageContainer').getElement();
			if (el) {
				height = el.offsetHeight;
			}
		}
		 
		return {width: width, height: height};
	},
		 
	
	/**
	 * Update page content
	 */
	updatePage: function(cmp, pageIndex, pageContentCmp) {
		var pageCmp = this.getPageComponentFromIndex(cmp, pageIndex);
		var e = pageCmp.get('e.update');
		e.setParams({pageComponent: pageContentCmp});
		e.fire();		
	},
	
	handlePagerClicked : function(cmp, pageIndex) {
		this.selectPage(cmp, pageIndex);
	},
	
	/**
	 * Handle carousel indicator key events
	 */	
	handlePagerKeyed : function(cmp, evt) {
		var keyCode = evt.getParam("event").keyCode;
        // left arrow or right arrow
        if (keyCode === 37 || keyCode == 39) {
            var pageComps = this.getPageComponents(cmp),
            	prevPage = evt.getParam("pageIndex"),
            	pageIndex = prevPage;
            
            if (keyCode === 37 && pageIndex > 0) {  // left arrow
            	pageIndex--;
            }
            if (keyCode === 39 && pageIndex < pageComps.length) {  // right arrow
            	pageIndex++;
            }
            
            if (cmp._keyPageSelectionTimeout != null) {
    			window.clearTimeout(cmp._keyPageSelectionTimeout);
    			cmp._keyPageSelectionTimeout = null;
    		}	 
    	 
    		// When coming from a key event, wait a second to commit to the page
    		// selection
            var me = this;
    		cmp._keyPageSelectionTimeout = window.setTimeout(function() {
    			cmp._keyPageSelectionTimeout = null;    			
    			me.selectPage(cmp, pageIndex);    			 
    		}, this.KEY_PAGE_SELECTION_TIMEOUT_DURATION);
    		
            if (evt.preventDefault) evt.preventDefault();
        }
	},
	
	/**
	 * Handle scrollStart event 
	 */
	handleScrollMove: function(cmp, evt) {
		if (!this.SHOW_SELECTED_PAGE_ONLY) {
			return;
		}
					
		var scroller = this.getScroller(cmp),
			nextPage,			
			prevSelectedPage = cmp.get('v.priv_currentPage');
		
		
		if (evt.isEventHandledByChildCarousel && (scroller.dirX == 1 || scroller.dirX == -1)) {			
			//nested scroller and swiping horizontal
			scroller.disable();
			delete evt.isEventHandledByChildCarousel;
		} else	if (scroller.absDistX > this.SCROLL_START_THRESHOLD && !cmp._isScrollStartProcessed) {
			
			if (scroller.dirX == 1) {
				//scrolling to right
				//scroller page starts with 0;
				nextPage = scroller.currPageX + 2;
			} else if (scroller.dirX == -1) {
				//scrolling to the left				
				nextPage = scroller.currPageX;
			}
					
			cmp._isScrollStartProcessed = true;
			var pages = this.getPageComponents(cmp);			
			if (nextPage > 0 && nextPage <= pages.length) {			
				this.showPage(this.getPageComponentFromIndex(cmp, nextPage), nextPage);
			}
		}
		
		if (!evt.isEventHandledByChildCarousel) {
			evt.isEventHandledByChildCarousel = true;
		}
	},
	
	/**
	 * Handle scroll event 
	 */
	handleScrollEnd: function(cmp, evt) {		
		var scroller = this.getScroller(cmp),
			//scroller page starts with 0
			currentPageX = scroller.currPageX + 1,			
			prevSelectedPage = cmp.get('v.priv_currentPage');
				
		cmp._isScrollStartProcessed = false;

		if (prevSelectedPage == currentPageX) {
			//scrolled back to the same page			 
			return;
		}		
		
		if (cmp.get('v.fireSlideChangedEvent')) {
			$A.getEvt("ui:slideChangedEvent").fire();
		}
		
		this.pageSelected(cmp, currentPageX);	 
	},
	
	showPage: function(pageCmp, pageIndex){
		var e = pageCmp.get('e.show');
		e.setParams({'pageIndex' : pageIndex});
		e.fire();		
	},
	
	hidePage: function(pageCmp, pageIndex) {		 
		var e = pageCmp.get('e.hide');
		e.setParams({'pageIndex' : pageIndex});
		e.fire();
	},
	
	/**
	 * Page is selected, delegate the event to page component
	 */
	pageSelected: function(cmp, pageIndex) {

		var prevSelectedPage = cmp.get('v.priv_currentPage');
			
		if (prevSelectedPage == pageIndex) {			
			return;
		}

		var curPageCmp = this.getPageComponentFromIndex(cmp, pageIndex);
		if (curPageCmp && curPageCmp.isRendered()) {
			var prePageCmp = this.getPageComponentFromIndex(cmp, prevSelectedPage);

			cmp.getAttributes().setValue('priv_currentPage', pageIndex);			
			this.firePageSelectedEventToPage(prePageCmp, pageIndex);
			this.firePageSelectedEventToPage(curPageCmp, pageIndex);			
			this.firePageSelectedEventToPageIndicator(cmp, curPageCmp, pageIndex);
			
			this.hideAllUnselectedPages(cmp, pageIndex);
		}
	},
	
	showAllPages: function(cmp) {	
		var pages = this.getPageComponents(cmp);
		for (var i=1; i<= pages.length; i++) {
			this.showPage(pages[i-1], i);
		}
	},
	
	hideAllUnselectedPages: function(cmp, selectPage) {
		if (cmp.get('v.continuousFlow')) {
			return;
		}
		
		var pages = this.getPageComponents(cmp);
		for (var i=1; i<= pages.length; i++) {			
			if (i != selectPage) {
				this.hidePage(pages[i-1], i);
			}
		}
	},
	/**
	 * Fire pageSelected event to page component
	 */	
	firePageSelectedEventToPage: function(pageCmp, selectedPage) {
		if (pageCmp) {
			var e = pageCmp.get('e.pageSelected');
			e.setParams({pageIndex : selectedPage});
			e.fire();		
		}
	},
		 
	firePageSelectedEventToPageIndicator: function(carouselCmp, pageCmp, selectedPage) {
		var pageIndicator = this.getPageIndicatorsComponent(carouselCmp);

		if (pageIndicator && pageIndicator.isRendered()) {			 
			var pageId = pageCmp.getElement().id,
				e = pageIndicator.get('e.pageSelected');
		
			e.setParams({pageIndex : selectedPage, pageId: pageId});
			e.fire();		 			
		}
	},
 
	/**
	 * Selecting a page from non-scrolling events
	 */
	selectPage : function(cmp, pageIndex, time) {		
		var pages = this.getPageComponents(cmp),
			prevSelectedPage = cmp.get('v.priv_currentPage');

		if (pageIndex > 0 && pageIndex <= pages.length && prevSelectedPage !== pageIndex) {

			this.showAllPages(cmp);		 
			
			scroller = this.getScroller(cmp);
			//scroller page starts with 0
			scroller.scrollToPage(--pageIndex, null, time);			
		}		
	},
	
	selectDefaultPage : function(cmp) {
		var curPage = cmp.get('v.priv_currentPage');
		
		if (curPage > -1) {
			//page already selected;
			return;
		}
		
		var	pageCmps = this.getPageComponents(cmp),
			defaultPage = cmp.get('v.defaultPage'),
			pageToSelect = 1;	
				 
		if (defaultPage) {
			pageToSelect = defaultPage;
		} else {
		    for (var i = 0; i < pageCmps.length; i++) {
		        if (pageCmps[i].get('v.isDefault')) {
		        	//page starts at 1
		        	pageToSelect = i + 1;
		        }
		    }	        
		}
		this.selectPage(cmp, pageToSelect, 0); 
	},
		 
		
	getPageComponents:function(cmp) {
		return cmp.get('v.pageComponents') || [];
	},
	
	getPageModels:function(cmp) {
		return cmp.get('v.pageModels');
	},
	
	getPageModelFromIndex: function(cmp, pageIndex) {
		var pageModels = this.getPageModels(cmp);
		//page start from 1
		pageIndex--;

		return pageModels ? pageModels[pageIndex] : null; 
	},
	
	getPageComponentFromIndex: function(cmp, pageIndex) {
		var pages = this.getPageComponents(cmp);
		//page start from 1
		pageIndex--;
		if (pages && pageIndex >=0 && pageIndex < pages.length) {
			return pages[pageIndex];
		}
		
		return null;
	},
	
	getPageIndicatorsComponent : function(cmp) {
		var navContainer = cmp.find('navContainer');
		var indicators = navContainer ? navContainer.get('v.body') : null;
		
		return cmp.get('v.continuousFlow') != true && indicators ? indicators[0] : null;
	},
	 
	getScroller : function(cmp) {		
		return cmp.find('scroller')._scroller;		
	},
	
	getSnap : function(cmp) {
		var id = cmp.getGlobalId().replace('.', '_').replace(':', '-');
		return cmp.get('v.continuousFlow') != true ? 'section.snap-class-' + id + '' : null;
	}
	
})
