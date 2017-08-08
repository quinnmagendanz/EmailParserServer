package norn.MailingList;

import java.util.HashSet;

import lib6005.parser.UnableToParseException;
import norn.Environment;

/**
 * ADT representing a MailingList that is named and defined in an Environment
 */
public class NamedList implements MailingList {
    
    // AF: AF(name) = the mailing list whose addresses are defined under name name
    // RI: true
    // Safety from rep exposure: name is private, final, immutable;
    
    private final String name;
    
    /**
     * Creates a new NamedList object whose addresses are defined under name name 
     * @param name the name of the mailing list
     */
    public NamedList(String name) {
        this.name = name;
    }

    //
    // INSTANCE METHODS
    //
    
    @Override
    public EmailList evaluate(Environment environment) throws UnableToParseException {
        if (environment.contains(name)) {
            return environment.get(name).evaluate(environment); // retrieve the saved MailingList expression for this name completely evaluate it
        }
        else {
            return new EmailList(new HashSet<>());
        }
    }

    @Override
    public MailingList evaluateName(String listname, Environment environment) {
        if (name.equals(listname)) {
            if (environment.contains(name)) {
                return environment.get(name);
            }
            else {
                return MailingList.emailList(new HashSet<>());
            }
        }
        else {
            return this;
        }
    }

    @Override
    public MailingList simplify() {
        return this;
    }

    @Override
    public boolean dependsOn(String listname) {
        return name.equals(listname);
    }

    //
    // OBJECT OVERRIDES
    //
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if ( !(thatObject instanceof NamedList)) { return false; }
        NamedList object2 = (NamedList)thatObject;
        
        return this.name.equals(object2.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
