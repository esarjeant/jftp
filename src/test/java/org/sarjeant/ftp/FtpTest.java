package org.sarjeant.ftp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.*;

import java.io.IOException;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Unit test the FtpClient logic. This starts a dummy FTP server use {@link org.mockftpserver.fake.FakeFtpServer}
 * and executes a series of mock file transfer operations.
 *
 * @author   Eric W. Sarjeant &lt;eric@sarjeant.com&gt;
 */
public class FtpTest {

    private static final int FTP_CONTROL_PORT = 21000;

    private FakeFtpServer fakeFtpServer;

    @Before
    public void before() throws Exception {

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(FTP_CONTROL_PORT);
        fakeFtpServer.addUserAccount(new UserAccount("anonymous", "user@foo.com", "/"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/test.bin"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();

    }

    @After
    public void after() throws Exception {
        assertNotNull("Fake FTP Server did not start", fakeFtpServer);
        fakeFtpServer.stop();
    }

    @Test
    public void testFtpDownload() {

        try
        {

            FtpClient ftp = new FtpClient("localhost", FTP_CONTROL_PORT);

            assertTrue("Logon Fails", ftp.logon("anonymous", "user@foo.com"));
            ftp.passive();
            ftp.cd("/data");
            ftp.pwd();

            ftp.binary();
            ftp.get("test.bin");

            ftp.quit();


        } catch (FtpIOException ex) {
            fail(ex.getMessage());
        } catch (FtpProtocolException e) {
            fail(e.getMessage());
        }
    }
}
