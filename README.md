## Introduction

This project is a full compiler for the PL/0 programming language, written entirely in Java.

PL/0 is similar to but much simpler than the general-purpose programming language Pascal, intended as an educational programming language. It serves as an example of how to construct a compiler.


## How the compiler works

A compiler is a program that turns a programming language into machine language or other languages. In this project, I’m going to compile the programming language into assembly and then into machine language.

The compiler can be divided into three components:

- Lexer
- Parser
- Code Generator

### Lexer
The first component the lexer or scanner. Its role is to take the program as input and divide it into tokens. A token is a  group of characters forming a basic unit of syntax, such as a identifier, number, etc.

### Parser
The second component is the parser. Its role is to do a syntax check of the program. It takes the list of tokens as input and creates an abstract syntax tree as output.

### Code Generator
The third and last component is the code generator. Its role is to transform the syntax tree created from the parser into machine language. In this case, it’s going to transform the syntax tree into assembly.


## Instructions

Clone the project and compile the source code.

Run the generated .jar and enter the PL/0 example file name.

```
java -jar /path/to/compiler.jar
```

```
Enter the source file name in PL/0: example.pl0
```

You can finally run the executable file compiled from the initial program.

```
/path/to/example.exe
```


