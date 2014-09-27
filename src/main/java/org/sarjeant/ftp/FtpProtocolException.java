package org.sarjeant.ftp;

/**
 * Protocol error occured with FTP. This includes addition
 * information about the original request, such as the
 * response string and the error code.
 *
 * @author  Eric W. Sarjeant &lt;eric@sarjeant.com&gt;
 **/
public class FtpProtocolException extends Exception {

    private FtpReply rep = null;

    /**
     * Create a protocol exception in FTP communication.
     *
     * @param r  Parsed response from the FTP server.
     **/
    public FtpProtocolException(FtpReply r) {
        super("Ftp Error Code: " + r.getResponseCode() + "\n" + r.getResponseString());
        rep = r;
    }

    public int getErrorCode() {
        return rep.getResponseCode();
    }

    public String getErrorString() {
        return rep.getResponseString();
    }

}
