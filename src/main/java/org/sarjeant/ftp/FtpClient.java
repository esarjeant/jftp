package org.sarjeant.ftp;

import java.net.*;
import java.io.*;

import java.util.StringTokenizer;
import java.lang.StringBuffer;

/**
 * <p>Implementation of an FTP client. This follows the
 * <a href="http://www.w3.org/Protocols/rfc959/7_Scenario.html">RFC959</a>
 * specification.</p>
 *
 * @author  Eric W. Sarjeant &lt;eric@sarjeant.com&gt;
 **/
public class FtpClient {

    private Socket sock = null;          // connect socket
    private Socket sockData = null;      // data channel socket
    private OutputStream sockOut = null;
    private InputStream sockIn = null;
    private OutputStream dataOut = null;
    private InputStream dataIn = null;

    private static final int PORT_FTP = 21;
    private static String EOL = "\r\n";

    private FtpReply reply = new FtpReply();

    /**
     * Create a new ftp client instance. Provide the location of an
     * ftp server.<p>
     *
     * @param host   The name/ip of remote host.
     **/
    public FtpClient(String host) throws FtpIOException {
        connect(host, PORT_FTP);
    }

    public FtpClient(String host, int portnum) throws FtpIOException {
        connect(host, portnum);
    }

    private void connect(String host, int portnum) throws FtpIOException {

        try {

            sock = new Socket(host, portnum);

            sockOut = sock.getOutputStream();
            sockIn = sock.getInputStream();

            readResponse();

        } catch (IOException io) {
            throw new FtpIOException(io);
        }

    }

    /**
     * Write just a command (eg: NOOP) that does not require any
     * parameters.<p>
     *
     * @param cmd   The command to execute
     * @throws FtpIOException Input/Output error during write.
     **/
    private void writeCommand(String cmd) throws FtpIOException {
        writeCommand(cmd, null);
    }

    /**
     * Read the contents of the data channel.
     **/
    private synchronized void readDataChannel(String file) throws FtpIOException {

        FileOutputStream fileOut = null;
        int c = 0;

        try {

            BufferedReader bufin = new BufferedReader(new InputStreamReader(dataIn));
            fileOut = new FileOutputStream(file, false);

            while ((c = bufin.read()) != -1) {
                fileOut.write(c);
            }

            bufin.close();
            fileOut.close();

        } catch (IOException io) {
            throw new FtpIOException(io);
        }

    }

    /**
     * Write the contents of a file to the data channel.
     **/
    private synchronized void writeDataChannel(String file) throws FtpIOException {

        FileInputStream fileIn = null;
        int c = 0;

        try {

            OutputStreamWriter out = new OutputStreamWriter(dataOut);
            fileIn = new FileInputStream(file);

            while ((c = fileIn.read()) != -1) {
                out.write(c);
            }

            out.close();
            fileIn.close();

        } catch (IOException io) {
            throw new FtpIOException(io);
        }

    }

    /**
     * Send a command to the primary socket. This takes the command and the
     * data associated with the command, and proceeds to send it over the
     * open socket.<p>
     *
     * @param cmd   The FTP command.
     * @param data  Payload for the command.
     * @return Result is {@code true} if the command succeeds.
     * @throws FtpIOException Input/Output error during write.
     **/
    private boolean writeCommand(String cmd, String data) throws FtpIOException {

       // always start with the command
        StringBuffer msg = new StringBuffer(cmd.toUpperCase());
        int c = 0;

        if (null == sockOut) {
            throw new FtpIOException("Socket not connected");
        }

        // optionally add data part of message
        if (data != null) {
            msg.append(" ");
            msg.append(data);
        }

        msg.append(EOL);   // terminate message

        try {

            synchronized (sockOut) {

                System.out.print("+");

                // send the request
                for (int i = 0; i < msg.length(); i++) {
                    System.out.print((char)msg.charAt(i));
                    sockOut.write(msg.charAt(i));
                }

                sockOut.flush();
                System.out.print("\n");

            }

        } catch (IOException io) {
            throw new FtpIOException(io);
        }

        // start reading the response
        return readResponse();

    }


    /**
     * Read the incoming response. Keep reading until the properly
     * formed message is received. This is of the form
     * &quot;### (data...)&quot;.<p>
     *
     * @return Result is {@code true} if response succeeded or partially succeeded.
     * @throws FtpIOException Input/Output error during read.
     **/
    private boolean readResponse() throws FtpIOException {

        try {

            BufferedReader bufin = new BufferedReader(new InputStreamReader(sockIn));
            String line;

            while ((line = bufin.readLine()) != null) {

                if (reply.isReply(line)) {
                    System.out.println("-" + reply);
                    return (!line.startsWith("5"));
                }

            }

            bufin.close();

        } catch (IOException io) {
            throw new FtpIOException(io);
        }

        return false;

    }

    /**
     * Authenticate the user.
     */
    public boolean logon(String username, String password) throws FtpIOException, FtpProtocolException {
        return (writeCommand("USER", username) && writeCommand("PASS", password));
    }

    /**
     * Switch to another directory.
     **/
    public void cd(String dir) throws FtpIOException, FtpProtocolException {
        writeCommand("CWD", dir);
    }

    /**
     * Change to parent directory.
     **/
    public void cdup() throws FtpIOException, FtpProtocolException {
        writeCommand("CDUP");
    }

    /**
     * Create a new directory.
     **/
    public void mkdir(String dir) throws FtpIOException, FtpProtocolException {
        writeCommand("MKD", dir);
    }

    /**
     * Structure mount.
     **/
    public void structureMount(String dir) throws FtpIOException, FtpProtocolException {
        writeCommand("SMNT", dir);
    }

    /**
     * Remove a directory.
     **/
    public void rmdir(String dir) throws FtpIOException, FtpProtocolException {
        writeCommand("RMD", dir);
    }

    /**
     * Display current directory.
     **/
    public void pwd() throws FtpIOException, FtpProtocolException {
        writeCommand("PWD");
    }

    /**
     * Initiate passive mode FTP file transfer. This call automatically
     * sets up the internal channel for receiving data.<p>
     **/
    public void passive() throws FtpIOException, FtpProtocolException {

        String ipaddr = new String();   // ip address to connect
        //String rmtPortStr = null;       // remote port number
        int rmtPort = 0;                // remote port number (numeric)

        // start passive-mode
        writeCommand("PASV");

        // find last location of "("
        String resp = reply.getResponseString();
        int openSlash = (resp.lastIndexOf('(') + 1);
        int closeSlash = resp.lastIndexOf(')');
        String scon = resp.substring(openSlash, closeSlash);
        int eid = 0;

        StringTokenizer st = new StringTokenizer(scon, ",");
        while (st.hasMoreElements()) {

            if (eid < 4) {
                ipaddr = ipaddr + (String)st.nextElement();
            }

            if (eid < 3) {
                ipaddr = ipaddr + ".";
            }

            if (eid == 4) {
                rmtPort = Integer.parseInt((String)st.nextElement());
            }

            if (eid == 5) {
                rmtPort = (rmtPort * 256) + Integer.parseInt((String)st.nextElement());
            }

            eid++;

        }

        // open the listener
        try {
            sockData = new Socket(ipaddr, rmtPort);
            dataOut = sockData.getOutputStream();
            dataIn = sockData.getInputStream();
        } catch (UnknownHostException uhe) {
            throw new FtpIOException(uhe);
        } catch (IOException io) {
            throw new FtpIOException(io);
        }

    }

    /**
     * Use binary transfer mode.
     **/
    public void binary() throws FtpIOException, FtpProtocolException {
        writeCommand("TYPE", "I");
    }

    /**
     * Use ascii (text) transfer mode.
     **/
    public void ascii() throws FtpIOException, FtpProtocolException {
        writeCommand("TYPE", "A");
    }


    /**
     * Download a file.
     **/
    public void get(String file) throws FtpIOException, FtpProtocolException {

        writeCommand("RETR", file);

        // open file for output
        readDataChannel(file);

    }

    /**
     * Upload a file.
     **/
    public void put(String file) throws FtpIOException, FtpProtocolException {

        writeCommand("STOR", file);

        // open file for output
        writeDataChannel(file);

    }

    /**
     * No operation - server should always return success/
     **/
    public void noop() throws FtpIOException, FtpProtocolException {
        writeCommand("NOOP");
    }

    /**
     * Current user is terminated; file transfers that are active
     * will be able to complete.
     **/
    public void reinitialize() throws FtpIOException, FtpProtocolException {
        writeCommand("REIN");
    }

    /**
     * Exit session; close and get out.
     **/
    public void quit() throws FtpIOException, FtpProtocolException  {

        writeCommand("QUIT");

        try {
            disconnect();
        } catch (IOException io) {
            throw new FtpIOException(io);
        }

    }

    /**
     * Obtain a handle to the current reply object. This includes info
     * about any response code / strings that have been received.
     **/
    public synchronized FtpReply getFtpReply() {
        return reply;
    }

    /**
     * Close the connection and exist the application. This is called
     * from a QUIT operation.<p>
     **/
    private void disconnect() throws IOException {

        if ((sock != null) && (sock.isConnected())) {
            sock.close();
        }

    }

}
