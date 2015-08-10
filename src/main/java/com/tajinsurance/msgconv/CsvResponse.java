package com.tajinsurance.msgconv;

import java.util.List;

/**
 * Created by berz on 28.04.14.
 */
public class CsvResponse {

    private final String filename;
    private final List<String[]> records;

    public CsvResponse(List<String[]> records, String filename) {
        this.records = records;
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }
    public List<String[]> getRecords() {
        return records;
    }

}
