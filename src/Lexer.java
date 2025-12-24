/*
 * Lexer.java
 *
 * Tokenizes Blang source code into a stream of tokens.
 * Handles keywords, identifiers, literals, operators, and comments.
 */

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String src;
    private final int len;
    private int pos = 0;
    private int line = 1;

    // Map of reserved keywords to their token types
    private static final Map<String, Token.Type> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("let", Token.Type.T_LET);
        KEYWORDS.put("if", Token.Type.T_IF);
        KEYWORDS.put("else", Token.Type.T_ELSE);
        KEYWORDS.put("func", Token.Type.T_FUNC);
        KEYWORDS.put("return", Token.Type.T_RETURN);
        KEYWORDS.put("true", Token.Type.T_TRUE);
        KEYWORDS.put("false", Token.Type.T_FALSE);
        KEYWORDS.put("print", Token.Type.T_PRINT);
        KEYWORDS.put("input", Token.Type.T_INPUT);
        KEYWORDS.put("while", Token.Type.T_WHILE);
        KEYWORDS.put("break", Token.Type.T_BREAK);
        KEYWORDS.put("continue", Token.Type.T_CONTINUE);
        KEYWORDS.put("switch", Token.Type.T_SWITCH);
        KEYWORDS.put("case", Token.Type.T_CASE);
        KEYWORDS.put("default", Token.Type.T_DEFAULT);
    }

    public Lexer(String source) {
        this.src = source;
        this.len = source.length();
    }

    // Returns the next token from the source code
    public Token nextToken() {
        skipWhitespaceAndComments();

        if (isAtEnd()) {
            return new Token(Token.Type.T_EOF, "", line, pos);
        }

        char c = peek();

        // Newline for statement termination
        if (c == '\n') {
            advance();
            return new Token(Token.Type.T_NEWLINE, "\\n", line - 1, pos);
        }

        // String literals
        if (c == '"') {
            return string();
        }

        // Numeric literals
        if (Character.isDigit(c)) {
            return number();
        }

        // Identifiers and keywords
        if (Character.isLetter(c) || c == '_') {
            return identifier();
        }

        // Two-character operators
        if (c == '=' && peekNext() == '=') {
            return doubleOp(Token.Type.T_EQEQ, "==");
        }
        if (c == '!' && peekNext() == '=') {
            return doubleOp(Token.Type.T_NEQ, "!=");
        }
        if (c == '<' && peekNext() == '=') {
            return doubleOp(Token.Type.T_LTE, "<=");
        }
        if (c == '>' && peekNext() == '=') {
            return doubleOp(Token.Type.T_GTE, ">=");
        }
        if (c == '&' && peekNext() == '&') {
            return doubleOp(Token.Type.T_AND, "&&");
        }
        if (c == '|' && peekNext() == '|') {
            return doubleOp(Token.Type.T_OR, "||");
        }

        // Single-character operators and delimiters
        advance();
        switch (c) {
            case '=':
                return new Token(Token.Type.T_EQ, "=", line, pos);
            case '+':
                return new Token(Token.Type.T_PLUS, "+", line, pos);
            case '-':
                return new Token(Token.Type.T_MINUS, "-", line, pos);
            case '*':
                return new Token(Token.Type.T_MUL, "*", line, pos);
            case '/':
                return new Token(Token.Type.T_DIV, "/", line, pos);
            case '%':
                return new Token(Token.Type.T_MOD, "%", line, pos);
            case '<':
                return new Token(Token.Type.T_LT, "<", line, pos);
            case '>':
                return new Token(Token.Type.T_GT, ">", line, pos);
            case '(':
                return new Token(Token.Type.T_LPAREN, "(", line, pos);
            case ')':
                return new Token(Token.Type.T_RPAREN, ")", line, pos);
            case '{':
                return new Token(Token.Type.T_LBRACE, "{", line, pos);
            case '}':
                return new Token(Token.Type.T_RBRACE, "}", line, pos);
            case '[':
                return new Token(Token.Type.T_LBRACKET, "[", line, pos);
            case ']':
                return new Token(Token.Type.T_RBRACKET, "]", line, pos);
            case ',':
                return new Token(Token.Type.T_COMMA, ",", line, pos);
            case ':':
                return new Token(Token.Type.T_COLON, ":", line, pos);
        }

        return new Token(Token.Type.T_INVALID, String.valueOf(c), line, pos);
    }

    // Parse string literal enclosed in double quotes
    private Token string() {
        advance(); // skip opening quote
        StringBuilder sb = new StringBuilder();
        
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\\' && !isAtEnd()) {
                advance(); // handle escape sequences
            }
            sb.append(advance());
        }
        
        if (!isAtEnd()) {
            advance(); // skip closing quote
        }
        return new Token(Token.Type.T_STRING, sb.toString(), line, pos);
    }

    // Parse integer or floating point number
    private Token number() {
        int start = pos;
        
        while (Character.isDigit(peek())) {
            advance();
        }

        boolean isFloat = false;
        if (peek() == '.' && Character.isDigit(peekNext())) {
            isFloat = true;
            advance(); // consume dot
            while (Character.isDigit(peek())) {
                advance();
            }
        }

        String lexeme = src.substring(start, pos);
        if (isFloat) {
            return new Token(Token.Type.T_FLOAT, lexeme, 0, Double.parseDouble(lexeme), line, pos);
        } else {
            return new Token(Token.Type.T_NUMBER, lexeme, Integer.parseInt(lexeme), 0.0, line, pos);
        }
    }

    // Parse identifier or keyword
    private Token identifier() {
        int start = pos;
        
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            advance();
        }
        
        String lexeme = src.substring(start, pos);
        Token.Type type = KEYWORDS.getOrDefault(lexeme, Token.Type.T_IDENT);
        return new Token(type, lexeme, line, pos);
    }

    // Skip whitespace and comments
    private void skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            char c = peek();
            
            if (c == '#') {
                // Hash-style comment
                while (!isAtEnd() && peek() != '\n') {
                    advance();
                }
            } else if (c == '/' && peekNext() == '/') {
                // C-style comment
                while (!isAtEnd() && peek() != '\n') {
                    advance();
                }
            } else if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else {
                break;
            }
        }
    }

    // Create token for two-character operator
    private Token doubleOp(Token.Type type, String lexeme) {
        advance();
        advance();
        return new Token(type, lexeme, line, pos);
    }

    // Consume and return current character
    private char advance() {
        char c = src.charAt(pos++);
        if (c == '\n') {
            line++;
        }
        return c;
    }

    // Peek at current character
    private char peek() {
        return isAtEnd() ? '\0' : src.charAt(pos);
    }

    // Peek at next character
    private char peekNext() {
        return (pos + 1 >= len) ? '\0' : src.charAt(pos + 1);
    }

    // Check if at end of source
    private boolean isAtEnd() {
        return pos >= len;
    }
}