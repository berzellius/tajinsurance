package com.tajinsurance.utils;

import java.io.IOException;
import java.util.Date;

/**
 * Created by berz on 27.03.14.
 */
public interface CodeUtils {

    static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    String getDigitCode(int c);

    Integer getDigitsForMonth(Date date);

    String getUploadsDirPath() throws IOException;

    String getUploadImagesDirPath() throws IOException;

    String getProjectLink(String link) throws IOException;

    boolean checkEmailIsValid(String email);
}
