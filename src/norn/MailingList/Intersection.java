package norn.MailingList;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lib6005.parser.UnableToParseException;
import norn.Environment;

/**
 * ADT for the intersection of two mailing lists
 */
public class Intersection implements MailingList {
    
    // AF: AF(list1, list2) = the intersection of list1 and list2 (where
    //      recipients in the mailing list are in both list1 and list2)
    // RI: true
    // Safety from rep exposure: list1 and list2 are private, final, immutable
    
    private final MailingList list1;
    private final MailingList list2;
    
    /**
     * Creates a new MailingList object that represents the intersection of the two provided lists
     * @param list1 a valid MailingList
     * @param list2 a valid MailingList
     */
    public Intersection(MailingList list1, MailingList list2) {
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
     * Returns the other mailing list that forms the intersection
     * @return a valid mailing list with recipients
     */
    public MailingList getList2() {
        return list2;
    }
    
    @Override
    public EmailList evaluate(Environment environment) throws UnableToParseException {
        Set<String> evaluatedSet = new HashSet<String>();
        evaluatedSet.addAll(list1.evaluate(environment).getEmails());
        evaluatedSet.retainAll(list2.evaluate(environment).getEmails());
        return new EmailList(evaluatedSet);
    }

    @Override
    public MailingList evaluateName(String listname, Environment environment) {
        return new Intersection(this.list1.evaluateName(listname, environment), this.list2.evaluateName(listname, environment));
    }

    @Override
    public MailingList simplify() {
        return new Intersection(list1.simplify(), list2.simplify());
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
        return "(" + this.list1.toString() + "*" + this.list2.toString() + ")";
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Intersection)) { return false; }
        Intersection that = (Intersection) thatObject;
        return that.getList1().equals(this.getList1()) && that.getList2().equals(this.getList2());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.list1, this.list2, "INTERSECTION");
        //produces a unique hash code for an intersection of lsit1 and list2
    }
}
