# Variables
JC = javac
JVM = java
SRCDIR = src
OUTDIR = class
MAIN = Main
TARGET_FILE = main.blang

# Default target: Compile all java files
all: compile

# Create output directory and compile
compile:
	mkdir -p $(OUTDIR)
	$(JC) -d $(OUTDIR) $(SRCDIR)/*.java

# Run the interpreter with main.blang
run: compile
	$(JVM) -cp $(OUTDIR) $(MAIN) $(TARGET_FILE)

# Remove the class directory
clean:
	rm -rf $(OUTDIR)

.PHONY: all compile run clean