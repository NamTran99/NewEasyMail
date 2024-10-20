package com.hungbang.email2018.f.c;


import java.io.Serializable;

import javax.mail.URLName;


/**
 * Created by Anh Nguyen on 2/23/2018.
 */

public class EmailFolder implements Serializable {
    public String displayName;
    public String parentName;
    public String apiName;
    public URLName urlName;
    public String[] attrs;
    public int folderType;

    public EmailFolder() {
    }

    public EmailFolder(String displayName, String parentName, String apiName, URLName urlName, String[] attrs, int folderType) {
        this.displayName = displayName;
        this.parentName = parentName;
        this.apiName = apiName;
        this.urlName = urlName;
        this.attrs = attrs;
        this.folderType = folderType;
    }
}
