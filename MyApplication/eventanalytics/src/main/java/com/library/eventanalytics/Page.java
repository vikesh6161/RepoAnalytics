/**
 @author Vikesh <vikesh6161@gmail.com>
 */
package com.library.eventanalytics;

public class Page
{
    String path;
    String referrer;
    String search;
    String title;
    String url;
    
    public Page(final String path, final String referer, final String search, final String title, final String url) {
        this.path = path;
        this.referrer = referer;
        this.search = search;
        this.title = title;
        this.url = url;
    }
}
