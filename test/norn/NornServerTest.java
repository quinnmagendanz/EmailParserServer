package norn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import org.junit.Test;

public class NornServerTest {
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    /**
     * Test Partitions
     * 
     * Server Init
     *  File (no entry, single entry, multiple entry, single file, multiple file)
     *  No file
     * 
     * HTTP Request
     *  empty URI
     *  invalid character
     *  valid command
     * 
     * Console Request
     *  empty URI
     *  invalid character
     *  valid command
     *  
     * Multiple http requests
     * 
     * Multiple console requests
     * 
     * Http and console requests
     * 
     * 
     */
    
    private static final String URL_PREFIX = "http://localhost:";
    
    private static final String URL_COMMAND = "/eval/";
    
    private static final String LOCALHOST = "127.0.0.1";

    private static final int CONSOLE_PORT = 5100;
    
    private static final int HTTP_PORT = 5021;
    
    private static String executeGet(int port, String command) throws IOException {
          HttpURLConnection connection = null;
          //Create connection
          URL url = new URL(URL_PREFIX + port + URL_COMMAND + command);
          connection = (HttpURLConnection) url.openConnection();
          connection.setRequestMethod("GET");
          connection.setRequestProperty("Content-Type", 
          "text/html");
        
          connection.setRequestProperty("Content-Language", "en-US");  
        
          connection.setUseCaches(false);
          connection.setDoOutput(true);
        
          //Send request
          DataOutputStream wr = new DataOutputStream (
              connection.getOutputStream());
          wr.close();
        
          //Get Response  
          InputStream is = connection.getInputStream();
          BufferedReader rd = new BufferedReader(new InputStreamReader(is));
          // contains anchor tag and addresses
          String response = rd.readLine();
          int breakLoc = response.indexOf("<br>");
          int addressStart = breakLoc + "<br>".length();
          rd.close();
          connection.disconnect();
          return response.substring(addressStart);
      }
    
    @Test(timeout = 5000) // test server creation with single file
    public void testSingleFile() throws InterruptedException{
        Thread testServer = new Thread(() -> {
            String[] args = {"--file", "shortTest.txt", "--httpport", "5021", "--consoleport", "5100"};
            NornServer.main(args);
        });
        testServer.start();
        Thread.sleep(1000); //allow time for server thread to start up
        
        try{
            String command = "short";
            String response = executeGet(5021, command);
            System.out.println(response);
            assertEquals("Expected single loaded response", response, "short@shorter");
        }catch(IOException e){
            e.printStackTrace();
            assertTrue("Checks failed", false);
        }
    }
    
    @Test(timeout = 5000) // test server creation with multiple files
    public void testMultipleFiles() throws InterruptedException{
        Thread testServer = new Thread(() -> {
            String[] args = {"--file", "dependentTest.txt", "testStartFile.txt", "--httpport", "5022", "--consoleport", "5101"};
            NornServer.main(args);
        });
        testServer.start();
        Thread.sleep(1000); //allow time for server thread to start up
        
        try{
            String command = "a";
            String response = executeGet(5022, command);
            assertTrue("Expected proper load from file", response.contains("q@m.mit.edu"));
            assertTrue("Expected proper load from file", response.contains("cam@woo.com"));
            assertTrue("Expected proper load from file", response.length() == 24);
            
            command = "gamma";
            response = executeGet(5022,command);
            assertTrue("Expected proper load from file", response.contains("c@c"));
            assertTrue("Expected proper load from file", response.contains("what@the.hell"));
            assertTrue("Expected proper load from file", response.length() == 18);
    
            command = "newOne";
            response = executeGet(5022,command);
            assertEquals("Expected proper load from file", "cam@woo.com", response);
            
            command = "oldOne";
            response = executeGet(5022,command);
            assertEquals("Expected proper load from file", "q@m.mit.edu", response);
            
            command = "wow";
            response = executeGet(5022,command);
            assertEquals("Expected proper load from file", "cam@woo.com", response);
            
            command = "b";
            response = executeGet(5022,command);
            assertEquals("Expected proper load from file", "c@c", response);
        }catch(IOException e){
            assertTrue("Checks failed", false);
        }
        testServer.interrupt();
    }
    
    @Test(timeout = 1000000) // test multiple connections into a server sequentially
    public void testServerHTTPSingle() throws InterruptedException {
        
        Thread testServer = new Thread(() -> {
            String[] args = {"--httpport", "5023", "--consoleport", "5102"};
            NornServer.main(args);
        }); 
        
        testServer.start();

        Thread.sleep(100); //allow time for server thread to start up
        
        //test single http connection
        testSingleHTTP();
    }
    
    @Test(timeout = 1000000) // test connections into a server
    public void testServerHTTPMultiple() throws InterruptedException {
        
        Thread testServer = new Thread(() -> {
            String[] args = {"--httpport", "5024", "--consoleport", "5103"};
            NornServer.main(args);
        });
        
        testServer.start();

        Thread.sleep(100); //allow time for server thread to start up
        
        //Test multiple simultaneous http connections
        testMultipleHTTP();
    }
    
    @Test(timeout = 100000) // test connections into a server
    public void testServerConsole() throws InterruptedException {
        
        Thread testServer = new Thread(() -> {
            String[] args = {"--httpport", "5025", "--consoleport", "5104"};
            NornServer.main(args);
        }); 
        
        testServer.start();

        Thread.sleep(1000); //allow time for server thread to start up
        
//        //test single http connection
//        testSingleHTTP();
//        
//        //Test multiple simultaneous http connections
//        testMultipleHTTP();
        
        //test single console connection
        testSingleConsole();
        
        //test multiple console connections
        testMultipleConsole();
        
        //test combined console and http connections
        testCombined();
    }
    
    private void testSingleHTTP() throws InterruptedException{

        Thread.sleep(1000); //allow time for server thread to start up
        
        try{
            final String command = "a=b@b";
            final String response = executeGet(5023,command);
            final String expected = "b@b";
            assertEquals("Expected response b@b", expected, response);
            
            final String command2 = "aa=a,";
            final String response2 = executeGet(5023,command2);
            final String expected2 = "b@b";
            assertEquals("Expected response b@b", expected2, response2);
        }catch(IOException e){
            assertTrue("Get failed", false);
        }
    }
    
    private void testMultipleHTTP() throws InterruptedException{

        Thread.sleep(1000); //allow time for server thread to start up
        
        Thread firstHTTP = new Thread(() -> {
            try{
                final String command = "b=(b@b,y@y.com)!y@y.com";
                final String response = executeGet(5024,command);
                final String expected = "b@b";
                assertEquals("Expected response b@b", expected, response);
            }catch(IOException e){
                assertTrue("Get failed", false);
            }
        });
        
        Thread secondHTTP = new Thread(() -> {
            try{
                final String command = "c=c@c,d@d";
                final String response = executeGet(5024,command);
                assertTrue("Expected response c@c, d@d", response.contains("c@c"));
                assertTrue("Expected response c@c, d@d", response.contains("d@d"));
                assertTrue("Expected response c@c, d@d", response.length() == 8);
            }catch(IOException e){
                assertTrue("Get failed", false);
            }
        });
        
        
        firstHTTP.start();
        secondHTTP.start();
        
        Thread.sleep(100); // wait for other threads to work
        
        // thirdHTTP
        try{
            String command = "d=(q@m,w@y,p@y)*(w@y)";
            String response = executeGet(5024,command);
            String expected = "w@y";
            assertEquals("Expected response w@y", expected, response);
            
            command = "b";
            response = executeGet(5024,command);
            expected = "b@b";
            assertEquals("Expected response b@b", expected, response);
        }catch(IOException e){
            assertTrue("Get failed", false);
        }
    }

    private void testSingleConsole(){

        Socket socket;
        try {
            socket = new Socket(LOCALHOST, 5104);
            socket.setSoTimeout(3000);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            assertTrue("expected hello message", in.readLine().startsWith("Welcome"));

            out.println("a = b@b"); 
            String response = in.readLine();
            assertEquals("Expected b@b", response, "b@b");
            
            out.println("e = f@f, g@g");
            String response2 = in.readLine();
            assertTrue("Expected f@f, g@g", response2.contains("f@f"));
            assertTrue("Expected f@f, g@g", response2.contains("g@g"));
            assertTrue("Expected f@f, g@g", response2.length() == 8);
        } catch (IOException e) {
            assertTrue("Failed connection", false);
        }
    }
    
    private void testMultipleConsole(){
        Socket socket1, socket2, socket3;
        try {
            socket1 = new Socket(LOCALHOST, 5104);
            socket1.setSoTimeout(3000);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            assertTrue("expected hello message", in1.readLine().startsWith("Welcome"));
            
            socket2 = new Socket(LOCALHOST, 5104);
            socket2.setSoTimeout(3000);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
            assertTrue("expected hello message", in2.readLine().startsWith("Welcome"));
            
            socket3 = new Socket(LOCALHOST, 5104);
            socket3.setSoTimeout(3000);
            BufferedReader in3 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
            PrintWriter out3 = new PrintWriter(socket2.getOutputStream(), true);
            assertTrue("expected hello message", in3.readLine().startsWith("Welcome"));
            
            out1.println("e");
            String response = in1.readLine();
            assertTrue("Expected old or new e", response.contains("g@g"));
            assertTrue("Expected old or new e", response.contains("f@f"));
            assertTrue("Expected old or new e", response.length() == 8);
            
            out2.println("e = e * f@f");
            response = in2.readLine();
            assertEquals("Expected new e", response, "f@f");
            
            out3.println("f = q@q, hEll0_me@m.i.t");
            response = in3.readLine();
            assertTrue("Expected var f set", response.contains("q@q"));
            assertTrue("Expected var f set", response.contains("hEll0_me@m.i.t"));
            assertTrue("Expected var f set", response.length() == 19);
            
            out1.println("f");
            response = in1.readLine();
            assertTrue("Expected var f set", response.contains("q@q"));
            assertTrue("Expected var f set", response.contains("hEll0_me@m.i.t"));
            assertTrue("Expected var f set", response.length() == 19);
            
            out2.println("g = f, h; h = a");
            response = in2.readLine();
            assertEquals("Expected h print", response, "b@b");
            
            out3.println("g, ");
            response = in3.readLine();
            assertTrue("Expected var f set", response.contains("q@q"));
            assertTrue("Expected var f set", response.contains("hEll0_me@m.i.t"));
            assertTrue("Expected var f set", response.contains("b@b"));
            assertTrue("Expected var f set", response.length() == 24);
            
            socket1.close();
            socket2.close();
            socket3.close();
        }catch(IOException e){
            
        }
    }
    
    private void testCombined(){
        try{
            Socket socket1 = new Socket(LOCALHOST, 5104);
            socket1.setSoTimeout(3000);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            assertTrue("expected hello message", in1.readLine().startsWith("Welcome"));
            
            String command = "i=me-and@you";
            String response = executeGet(5025,command);
            String expected = "me-and@you";
            assertEquals("Expected response me-and@you", expected, response);
            
            out1.println("i");
            String responseSocket = in1.readLine();
            assertEquals("Expected response me-and@you", responseSocket, "me-and@you");
            
            out1.println("i=i,ME_ETOO@YEAH, me-AND@you");
            responseSocket = in1.readLine();
            assertTrue("Expected response me-and@you, ME_ETOO@YEAH", responseSocket.contains("me-and@you"));
            assertTrue("Expected response me-and@you, ME_ETOO@YEAH", responseSocket.contains("ME_ETOO@YEAH".toLowerCase()));
            assertTrue("Expected response me-and@you, ME_ETOO@YEAH", responseSocket.length() == 24);
            
            command = "i";
            response = executeGet(5025,command);
            assertTrue("Expected response me-and@you, ME_ETOO@YEAH", responseSocket.contains("me-and@you"));
            assertTrue("Expected response me-and@you, ME_ETOO@YEAH", responseSocket.contains("ME_ETOO@YEAH".toLowerCase()));
            assertTrue("Expected response me-and@you, ME_ETOO@YEAH", responseSocket.length() == 24);
            
            socket1.close();
        }catch(IOException e){
            assertTrue("Get failed", false);
        }
        
        
        
    }
}
