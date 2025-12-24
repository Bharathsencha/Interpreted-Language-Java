/*
 * Parser.java
 *
 * Converts tokens from the lexer into an Abstract Syntax Tree (AST).
 * Implements recursive-descent parsing with operator precedence.
 */
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token cur;
    private boolean hasCur;

    public Parser(String source) {
        this.lexer = new Lexer(source);
        this.hasCur = false;
        advance();
    }

    // Move to next token
    private void advance() {
        this.cur = lexer.nextToken();
        this.hasCur = true;
    }

    // Check if current token matches type
    private boolean check(Token.Type type) {
        return hasCur && cur.type == type;
    }

    // Match and consume token if type matches
    private boolean match(Token.Type type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    // Consume expected token or throw error
    private void consume(Token.Type type, String errMsg) {
        if (check(type)) {
            advance();
        } else {
            throw new RuntimeException("Syntax Error: " + errMsg + " (Found '" + cur.lexeme + "' on line " + cur.line + ")");
        }
    }

    // Expression parsing with precedence climbing

    // Entry point for expression parsing
    private AstNode expression() {
        return logicalOr();
    }

    // Parse logical OR: expr || expr
    private AstNode logicalOr() {
        AstNode expr = logicalAnd();
        while (match(Token.Type.T_OR)) {
            expr = new AstNode.BinOpNode("||", expr, logicalAnd());
        }
        return expr;
    }

    // Parse logical AND: expr && expr
    private AstNode logicalAnd() {
        AstNode expr = equality();
        while (match(Token.Type.T_AND)) {
            expr = new AstNode.BinOpNode("&&", expr, equality());
        }
        return expr;
    }

    // Parse equality: expr == expr, expr != expr
    private AstNode equality() {
        AstNode expr = comparison();
        while (check(Token.Type.T_EQEQ) || check(Token.Type.T_NEQ)) {
            String op = cur.lexeme;
            advance();
            expr = new AstNode.BinOpNode(op, expr, comparison());
        }
        return expr;
    }

    // Parse comparison: expr < expr, expr > expr, expr <= expr, expr >= expr
    private AstNode comparison() {
        AstNode expr = addition();
        while (check(Token.Type.T_LT) || check(Token.Type.T_GT) || 
               check(Token.Type.T_LTE) || check(Token.Type.T_GTE)) {
            String op = cur.lexeme;
            advance();
            expr = new AstNode.BinOpNode(op, expr, addition());
        }
        return expr;
    }

    // Parse addition and subtraction: expr + expr, expr - expr
    private AstNode addition() {
        AstNode expr = multiplication();
        while (check(Token.Type.T_PLUS) || check(Token.Type.T_MINUS)) {
            String op = cur.lexeme;
            advance();
            expr = new AstNode.BinOpNode(op, expr, multiplication());
        }
        return expr;
    }

    // Parse multiplication, division, modulo: expr * expr, expr / expr, expr % expr
    private AstNode multiplication() {
        AstNode expr = unary();
        while (check(Token.Type.T_MUL) || check(Token.Type.T_DIV) || check(Token.Type.T_MOD)) {
            String op = cur.lexeme;
            advance();
            expr = new AstNode.BinOpNode(op, expr, unary());
        }
        return expr;
    }

    // Parse unary expressions (currently just delegates to callOrIndex)
    private AstNode unary() {
        return callOrIndex();
    }

    // Parse function calls and array indexing: func(args), arr[index]
    private AstNode callOrIndex() {
        AstNode expr = primary();
        
        while (true) {
            if (match(Token.Type.T_LPAREN)) {
                // Function call
                List<AstNode> args = new ArrayList<>();
                if (!check(Token.Type.T_RPAREN)) {
                    do {
                        args.add(expression());
                    } while (match(Token.Type.T_COMMA));
                }
                consume(Token.Type.T_RPAREN, "Expected ')' after arguments");
                
                if (expr instanceof AstNode.IdentNode) {
                    expr = new AstNode.CallNode(((AstNode.IdentNode) expr).name, args);
                } else {
                    throw new RuntimeException("Error: Can only call identifiers.");
                }
            } else if (match(Token.Type.T_LBRACKET)) {
                // Array/list indexing
                AstNode idx = expression();
                consume(Token.Type.T_RBRACKET, "Expected ']' after index");
                expr = new AstNode.IndexNode(expr, idx);
            } else {
                break;
            }
        }
        return expr;
    }

    // Parse primary expressions: literals, identifiers, parenthesized expressions
    private AstNode primary() {
        // Integer literal
        if (check(Token.Type.T_NUMBER)) {
            int v = cur.number;
            advance();
            return new AstNode.NumberNode(v);
        }
        
        // Float literal
        if (check(Token.Type.T_FLOAT)) {
            double v = cur.fnumber;
            advance();
            return new AstNode.FloatNode(v);
        }
        
        // String literal
        if (check(Token.Type.T_STRING)) {
            String s = cur.lexeme;
            advance();
            return new AstNode.StringNode(s);
        }
        
        // Boolean literals
        if (match(Token.Type.T_TRUE)) {
            return new AstNode.BoolNode(true);
        }
        if (match(Token.Type.T_FALSE)) {
            return new AstNode.BoolNode(false);
        }
        
        // Identifier
        if (check(Token.Type.T_IDENT)) {
            String name = cur.lexeme;
            advance();
            return new AstNode.IdentNode(name);
        }
        
        // Parenthesized expression
        if (match(Token.Type.T_LPAREN)) {
            AstNode expr = expression();
            consume(Token.Type.T_RPAREN, "Expected ')' after expression");
            return expr;
        }
        
        // List literal: [1, 2, 3]
        if (match(Token.Type.T_LBRACKET)) {
            List<AstNode> items = new ArrayList<>();
            if (!check(Token.Type.T_RBRACKET)) {
                do {
                    items.add(expression());
                } while (match(Token.Type.T_COMMA));
            }
            consume(Token.Type.T_RBRACKET, "Expected ']' at end of list");
            return new AstNode.ListNode(items);
        }
        
        // Input statement: input("prompt")
        if (match(Token.Type.T_INPUT)) {
            AstNode promptExpr = null;
            if (match(Token.Type.T_LPAREN)) {
                if (!check(Token.Type.T_RPAREN)) {
                    promptExpr = expression();
                }
                consume(Token.Type.T_RPAREN, "Expected ')' after input prompt");
            }
            return new AstNode.InputNode(promptExpr);
        }
        
        throw new RuntimeException("Syntax Error: Unexpected token '" + cur.lexeme + "' on line " + cur.line);
    }

    // Statement parsing

    // Parse a single statement
    private AstNode statement() {
        // Skip newlines
        while (match(Token.Type.T_NEWLINE));

        // Variable declaration: let name = value
        if (match(Token.Type.T_LET)) {
            String name = cur.lexeme;
            consume(Token.Type.T_IDENT, "Expected variable name after 'let'");
            consume(Token.Type.T_EQ, "Expected '=' in variable declaration");
            return new AstNode.LetNode(name, expression());
        }
        
        // Print statement: print(arg1, arg2, ...)
        if (match(Token.Type.T_PRINT)) {
            consume(Token.Type.T_LPAREN, "Expected '(' after print");
            List<AstNode> args = new ArrayList<>();
            if (!check(Token.Type.T_RPAREN)) {
                do {
                    args.add(expression());
                } while (match(Token.Type.T_COMMA));
            }
            consume(Token.Type.T_RPAREN, "Expected ')' after print arguments");
            return new AstNode.PrintNode(args);
        }

        // If-else statement: if (cond) { ... } else { ... }
        if (match(Token.Type.T_IF)) {
            consume(Token.Type.T_LPAREN, "Expected '(' after 'if'");
            AstNode cond = expression();
            consume(Token.Type.T_RPAREN, "Expected ')' after condition");
            while (match(Token.Type.T_NEWLINE));
            consume(Token.Type.T_LBRACE, "Expected '{' to start if-block");
            List<AstNode> thenB = block();
            List<AstNode> elseB = new ArrayList<>();
            
            if (match(Token.Type.T_ELSE)) {
                if (check(Token.Type.T_IF)) {
                    // else if
                    elseB.add(statement());
                } else {
                    // else
                    while (match(Token.Type.T_NEWLINE));
                    consume(Token.Type.T_LBRACE, "Expected '{' to start else-block");
                    elseB = block();
                }
            }
            return new AstNode.IfNode(cond, thenB, elseB);
        }

        // While loop: while (cond) { ... }
        if (match(Token.Type.T_WHILE)) {
            consume(Token.Type.T_LPAREN, "Expected '(' after 'while'");
            AstNode cond = expression();
            consume(Token.Type.T_RPAREN, "Expected ')'");
            while (match(Token.Type.T_NEWLINE));
            consume(Token.Type.T_LBRACE, "Expected '{' to start while-body");
            return new AstNode.WhileNode(cond, block());
        }

        // Break statement
        if (match(Token.Type.T_BREAK)) {
            return new AstNode.BreakNode();
        }
        
        // Continue statement
        if (match(Token.Type.T_CONTINUE)) {
            return new AstNode.ContinueNode();
        }

        // Switch statement: switch (expr) { case val: ... default: ... }
        if (match(Token.Type.T_SWITCH)) {
            consume(Token.Type.T_LPAREN, "Expected '(' after switch");
            AstNode expr = expression();
            consume(Token.Type.T_RPAREN, "Expected ')' after switch expression");
            while (match(Token.Type.T_NEWLINE));
            consume(Token.Type.T_LBRACE, "Expected '{' to start switch block");

            List<AstNode.CaseNode> cases = new ArrayList<>();
            List<AstNode> defaultCase = null;

            while (!check(Token.Type.T_RBRACE) && !check(Token.Type.T_EOF)) {
                while (match(Token.Type.T_NEWLINE));
                
                if (match(Token.Type.T_CASE)) {
                    AstNode val = expression();
                    consume(Token.Type.T_COLON, "Expected ':' after case value");
                    List<AstNode> body = new ArrayList<>();
                    
                    // Parse case body until next case, default, or closing brace
                    while (!check(Token.Type.T_CASE) && !check(Token.Type.T_DEFAULT) && !check(Token.Type.T_RBRACE)) {
                        if (match(Token.Type.T_NEWLINE)) {
                            continue;
                        }
                        body.add(statement());
                    }
                    cases.add(new AstNode.CaseNode(val, body));
                } else if (match(Token.Type.T_DEFAULT)) {
                    consume(Token.Type.T_COLON, "Expected ':' after default");
                    defaultCase = new ArrayList<>();
                    
                    while (!check(Token.Type.T_CASE) && !check(Token.Type.T_RBRACE)) {
                        if (match(Token.Type.T_NEWLINE)) {
                            continue;
                        }
                        defaultCase.add(statement());
                    }
                } else {
                    throw new RuntimeException("Expected 'case' or 'default' inside switch");
                }
            }
            consume(Token.Type.T_RBRACE, "Expected '}' at end of switch");
            return new AstNode.SwitchNode(expr, cases, defaultCase);
        }

        // Return statement: return value
        if (match(Token.Type.T_RETURN)) {
            return new AstNode.ReturnNode(expression());
        }

        // Assignment or expression statement
        AstNode expr = expression();
        if (match(Token.Type.T_EQ)) {
            if (!(expr instanceof AstNode.IdentNode)) {
                throw new RuntimeException("Cannot assign to non-variable on line " + cur.line);
            }
            return new AstNode.AssignNode(((AstNode.IdentNode) expr).name, expression());
        }
        return expr;
    }

    // Parse a block of statements: { stmt1 stmt2 ... }
    private List<AstNode> block() {
        List<AstNode> items = new ArrayList<>();
        while (!check(Token.Type.T_RBRACE) && !check(Token.Type.T_EOF)) {
            if (match(Token.Type.T_NEWLINE)) {
                continue;
            }
            items.add(statement());
        }
        consume(Token.Type.T_RBRACE, "Expected '}' at end of block");
        return items;
    }

    // Parse function definition: func name(params) { body }
    private AstNode functionDef() {
        String name = cur.lexeme;
        consume(Token.Type.T_IDENT, "Expected function name");
        consume(Token.Type.T_LPAREN, "Expected '('");
        
        List<String> params = new ArrayList<>();
        if (!check(Token.Type.T_RPAREN)) {
            do {
                params.add(cur.lexeme);
                consume(Token.Type.T_IDENT, "Expected parameter name");
            } while (match(Token.Type.T_COMMA));
        }
        
        consume(Token.Type.T_RPAREN, "Expected ')'");
        while (match(Token.Type.T_NEWLINE));
        consume(Token.Type.T_LBRACE, "Expected '{'");
        return new AstNode.FuncDefNode(name, params, block());
    }

    // Parse entire program
    public AstNode parseProgram() {
        List<AstNode> items = new ArrayList<>();
        
        while (!check(Token.Type.T_EOF)) {
            if (match(Token.Type.T_NEWLINE)) {
                continue;
            }
            if (match(Token.Type.T_FUNC)) {
                items.add(functionDef());
            } else {
                items.add(statement());
            }
        }
        return new AstNode.BlockNode(items);
    }
}