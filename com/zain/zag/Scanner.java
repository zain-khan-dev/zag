package com.zain.zag;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static com.zain.zag.TokenType.*;


public class Scanner {
    
    private final String source;
    private final List<Token>tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;



    private static final Map<String, TokenType> keywords;


    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }



    Scanner(String source) {
        this.source = source;
    }

    private char advance() {
        return source.charAt(current++);
    }


    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        // get the lexeme between the start and current
        // literal is populated for string and numbers.
        tokens.add(new Token(type, lexeme , literal, line));
    }


    // function to check if the next character is what is given as parameter if it does then we increment the current value.
    private boolean match(char c){
        if(isAtEnd()){
            return false;
        }
        if(source.charAt(current) != c){
            return false;
        }
        advance();
        return true;
    }

    char peek(){
        // if we at end then return null terminator
        if(isAtEnd())
            return  '\0';
        return source.charAt(current);
    }





    void handleString() {

        // Consumer characters in the string untill we hit an ending double-quote
        while(peek() != '"' && isAtEnd()){
            // to support multi line string
            if(peek() == '\n'){
                line++;
            }
            // advance until we encounter double quote.
            advance();
        }
        // if it we reach the end without ever encountering the double-quote then raise an error
        if(isAtEnd()){
            Zag.error(line, "Unterminated String ");
            return;
        }
        
        String literal = "";
        advance(); // To move forward from the last quote
        literal = source.substring(start+1, current-1);

        // add token with literal as the word between the quotes.
        addToken(STRING, literal);
    }


    boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }


    char peekNext(){
        if(current + 1 >= source.length())
        return '\0';
        return source.charAt(current+1);
    }


    void handleNumbers() {

        // while we encounter number we keep on incrementing current 
        while(isDigit(peek()))advance();

        // if current character to be consumed is . and the next character from current we consume will be a digit then it is a floating point number 
        // we keep on incrementing till we find the numbers after '.' 
        if(peek() == '.' && isDigit(peekNext())){
            advance();
            while(isDigit(peek())) advance();
        }
        // We extract the number and parse it as double adding it to token list with literal value populated
        String number = source.substring(start, current);
        addToken(NUMBER, Double.parseDouble(number));
        
    }


    boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z' )|| (c >= 'A' && c <= 'Z') || (c == '_');
    }

    boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    void handleIdentifier() {
        while(isAlphaNumeric(peek())) {
            advance();
        }
        
        String token = source.substring(start, current);
        TokenType tokenType = keywords.get(token);
        if(tokenType == null){
            addToken(IDENTIFIER);
        }
        else{
            addToken(tokenType);
        }
    }
    


    void handleMultiComment() {
        while(!(peek() == '*' && peekNext() =='/' )){
            if(peek() == '\n'){
                line++;
            }
            advance();
        }
        // advance remove for star
        advance();
        // advance to remove for slash
        advance();
    }


    private void scanToken() {
        char c = advance();
        switch(c){

            // case when only one character is token
            case '(':addToken(LEFT_PAREN); break;
            case ')':addToken(RIGHT_PAREN); break;
            case '{':addToken(LEFT_BRACE); break;
            case '}':addToken(RIGHT_BRACE); break;
            case '+':addToken(PLUS); break;
            case '-':addToken(MINUS); break;
            case ';':addToken(SEMICOLON); break;
            case '*':addToken(STAR); break;
            case ',':addToken(COMMA);break;
            case '?':addToken(QUESTION_MARK);break;
            case ':':addToken(COLON);break;
            
            // case where either one or two characters can act as a lexeme together

            case '!':addToken(match('=')?BANG_EQUAL:BANG);break;
            case '=':addToken(match('=')?EQUAL_EQUAL:EQUAL);break;
            case '<':addToken(match('=')?LESS_EQUAL:LESS);break;
            case '>':addToken(match('=')?GREATER_EQUAL:GREATER);break;




            case '/':
                // case for handling signle line comments
                if(match('/')){
                    // exhaust the complete line since it is a comment
                    while(peek() != '\n' && !isAtEnd()) advance();
                }
                else
                if(match('*')){
                    handleMultiComment();

                }
                else{
                    addToken(SLASH);
                }
                break;





            // handling white space characters and new line

            // ignone white space characters these are used to just delimit the tokens.
            case ' ':
            case '\r':
            case '\t':
            break;


            // When new line is encountered increment the line number.
            case '\n':
            line++;
            break;

            case '"':handleString();break;


            // any other character apart from these will report unexpected character
            default: 
                if(isDigit(c)){
                    handleNumbers();
                }
                else
                if(isAlpha(c)){
                    handleIdentifier();
                }
                else{
                    Zag.error(line, "Unexpected character" + c);
                }
                break;
        }
    }

    private boolean isAtEnd() {
        // check if we have reached the end of file
       return current >= source.length();
    }

    List<Token> scanTokens() {

        // Until we encounter an EOF we iterate over.
        while(!isAtEnd()){
            // reinitialize start to current each time we have completed scan token
            start = current;
            // scan tokens function will scan and add token to list token 
            scanToken();
        }
        // Enter an EOF token at the end of processing
        tokens.add(new Token(EOF,"",null,line));
        return tokens;
    }
    
}
