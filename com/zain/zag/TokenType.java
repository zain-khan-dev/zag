package com.zain.zag;

enum TokenType {
    
    // Single Character Tokens
    LEFT_PAREN, RIGHT_PAREN, PLUS, MINUS, 
    SLASH, STAR, LEFT_BRACE, RIGHT_BRACE, SEMICOLON, DOT, COMMA,


    // One or two character Tokens
    BANG, BANG_EQUAL, EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL_EQUAL,


    //Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    WHILE, IF, ELSE, AND, OR, CLASS, FALSE, TRUE, FUN, FOR, NIL, PRINT, RETURN, SUPER, THIS, VAR,


    EOF

}
