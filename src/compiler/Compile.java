package compiler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;

public class Compile {

    public static void main(String[] args) {
        String fileName;
        if (args.length == 0) {
            System.out.print("Enter the source file name in PL/0: ");
            fileName = new Scanner(System.in).nextLine();
        } else {
            fileName = args[0];
        }
        if (fileName.isEmpty()) {
            System.out.println("Error\n");
            System.out.println("Use: java -jar \"Compiler.jar\" <file>\n");
        } else {
            Reader sourceFile = null;
            try {
                sourceFile = new BufferedReader(new FileReader(fileName));
                Scanner scanner = new Scanner(sourceFile);
                ErrorIndicator errorIndicator = new ErrorIndicator();
                Parser parser = new Parser(errorIndicator);
                CodeGenerator codeGen = new CodeGenerator(fileName, errorIndicator);
                SyntaxAnalyzer syntax = new SyntaxAnalyzer(scanner, parser, codeGen, errorIndicator);
                try {
                    syntax.analyze();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
                try {
                    sourceFile.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

}
