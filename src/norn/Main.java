package norn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lib6005.parser.UnableToParseException;

/**
 * Console interface to the expression system.
 * 
 */
public class Main {
    static Environment environment = new Environment();
    
    /**
     * Read expression and command inputs from the console and output results.
     * An empty input terminates the program.
     * @param args unused
     * @throws IOException if there is an error reading the input
     */
    public static void main(String[] args) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        
        while (true) {
            System.out.print("> ");
            final String input = in.readLine();
            
            if (input.isEmpty()) {
                return; // exits the program
            }
            
            try {
                final String output = environment.execute(input);

                System.out.println(output);
            }
            catch (UnableToParseException e) {
                throw new IllegalArgumentException("Could not parse input", e);
            } catch (RuntimeException re) {
                System.out.println(re.getClass().getName() + ": " + re.getMessage());
            }
        }
    }
    
 /**Testing for sequencing:
  *     Number of individual email ids: 0, 1, >1
        Union in sequence: yes, no
        Difference in sequence: yes, no
        Intersection in sequence: yes, no
        Test list created and added to in one line
        Test empty set
        Number of empty list expressions: 0, 1, >1
        
        Test 1: 
        
        Input: a = a@a.com; a,c@c.com
        Output: a@a.com
                c@c.com, a@a.com
                
        Test 2:
        
        Input: ab = A@a.com, b@b.com; ab!a@a.com
        Output: b@b.com, a@a.com
                b@b.com
                
        Test 3:
        
        Input: ab = a@a.com, b@b.com; bc = b@b.com, c@c.com; b = ab * bc
        Output: b@b.com, a@a.com
                c@c.com, b@b.com
                b@b.com
                
        Test 4: 
        
        Input: abc = a@a.com, b@b.com, c@c.com; bc = b@b.com, c@c.com; bc2 = abc * bc;  bc2!c@c.com
        Output: c@c.com, b@b.com, a@a.com
                c@c.com, b@b.com
                c@c.com, b@b.com
                b@b.com  
                
        Test 5:
        Input:  a = a@a.com; ; a,b@b
        Output: a@a.com
                
                b@b, a@a.com
        
        Test 6:
        Input: a = a@a.com;  a,b@b;
        Output: a@a.com
                b@b, a@a.com
          
          
        Test 7:
        Input: * * *
        Output: 
        
       ****ALL TESTS PASSED*****
  */
        
    

        
}
