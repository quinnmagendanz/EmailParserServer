package norn.MailingList;

import java.util.Objects;

import lib6005.parser.UnableToParseException;
import norn.Environment;

/**
 * ADT for the command that assigns the name of a MailingList to a MailingList
 */
public class Definition implements MailingList {
    
    // AF: AF(name, list) = the parsed definition of the assignment of list to name 
    // RI: true
    // Safety from rep exposure: name and list are private, final, immutable
    
    private final String name;
    private final MailingList list;
    
    /**
     * Returns a MailingList command representing the definition of name to be list 
     * @param name the name of the list
     * @param list a valid MailingList
     */
    public Definition(String name, MailingList list) {
        this.name = name;
        this.list = list;
    }
    
    //
    // INSTANCE METHODS
    //
    
    /**
     * Returns the name of the definition
     * @return the list's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the list of this assignment
     * @return a valid mailing list with recipients
     */
    public MailingList getList() {
        return list;
    }
    
    /**
     * Both assigns the definition in environment and evaluates the MailingList
     * @param environment a mapping of EmailList names to EmailLists
     * @return an EmailList representation of this ListExpression
     */
    @Override
    
    public EmailList evaluate(Environment environment) throws UnableToParseException {
        environment.assign(name, list.simplify());
        
        return list.evaluate(environment);
    }

    @Override
    public MailingList evaluateName(String listname, Environment environment) {
        // probably shouldn't ever be called on a definition...
        return new Definition(this.name, this.list.evaluateName(listname, environment));
    }

    @Override
    public MailingList simplify() {
        return list.simplify();
    }

    @Override
    public boolean dependsOn(String listname) {
        return list.dependsOn(listname);
    }

    //
    // OBJECT OVERRIDES
    //
    
    @Override
    public String toString() {
        return "(" + this.name + "=" + this.list.toString() + ")";
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Definition)) { return false; }
        Definition that = (Definition) thatObject;
        return that.getName().equals(this.getName()) && that.getList().equals(this.getList());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.list, "DEFINITION");
        //produces a unique hash code for a defintion name = list
    }
}
