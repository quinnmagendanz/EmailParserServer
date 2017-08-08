package norn.MailingList;

import java.util.Set;

import lib6005.parser.UnableToParseException;
import norn.Environment;

/**
 * An immutable data type representing an email list expression
 */
public interface MailingList {
    // Datatype definition:
    //   MailingList = EmailList(recipients:Set<String>) + NamedList(name:String)
    //      + Union(list1:MailingList, list2:MailingList)
    //      + Difference(list1:MailingList, list2:MailingList)
    //      + Intersection(list1:MailingList, list2:MailingList)
    //
    //      + Defintion(name:String, list:MailingList)
    //      + Sequence(list1:MailingList, list2:MailingList)
    //
    // NOTE: Definition and Sequence ADTS are "command" types; their evaluate method will
    //    perform any necessary assignments in the provided environment as well as return 
    //    their evaluation
    
    /**
     * Creates a mailing list with a set of recipients
     * @param mailingList a set of strings, which represents the set of
     *      recipients of a mailing list. Each string is a valid email
     *      address. A valid email address contains a username and
     *      domain name. Usernames and domain names are nonempty
     *      case-insensitive strings of letters, digits, underscores,
     * @return a mailing list
     */
    public static MailingList emailList(Set<String> recipients) {
        return new EmailList(recipients);
    }
    /**
     * Creates a mailing list with a set of recipients
     * @param name the name of the mailing list
     * @return a mailing list
     */
    public static MailingList emailList(String name) {
        return new NamedList(name);
    }
    
    /**
     * Creates a mailing list with a set of recipients
     * @param list1 a valid MailingList
     * @param list2 a valid MailingList
     * @return a mailing list representing the set union of list1 and list2
     *      (recipients in either list1 or list2)
     */
    public static MailingList union(MailingList list1, MailingList list2) {
        return new Union(list1, list2);
    }
    
    /**
     * Creates a mailing list with a set of recipients
     * @param list1 a valid MailingList
     * @param list2 a valid MailingList
     * @return a mailing list representing the set difference of list1 and list2
     *      (recipients in list1 but not list2)
     */
    public static MailingList difference(MailingList list1, MailingList list2) {
        return new Difference(list1, list2);
    }
    
    /**
     * Creates a mailing list with a set of recipients
     * @param list1 a valid MailingList
     * @param list2 a valid MailingList
     * @return a mailing list representing the set intersection of list1 and list2
     *      (recipients in both list1 and list2)
     */
    public static MailingList intersection(MailingList list1, MailingList list2) {
        return new Intersection(list1, list2);
    }
    
    /**
     * Parse a list expression.
     * 
     * @param input list expression to parse, as defined in the Norn specification
     * @return expression AST for the input, according to precedence (highest
     *      to lowest): '*', '!', ','
     *      
     *      If operators are of the same precedence, left-to-right ordering occurs.
     *      
     *      Parenthesis may be used to group expressions.
     *      
     * @throws IllegalArgumentException if the expression is invalid
     */
    public static MailingList parse(String input) throws IllegalArgumentException {
        try {
            return MailingParser.parse(input);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid input", e);
        }
    }
    
    /**
     * Simplifies a MailingList to a EmailList - ie Difference(e, f) returns an EmailList of all recipients 
     * in e but not in f; Intersection(e, f) returns an EmailList of all recipients in both e and f
     * @param environment a mapping of EmailList names to EmailLists
     * @return an EmailList representation of this ListExpression
     * @throws UnableToParseException if a cyclical assignment is detected in a definition evaluation
     */
    public EmailList evaluate(Environment environment) throws UnableToParseException;

    /**
     * Simplifies a MailingList by substituting all NamedLists with name listname with its 
     * value in the environment
     * @param listname the listname to evaluate
     * @param environment a mapping of EmailList names to EmailLists
     * @return a new MailingList object with composed of only Unions, Intersections, and 
     * Differences of each other and EmailList objects
     */
    public MailingList evaluateName(String listname, Environment environment);

    /**
     * Simplifies this MailingList to a non-command MailingList expression
     * @return a MailingList expression that evaluates to the same set as this but contains
     * no Command-type MailingList expressions (Definition, Sequence)
     */
    public MailingList simplify();

    /**
     * Determines whether a MailingList depends on a certain listname
     * @param listname the name of the list
     * @return true iff this MailingList depends on listname
     */
    public boolean dependsOn(String listname);
    
    /**
     * @return a parsable representation of this expression, such that
     * for all e:ListExpression, e.equals(ListParser.parse(e.toString())).
     */
    @Override 
    public String toString();

    /**
     * @param thatObject any object
     * @return true if and only if this and thatObject are structurally-equal
     * MailingLists.
     * 
     * Different groupings of recipients, while they may be mathematically equal, such as
     *      (bob@mit.edu ! bob@gmail.com) ! janedoe@mit.edu and
     *      bob@mit.edu ! (bob@gmail.com ! janedoe@mit.edu) are not equal since they
     *      result in a different recursive formula
     */
    @Override
    public boolean equals(Object thatObject);
    
    /**
     * @return hash code value consistent with the equals() definition of structural
     *      equality, such that for all e1,e2:MailingList,
     *      e1.equals(e2) implies e1.hashCode() == e2.hashCode()
     */
    @Override
    public int hashCode();
}
