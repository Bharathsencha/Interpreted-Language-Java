# Blang Programming Language

A dynamically-typed interpreted programming language built with Java.

## Overview

Blang is a simple, dynamically-typed language with familiar syntax for variables, control flow, functions, and basic data structures. It includes a built-in interpreter and build system for easy compilation and execution.

## Project Structure

```
.
├── src/           # Java source files for the interpreter
├── class/         # Compiled Java class files
├── main.blang     # Example Blang program
└── Makefile       # Build automation
```

## Installation

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Make (for build automation)

### Build Instructions

Clone or download the repository, then compile the interpreter:

```bash
make
```

This creates the `class/` directory and compiles all Java source files.

## Usage

Run a Blang program:

```bash
make run
```

Or manually:

```bash
java -cp class Main your_program.blang
```

Clean compiled files:

```bash
make clean
```

## Language Reference

### Variables and Data Types

Variables are declared using the `let` keyword. Blang supports dynamic typing.

```javascript
let name = "Bharath"      # String
let age = 21              # Integer
let pi = 3.14             # Float
let is_active = true      # Boolean
let items = [1, 2, 3]     # List
```

### Input and Type Casting

The `input()` function returns a string. Use type casting functions for conversions:

```javascript
let val = input("Enter a number: ")
let num = int(val)
print("Type is:", typeof(num))
print("Length of name is:", len("Bharath"))
```

**Type Casting Functions:**
- `int(value)` - Convert to integer
- `float(value)` - Convert to float
- `string(value)` - Convert to string
- `typeof(value)` - Get type name
- `len(value)` - Get length of string or list

### Control Flow

#### If-Else Statement

```javascript
if (age >= 18) {
    print("Adult")
} else {
    print("Minor")
}
```

#### While Loop

```javascript
let i = 0
while (i < 10) {
    i = i + 1
    if (i == 3) { continue }
    if (i == 8) { break }
    print(i)
}
```

**Loop Control:**
- `break` - Exit the loop
- `continue` - Skip to next iteration

#### Switch Statement

```javascript
let choice = int(input("1 or 2? "))
switch (choice) {
    case 1:
        print("Selected One")
        break
    case 2:
        print("Selected Two")
        break
    default:
        print("Invalid Choice")
}
```

### Lists

Create and modify lists using built-in functions:

```javascript
let my_list = []
append(my_list, "Apple")
print(my_list)  # ["Apple"]
```

### Functions

Define functions using the `func` keyword:

```javascript
func greet(n) {
    print("Hello", n)
}

greet("User")
```

## Makefile Commands

The included Makefile provides convenient build commands:

```makefile
make          # Compile all Java sources
make run      # Compile and execute main.blang
make clean    # Remove compiled class files
```

## Example Program

```javascript
let name = input("Enter your name: ")
let age = int(input("Enter your age: "))

if (age >= 18) {
    print("Welcome", name, "you are an adult")
} else {
    print("Hello", name, "you are a minor")
}

let numbers = [1, 2, 3, 4, 5]
let i = 0
while (i < len(numbers)) {
    print("Number:", numbers[i])
    i = i + 1
}
```

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0).

You are free to:
- Use this software for any purpose
- Change the software to suit your needs
- Share the software with others
- Share the changes you make

Under the following terms:
- If you distribute this software, you must make the source code available
- If you modify this software and distribute it, you must license your modifications under GPL-3.0
- You must include a copy of the GPL-3.0 license and copyright notice

See the [LICENSE](LICENSE) file for the full license text, or visit https://www.gnu.org/licenses/gpl-3.0.en.html
