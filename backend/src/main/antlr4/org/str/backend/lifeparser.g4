/**
 * Define a grammar called lifeparser
 */
grammar lifeparser;

file:
//	identify_statement
//	description_statements
//	rule_statement
	pattern_statements
	EOF
	;

identify_statement: HASH LIFE_ID NL;

description_statements: description_statement*;

description_statement: HASH D NL;

rule_statement: HASH N NL;

pattern_statements: pattern_statement*;

pattern_statement:
	HASH P x=SIGNED_INTEGER_CONST y=SIGNED_INTEGER_CONST NL
	content+=content_line+
	;

content_line:
	chars+=(DOT | STAR)+ NL;

// Tokens
HASH: '#';
DOT: '.';
STAR: '*';
D: 'D';
N: 'N';
P: 'P';
LIFE_ID: 'Life 1.05';

// Decimal integer constant
fragment INTEGER_CONST: ('0'..'9')+;

// Signed integer constant
SIGNED_INTEGER_CONST: ('+' | '-')? INTEGER_CONST; 

WS : (' ' | '\t' | '\f')+ -> skip ; // skip spaces, tabs, newlines
NL : ('\r' | '\n' | '\r\n'); // -> skip ; // skip newlines
//TEXT: ~('P' | '#' | '*' | '.' | '\r' | '\n') (~('\r' | '\n')) +;
//TEXT: [a-zA-Z0-9]*?;
OTHER: . -> skip;
