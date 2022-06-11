package com.zain.zag;
import java.util.List;
import java.util.ArrayList;
import static com.zain.zag.TokenType.*;



public class Parser {
    
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;

    private int current = 0;

    Parser(List<Token>tokens){
        this.tokens = tokens;
    }


    List<Stmt> parse() {
        List<Stmt>statements = new ArrayList<>();
        try{
            while(!isAtEnd()){
                statements.add(statement());
            }
        } catch(ParseError error){
            return null;
        }
        return statements;
        
    }


    private ParseError error(Token token, String message) {
        Zag.error(token, message);
        return new ParseError();
    }

    private Token consume(TokenType token, String errorMessage){
        if(check(token)){
            return advance();
        }
        throw error(peek(), errorMessage);
    }



    private void synchronize() {
        advance();

        while(!isAtEnd()){

            if(previous().type == SEMICOLON)
            return;

            switch(peek().type){
                case CLASS:
                case IF:
                case WHILE:
                case FUN:
                case FOR:
                case PRINT:
                case RETURN:
                case VAR:
                    return;
            }
            advance();
        }

    }

    private Expr primary() {

        if(match(FALSE)){
            return new Expr.Literal(false);
        }
        if(match(TRUE)){
            return new Expr.Literal(true);
        }
        if(match(NIL)){
            return new Expr.Literal(null);
        }
        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }
        
        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "An expression expected");
    }

    private Expr unary() {

        Expr result;
        if(match(MINUS, BANG)){
            Token operator = previous();
            Expr rightExpr = unary();
            result = new Expr.Unary(operator, rightExpr);
        }
        else{
            result = primary();
        }
        return result;
    }

    private Expr factor() {
        Expr result = unary();
        while(match(STAR, SLASH)){
            Token operator = previous();
            Expr rightExpr = unary();
            result = new Expr.Binary(result, operator, rightExpr);
        }
        return result;
    }


    private Expr term() {
        Expr result = factor();
        while(match(MINUS, PLUS)){
            Token operator = previous();
            Expr rightExpr = factor();
            result = new Expr.Binary(result, operator, rightExpr);
        }
        return result;
    }

    private Expr conditional() {
        Expr result = term();
        if(match(QUESTION_MARK)){
            Token questionOperator = previous();
            Expr leftExpr = term();
            if(match(COLON)){
                Token colonOperator = previous();
                Expr rightExpr = term();
                return new Expr.Binary(result, questionOperator, new Expr.Binary(leftExpr, colonOperator, rightExpr));
            }
            throw error(questionOperator, "Expected an expression");
            
        }
        return result;

    }


    private Expr comparison() {
        Expr result = conditional();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr rightExpr = conditional();
            result = new Expr.Binary(result, operator, rightExpr);
        }
        return result;
    }




    private Expr equality() {
        Expr expr = comparison();
        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comma() {
        Expr result = equality();
        while(match(COMMA)){
            Token operator = previous();
            Expr right = equality();
            result = new Expr.Binary(result, operator, right);
        }
        return result;
    }




    public Expr expression() {
        return comma();
    }



    private Token previous() {

        return tokens.get(current-1);

    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
      }

    private Token peek() {
        return tokens.get(current);
    }


    private boolean isAtEnd() {
        if(peek().type == EOF)
        return true;
        return false;
    }

    

    private boolean check(TokenType type){
        if(isAtEnd())
        return false;
        return peek().type == type;
    }


    private boolean match(TokenType ...tokens){
        for(TokenType tokentype:tokens){
            if(check(tokentype)){
                advance();
                return true;
            }
        }
        return false;
    }




    private Stmt handlePrintStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expect ; after print statement.");
        return new Stmt.Print(value);
    }


    private Stmt handleExpressionStmt() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ; after expression.");
        return new Stmt.Expression(expr);
    }


    private Stmt handleVarDeclaration() {
        Object value = primary();
        if(match(EQUAL)){
            Expr expr = expression();
            new Stmt.Expression(expr);
        }
        consume(SEMICOLON, "Expected Semicolon at the end of statement");
    }

    private Stmt statement() {
        if(match(PRINT)){
            return handlePrintStatement();   
        }
        else
        if(match(VAR)){
            return handleVarDeclaration();
        }
        return handleExpressionStmt(); 
    }

    


}
