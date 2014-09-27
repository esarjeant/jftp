package org.sarjeant.ftp;

import java.lang.Integer;

/**
 * Accept the standard FTP reply message and provide an object
 * oriented view into that response. User can query the response
 * code as a number and/or display actual response data onscreen.
 *
 * @author  Eric W. Sarjeant &lt;eric@sarjeant.com&gt;
 **/
public class FtpReply {

    private boolean isError = true;
    private String rawmsg = new String();
    private int responseCode = 0;
    private String responseStr = new String();

    /**
     * Construct with a well formed response message. This is
     * an empty constructor.<p>
     **/
    public FtpReply() {
        // do nothing
    }

    /**
     * Check a message to see if it is a reply or not. The message
     * should be a response from an FTP server in the valid format.
     * Refer to RFC959 for more information.<p>
     *
     * @param msg  The message to check.
     **/
    public boolean isReply(String msg) {

        boolean isfinal = false;
        String snum = msg.substring(0, 3);

        // save the original message
        rawmsg = msg;

        // check for error
        if (msg.charAt(0) == '2') {
            isError = false;
        }

        // attempt to get a number
        try {
            responseCode = Integer.parseInt(snum);
        } catch (NumberFormatException ne) {
            responseCode = -1;
        }

        // parse the string
        responseStr = msg.substring(3);

        // is this the final reply?
        if ((responseCode > 0) && (msg.charAt(3) == ' ')) {
            isfinal = true;
        }

        return isfinal;

    }

    /**
     * If the response was an error number, then an exeception is
     * thrown.
     **/
    public boolean hasError() {
        return isError;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseString() {
        return responseStr;
    }

    public String toString() {
        return rawmsg;
    }

}