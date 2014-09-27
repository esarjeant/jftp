package org.sarjeant.ftp;

/**
 * General FTP Input/Output error. Occurs when there is a problem
 * transmitting data and/or establishing a connection.<p>
 *
 * @author  Eric W. Sarjeant &lt;eric@sarjeant.com&gt;
 **/
public class FtpIOException extends Exception {

    public FtpIOException(Exception e) {
        super(e);
    }

    public FtpIOException(String e) {
        super(e);
    }

}
