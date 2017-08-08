package norn.MailingList;

import java.util.Objects;

import lib6005.parser.UnableToParseException;
import norn.Environment;

/**
 * ADT for the command representing a sequence of MailingLists
 */
public class Sequence implements MailingList {
    
    // AF: AF(list1, list2) = the sequence of MailingLists whose recipients are produced by list2 after
    //      substituting the expressions of all named list definitions found in list1 
    // RI: true
    // Safety from rep exposure: list1 and list2 are private, final, immutable;
    
    private final MailingList list1;
    private final MailingList list2;
    
    /**
     * Creates a new MailingList command representing the sequence of the provided lists
     * @param list1 a valid MailingList
     * @param list2 a valid MailingList
     */
    public Sequence(MailingList list1, MailingList list2) {
        this.list1 = list1;
        this.list2 = list2;
    }
    
    //
    // INSTANCE METHODS
    //
    
    /**
     * Returns one of the mailing lists that forms the intersection
     * @return a valid mailing list with recipients
     */
    public MailingList getList1() {
        return list1;
    }
    
    /**
     * Returns the other the mailing list that forms the intersection
     * @return a valid mailing list with recipients
     */
    public MailingList getList2() {
        return list2;
    }
    
    /**
     * Both evaluates list1 and list2 sequentially and returns the evaluation of list2
     * @param environment a mapping of EmailList names to EmailLists
     * @return an EmailList representation of this ListExpression
     */
    @Override
    
    public EmailList evaluate(Environment environment) throws UnableToParseException {
        list1.evaluate(environment);
        return list2.evaluate(environment);
    }

    @Override
    public MailingList evaluateName(String listname, Environment environment) {
        // probably shouldn't ever be called on a sequence...
        return new Union(this.list1.evaluateName(listname, environment), this.list2.evaluateName(listname, environment));
    }
    @Override
    public MailingList simplify() {
        return list2.simplify();
    }

    @Override
    public boolean dependsOn(String listname) {
        return list1.dependsOn(listname) || list2.dependsOn(listname);
    }

    //
    // OBJECT OVERRIDES
    //
    
    @Override
    public String toString() {
        return this.list1.toString() + ";" + this.list2.toString();
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Sequence)) { return false; }
        Sequence that = (Sequence) thatObject;
        return that.getList1().equals(this.getList1()) && that.getList2().equals(this.getList2());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.list1, this.list2, "SEQUENCE");
        //produces a unique hash code for a union of lsit1 and list2
    }
}
