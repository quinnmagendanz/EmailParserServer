package norn;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import norn.MailingList.EmailList;
import norn.MailingList.MailingList;
import norn.MailingList.MailingParser;

public class ParserTest {
    // parse(input):
    //  - legal inputs:
    //    ~ Input size: 1, 2, >2
    //    ~ Allowed special characters
    //    ~ Union, Difference, Intersection, EmailList
    //    ~ Chained operations, Nested operations
    //    ~ Non-default groupings using ()
    //    ~ Whitespace
    //    ~ alphanumeric
    //  - illegal inputs:
    //    ~ illegal characters (includes illegal operators)
    //    ~ incomplete expressions
    //    ~ incomplete emails
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // covers EmailList, input size = 1
    @Test
    public void testParseSingleEmail() {
        MailingList result = MailingParser.parse("a+@b.com");
        EmailList expected = new EmailList(new HashSet<>(Arrays.asList("a+@b.com")));

        assertEquals("Expected proper parse", expected, result);
    }
    
    //covers EmailList, input size = 2, whitespace, alphanumeric
    @Test
    public void testParseMultipeEmails() {
        MailingList result = MailingParser.parse("a@b.com , b1234jfjh@a2eeee.org");
        MailingList expected = MailingParser.parse(result.toString());

        assertEquals("Expected proper parse", expected, result);
    }
    
    //covers Difference, input size > 2, nested operations
    @Test
    public void testParseDifference() {
        MailingList result = MailingParser.parse("a@b.com!b@a.com!c@c.com");
        
        MailingList expected = MailingParser.parse(result.toString());

        assertEquals("Expected proper parse", expected, result);
    }
    
    //covers Intersection, input size > 2
    @Test
    public void testParseIntersection() {
        MailingList result = MailingParser.parse("a@b.com*b@a.com*c@c.com");
        MailingList expected = MailingParser.parse(result.toString());

        assertEquals("Expected proper parse", expected, result);
    }
    
    //covers EmailList, input size > 2
    @Test
    public void testParseLargeEmailList() {
        MailingList result = MailingParser.parse("a@a.com,b@b.com,c@c.com,d@d.com");
        MailingList expected = MailingParser.parse(result.toString());

        assertEquals("Expected proper parse", expected, result);
    }
    
    //covers Union, Difference, Intersection, chained operations, input size > 2, whitespace
    @Test
    public void testChainedOperations() {
        MailingList result = MailingParser.parse("a@a.com , b@b.com! c@c.com* d@d.com ");
        MailingList expected = MailingParser.parse(result.toString());

        assertEquals("Expected proper parse", expected, result);
    }
    
    //covers Union, Difference, Intersection, chained operations, input size > 2, whitespace, non-default groupings
    @Test
    public void testGroupedChainedOperations() {
        MailingList result = MailingParser.parse("a@a.com , (b@b.com! a@a.com)");
        MailingList expected = MailingParser.parse(result.toString());

        assertEquals("Expected proper parse", expected, result);
    }

    
    // covers incomplete expression
    @Test(expected=IllegalArgumentException.class)
    public void testUnmatchedParenthesis() {
       MailingParser.parse("(a@a.com , b@b.com! a@a.com");
    }
    
    // covers incomplete email
    @Test(expected=IllegalArgumentException.class)
    public void testIncompleteEmail() {
       MailingParser.parse("a@ , b@b.com");
    }
    
    // covers incomplete email
    @Test(expected=IllegalArgumentException.class)
    public void testIllegalCharacters() {
       MailingParser.parse("a@a@a.com");
    }
}
