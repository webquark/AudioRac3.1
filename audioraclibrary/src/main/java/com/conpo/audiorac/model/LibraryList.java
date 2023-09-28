package com.conpo.audiorac.model;

import java.util.ArrayList;

public class LibraryList extends ArrayList<Library> {

    public LibraryList() {
    }

    public ArrayList<Record> toRecordList() {

        ArrayList<Record> arr = new ArrayList<>();

        for (Library lib : this) {
            Record rec = new Record();
            rec.put("name", lib.lib_name);
            rec.put("url", lib.url);
            rec.put("use_name", lib.use_name);
            rec.put("use_login", lib.use_login);
            rec.put("app_type", lib.app_type);

            arr.add(rec);
        }

        return arr;
    }
}
