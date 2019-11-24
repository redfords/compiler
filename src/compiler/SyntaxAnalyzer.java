package compiler;

import java.io.IOException;

public class SyntaxAnalyzer {

    private final Scanner scanner;
    private final Parser parser;
    private final CodeGenerator codeGen;
    private final ErrorIndicator errorIndicator;

    public SyntaxAnalyzer(Scanner scanner, Parser parser, CodeGenerator codeGen, ErrorIndicator errorIndicator) {
        this.scanner = scanner;
        this.parser = parser;
        this.codeGen = codeGen;
        this.errorIndicator = errorIndicator;
    }

    SyntaxAnalyzer(java.util.Scanner scanner, Parser parser, CodeGenerator codeGen, ErrorIndicator errorIndicator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void analyze() throws IOException {
        scanner.scan();
        program();
        codeGen.push();
    }

    private void program() throws IOException {
        codeGen.loadByte(0xBF);
        codeGen.loadInt(0);
        
        block(0);
        if(scanner.getS().equals(Terminal.STOP)){
            scanner.scan();
            codeGen.loadByte(0xE9);
            int aux = codeGen.getMemoryStop() + 4;
            codeGen.loadInt(ConstValues.END_PROGRAM - aux);
            codeGen.loadInt(0x701, codeGen.getSize(212) + codeGen.getSize(200) - 0x700 + codeGen.getMemoryStop());
            for(int i=0; i< parser.getVarValue() / 4; i++)
                codeGen.loadInt(0);
            int textSize = codeGen.getMemoryStop() - ConstValues.HEADER_SIZE;
            codeGen.loadInt(ConstValues.VIRTUAL_SIZE, textSize);
            int fileAlignment = codeGen.getSize(ConstValues.FILE_ALIGNMENT);
            while(codeGen.getMemoryStop() % fileAlignment != 0)
                codeGen.loadByte(0);
            textSize = codeGen.getMemoryStop() - ConstValues.HEADER_SIZE;
            codeGen.loadInt(ConstValues.CODE_SECTION_SIZE, textSize);
            codeGen.loadInt(ConstValues.RAW_DATA_SIZE, textSize);
            int sectionAlignment = codeGen.getSize(ConstValues.SECTION_ALIGNMENT);
            int count = (2 + textSize / sectionAlignment ) * sectionAlignment;
            codeGen.loadInt(ConstValues.IMAGE_SIZE, count);
            codeGen.loadInt(ConstValues.BASE_OF_DATA, count);
        }else {errorIndicator.display(1);}
    }
        
    private void block(int base) throws IOException{
        codeGen.loadByte(0xE9);codeGen.loadInt(0);
        int displacement = 0;
        int initialPosition = codeGen.getMemoryStop();
            if(scanner.getS().equals(Terminal.CONST)){
                    do{
                        scanner.scan();
                        if(scanner.getS().equals(Terminal.IDENTIFIER)){
                            String identifier = scanner.getString();
                            scanner.scan();
                            if((scanner.getS().equals(Terminal.EQUAL) || scanner.getS().equals(Terminal.ASSIGNMENT))){
                                scanner.scan();
                                if(scanner.getS().equals(Terminal.MINUS)) {
                                    scanner.scan();
                                    if(scanner.getS().equals(Terminal.NUMBER)){
                                        if(parser.find(identifier, base + displacement - 1, base) == null){
                                            int negative = Integer.parseInt(scanner.getString()) * (-1);
                                            parser.add(base + displacement, identifier, Terminal.CONST, negative);
                                            displacement++; }
                                    }else {errorIndicator.display(3);}
                                }
                                else if(scanner.getS().equals(Terminal.NUMBER)){
                                    if(parser.find(identifier, base + displacement - 1, base) == null){
                                        int value = Integer.parseInt(scanner.getString());
                                        parser.add(base + displacement, identifier, Terminal.CONST, value);
                                        displacement++;
                                    }else {errorIndicator.display(16);}
                                }else {errorIndicator.display(3);}
                            }else {errorIndicator.display(2);}
                        }else {errorIndicator.display(17);}
                        scanner.scan();
                    }while(scanner.getS().equals(Terminal.COMMA));
                    if(scanner.getS().equals(Terminal.SEMICOLON))
                        {scanner.scan();}
                    else {errorIndicator.display(4);}
                    }
            if( scanner.getS().equals(Terminal.VAR)){
                do{
                    scanner.scan();
                    if(scanner.getS().equals(Terminal.IDENTIFIER)){
                        if(parser.find(scanner.getString(), base + displacement - 1, base) == null){
                            parser.add(base + displacement, scanner.getString(), Terminal.VAR, parser.nextValue());
                            displacement++;
                        }else {errorIndicator.display(17);}
                    }else {errorIndicator.display(12);}
                    scanner.scan();                    
                }while(scanner.getS().equals(Terminal.COMMA));
                if(scanner.getS().equals(Terminal.SEMICOLON))
                    {scanner.scan();}
                else {errorIndicator.display(4);}
            }
            
            if(scanner.getS().equals(Terminal.PROCEDURE)){
                while(scanner.getS().equals(Terminal.PROCEDURE)){
                    scanner.scan();
                    if(scanner.getS().equals(Terminal.IDENTIFIER)){
                        if(parser.find(scanner.getString(), base + displacement - 1, base) == null){
                         parser.add(base + displacement, scanner.getString(), Terminal.PROCEDURE, codeGen.getMemoryStop());
                         displacement++;
                        }else {errorIndicator.display(16);}
                        scanner.scan();
                        if(scanner.getS().equals(Terminal.SEMICOLON)){
                            scanner.scan();
                            block(base + displacement);
                            codeGen.loadByte(ConstValues.RET);
                            if(scanner.getS().equals(Terminal.SEMICOLON))
                            {scanner.scan();}
                            else {errorIndicator.display(4);}
                        } else {errorIndicator.display(4);}
                    }else {errorIndicator.display(17);}
                }
            }
        int range = codeGen.getMemoryStop() - initialPosition;
        if(range != 0)  codeGen.loadInt(initialPosition - 4, range);
            else codeGen.setMemoryStop(codeGen.getMemoryStop() - 5);
        statement(base,displacement);
    }
    
    private void statement(int base, int displacement) throws IOException{
        SymbolTable aux;
        int fixupPrev, fixupPost;
        switch(scanner.getS()){
            case IDENTIFIER:
                aux = parser.find(scanner.getString(), base + displacement - 1 , 0);
                if(aux == null) {errorIndicator.display(17);}
                else if(aux.getType() != Terminal.VAR){
                    if(aux.getType() == Terminal.PROCEDURE)
                        {errorIndicator.display(5);}
                    else {errorIndicator.display(18);}
                }
                scanner.scan();
                if(scanner.getS().equals(Terminal.ASSIGNMENT) || scanner.getS().equals(Terminal.EQUAL)){
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x89);codeGen.loadByte(0x87);codeGen.loadInt(aux.getValue());
                    
                }else {errorIndicator.display(2);}
                break;
            case CALL:
                scanner.scan();
                if(scanner.getS().equals(Terminal.IDENTIFIER)){
                    aux = parser.find(scanner.getString(), base + displacement - 1 , 0);
                    if(aux == null){errorIndicator.display(17);}
                    else if (aux.getType() != Terminal.PROCEDURE)
                        {errorIndicator.display(18);}
                    codeGen.loadByte(0xE8);codeGen.loadInt(aux.getValue() - (codeGen.getMemoryStop() + 4));
                    scanner.scan();
                }
                break;    
            case BEGIN:
                scanner.scan();
                statement(base,displacement);
                while(scanner.getS().equals(Terminal.SEMICOLON)){
                    scanner.scan();
                    statement(base, displacement);
                }
                if(scanner.getS().equals(Terminal.END))
                    scanner.scan();
                else {errorIndicator.display(6);}
                break;
                
                case IF:
                    scanner.scan();
                    if(scanner.getS().equals(Terminal.NOT)){
                    scanner.scan();
                    if(scanner.getS().equals(Terminal.OPEN_PARENTHESIS)){
                        scanner.scan();
                        conditionNot(base, displacement);           
                            fixupPrev = codeGen.getMemoryStop();
                            if(scanner.getS().equals(Terminal.CLOSE_PARENTHESIS))
				{scanner.scan();}
                            else {errorIndicator.display(10);}
                            if(scanner.getS().equals(Terminal.THEN))
				{scanner.scan();}
                            else {errorIndicator.display(7);}
                            statement(base, displacement);
                            codeGen.loadByte(0xE9);codeGen.loadInt(0);
                            int fixupElse = codeGen.getMemoryStop();
                            codeGen.loadInt(fixupPrev - 4, (codeGen.getMemoryStop() - fixupPrev));
                            if(scanner.getS().equals(Terminal.ELSE)) {
				scanner.scan();
				statement(base, displacement);
                            }
			codeGen.loadInt(fixupElse - 4, (codeGen.getMemoryStop() - fixupElse));
                        }
                        else {errorIndicator.display(9);}
                    }	
                    else {
			condition(base, displacement);           
			fixupPrev = codeGen.getMemoryStop();
			if(scanner.getS().equals(Terminal.THEN))
				{scanner.scan();}
			else {errorIndicator.display(7);}
			statement(base, displacement);
			codeGen.loadByte(0xE9);codeGen.loadInt(0);
			int fixupElse = codeGen.getMemoryStop();
			codeGen.loadInt(fixupPrev - 4, (codeGen.getMemoryStop() - fixupPrev));
			if(scanner.getS().equals(Terminal.ELSE)) {
				scanner.scan();
				statement(base, displacement);
			}
			codeGen.loadInt(fixupElse - 4, (codeGen.getMemoryStop() - fixupElse));
                    }
                    break;
                
            case WHILE:
                scanner.scan();
                int address1 = codeGen.getMemoryStop();
                condition(base, displacement);
                int address2 = codeGen.getMemoryStop();
                if(scanner.getS().equals(Terminal.DO)){
                    scanner.scan();
                    statement(base, displacement);
                    codeGen.loadByte(0xE9);codeGen.loadInt(address1 - (codeGen.getMemoryStop() + 4));
                    codeGen.loadInt(address2 - 4, codeGen.getMemoryStop() - address2);
                }
                else {errorIndicator.display(8);}
                break;
                
            case READLN:
                scanner.scan();
                if(scanner.getS().equals(Terminal.OPEN_PARENTHESIS)){
                    do{
                        scanner.scan();
                        if(scanner.getS().equals(Terminal.IDENTIFIER)){
                            aux = parser.find(scanner.getString(), base + displacement - 1 , 0);
                            if(aux == null)
                                {errorIndicator.display(17);}
                            else if(aux.getType() != Terminal.VAR)
                                {errorIndicator.display(17);}
                            codeGen.loadByte(0xE8);codeGen.loadInt(ConstValues.READ_NUM - (codeGen.getMemoryStop() + 4));
                            codeGen.loadByte(0x89);codeGen.loadByte(0x87);codeGen.loadInt(aux.getValue());
                            scanner.scan();
                        }else {errorIndicator.display(12);}
                    }while(scanner.getS().equals(Terminal.COMMA));
                }else {errorIndicator.display(9);}
                if(scanner.getS().equals(Terminal.CLOSE_PARENTHESIS))
                {scanner.scan();}
                else {errorIndicator.display(10);}
                break;
            case WRITE:
                scanner.scan();
                if(scanner.getS().equals(Terminal.OPEN_PARENTHESIS)){
                    do{
                        scanner.scan();
                        if(scanner.getS().equals(Terminal.LITERAL_STRING)){
                            int stringSize = scanner.getString().length();
                            int baseOfCode = codeGen.getSize(ConstValues.BASE_OF_CODE);
                            int imageBase = codeGen.getSize(ConstValues.IMAGE_BASE);
                            int stringPosition = baseOfCode + imageBase - ConstValues.HEADER_SIZE + codeGen.getMemoryStop()+15;
                            codeGen.loadByte(0xB8);codeGen.loadInt(stringPosition);
                            codeGen.loadByte(0xE8);codeGen.loadInt(ConstValues.READ_EAX - (codeGen.getMemoryStop() + 4));
                            codeGen.loadByte(0xE9);
                            codeGen.loadInt(stringSize - 1);
                            for(int i=1; i<(stringSize - 1); i++){
                                codeGen.loadByte(scanner.getString().charAt(i));
                            }
                            codeGen.loadByte(0);
                            scanner.scan();
                        }else{
                            expression(base, displacement);
                            codeGen.PopEAX();
                            codeGen.loadByte(0xE8);
                            codeGen.loadInt(ConstValues.WRITE_NUM - (codeGen.getMemoryStop() + 4));
                        }
                    }while(scanner.getS().equals(Terminal.COMMA));
                }else {errorIndicator.display(9);}
                if(scanner.getS().equals(Terminal.CLOSE_PARENTHESIS))
                {scanner.scan();}
                else {errorIndicator.display(10);}
                break;
                
            case WRITELN:
                scanner.scan();
                if(scanner.getS().equals(Terminal.OPEN_PARENTHESIS)){
                    do{
                        scanner.scan();
                        if(scanner.getS().equals(Terminal.LITERAL_STRING)){
                            int stringSize = scanner.getString().length();
                            int baseOfCode = codeGen.getSize(ConstValues.BASE_OF_CODE);
                            int imageBase = codeGen.getSize(ConstValues.IMAGE_BASE);
                            int stringPosition = baseOfCode + imageBase - ConstValues.HEADER_SIZE + codeGen.getMemoryStop() + 15;
                            codeGen.loadByte(0xB8);codeGen.loadInt(stringPosition);
                            codeGen.loadByte(0xE8);
                            codeGen.loadInt(ConstValues.READ_EAX - (codeGen.getMemoryStop() + 4));
                            codeGen.loadByte(0xE9);
                            codeGen.loadInt(stringSize - 1);
                            for(int i=1; i<(stringSize - 1);i++){
                                codeGen.loadByte(scanner.getString().charAt(i));
                            }
                            codeGen.loadByte(0);
                            scanner.scan();
                        }else{
                            expression(base, displacement);
                            codeGen.PopEAX();
                            codeGen.loadByte(0xE8);
                            codeGen.loadInt(ConstValues.WRITE_NUM - (codeGen.getMemoryStop() + 4));
                        }
                    }while(scanner.getS().equals(Terminal.COMMA));
                    if(scanner.getS().equals(Terminal.CLOSE_PARENTHESIS))
                        {scanner.scan();}
                    else {errorIndicator.display(10);}
                }
                codeGen.loadByte(0xE8);
                codeGen.loadInt(ConstValues.LINE_JUMP - (codeGen.getMemoryStop() + 4));
                break;
            
            case HALT:
                codeGen.loadByte(0xE9);
                int endProgram = codeGen.getMemoryStop() + 4;
                codeGen.loadInt(ConstValues.END_PROGRAM - endProgram);
                scanner.scan();
                break;
        }        
    }
    
    private void condition(int base, int displacement) throws IOException{
        if(scanner.getS().equals(Terminal.ODD)){
            scanner.scan();
            expression(base, displacement);
            codeGen.PopEAX();
            codeGen.loadByte(0xA8);codeGen.loadByte(0x01);
            codeGen.loadByte(0x7B);codeGen.loadByte(0x05);
            codeGen.loadByte(0xE9);codeGen.loadInt(0);
        }else{
            expression(base, displacement);
            switch(scanner.getS()){
                case EQUAL:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x74);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case DIFFERENT:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x75);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case LESS:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7C);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case LESS_EQUAL:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7E);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case GREATER:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7F);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case GREATER_EQUAL:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7D);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                default:
                    {errorIndicator.display(11);}
            }
        }
    }
        
    private void conditionNot(int base, int displacement) throws IOException{
        if(scanner.getS().equals(Terminal.ODD)){
            scanner.scan();
            expression(base, displacement);
            codeGen.PopEAX();
            codeGen.loadByte(0xA8);codeGen.loadByte(0x01);
            codeGen.loadByte(0x7B);codeGen.loadByte(0x05);
            codeGen.loadByte(0xE9);codeGen.loadInt(0);
        }else{
            expression(base, displacement);
            switch(scanner.getS()){
                case EQUAL:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x75);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case DIFFERENT:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x74);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case LESS:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();//POP EAX
                    codeGen.loadByte(0x5B);//POP EBX
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7D);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case LESS_EQUAL:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7F);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case GREATER:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7E);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                case GREATER_EQUAL:
                    scanner.scan();
                    expression(base, displacement);
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x39);codeGen.loadByte(ConstValues.RET);
                    codeGen.loadByte(0x7C);codeGen.loadByte(0x05);
                    codeGen.loadByte(0xE9);codeGen.loadInt(0);
                    break;
                default:
                    {errorIndicator.display(11);}
            }
        }
    }
    
    private void expression(int base, int displacement) throws IOException{
        switch(scanner.getS()){
            case PLUS:
                scanner.scan();
                term(base,displacement);
                break;
            case MINUS:
                scanner.scan();
                term(base,displacement);
                codeGen.PopEAX();
                codeGen.loadByte(0xF7); codeGen.loadByte(0xD8);
                codeGen.loadByte(0x50);
                break;
            default:
                term(base,displacement);
                break;
        }
        
        while(scanner.getS().equals(Terminal.PLUS) || scanner.getS().equals(Terminal.MINUS)){
            String st = scanner.getS().toString();
            scanner.scan();
            term(base,displacement);
            if(st.equals("PLUS")){
                codeGen.PopEAX();
                codeGen.loadByte(0x5B);
                codeGen.loadByte(0x01); codeGen.loadByte(0xD8);
                codeGen.loadByte(0x50);
           }
           if(st.equals("MINUS")){
                codeGen.PopEAX();
                codeGen.loadByte(0x5B);
                codeGen.loadByte(0x93);
                codeGen.loadByte(0x29); codeGen.loadByte(0xD8);
                codeGen.loadByte(0x50);
           }
        }
    }
    
    private void term(int base, int displacement) throws IOException{
        factor(base, displacement);
        Terminal st;
        while(scanner.getS().equals(Terminal.BY) || scanner.getS().equals(Terminal.DIVIDED)){
            st = scanner.getS();
            scanner.scan();
            factor(base, displacement);
            switch(st){
                case BY:
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0xF7); codeGen.loadByte(0xEB);
                    codeGen.loadByte(0x50);
                    factor(base, displacement);
                    break;
                case DIVIDED:
                    codeGen.PopEAX();
                    codeGen.loadByte(0x5B);
                    codeGen.loadByte(0x93);
                    codeGen.loadByte(0x99);
                    codeGen.loadByte(0xF7); codeGen.loadByte(0xFB);
                    codeGen.loadByte(0x50);
                    factor(base, displacement);
                    break;
            }
        }
    }
    
    private void factor(int base,int displacement) throws IOException{
        SymbolTable aux;
        switch(scanner.getS()){
            case IDENTIFIER:
                aux = parser.find(scanner.getString(), base + displacement - 1, 0);
                if(aux == null) {errorIndicator.display(17);}
                else if(aux.getType() == Terminal.VAR){
                    codeGen.loadByte(0x8B);codeGen.loadByte(0x87);codeGen.loadInt(aux.getValue());
                    codeGen.loadByte(0x50);
                    scanner.scan();
                }
                else if(aux.getType() == Terminal.CONST){
                    codeGen.loadByte(0xB8);codeGen.loadInt(aux.getValue());
                    codeGen.loadByte(0x50);
                    scanner.scan();
                }                  
                else {errorIndicator.display(18);}
                break;
            case NUMBER: 
                codeGen.loadByte(0xB8);codeGen.loadInt(Integer.parseInt(scanner.getString()));
                codeGen.loadByte(0x50);
                scanner.scan();
                break;
            case OPEN_PARENTHESIS: 
                scanner.scan();
                expression(base,displacement);
                if(scanner.getS().equals(Terminal.CLOSE_PARENTHESIS))
                {scanner.scan();}
                else {errorIndicator.display(39);}
                break;
        }
    }
}

