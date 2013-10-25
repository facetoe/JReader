package com.facetoe.jreader;

/**
 * Created with IntelliJ IDEA.
 * User: facetoe
 * Date: 20/10/13
 * Time: 10:02 PM
 */
public enum State {
    IDLE,
    ERROR,
    MAKE_CONFIG,
    CHOOSE_DOCS,
    DOWNLOAD_SRC,
    EXTRACT_SOURCE,
    MAKE_PROFILE,
    PARSE_DOCS,
    COMPLETE
}
