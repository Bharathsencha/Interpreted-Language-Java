/*
 * Main.java
 *
 * Entry point for the Blang interpreter.
 * Reads a source file, parses it into an AST, and executes it.
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        // Expect a source file path as the first argument
        if (args.length < 1) {
            System.err.println("Usage: java Main <file.blang>");
            System.exit(1);
        }

        String filePath = args[0];

        try {
            // Read entire source file into memory
            String source = new String(Files.readAllBytes(Paths.get(filePath)));

            // Initialize parser with source code
            Parser parser = new Parser(source);

            // Parse source into an Abstract Syntax Tree
            AstNode program = parser.parseProgram();

            if (program == null) {
                System.err.println("Parsing failed.");
                System.exit(1);
            }

            // Execute the AST
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(program);

        } catch (IOException e) {
            // File not found or unreadable
            System.err.println("Could not read file: " + filePath);
            System.exit(1);

        } catch (RuntimeException e) {
            // Syntax or runtime errors from parser/interpreter
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
