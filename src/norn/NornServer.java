package norn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.sun.net.httpserver.*;

import lib6005.parser.UnableToParseException;


/**
 * A server that stores sets of emails and allows multiple users to manipulate 
 * the email sets using socket connections from other consoles and http requests
 */
public class NornServer{
    
    public static final int HTTP_PORT = 5021;
    public static final int CONSOLE_PORT = 4444;
    private static final String REJECT_RESPONSE = "Must enter valid command";
    private static final String URL_COMMAND_EXTENSION = "/eval";
    
    private final HttpServer httpServer;
    private final ConsoleServer consoleServer;

    private final Environment env;
    
    /**
     * AF
     *  AF(httpServer, consoleServer, env) represents a single server for email address management
     *  in the env name space of emails. Requests are handles via httpServer and consoleServer.
     * 
     * RI
     *  env, httpServer, consoleServer not null. 
     * 
     * Rep Exposure
     *  all instance variables private, final, and not returned
     * 
     * Thread Safety
     *  httpServer, consoleServer, and env all thread safe data types with safely sequence command calls
     */

    
    /**
     * Make a NornServer and starts it listening for http
     * requests on httpPort at the "http://server_url/eval" extension and console requests on consolePort
     * @param httpPort the port number on which to listen for http requests, between 0 and 65535
     * @param consolePort the port number on which to listen for console requests, between 0 and 65535
     * @throws IOException if can't connect
     * @throws UnableToParseException 
     */   
     public NornServer(int httpPort, int consolePort, final ArrayList<String> starterFiles)  throws IOException, FileNotFoundException, UnableToParseException {
        env = new Environment();
        
        for(String loadFile : starterFiles){
            String response = env.execute("!load"+ loadFile);
            if(response.equals(Environment.FILE_CANNOT_BE_PARSED)){
                throw new UnableToParseException("Given file cannot be parsed");
            }else if(response.equals(Environment.FILE_NOT_FOUND)){
                throw new FileNotFoundException();
            }
        }
        
        httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
        httpServer.createContext(URL_COMMAND_EXTENSION, new HttpHandling());
        httpServer.setExecutor(null); // creates a default executor
        
        consoleServer = new ConsoleServer(consolePort);
        
        System.err.println("Server starting up...");
    }
    
    /**
     * starts up the http and the console servers to handle requests from consoles and http requests
     */
    public void serve(){
        System.err.println("Server ready for requests");
        try{
            httpServer.start();
            consoleServer.start();
        }catch(IOException e){
            System.err.println("Connections failed");
            e.printStackTrace();
            consoleServer.close();
            httpServer.stop(0);
        }
    }


    /**
     * A ConsoleServer is a threadsafe datatype that handles all socket connections with other consoles.
     *
     */
    public class ConsoleServer{
        private final ServerSocket serverSocket;
        
        /**
         * AF
         *  AF(serverSocket) represents a communication buffer through socket serverSocket
         *  
         * RI
         *  serversocekt != null
         *  
         * Rep Exposure
         *  instance variable private, final, not returned in any methods
         *  
         * Thread safety
         *  serverSocket threadsafe datatype with safe sequencing of commands
         */
        
        /**
         * creats a new console server a designated port
         * @param port
         * @throws IOException
         */
        public ConsoleServer(int port) throws IOException{
            serverSocket = new ServerSocket(port);
        }
        
        /**
         * Run the server, listening for and handling client connections.
         * Never returns, unless an exception is thrown.
         * 
         * @throws IOException if an error occurs waiting for a connection
         *                     (IOExceptions from individual clients do *not* terminate serve())
         */
        public void start() throws IOException {
            while (true) {
                // block until a client connects
                Socket socket = serverSocket.accept();

                // handle the client
                Thread connectionHandler = new Thread(new Runnable() {
                    public void run() {
                        try {
                            try {
                                handleConnection(socket);
                            } finally {
                                socket.close();
                            }
                        } catch (IOException ioe) {
                            System.err.println("Socket client disconnected");
                        }
                    }
                });
                connectionHandler.start();
            }
        }
        
        /**
         * Handle a single client connection. Returns when client disconnects.
         * 
         * @param socket socket where the client is connected
         * @throws IOException if the connection encounters an error or terminates unexpectedly
         */
        private void handleConnection(Socket socket) throws IOException {
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            try {
                out.println("Welcome to the Norn e-mail address management system.");
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    String output = env.execute(line);
                    out.println(output);
                }
            } catch(UnableToParseException | IllegalArgumentException e){
                String output = REJECT_RESPONSE;
                out.println(output);
            } finally {
                out.close();
                in.close();
            }
        }
        
        public void close(){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * HttpHandling is a handler for the HttpServer which takes in a single http request
     *  and directs into the address environment for parsing. 
     *
     */
    public class HttpHandling implements HttpHandler{
        
        /**
         * Pass the http request into the environment and return the response
         * 
         * @param t the current HttpExchange containing the http message
         */
        public void handle(HttpExchange t) throws IOException {
            System.err.println("Http request recieved");          
            t.getRequestBody().close();
            String response;
            try {
                final String command = t.getRequestURI().toString().replaceAll(URL_COMMAND_EXTENSION + "/", "");
                System.err.println("Command recieved: " + command) ;
                final String output = env.execute(command);  
                response = "<a href=\"mailto:" + output + "\">email these recipients</a><br>" + output;
            } catch (UnableToParseException e) {
                response = REJECT_RESPONSE;
            }  
            
            t.getResponseHeaders().set("Content-Type", "text/html");
            t.sendResponseHeaders(200, response.length()); 
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            t.close();
            System.err.println("Http response: " + response);  
        }
    }
    
    
    /**
     * Starts a Norn web server listening on port 5021 with the given arguments.
     * 
     * <br> Usage:
     * <pre>
     *      norn.NornServer [--file (FILE )*][--consoleport PORT_NUMBER][--httpport PORT_NUMBER]
     * </pre>
     * 
     * <p>  FILE is an optional argument specifying a file pathname where an environment of assigned
     *      lists has been stored. If this argument is given, the stored environment should be loaded 
     *      as the starting environment.
     *      PORT_NUMBER is an optional arguments to specify the desired http port number or console 
     *      port number to be used by the server. If no arguments given, the http server will listen on
     *      port 5021 and console server will start a socket on port 4444.
     * <br> E.g. "--file mailinglists.txt" starts the server initialized with the environment stored in
     *      mailinglists.txt.
     * @param args arguments as described
     */
        
    public static void main(String[] args) {
        
        int httpPort = HTTP_PORT;
        int consolePort = CONSOLE_PORT;
        ArrayList<String> files = new ArrayList<>();

        Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                if (flag.equals("--httpport")) {
                    httpPort = Integer.parseInt(arguments.remove());
                } else if (flag.equals("--consoleport")) {
                    consolePort = Integer.parseInt(arguments.remove());
                } else if (flag.equals("--file")) {
                    while(!arguments.element().substring(0,2).equals("--")){
                        files.add(arguments.remove());
                    }
                } else {
                    throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                }
            }
            
            NornServer addressServer = new NornServer(httpPort, consolePort, files);
            addressServer.serve();
//        } catch (IllegalArgumentException iae) {
//            System.err.println(iae.getMessage());
//            System.err.println("usage: GameServer [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
//            return;
        } catch (FileNotFoundException e){
            System.err.println("file not found");
        } catch (UnableToParseException e) {
            System.err.println("file commands incorrectly formatted");
        } catch (IOException e) {
            System.err.println("server connections lost");
        }
    }
}
