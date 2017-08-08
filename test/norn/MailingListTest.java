package norn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

import lib6005.parser.UnableToParseException;
import norn.MailingList.EmailList;
import norn.MailingList.MailingList;
import norn.MailingList.MailingParser;

/**
 * Tests for the MailingList abstract data type.
 */
public class MailingListTest {
    /*
     * Testing strategy
     * 
     * equals:
     *      Test: reflexivity, symmetry, transitivity, repeat recipients, case
     *      recipients: 0, 1, >1
     *      Union, Intersection, Difference
     * hashCode:
     *      Test: equal objects have same hash code
     *      recipients: 0, 1, >1
     *      Union, Intersection, Difference
     * toString:
     *      recipients: 0, 1, >1
     *      Union, Intersection, Difference
     * 
     * evaluate:
     *      recipients: 0, 1, >1
     *      environment size: 0, 1, >1
     * All tests cover each area at least once
     *
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // covers size = 0 hashcode, equals
    @Test
    public void testEqualsHashCodeEmpty() throws UnableToParseException {
        final Environment environment = new Environment();
        MailingList empty = new EmailList(new HashSet<>()).evaluate(environment);
        MailingList emptyIntersection = MailingParser.parse("j@mit.edu * b@mit.edu").evaluate(environment);
        assertEquals(empty, emptyIntersection);
        assertEquals(empty.hashCode(), emptyIntersection.hashCode());
    }
    
    // covers size = 1 hashcode, equals
    @Test
    public void testEqualsHashCodeSizeOne() throws UnableToParseException {
        final Environment environment = new Environment();
        MailingList one = MailingParser.parse("j@mit.edu").evaluate(environment);
        MailingList two = MailingParser.parse("j@mit.edu, J@MIT.EDU").evaluate(environment);
        MailingList three = MailingParser.parse("j@mit.edu ! b@mit.edu").evaluate(environment);
        assertTrue(one.equals(one)); // reflexivity
        assertTrue(one.equals(two) && two.equals(one)); // symmetry
        assertTrue(two.equals(three) && one.equals(three)); // transitivity
        assertTrue(one.hashCode() == two.hashCode() && two.hashCode() == three.hashCode()); // hashcode for equal objects equal
    }
    
    // covers size >1 hashcode, equals, intersection, union, difference
    @Test
    public void testEqualsHashCodeMultipleRecipients() throws UnableToParseException {
        final Environment environment = new Environment();
        MailingList one = MailingParser.parse("(j@mit.edu, b@mit.edu, c@mit.edu) ! b@mit.edu").evaluate(environment);
        MailingList two = MailingParser.parse("(j@mit.edu, b@mit.edu, c@mit.edu) * (j@mit.edu, d@mit.edu, c@mit.edu)").evaluate(environment);
        assertTrue(one.equals(two) && one.hashCode() == two.hashCode());
    }
    
    // covers intersection, union, difference, sizes 0, 1, >1
    @Test
    public void testToString() {
        //TODO: toString doesn't work
        MailingList emptyIntersection = MailingParser.parse("j@mit.edu * b@mit.edu");
        assertEquals(emptyIntersection, MailingParser.parse(emptyIntersection.toString()));
        MailingList union = MailingParser.parse("j@mit.edu, b@mit.edu, c@mit.edu");
        assertEquals(union, MailingParser.parse(union.toString()));
        MailingList difference = MailingParser.parse("j@mit.edu, b@mit.edu, c@mit.edu ! b@mit.edu");
        assertEquals(difference, MailingParser.parse(difference.toString()));
        MailingList intersection = MailingParser.parse("(j@mit.edu, b@mit.edu, c@mit.edu) * (j@mit.edu, d@mit.edu, c@mit.edu)");
        assertEquals(intersection, MailingParser.parse(intersection.toString()));
    }
    
    // covers size = 0, 1, >1 intersection, union, difference
    @Test
    public void testEvaluate() throws UnableToParseException {
        final Environment environment = new Environment();
        MailingList emptyIntersection = MailingParser.parse("j@mit.edu * b@mit.edu");
        MailingList emptyDifference= MailingParser.parse("j@mit.edu ! j@mit.edu");
        assertEquals(emptyIntersection.evaluate(environment), emptyDifference.evaluate(environment));
        MailingList one = MailingParser.parse("j@mit.edu");
        environment.assign("a", MailingParser.parse("j@mit.edu"));
        MailingList two = MailingParser.parse("a");
        assertEquals(one, two.evaluate(environment));
        MailingList three = MailingParser.parse("(j@mit.edu, a@mit.edu, b@mit.edu) ! (d@mit.edu)");
        MailingList four = MailingParser.parse("b, c");
        environment.assign("b", MailingParser.parse("j@mit.edu"));
        environment.assign("c", MailingParser.parse("a@mit.edu, b@mit.edu"));
        assertEquals(three.evaluate(environment), four.evaluate(environment));
    }
}
