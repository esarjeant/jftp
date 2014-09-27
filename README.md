Java FTP Client
===============

AUTHOR: Eric W. Sarjeant <eric@sarjeant.com>  
DATE:   November 2003


Introduction
------------
This is an implementation of a primitive FTP client described in
RFC 959:

      <http://www.w3.org/Protocols/rfc959/7_Scenario.html>

Most transfer types are supported here, but this is by no means a
complete library.

The purpose of this is to augment the URL-based handlers of Sun's
JDK with a more fundamental ftp client approach. Currently the client
persay is rather incomplete, this is principally a library suitable
for development purposes only.

If you have any bugs with this, report them to me via email at
<eric@sarjeant.com> or check my webpage <http://micromux.com> for
any newer contact info.

The latest version of this can also be downloaded from my
homepage.

Using with Maven
----------------
If you are looking to quickly integrate this with a maven build:

>  &lt;dependency&gt;  
>    &nbsp;&nbsp;&lt;groupId>org.sarjeant</groupId&gt;  
>    &nbsp;&nbsp;&lt;artifactId>jftp</artifactId&gt;  
>    &nbsp;&nbsp;&lt;version>1.1-SNAPSHOT</version&gt;  
>    &nbsp;&nbsp;&lt;scope>compile</scope&gt;  
>  &lt;/dependency&gt;  

