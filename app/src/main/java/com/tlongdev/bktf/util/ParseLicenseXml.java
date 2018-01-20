package com.tlongdev.bktf.util;

import android.content.res.XmlResourceParser;

import com.tlongdev.bktf.model.License;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseLicenseXml {

    private static final String TAG_ROOT = "licenses";
    private static final String TAG_CHILD = "license";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_URL = "url";

    public static List<License> Parse(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        List<License> licenses = new ArrayList<>();
        int event = parser.getEventType();

        String name = null;
        String url = null;
        String license = null;

        while (event != XmlResourceParser.END_DOCUMENT) {
            if (event == XmlResourceParser.START_TAG) {
                if (!parser.getName().equals(TAG_ROOT)
                        && !parser.getName().equals(TAG_CHILD))
                    throw new XmlPullParserException(
                            "Error in xml: tag isn't '" + TAG_ROOT + "' or '"
                                    + TAG_CHILD + "' at line:"
                                    + parser.getLineNumber());
                name = parser.getAttributeValue(null, ATTR_NAME);
                url = parser.getAttributeValue(null, ATTR_URL);
            } else if (event == XmlResourceParser.TEXT) {
                license = parser.getText();
            } else if (event == XmlResourceParser.END_TAG) {
                if (name != null && url != null && license != null
                        && !parser.getName().equals(TAG_ROOT)) {

                    licenses.add(new License(name.trim(), url.trim(), license.trim()));

                } else if (name == null) {
                    throw new XmlPullParserException(
                            "Error in xml: doesn't contain a 'name' at line:"
                                    + parser.getLineNumber());
                } else if (url == null) {
                    throw new XmlPullParserException(
                            "Error in xml: doesn't contain a 'type' at line:"
                                    + parser.getLineNumber());
                } else if (license == null) {
                    throw new XmlPullParserException(
                            "Error in xml: doesn't contain a 'license text' at line:"
                                    + parser.getLineNumber());
                }
            }
            event = parser.next();
        }
        parser.close();
        return licenses;
    }
}