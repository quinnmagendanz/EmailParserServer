package norn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lib6005.parser.UnableToParseException;
import norn.MailingList.MailingList;
import norn.MailingList.MailingParser;

/** threadsafe ADT for storing an environment of String -> MailingList mappings. */
public class Environment {
    /* Abstraction Function:
     *     AF(environment) = an environment in which each (listname, list) pair in environment
     *     is a defined listname -> list assignment
     * Representation Invariant:
     *     true
     * Safety from Rep Exposure:
     *     fields are private and final and never released to the observer
     * Thread safety argument:
     *     all mutators and observers are syncrhonized around this instance - only one thread may
     *     access or modify the single private, mutable variable, environment, at a time
     */
    
    public static final String FILE_NOT_FOUND = "File doesn't exist, or couldn't be opened or read";
    
    public static final String FILE_CANNOT_BE_PARSED = "File couldn't be parsed";
    
    private final Map<String, MailingList> environment = new HashMap<>(); /* Essentially the same as the ListDictionary ADT, but ADT is not necessarily required */

    /**
     * Creates a new Environment object
     */
    public Environment() { }


    //
    // ENVIRONMENT METHODS
    //


    /**
     * Clears the saved environment of assigned list expressions
     */
    public synchronized void clear() {
        environment.clear();
    }

    /**
     * Returns true if the environment contains a list named name
     * @param name the name of the list
     * @return true iff the environment contains a MailingList named name
     */
    public synchronized boolean contains(String name) {
        return environment.containsKey(name);
    }

    /**
     * Returns the MailingList named name
     * @param name the name of the list, requires this contains name
     * @return the MailingList named name
     */
    public synchronized MailingList get(String name) {
        return environment.get(name);
    }

    /**
     * Assigns the provided value to the provided name in the environment
     * @param name the name of the list
     * @param value the value of the list named name; requires that value does not contain a 
     * cyclical dependency as described in the norn2 specification
     * @throws UnableToParseException if a cyclical definition is detected
     */
    public synchronized void assign(String name, MailingList value) throws UnableToParseException {
        MailingList simplifiedValue = value.evaluateName(name, this);
        for (final String key : environment.keySet()) { //check for cyclical dependencies before adding to environment
            if (environment.get(key).dependsOn(name) && simplifiedValue.dependsOn(key)) { 
                throw new UnableToParseException("Cyclical definitions not supported; \"" + key + 
                    "\" depends on \"" + name + "\" and vice versa");
            }
        }

        environment.put(name, simplifiedValue);
    }


    //
    // COMMAND METHODS
    //

    /**
     * Executes the provided command in the scope of this environment
     * @param command the command to evaluate
     * @return the string representation of the set of recipients in the evaluated command
     * @throws UnableToParseException if the command cannot be parsed
     * @throws IOException if fails to load file
     */
    public synchronized String execute(String command) throws UnableToParseException {
        int startCommand = 5;
        if (command.startsWith("!save")){
            String fileName = command.substring(startCommand);
            return save(fileName);   //got to check what response should be for empty file                
        }
        else if (command.startsWith("!load")) {
            String fileName = command.substring(startCommand);
            String response = load(fileName);
            return response;
            
        }else {
            String parsed = MailingParser.parse(command).evaluate(this).toString().replace("(", "").replace(")", "").replace(";;", ";");
            return parsed;
        }
    }

    /**
     * Saves the environment into the specified fileName, and clears the environment
     * @param fileName the name of the file to write the saved contents of environment
     * @return the empty string if successful, otherwise a printable error message
     * @throws IOException if the file couldn't be created or written to
     */
    private synchronized String save(String fileName) {
        String response = "";
        BufferedWriter writer = null;
        String testFolder = "test/";
        try {
            writer = new BufferedWriter(new FileWriter(testFolder + fileName));   
            for (String name: environment.keySet()){
                writer.write(name + "=" + environment.get(name) + ";");
            }
        } catch (IOException e) {
            response = "File could not be created or written to";
            return response;
        } finally {
            if ( writer != null){
              try{
                  writer.close();
              }catch(IOException e){}
              environment.clear();
              response = "";
          }
        }
        return response;
    }
            


    /**
     * Loads from the specified file the environment
     * @param file the file from which to load the environment
     * @return the empty string if successful, otherwise a printable error message
     * @throws IOException if the file doesn't exist, or couldn't be opened or read
     * @throws UnableToParseException 
     */
    private synchronized String load(String fileName){
        String testFolder = "test/";
        try (BufferedReader br = new BufferedReader(new FileReader(testFolder + fileName))){
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line + ";");
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String response = sb.toString();
            execute(response);
            String empty = "";
            return empty;
        } catch (IOException e) {
            String response = FILE_NOT_FOUND;
            return response;
        } catch (UnableToParseException e){
            String response = FILE_CANNOT_BE_PARSED;
            return response;
        }
    }
}

