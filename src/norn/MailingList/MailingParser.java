package norn.MailingList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import lib6005.parser.ParseTree;
import lib6005.parser.Parser;
import lib6005.parser.UnableToParseException;

public class MailingParser {
    
    private static final File GRAMMARFILE = new File("src/norn/MailingList/MailingList.g");
    
    // the nonterminals of the grammar
    private enum MailingGrammar {
        ROOT, SEQUENCE, DEFINITION, UNION, DIFFERENCE, INTERSECTION, LIST, EMAIL, LIST_NAME, WHITESPACE
    }
    
    private static Parser<MailingGrammar> parser = makeParser(GRAMMARFILE);
    
    /**
     * Compile the grammar into a parser.
     * 
     * @param grammarFilename <b>Must be in this class's Java package.</b>
     * @return parser for the grammar
     * @throws RuntimeException if grammar file can't be read or has syntax errors
     */
    private static Parser<MailingGrammar> makeParser(final File grammar) {
        try {
            return Parser.compile(grammar, MailingGrammar.ROOT);
        } catch (IOException e) {
            throw new RuntimeException("can't read the grammar file", e);
        } catch (UnableToParseException e) {
            throw new RuntimeException("the grammar has a syntax error", e);
        }
    }
    
    /**
     * Parse a string into an expression.
     * @param string string to parse
     * @return IntegerExpression parsed from the string
     * @throws UnableToParseException if the string doesn't match the IntegerExpression grammar
     */
      public static MailingList parse(final String input) throws IllegalArgumentException {
          // parse the example into a parse tree
          ParseTree<MailingGrammar> parseTree;
          try {
              parseTree = parser.parse(input);
          } catch (UnableToParseException e) {
              throw new IllegalArgumentException("invalid input", e);
          }
          // System.out.println(parseTree);
          // make an AST from the parse tree
          final MailingList expression = makeAbstractSyntaxTree(parseTree);

          return expression;
     }
      
      /**
       * Convert a parse tree into an abstract syntax tree.
       * 
       * @param parseTree constructed according to the grammar in norn.g
       * @param lists a ListDictionary that keeps track of all the list names and
       *        the associated emails
       * @return abstract syntax tree corresponding to parseTree
       */
    private static MailingList makeAbstractSyntaxTree(final ParseTree<MailingGrammar> parseTree) {
        switch (parseTree.name()) {
        //ROOT, UNION, DIFFERENCE, INTERSECTION, LIST, EMAIL, LIST_NAME, WHITESPACE,
        case ROOT: // root ::= sequence;
        {
            final ParseTree<MailingGrammar> child = parseTree.children().get(0);
            return makeAbstractSyntaxTree(child);
        }
        case SEQUENCE: // sequence ::= definition (';' definition)*;
        { 
            final List<ParseTree<MailingGrammar>> children = parseTree.children();
            MailingList expression = makeAbstractSyntaxTree(children.get(0));
            for (int i = 1; i < children.size(); ++i) { // execute instructions in series
                expression = new Sequence(expression, makeAbstractSyntaxTree(children.get(i)));
            }
            return expression;
        }
        case DEFINITION: // definition ::= (list_name '=')? union;
        {
            final List<ParseTree<MailingGrammar>> children = parseTree.children();
            
            if (children.size() == 1) { // not actually assigning; continue parse;
                return makeAbstractSyntaxTree(children.get(0));
            }
            else { // assigning a list
                final String name = children.get(0).text();
                return new Definition(name, makeAbstractSyntaxTree(children.get(1)));
            }
        }
        case UNION: // union ::= difference (',' difference)*;
        {
            final List<ParseTree<MailingGrammar>> children = parseTree.children();
            if (children.size() == 0) {
                MailingList empty = MailingList.emailList(new HashSet<String>());
                return empty;
            }

            boolean isEmailsOnly = true;

            for (int i = 0; i < children.size(); ++i) {
                final ParseTree<MailingGrammar> child = children.get(i);
                isEmailsOnly = isEmailsOnly && child.name() == MailingGrammar.DIFFERENCE &&
                    child.children().size() == 1 && child.children().get(0).name() == MailingGrammar.INTERSECTION &&
                    child.children().get(0).children().size() == 1 && child.children().get(0).children().get(0).name() == MailingGrammar.LIST &&
                    child.children().get(0).children().get(0).children().size() == 1 &&
                    child.children().get(0).children().get(0).children().get(0).name() == MailingGrammar.EMAIL;
            }
            if (isEmailsOnly) { //only emails, make EmailList instead of Union
                final Set<String> emails = new HashSet<>();
                for (int i = 0; i < children.size(); ++i) {
                    emails.addAll(getLeafEmails(children.get(i)));
                }
                return new EmailList(emails);
            }
            else {
                MailingList expression = makeAbstractSyntaxTree(children.get(0));
                for (int i = 1; i < children.size(); ++i) {
                    expression = new Union(expression, makeAbstractSyntaxTree(children.get(i)));
                }
                return expression;
            }
        }
        case DIFFERENCE: // difference ::= intersection ('!' intersection)*;
        {
            final List<ParseTree<MailingGrammar>> children = parseTree.children();
            MailingList expression = makeAbstractSyntaxTree(children.get(0));
            for (int i = 1; i < children.size(); ++i) {
                expression = new Difference(expression, makeAbstractSyntaxTree(children.get(i)));
            }
            return expression;
        }
        case INTERSECTION: // intersection ::= list ('*' list)*;
        {
            final List<ParseTree<MailingGrammar>> children = parseTree.children();
            MailingList expression = makeAbstractSyntaxTree(children.get(0));
            for (int i = 1; i < children.size(); ++i) {
                expression = new Intersection(expression, makeAbstractSyntaxTree(children.get(i)));
            }
            return expression;
        }
        case LIST: // list ::= email | list_name | '(' sequence ')';
        {
            final ParseTree<MailingGrammar> child = parseTree.children().get(0);
            // check which alternative (number or sum) was actually matched
            switch (child.name()) {
            case EMAIL:
                return makeAbstractSyntaxTree(child);
            case LIST_NAME:
                return makeAbstractSyntaxTree(child);
            case SEQUENCE:
                return makeAbstractSyntaxTree(child);
            default:
                throw new AssertionError("should never get here in constructing group");
            }
        }
        case EMAIL: // email ::= ([A-Za-z0-9\._\-\+]+ '@' [A-Za-z0-9\-_\.]+)?;
        {
            final String email = parseTree.text();
            return new EmailList(new HashSet<String>(Arrays.asList(email)));
        }
        case LIST_NAME: // list_name ::= [A-Za-z0-9]+;
        {
            return new NamedList(parseTree.text());
        }
        default:
            throw new AssertionError("should never get here");
        }
    }

    /**
     * Gets the emails at a leaf of a parseTree
     * @param parseTree the tree to search; requires isEmailOnly(parseTree)
     * @return the email addresses at the leaf of this parseTree
     */
    private static Set<String> getLeafEmails(ParseTree<MailingGrammar> parseTree) {
        switch (parseTree.name()) {
            case ROOT:
            case SEQUENCE:
            case DEFINITION:
            case UNION:
            case DIFFERENCE:
            case INTERSECTION:
            case LIST:
            {
                final Set<String> emailAddresses = new HashSet<>();
                for (int i = 0; i < parseTree.children().size(); ++i) {
                    emailAddresses.addAll(getLeafEmails(parseTree.children().get(i)));
                } 
                return emailAddresses;
            }
            case EMAIL:
            {
                return new HashSet<String>(Arrays.asList(parseTree.text().replaceAll("\\s+", "")));
            }
            case LIST_NAME:
            {
                throw new AssertionError("Supplied parseTree is not only emails");
            }
            default:
            {
                throw new AssertionError("should never get here");
            }
        }
    }
}

