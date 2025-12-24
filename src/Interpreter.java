/*
 * Interpreter.java
 *
 * Executes the Abstract Syntax Tree produced by the parser.
 * Handles scoping, control flow, function calls, and expression evaluation
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;

public class Interpreter {
    
    // Environment manages variable and function scopes
    // Each scope can have a parent scope for nested contexts
    private static class Environment {
        private final Map<String, Value> vars = new HashMap<>();
        private final Map<String, AstNode.FuncDefNode> funcs = new HashMap<>();
        private final Environment parent;

        public Environment(Environment parent) {
            this.parent = parent;
        }

        // Define a new variable in the current scope
        public void define(String name, Value val) {
            vars.put(name, val);
        }

        // Assign to an existing variable, searching parent scopes if needed
        public void assign(String name, Value val) {
            if (vars.containsKey(name)) {
                vars.put(name, val);
                return;
            }
            if (parent != null) {
                parent.assign(name, val);
                return;
            }
            throw new RuntimeException("Runtime Error: Undefined variable '" + name + "'");
        }

        // Get variable value, searching parent scopes if needed
        public Value get(String name) {
            if (vars.containsKey(name)) {
                return vars.get(name);
            }
            if (parent != null) {
                return parent.get(name);
            }
            return Value.makeNull();
        }

        // Define a function in the current scope
        public void defineFunc(String name, AstNode.FuncDefNode def) {
            funcs.put(name, def);
        }

        // Get function definition, searching parent scopes if needed
        public AstNode.FuncDefNode getFunc(String name) {
            if (funcs.containsKey(name)) {
                return funcs.get(name);
            }
            if (parent != null) {
                return parent.getFunc(name);
            }
            return null;
        }
    }

    // Control flow signals implemented as exceptions
    // These allow break, continue, and return to jump out of nested contexts
    
    // Signal for return statement, carries the return value
    private static class ReturnSignal extends RuntimeException {
        public final Value value;
        
        public ReturnSignal(Value value) {
            this.value = value;
        }
    }
    
    // Signal for break statement in loops and switch cases
    private static class BreakSignal extends RuntimeException {}
    
    // Signal for continue statement in loops
    private static class ContinueSignal extends RuntimeException {}

    // Scanner for reading user input
    private final Scanner scanner = new Scanner(System.in);

    // Main entry point to interpret a program
    public void interpret(AstNode program) {
        Environment global = new Environment(null);
        try {
            execute(program, global);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    // Execute a single AST node in the given environment
    // Returns the value produced by the node
    private Value execute(AstNode node, Environment env) {
        if (node == null) {
            return Value.makeNull();
        }

        // Handle input() function - reads a line from stdin
        // Returns a string value by default
        if (node instanceof AstNode.InputNode) {
            AstNode.InputNode inNode = (AstNode.InputNode) node;
            if (inNode.expr != null) {
                Value promptVal = execute(inNode.expr, env);
                System.out.print(promptVal.toString());
                System.out.flush();
            }
            if (scanner.hasNextLine()) {
                return Value.makeString(scanner.nextLine());
            }
            return Value.makeString("");
        }

        // Handle list literals like [1, 2, 3]
        // Creates a new list value and evaluates each element
        if (node instanceof AstNode.ListNode) {
            Value list = Value.makeList();
            for (AstNode item : ((AstNode.ListNode) node).items) {
                list.addToList(execute(item, env));
            }
            return list;
        }

        // Handle literal values - these simply return their wrapped value
        if (node instanceof AstNode.NumberNode) {
            return Value.makeInt(((AstNode.NumberNode) node).value);
        }
        if (node instanceof AstNode.FloatNode) {
            return Value.makeFloat(((AstNode.FloatNode) node).value);
        }
        if (node instanceof AstNode.StringNode) {
            return Value.makeString(((AstNode.StringNode) node).value);
        }
        if (node instanceof AstNode.BoolNode) {
            return Value.makeBool(((AstNode.BoolNode) node).value);
        }
        
        // Handle variable references - look up the value in the environment
        if (node instanceof AstNode.IdentNode) {
            return env.get(((AstNode.IdentNode) node).name);
        }

        // Handle binary operations like +, -, *, /, ==, <, >, and, or
        if (node instanceof AstNode.BinOpNode) {
            AstNode.BinOpNode bin = (AstNode.BinOpNode) node;
            return evaluateBinOp(bin.op, execute(bin.left, env), execute(bin.right, env));
        }

        // Handle variable declaration: let name = value
        if (node instanceof AstNode.LetNode) {
            AstNode.LetNode let = (AstNode.LetNode) node;
            env.define(let.name, execute(let.expr, env));
            return Value.makeNull();
        }

        // Handle variable assignment: name = value
        if (node instanceof AstNode.AssignNode) {
            AstNode.AssignNode assign = (AstNode.AssignNode) node;
            env.assign(assign.name, execute(assign.expr, env));
            return Value.makeNull();
        }

        // Handle print statement - outputs all arguments separated by spaces
        if (node instanceof AstNode.PrintNode) {
            for (AstNode arg : ((AstNode.PrintNode) node).args) {
                System.out.print(execute(arg, env).toString() + " ");
            }
            System.out.println();
            return Value.makeNull();
        }

        // Handle if-else statement
        // Creates new scope for each branch to isolate variables
        if (node instanceof AstNode.IfNode) {
            AstNode.IfNode ifn = (AstNode.IfNode) node;
            if (execute(ifn.cond, env).asBool()) {
                executeBlock(ifn.thenBlock, new Environment(env));
            } else {
                executeBlock(ifn.elseBlock, new Environment(env));
            }
            return Value.makeNull();
        }

        // Handle while loop
        // Creates new scope for each iteration
        // Catches break and continue signals to control loop flow
        if (node instanceof AstNode.WhileNode) {
            AstNode.WhileNode whn = (AstNode.WhileNode) node;
            while (execute(whn.cond, env).asBool()) {
                try {
                    executeBlock(whn.body, new Environment(env));
                } catch (BreakSignal b) {
                    break;
                } catch (ContinueSignal c) {
                    continue;
                }
            }
            return Value.makeNull();
        }

        // Handle switch statement
        // Evaluates expression and compares with each case value
        // Executes matching case or default case if no match
        if (node instanceof AstNode.SwitchNode) {
            AstNode.SwitchNode sw = (AstNode.SwitchNode) node;
            Value target = execute(sw.expr, env);
            boolean matched = false;

            for (AstNode.CaseNode c : sw.cases) {
                Value caseVal = execute(c.value, env);
                // Compare values using string representation
                if (target.toString().equals(caseVal.toString())) {
                    try {
                        executeBlock(c.body, new Environment(env));
                    } catch (BreakSignal b) {
                        // Break exits the switch
                    }
                    matched = true;
                    break;
                }
            }

            // Execute default case if no case matched
            if (!matched && sw.defaultCase != null) {
                try {
                    executeBlock(sw.defaultCase, new Environment(env));
                } catch (BreakSignal b) {
                    // Break exits the switch
                }
            }
            return Value.makeNull();
        }

        // Handle block of statements
        if (node instanceof AstNode.BlockNode) {
            executeBlock(((AstNode.BlockNode) node).items, new Environment(env));
            return Value.makeNull();
        }

        // Handle function definition - stores function in environment
        if (node instanceof AstNode.FuncDefNode) {
            env.defineFunc(((AstNode.FuncDefNode) node).name, (AstNode.FuncDefNode) node);
            return Value.makeNull();
        }

        // Handle function call - delegates to handleCall
        if (node instanceof AstNode.CallNode) {
            return handleCall((AstNode.CallNode) node, env);
        }
        
        // Handle return statement - throws signal with return value
        if (node instanceof AstNode.ReturnNode) {
            throw new ReturnSignal(execute(((AstNode.ReturnNode) node).expr, env));
        }
        
        // Handle break statement - throws signal for nearest loop/switch
        if (node instanceof AstNode.BreakNode) {
            throw new BreakSignal();
        }
        
        // Handle continue statement - throws signal for nearest loop
        if (node instanceof AstNode.ContinueNode) {
            throw new ContinueSignal();
        }

        return Value.makeNull();
    }

    // Execute a block of statements sequentially
    private void executeBlock(List<AstNode> block, Environment env) {
        for (AstNode stmt : block) {
            execute(stmt, env);
        }
    }

    // Handle function calls including built-in functions
    private Value handleCall(AstNode.CallNode call, Environment env) {
        // Built-in type conversion functions
        if (call.name.equals("int")) {
            return Value.makeInt(execute(call.args.get(0), env).asInt());
        }
        if (call.name.equals("float")) {
            return Value.makeFloat(execute(call.args.get(0), env).asFloat());
        }
        if (call.name.equals("string")) {
            return Value.makeString(execute(call.args.get(0), env).toString());
        }
        
        // Built-in typeof() function - returns type name as string
        if (call.name.equals("typeof")) {
            return Value.makeString(execute(call.args.get(0), env).getTypeName());
        }

        // Built-in len() function - returns length of string or list
        if (call.name.equals("len")) {
            Value v = execute(call.args.get(0), env);
            if (v.getType() == Value.Type.VAL_STRING) {
                return Value.makeInt(v.toString().length());
            }
            if (v.getType() == Value.Type.VAL_LIST) {
                return Value.makeInt(v.asList().size());
            }
            return Value.makeInt(0);
        }

        // Built-in append() function - adds element to list
        if (call.name.equals("append")) {
            if (call.args.size() < 2) {
                throw new RuntimeException("Runtime Error: append() requires 2 arguments");
            }
            
            Value listVal = execute(call.args.get(0), env);
            if (listVal.getType() == Value.Type.VAL_LIST) {
                listVal.addToList(execute(call.args.get(1), env).copy());
                return Value.makeNull();
            }
            throw new RuntimeException("Runtime Error: First argument to append() must be a list");
        }

        // User-defined function call
        AstNode.FuncDefNode func = env.getFunc(call.name);
        if (func != null) {
            // Create new scope for function execution
            Environment scope = new Environment(env);
            
            // Bind arguments to parameters
            for (int i = 0; i < func.params.size(); i++) {
                Value val = i < call.args.size() ? execute(call.args.get(i), env) : Value.makeNull();
                scope.define(func.params.get(i), val);
            }
            
            // Execute function body and catch return signal
            try {
                executeBlock(func.body, scope);
            } catch (ReturnSignal rs) {
                return rs.value;
            }
            return Value.makeNull();
        }
        
        throw new RuntimeException("Runtime Error: Undefined function '" + call.name + "'");
    }

    // Evaluate binary operations between two values
    private Value evaluateBinOp(String op, Value l, Value r) {
        // Logical operators
        if (op.equals("&&")) {
            return Value.makeBool(l.asBool() && r.asBool());
        }
        if (op.equals("||")) {
            return Value.makeBool(l.asBool() || r.asBool());
        }
        
        // Equality operators - compare string representations
        if (op.equals("==")) {
            return Value.makeBool(l.toString().equals(r.toString()));
        }
        if (op.equals("!=")) {
            return Value.makeBool(!l.toString().equals(r.toString()));
        }

        // Numeric operations - work with both int and float
        if ((l.getType() == Value.Type.VAL_INT || l.getType() == Value.Type.VAL_FLOAT) &&
            (r.getType() == Value.Type.VAL_INT || r.getType() == Value.Type.VAL_FLOAT)) {
            
            double dl = l.asFloat();
            double dr = r.asFloat();
            boolean isF = (l.getType() == Value.Type.VAL_FLOAT || r.getType() == Value.Type.VAL_FLOAT);
            
            switch (op) {
                case "+":
                    if (isF) {
                        return Value.makeFloat(dl + dr);
                    } else {
                        return Value.makeInt((int)(dl + dr));
                    }
                case "-":
                    if (isF) {
                        return Value.makeFloat(dl - dr);
                    } else {
                        return Value.makeInt((int)(dl - dr));
                    }
                case "*":
                    if (isF) {
                        return Value.makeFloat(dl * dr);
                    } else {
                        return Value.makeInt((int)(dl * dr));
                    }
                case "/":
                    if (dr == 0) {
                        return Value.makeFloat(0);
                    } else {
                        return Value.makeFloat(dl / dr);
                    }
                case "<":
                    return Value.makeBool(dl < dr);
                case ">":
                    return Value.makeBool(dl > dr);
                case "<=":
                    return Value.makeBool(dl <= dr);
                case ">=":
                    return Value.makeBool(dl >= dr);
            }
        }
        
        // String concatenation with + operator
        if (op.equals("+") && (l.getType() == Value.Type.VAL_STRING || r.getType() == Value.Type.VAL_STRING)) {
            return Value.makeString(l.toString() + r.toString());
        }
        
        return Value.makeNull();
    }
}