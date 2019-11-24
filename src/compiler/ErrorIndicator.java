package compiler;

public class ErrorIndicator {
    
    public void display(int code) {
        System.err.print("ERROR: ");
        switch (code) {
            case 1:
                System.err.println("Dot (.) expected");
                break;
            case 2:
                System.err.println("Equal by definition (:=) expected");
                break;
            case 3:
                System.err.println("Number expected ");
                break;
            case 4:
                System.err.println("Semicolon (;) expected");
                break;
            case 5:
                System.err.println("CALL expected");
                break;
            case 6:
                System.err.println("END expected");
                break;
            case 7:
                System.err.println("THEN expected");
                break;
            case 8:
                System.err.println("DO expected");
                break;
            case 9:
                System.err.println("Parenthesis expected");
                break;
            case 10:
                System.err.println("Parenthesis expected");
                break;
            case 11:
                System.err.println("Logical operator expected");
                break;
            case 12:
                System.err.println("Identifier expected");
                break;
            case 16:
                System.err.println("The indentifier already exists");
                break;
            case 17:
                System.err.println("Undeclared Indentifier");
                break;
            case 18:
                System.err.println("Indicator type error");
                break;
            case 21:
                System.err.println("Number out of range");
                break;
            case 22:
                System.err.println("Identifier is too long");
                break;
            case 23:
                System.err.println("String is too long");
                break;
            case 25:
                System.err.println("I/O exception");
                break;
            default:
                System.err.println("Undefined error ");
                break;
        }
        System.exit(1);
    }

}
