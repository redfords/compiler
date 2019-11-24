package compiler;

import java.io.Serializable;

public class SymbolTable implements Serializable {

    private String name;
    private Terminal type;
    private int value;

    public SymbolTable(String name, Terminal type, int value ){
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Terminal getType() {
        return type;
    }

    public void setType(Terminal type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
