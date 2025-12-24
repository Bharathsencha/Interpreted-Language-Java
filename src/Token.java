/*
 * Token.java
 *
 * Defines the Token structure produced by the lexer.
 * Each token represents a lexical unit of the source code
 * and stores its type, raw lexeme, and source position.
 */

public class Token {

    
    //Enumeration of all possible token types produced by the lexer.
    //This is a Java port of the original TokenType enum from token.h,
    //with some unused tokens (T_FOR, T_IN) removed.

    public enum Type {
        T_EOF,
        T_IDENT,
        T_NUMBER, // Integer literals
        T_FLOAT,  // Floating-point literals
        T_STRING,
        T_TRUE,
        T_FALSE,

        // Operators
        T_PLUS, T_MINUS, T_MUL, T_DIV, T_MOD,
        T_EQ,       // Assignment (=)
        T_EQEQ,     // Equality (==)
        T_NEQ,      // Not equal (!=)
        T_LT, T_GT, T_LTE, T_GTE,

        // Punctuation and delimiters
        T_LPAREN, T_RPAREN,
        T_LBRACE, T_RBRACE,
        T_LBRACKET, T_RBRACKET,
        T_COMMA, T_COLON,
        T_NEWLINE,

        // Language keywords
        T_LET, T_IF, T_ELSE, T_FUNC, T_RETURN,
        T_PRINT, T_INPUT, T_WHILE,
        T_BREAK, T_CONTINUE,
        T_SWITCH, T_CASE, T_DEFAULT,

        T_INVALID,

        // Logical operators
        T_AND, T_OR,
    }

    // Token metadata
    public final Type type;     // Kind of token
    public final String lexeme; // Raw text from source
    public final int number;    // Integer value (only for T_NUMBER)
    public final double fnumber;// Floating value (only for T_FLOAT)
    public final int line;      // Line number in source
    public final int col;       // Column number in source

    
    // Constructor for non-numeric tokens.
    
    public Token(Type type, String lexeme, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.number = 0;
        this.fnumber = 0.0;
        this.line = line;
        this.col = col;
    }

    
     //Constructor for numeric tokens (integer or float).
     
    public Token(Type type, String lexeme, int number, double fnumber, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.number = number;
        this.fnumber = fnumber;
        this.line = line;
        this.col = col;
    }

    //Human-readable representation used for debugging and logging.
    @Override
    public String toString() {
        return String.format("Token(%s, '%s', line %d)", type, lexeme, line);
    }
}
