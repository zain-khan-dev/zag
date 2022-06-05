package com.zain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import com.zain.Scanner;
import java.util.List;
public class Zag {

    private static boolean hadError = false;


    public static void error (int line, String message) { 

        report(line, "", message);

    }


    private static void report(int line, String where, String message){

        System.err.println("["+line+"]" + " Error: "+ where +" Failed with "+ message);


        hadError = true;
    }


    public static void parseCommand(String command) {
        System.out.println("This command will be parsed " + command);
    }


    public static void run(String command) {
        Scanner scanner = new Scanner(command);

        List<Token> tokens = scanner.scanTokens();
        for(Token token:tokens){
            System.out.println(token.type + " " + token.lexeme);
        }

    }

    public static void runFile(String fileName) throws IOException{
        byte[] bytes = Files.readAllBytes(Path.of(fileName));

        run(new String(bytes, Charset.defaultCharset()));
        if(hadError){
            System.exit(65);
        }

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