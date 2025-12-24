/*
 * Value.java
 *
 * Runtime value representation used by the interpreter.
 * Implements a dynamically typed value system with basic coercions.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Value {

    // All possible runtime value types
    public enum Type {
        VAL_INT,
        VAL_FLOAT,
        VAL_STRING,
        VAL_BOOL,
        VAL_LIST,
        VAL_NULL
    }

    // Tag indicating the active value type
    private final Type type;

    // Holds the actual data (boxed primitives, String, List<Value>, or null)
    private Object data;

    // Private constructor forces use of factory methods
    private Value(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    // Factory methods for creating typed values
    public static Value makeInt(int value) {
        return new Value(Type.VAL_INT, value);
    }

    public static Value makeFloat(double value) {
        return new Value(Type.VAL_FLOAT, value);
    }

    public static Value makeString(String value) {
        return new Value(Type.VAL_STRING, value != null ? value : "");
    }

    public static Value makeBool(boolean value) {
        return new Value(Type.VAL_BOOL, value);
    }

    public static Value makeList() {
        return new Value(Type.VAL_LIST, new ArrayList<Value>());
    }

    public static Value makeNull() {
        return new Value(Type.VAL_NULL, null);
    }

    public Type getType() {
        return type;
    }

    // Convert value to integer using loose coercion rules
    public int asInt() {
        if (type == Type.VAL_INT) return (int) data;
        if (type == Type.VAL_FLOAT) return (int) (double) data;
        if (type == Type.VAL_BOOL) return (boolean) data ? 1 : 0;
        if (type == Type.VAL_STRING) {
            try {
                return (int) Double.parseDouble((String) data);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // Convert value to floating point
    public double asFloat() {
        if (type == Type.VAL_FLOAT) return (double) data;
        if (type == Type.VAL_INT) return (double) (int) data;
        if (type == Type.VAL_BOOL) return (boolean) data ? 1.0 : 0.0;
        if (type == Type.VAL_STRING) {
            try {
                return Double.parseDouble((String) data);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    // Truthiness rules for conditional expressions
    public boolean asBool() {
        if (type == Type.VAL_BOOL) return (boolean) data;
        if (type == Type.VAL_INT) return (int) data != 0;
        return data != null && type != Type.VAL_NULL;
    }

    // Unsafe cast is intentional; caller must ensure type correctness
    @SuppressWarnings("unchecked")
    public List<Value> asList() {
        return (List<Value>) data;
    }

    // Deep copy for lists, shallow copy for primitives
    public Value copy() {
        if (type == Type.VAL_LIST) {
            Value newList = makeList();
            for (Value item : asList()) {
                newList.addToList(item.copy());
            }
            return newList;
        }
        return new Value(type, data);
    }

    // Append element to list value
    public void addToList(Value v) {
        if (type == Type.VAL_LIST) {
            asList().add(v);
        }
    }

    // String representation used for printing values
    @Override
    public String toString() {
        switch (type) {
            case VAL_INT:
            case VAL_FLOAT:
            case VAL_BOOL:
                return String.valueOf(data);

            case VAL_STRING:
                return (String) data;

            case VAL_LIST:
                return "[" + asList().stream()
                        .map(Value::toString)
                        .collect(Collectors.joining(", ")) + "]";

            default:
                return "null";
        }
    }

    // Human-readable type name (used for errors and debugging)
    public String getTypeName() {
        switch (type) {
            case VAL_INT:    return "int";
            case VAL_FLOAT:  return "float";
            case VAL_STRING: return "string";
            case VAL_BOOL:   return "bool";
            case VAL_LIST:   return "list";
            case VAL_NULL:   return "null";
            default:         return "unknown";
        }
    }
}
