/* Copyright (c) 2015-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package norn;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import lib6005.parser.UnableToParseException;
import norn.MailingList.MailingParser;

/**
 * Tests for the static methods of Commands.
 */
public class EnvironmentTest {

    // Testing strategy
    //   execute:
    //      assignments:
    //          Number of items in list: 0, 1, >1
    //          Adding empty emails to list
    //          Case sensitivity
    //          Reassigning list
    //          Assign list to other lists
    //          Editing list definitions
    //      evaluations:
    //          Number of emails in evaluation: 0, 1, >1
    //          Number of email lists in evaluation: 0, 1, >1
    //          Test Non-existent email lists
    //          Test empty emails within lists
    //          Fully closed list expressions
    //  save:
    //          File can/can't be created/written to
    //  load:
    //          File does/doesn't exist
    //          File empty, non-empty

        
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    //Test assign()
    
    @Test
    public void testAssignEmpty() throws UnableToParseException, IOException {
        Environment env = new Environment();
        String returned = env.execute("list1= ");
        String expected = " ";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));
    }

    @Test
    public void testAssignSingleton() throws UnableToParseException, IOException {
        Environment env = new Environment();
        String returned = env.execute("list1= a@b");
        String expected = "a@b";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));
    }
    
    @Test
    public void testAssignMultiple() throws UnableToParseException, IOException {
        Environment env = new Environment();
        String returned = env.execute("list1= a@b.com, A@b.com, c@d.com");
        String expected = "a@b.com, c@d.com";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));

        String returned2 = env.execute("list2= list1, d@e.com");
        String expected2 = "a@b.com, c@d.com, d@e.com";
        assertEquals(MailingParser.parse(expected2), MailingParser.parse(returned2));

    }
    
    @Test
    public void testAssignAndReassign() throws UnableToParseException, IOException {
        Environment env = new Environment();
        env.execute("list1= a@b");
        String returned = env.execute("list1 = a@c.com");
        String expected = "a@c.com";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));

    }
    
    @Test
    public void testEditDefinition() throws UnableToParseException, IOException {
        Environment env = new Environment();
        env.execute("list1= a@b");
        String returned = env.execute("list1 = list1, a@c.com");
        String expected = "a@c.com, a@b";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));

    }
    
    @Test
    public void testAssignToOtherListsMultiple() throws UnableToParseException, IOException {
        Environment env = new Environment();
        env.execute("list1= a@b");
        env.execute("list2= b@b");
        env.execute("list3= c@b");
        String returned = env.execute("list4=list1, list2, list3");
        String expected = "a@b, b@b, c@b";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));

    }
    
    @Test
    public void testAssignMultipleEmpty() throws UnableToParseException, IOException {
        Environment env = new Environment();
        String returned = env.execute("list1= a@b, , b@c , ");
        String expected = "a@b,b@c";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));

    }

    
    
    //Test evaluate()
    
    @Test
    public void testEvaluateEmpty() throws UnableToParseException, IOException {
        Environment env = new Environment();
        String returned = env.execute(" ");
        String expected = "";
       
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));
    }
    
    @Test
    public void testEvaluateSingleList() throws UnableToParseException, IOException {
        Environment env = new Environment();
        env.execute("list1 = a@a.com");
        String returned = env.execute("list1, b@b.com, c@c.com");
        String expected = "(a@a.com, b@b.com, c@c.com)";
        MailingParser.parse("(a@a.com,b@b.com)");
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));
    }

    @Test
    public void testEvaluateMultipleLists() throws UnableToParseException, IOException {
        Environment env = new Environment();
        env.execute("list1 = a@a.com");
        env.execute("list2 = b@b.com");
        env.execute("list3= c@c.com, d@d.com");
        String returned = env.execute("list1, list2, list3");
        String expected = "a@a.com, b@b.com, c@c.com, d@d.com";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));
    }

    @Test
    public void testFullyClosedListExpressions() throws UnableToParseException, IOException {
        Environment env = new Environment();
        String returned = env.execute("(suite=room1,room2);(room1=elliott@mit.edu;room2=(a@a.com;cameron@mit.edu));suite");
        String expected = "elliott@mit.edu, cameron@mit.edu";
        assertEquals(MailingParser.parse(expected), MailingParser.parse(returned));
    }
    
    //Test save()
    
    @Test
    public void testSaveUnwritableName() throws UnableToParseException, IOException{
        Environment env = new Environment();
        env.execute("list1 = a@a.com");
        env.execute("list2 = b@b.com");
        String returned = env.execute("!savenorn"); //norn is a pre-existing folder
        String expected = "File could not be created or written to";
        assertEquals(expected, returned);
    }
    
    @Test
    public void testSaveWritableName() throws UnableToParseException, IOException{
        Environment env = new Environment();
        env.execute("list1 = a@a.com");
        env.execute("list2 = b@b.com");
        String returned = env.execute("!saveThisTestsSavingAnEnvironment");
        String expected = "";
        assertEquals(returned, expected);
        env.execute("!loadThisTestsSavingAnEnvironment");
        String list1 = env.execute("list1");
        String list2 = env.execute("list2");
        assertEquals(list1, "a@a.com");
        assertEquals(list2, "b@b.com");
    }
    
    //Test load()
    
    @Test
    public void testLoadFileDoesntExist() throws UnableToParseException, IOException{
        Environment env = new Environment();
        String returned = env.execute("!loadThisIsNotAnExistingFile");
        String expected = "File doesn't exist, or couldn't be opened or read";
        assertEquals(returned, expected);
    }
    
    @Test
    public void testLoadFileEmpty() throws UnableToParseException, IOException{
        Environment env = new Environment();
        env.execute("!saveThisIsAnEmptyFile");
        String returned = env.execute("!loadThisIsAnEmptyFile");
        String expected = "";
        assertEquals(returned, expected);
    }
    
    @Test
    public void testLoadFileNotEmpty() throws UnableToParseException, IOException{
        Environment env = new Environment();
        env.execute("list1 = a@a.com");
        env.execute("list2 = b@b.com");
        env.execute("!saveThisFileHasStuffInIt");
        String empty = "";
        assertEquals(empty, env.execute("list1"));
        env.execute("!loadThisFileHasStuffInIt");
        String expected1 = "a@a.com, b@b.com";
        String expected2 = "b@b.com, a@a.com";
        String returned = env.execute("list1,list2");
        assert (expected1.equals(returned) || expected2.equals(returned));
    }
    
}

