package com.zain.zag;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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
                statements.add(declaration());
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
        
        if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        throw error(peek(), "An expression expected");
    }


    private Expr finishCall(Expr expr) {
        // handle comma seperated arguments

        List<Expr> arguments = new ArrayList<>();

        if(!check(RIGHT_PAREN)){
            if(arguments.size() > 255){
                error(peek(), "Number of arguments cannot be greater than 255");
            }
            do {
                arguments.add(equality());
            }while(match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "')' expected after argument list");
        //call function

        return new Expr.Call(expr, paren, arguments);

    }

    private Expr call() {
        Expr expr = primary();
        
        while(true){
            if(match(LEFT_PAREN)){
                expr = finishCall(expr);
            }
            if(match(DOT)){
                Token name = consume(IDENTIFIER, "Expect property name after .");
                expr = new Expr.Get(expr, name);
            }
            else{
                break;
            }
        }
        return expr;
    }



    private Expr unary() {

        Expr result;
        if(match(MINUS, BANG)){
            Token operator = previous();
            Expr rightExpr = unary();
            result = new Expr.Unary(operator, rightExpr);
        }
        else{
            result = call();
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
        Expr result = assignment();
        while(match(COMMA)){
            Token operator = previous();
            Expr right = equality();
            result = new Expr.Binary(result, operator, right);
        }
        return result;
    }

    private Expr logical_or(){
        Expr left = logical_and();
        while(match(OR)){
            Token op = previous();
            Expr right = logical_and();
            left = new Expr.logical(left, op, right);
        }
        return left;
    }


    private Expr logical_and() {
        Expr left = equality();
        while(match(AND)){
            Token op = previous();
            Expr right = equality();
            right = new Expr.logical(left, op, right);
        }
        return left;
    }


    private Expr assignment() {
        Expr expr =  logical_or();
        if(match(EQUAL)){
            Token equals = previous();
            Expr right = assignment();
            if(expr instanceof Expr.Variable){
                Token variable = ((Expr.Variable)expr).name;
                return new Expr.Assign(variable, right);
            }
            error(equals, "L value should be an assignment target");
        }
        return expr;
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
        Token name = consume(IDENTIFIER, "Expected variable name");
        Expr expr = null;
        if(match(EQUAL)){ // handle match after identifier

            expr = expression(); // handle expression after identifier
        }
        consume(SEMICOLON, "Expected ';' after variable declaration");
        return new Stmt.Var(name, expr);
    }


    private Stmt.Function handleFunDeclaration(String kind) {
        Token name = consume(IDENTIFIER, "Expected function name");
        consume(LEFT_PAREN, "Expected '(' after function name");
        List<Token>params = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            if(params.size() > 255){
                error(peek(), "Number of parameters to a function should be less than 255");
            }
            do {
                params.add(consume(IDENTIFIER, "Expected parameter name"));
            } while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')' after parameter list");
        consume(LEFT_BRACE, "Expected '{' after "+ kind +" decalration");
        List<Stmt> body = block();

        return new Stmt.Function(name, params, body);
    }


    private Stmt handleClassDeclaration() {
        Token name = consume(IDENTIFIER, "Class must have a name");
        List<Stmt.Function> functionList = new ArrayList<>();

        consume(LEFT_BRACE, "Class must have an opening brace");

        while(!check(RIGHT_BRACE) && !isAtEnd()){ // either variable declration or function declrataion
            functionList.add(handleFunDeclaration("method"));
        }

        consume(RIGHT_BRACE, "Class must have closing brace");


        return new Stmt.Class(name, functionList);
    }


    private Stmt declaration() {
        try {

            if(match(VAR)) return handleVarDeclaration();


            if(match(CLASS)) return handleClassDeclaration();

            if(match(FUN)) return handleFunDeclaration("function");

            return statement();
        }
        catch(ParseError e){
            synchronize();
            return null;
        }
    }


    private Stmt handleIfStatements() {
        consume(LEFT_PAREN, "If statement missing left parenthesis");
        Expr condition = expression();
        consume(RIGHT_PAREN, "If statement missing right parenthesis");
        Stmt ifBlock = statement();
        Stmt elseBlock = null;
        if(match(ELSE)){
            elseBlock = statement();
            return new Stmt.If(condition, ifBlock, elseBlock);
        }

        return new Stmt.If(condition, ifBlock, elseBlock);
    }


    Stmt handleWhileStatment() {
        consume(LEFT_PAREN, "Missing '(' opening parenthesis after while");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Missing ')' closing parenthesis after while");

        Stmt body = statement();
        return new Stmt.While(expr, body);
    }



    private Stmt handleForStmt() {
        consume(LEFT_PAREN, "Missing left parenthesis after for keyword");
        Stmt initializer;
        if(match(SEMICOLON)){
            initializer = null;
        }
        else
        if(match(VAR)){
            initializer = handleVarDeclaration();
        }
        else{
            initializer = handleExpressionStmt();
        }

        Expr condition = null;
        if(!check(SEMICOLON)){
            condition = expression();
        }

        consume(SEMICOLON, "Expected semicolon after initializer");

        Expr increment = null;
        if(!check(RIGHT_PAREN)){
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for loop");

        Stmt body = statement();

        if(increment != null){
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }


        if (condition == null) 
            condition = new Expr.Literal(true);
        
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }


        return body;
    }


    private Stmt handleReturn(){
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON))
            value = expression();

        consume(SEMICOLON, "Expected semicolon after return statement");
        return new Stmt.Return(keyword, value);
    }

    private Stmt statement() {
        if(match(PRINT)){
            return handlePrintStatement();   
        }
        if(match(VAR)){
            return handleVarDeclaration();
        }
        if(match(LEFT_BRACE))
            return new Stmt.Block(block());
        if(match(IF))
            return handleIfStatements();
        if(match(WHILE))
            return handleWhileStatment();
        if(match(FOR))
            return handleForStmt();
        if(match(RETURN))
            return handleReturn();
        return handleExpressionStmt(); 
    }


    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Missing closing brace } after block");
        return statements;
    }
    


}
