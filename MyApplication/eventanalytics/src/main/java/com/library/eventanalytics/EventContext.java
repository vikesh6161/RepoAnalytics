/**
 @author Vikesh <vikesh6161@gmail.com>
 */

package com.library.eventanalytics;

import android.content.Context;
import java.util.Date;
import java.util.UUID;

public class EventContext {
    Page page;
    String ip;
    Library library;
    Date timezone;

    public EventContext(final Page page, final Context context) {
        this.page = page;
        final Library library = new Library("eventanalytics", "v2.0");
        final Date date = new Date();
        this.library = library;
        this.timezone = date;
        this.ip = UUID.randomUUID().toString(); //unique per installation
    }
}
