package com.zain.zag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;





public class Zag {

    private static boolean hadError = false;
    static boolean hadRuntimeError = false;


    public static void error (int line, String message) { 

        report(line, "", message);

    }


    private static void report(int line, String where, String message){

        System.err.println("["+line+"]" + " Error: "+ where +" Failed with "+ message);


        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
          report(token.line, " at end", message);
        } else {
          report(token.line, " at '" + token.lexeme + "'", message);
        }
      }


    public static void parseCommand(String command) {
        System.out.println("This command will be parsed " + command);
    }


    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +"\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void run(String command) {
        Scanner scanner = new Scanner(command);

        List<Token> tokens = scanner.scanTokens();
        
        Parser parser = new Parser(tokens);

        Expr expr = parser.parse();

        Interpreter interpreter = new Interpreter();

        interpreter.interpret(expr);

        System.out.println(new AstPrinter().print(expr));

        // for(Token token:tokens){
        //     System.out.println(token.type + " " + token.lexeme);
        // }
    }

    public static void runFile(String fileName) throws IOException{
        byte[] bytes = Files.readAllBytes(Path.of(fileName));

        run(new String(bytes, Charset.defaultCharset()));
        if(hadError){
            System.exit(65);
        }
        if (hadRuntimeError) 
            System.exit(70);

    }

    public static void runPrompt() throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.print("> ");
            String command = br.readLine();
            if(command == null){
                break;
            }
            run(command);
        }
        br.close();
    }


    public static void main(String []args) throws IOException {
        
        // Expr expression = new Expr.Binary(
        //     new Expr.Unary(
        //         new Token(TokenType.MINUS, "-", null, 1),
        //          new Expr.Literal(123)),
        //          new Token(TokenType.STAR, "*", null, 1), new Expr.Grouping(new Expr.Literal(45.67)));

        // System.out.println(new AstPrinter().print(expression));
        
        if(args.length > 1){
            System.out.println("Zag usage Zag [file.zag]");
            System.exit(64);
        }
        else
        if(args.length == 1){
            runFile(args[0]);
            hadError = false;
        }
        else{
            runPrompt();
        }
    }

}