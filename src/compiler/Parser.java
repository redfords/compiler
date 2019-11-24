package compiler;

public class Parser {
    
    private final ErrorIndicator ErrorIndicator;
    private final SymbolTable[] table;
    private int varValue = 0;
    
    public Parser(ErrorIndicator ErrorIndicator) {  
        this.ErrorIndicator = ErrorIndicator;
        this.table = new SymbolTable[ConstValues.MAX_QUANTITY_IDENT];
    }
    
    public SymbolTable find(String name, int begin, int end){
        for(int i=begin; i>=end; i--)
        {
            if(table[i].getName().equals(name.toLowerCase()))
                return table[i];
        }
        return null;
    }
    
    public void add(int index, String name, Terminal type, int value){
        table[index] = new SymbolTable(name.toLowerCase(), type, value);
    }
    
    public int nextValue(){
        varValue += 4;
        return varValue - 4;
    }
    
    public int getVarValue(){
        return varValue;
    }
    
}
