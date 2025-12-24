/*
 * AstNode.java
 *
 * Defines all Abstract Syntax Tree (AST) node types for Blang.
 * Each node represents a concrete language construct produced by the parser
 * and consumed by the interpreter.
 */
import java.util.ArrayList;
import java.util.List;

public abstract class AstNode {
    // Line number in source file where this node appears, used for error reporting
    public int line;

    // Helper class for managing lists of AST nodes
    public static class NodeList extends ArrayList<AstNode> {}

    // Literal value nodes - represent constant values in the source code
    
    // Represents an integer literal like 42 or -10
    public static class NumberNode extends AstNode {
        public int value;
        
        public NumberNode(int v) {
            this.value = v;
        }
    }

    // Represents a floating point literal like 3.14 or -0.5
    public static class FloatNode extends AstNode {
        public double value;
        
        public FloatNode(double v) {
            this.value = v;
        }
    }

    // Represents a string literal like "hello" or "Bharath"
    public static class StringNode extends AstNode {
        public String value;
        
        public StringNode(String v) {
            this.value = v;
        }
    }

    // Represents a boolean literal: true or false
    public static class BoolNode extends AstNode {
        public boolean value;
        
        public BoolNode(boolean v) {
            this.value = v;
        }
    }

    // Represents an identifier (variable name) like age, name, or counter
    public static class IdentNode extends AstNode {
        public String name;
        
        public IdentNode(String name) {
            this.name = name;
        }
    }

    // Represents a list literal like [1, 2, 3] or ["a", "b", "c"]
    public static class ListNode extends AstNode {
        public List<AstNode> items;
        
        public ListNode(List<AstNode> items) {
            this.items = items;
        }
    }

    // Expression nodes - represent operations and computations
    
    // Represents binary operations like addition, subtraction, comparison
    // Examples: x + y, a * b, age >= 18
    public static class BinOpNode extends AstNode {
        public String op;        // The operator: +, -, *, /, ==, !=, <, >, <=, >=, and, or
        public AstNode left;     // Left operand expression
        public AstNode right;    // Right operand expression
        
        public BinOpNode(String op, AstNode left, AstNode right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }
    }

    // Represents array/list indexing like items[0] or name[2]
    public static class IndexNode extends AstNode {
        public AstNode target;   // The list or string being indexed
        public AstNode index;    // The index expression
        
        public IndexNode(AstNode target, AstNode index) {
            this.target = target;
            this.index = index;
        }
    }

    // Statement nodes - represent commands and declarations
    
    // Represents variable declaration: let name = "Bharath"
    public static class LetNode extends AstNode {
        public String name;      // Variable name being declared
        public AstNode expr;     // Initial value expression
        
        public LetNode(String name, AstNode expr) {
            this.name = name;
            this.expr = expr;
        }
    }

    // Represents variable assignment: name = "John"
    public static class AssignNode extends AstNode {
        public String name;      // Variable name being assigned to
        public AstNode expr;     // New value expression
        
        public AssignNode(String name, AstNode expr) {
            this.name = name;
            this.expr = expr;
        }
    }

    // Represents print statement: print("Hello", name, age)
    public static class PrintNode extends AstNode {
        public List<AstNode> args;   // List of expressions to print
        
        public PrintNode(List<AstNode> args) {
            this.args = args;
        }
    }

    // Represents input statement: input("Enter name: ")
    public static class InputNode extends AstNode {
        public AstNode expr;     // Prompt message expression
        
        public InputNode(AstNode expr) {
            this.expr = expr;
        }
    }

    // Control flow nodes - represent conditional and looping constructs
    
    // Represents if-else statement: if (condition) { ... } else { ... }
    public static class IfNode extends AstNode {
        public AstNode cond;              // Condition expression
        public List<AstNode> thenBlock;   // Statements to execute if condition is true
        public List<AstNode> elseBlock;   // Statements to execute if condition is false
        
        public IfNode(AstNode cond, List<AstNode> thenBlock, List<AstNode> elseBlock) {
            this.cond = cond;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }
    }

    // Represents while loop: while (condition) { ... }
    public static class WhileNode extends AstNode {
        public AstNode cond;          // Loop condition expression
        public List<AstNode> body;    // Statements to execute in each iteration
        
        public WhileNode(AstNode cond, List<AstNode> body) {
            this.cond = cond;
            this.body = body;
        }
    }

    // Represents switch statement: switch (expr) { case 1: ... default: ... }
    public static class SwitchNode extends AstNode {
        public AstNode expr;                 // Expression being switched on
        public List<CaseNode> cases;         // List of case branches
        public List<AstNode> defaultCase;    // Default case statements
        
        public SwitchNode(AstNode expr, List<CaseNode> cases, List<AstNode> defaultCase) {
            this.expr = expr;
            this.cases = cases;
            this.defaultCase = defaultCase;
        }
    }

    // Represents a single case in a switch statement: case value: { ... }
    public static class CaseNode extends AstNode {
        public AstNode value;         // Value to match against
        public List<AstNode> body;    // Statements to execute if matched
        
        public CaseNode(AstNode value, List<AstNode> body) {
            this.value = value;
            this.body = body;
        }
    }

    // Represents a block of statements enclosed in braces: { ... }
    public static class BlockNode extends AstNode {
        public List<AstNode> items;   // List of statements in the block
        
        public BlockNode(List<AstNode> items) {
            this.items = items;
        }
    }

    // Function definition and call nodes
    
    // Represents function definition: func greet(name) { ... }
    public static class FuncDefNode extends AstNode {
        public String name;              // Function name
        public List<String> params;      // Parameter names
        public List<AstNode> body;       // Function body statements
        
        public FuncDefNode(String name, List<String> params, List<AstNode> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }
    }

    // Represents function call: greet("John")
    public static class CallNode extends AstNode {
        public String name;          // Function name being called
        public List<AstNode> args;   // Argument expressions
        
        public CallNode(String name, List<AstNode> args) {
            this.name = name;
            this.args = args;
        }
    }

    // Represents return statement: return value
    public static class ReturnNode extends AstNode {
        public AstNode expr;    // Expression to return, null for empty return
        
        public ReturnNode(AstNode expr) {
            this.expr = expr;
        }
    }

    // Control flow signal nodes - represent loop control statements
    
    // Represents break statement to exit a loop or switch case
    public static class BreakNode extends AstNode {}
    
    // Represents continue statement to skip to next loop iteration
    public static class ContinueNode extends AstNode {}
}