package com.tajinsurance.utils;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
/**
 * Created by berz on 27.03.14.
 */
public class CodeUtilsImpl implements CodeUtils {

    public CodeUtilsImpl() {
        try {
            this.tiasProperties = PropertiesLoaderUtils.loadAllProperties("tias.properties");
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private Properties tiasProperties = null;

    private String getProperty(Properties properties, String name) throws IOException {
        if(properties == null) throw new IOException("properties class " + properties.toString() + " has not been loaded");

        return properties.getProperty(name);
    }

    @Override
    public String getDigitCode(int c) {
        String code = "";
        for(int i = 0; i < c; i++){
            int d = (int) (Math.random()*10);
            code += Integer.valueOf(d).toString();
        }
        return code;
    }

    @Override
    public Integer getDigitsForMonth(Date date) {
        if(date == null) date = new Date();

        Calendar cDate = Calendar.getInstance();
        cDate.setTime(date);

        return cDate.get(Calendar.YEAR)*100 + cDate.get(Calendar.MONTH) + 1;
    }

    @Override
    public String getUploadsDirPath() throws IOException {

        return new File(
                this.getProperty(this.tiasProperties, "tias.path_to_upload_dir")
        ).getAbsolutePath()
                + "/";
    }

    @Override
    public String getUploadImagesDirPath() throws IOException {
        return new File(
                this.getProperty(this.tiasProperties, "tias.path_to_upload_images")
        ).getAbsolutePath()
                + "/";
    }

    @Override
    public String getProjectLink(String link) throws IOException {
        return this.getProperty(this.tiasProperties, "tias.url") + link;
    }

    @Override
    public boolean checkEmailIsValid(String email) {
        return email.matches(EMAIL_PATTERN);
    }
}
