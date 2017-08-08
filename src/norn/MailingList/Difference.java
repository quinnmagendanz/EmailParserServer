package norn.MailingList;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lib6005.parser.UnableToParseException;
import norn.Environment;

/**
 * ADT for the difference of two mailing lists
 */
public class Difference implements MailingList {
    
    // AF: AF(list1, list2) = the difference of list1 and list2 (where
    //      recipients in the mailing list are in list1 but not list2)
    // RI: true
    // Safety from rep exposure: list1 and list2 are private, final, immutable
    
    private final MailingList list1;
    private final MailingList list2;
    
    /**
     * Creates a MailingList object representing the difference between the two provided lists
     * @param list1 a valid MailingList
     * @param list2 a valid MailingList
     */
    public Difference(MailingList list1, MailingList list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    //
    // INSTANCE METHODS
    //
    
    /**
     * Gets the mailing list in which the recipients are included
     *      in the difference
     * @return a mailing list object representing recipients
     */
    public MailingList getList1() {
        return list1;
    }
    
    /**
     * Gets the mailing list in which the recipients are excluded
     *      in the difference
     * @return a mailing list object representing recipients
     */
    public MailingList getList2() {
        return list2;
    }
    
    @Override
    public EmailList evaluate(Environment environment) throws UnableToParseException {
        Set<String> evaluatedSet = new HashSet<String>();
        evaluatedSet.addAll(list1.evaluate(environment).getEmails());
        evaluatedSet.removeAll(list2.evaluate(environment).getEmails());
        return new EmailList(evaluatedSet);
    }

    @Override
    public MailingList evaluateName(String listname, Environment environment) {
        return new Difference(this.list1.evaluateName(listname, environment), this.list2.evaluateName(listname, environment));
    }

    @Override
    public MailingList simplify() {
        return new Difference(list1.simplify(), list2.simplify());
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
        return "(" + this.list1.toString() + "!" + this.list2.toString() + ")";
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Difference)) { return false; }
        Difference that = (Difference) thatObject;
        return that.getList1().equals(this.getList1()) && that.getList2().equals(this.getList2());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.list1, this.list2, "DIFFERENCE");
        //produces a unique hash code for a difference of lsit1 and list2
    }
}
