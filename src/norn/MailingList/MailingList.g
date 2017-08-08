@skip whitespace {
    root ::= sequence;
    sequence ::= definition (';' definition)*;
    definition ::= (list_name '=')? union;
    union ::= difference (',' difference)*;
    difference ::= intersection ('!' intersection)*;
    intersection ::= list ('*' list)*;
    list ::= email | list_name | '(' sequence ')';
}
email ::= ([A-Za-z0-9\._\-\+]+ '@' [A-Za-z0-9\-_\.]+)?;
list_name ::= [A-Za-z0-9]+;
whitespace ::= [ \t\r\n]+;