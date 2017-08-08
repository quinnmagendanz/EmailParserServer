package norn.MailingList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import norn.Environment;

/**
 * ADT for a set of email addresses
 */
public class EmailList implements MailingList {
    
    // AF: AF(emails) = set of valid emails(recipients) in the mailing list
    // RI: Every email is a valid username and domain name
    // Safety from rep exposure: private final field; mutable, but never
    //      returns the mutable object itself (returns unmodifiable
    //      wrapper but Strings are immutable, so the set cannot be
    //      mutated)
    
    private final Set<String> emails = new HashSet<>();
    
    /**
     * Creates a new EmailList object with the specified set of recipients
     * @param mailingList a string that represents a valid mailing list name
     */
    public EmailList(Set<String> recipients) {
        for (String recipient : recipients) {
            if (!recipient.matches("\\s*")) {
                emails.add(recipient.toLowerCase());
            }
        }
        checkRep();
    }
    
    // assert rep invariant
    private void checkRep() {
        for (String email : emails) {
            assert email.matches("[A-Za-z0-9\\._\\-\\+]+@[A-Za-z0-9\\-_\\.]+");//"[a-zA-Z0-9\\.\\-\\_]+@[a-zA-Z0-9\\.\\-\\_]+");
        }
    }

    //
    // INSTANCE METHODS
    //
    
    @Override
    public EmailList evaluate(Environment environment) {
        checkRep();
        return this;
    }

    /**
     * Gets the set of emails in this EmailList
     * @return the set of emails in this EmailList
     */
    public Set<String> getEmails() {
        checkRep();
        return Collections.unmodifiableSet(emails);
    }

    @Override
    public MailingList evaluateName(String listname, Environment environment) {
        return this; // no need to simplify an EmailList
    }

    @Override
    public MailingList simplify() {
        return this;
    }

    @Override
    public boolean dependsOn(String listname) {
        return false;
    }

    //
    // OBJECT OVERRIDES
    //
    
    @Override
    public String toString() {
        return emails.toString().replace("[", "(").replace("]", ")");
    }
    
    @Override
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof EmailList)) { return false; }
        EmailList that = (EmailList)thatObject;
        return that.getEmails().containsAll(this.getEmails()) &&
            this.getEmails().containsAll(that.getEmails());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.emails, "EMAILLIST");
    }
}
