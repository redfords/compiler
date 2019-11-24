package compiler;

public class ConstValues {
    
    final static int MAX_LENGHT_IDENT = 63;
    
    final static int MAX_SIZE_NUM = 2147483647;
    
    final static int MAX_QUANTITY_IDENT = 32767;
    
    final static int MAX_SIZE_STRING = 80;
    
    final static int MAX_SIZE_MEMORY = 32768;
    
    final static int HEADER_SIZE = 0x200;
    
    final static  int READ_EAX = 0x3E0;

    final static  int WRITE_NUM = 0x420;
    
    final static  int LINE_JUMP = 0x410;
    
    final static  int VIRTUAL_SIZE = 0x1A0;
    
    final static  int FILE_ALIGNMENT = 0xDC;
    
    final static  int CODE_SECTION_SIZE = 0xBC;
    
    final static  int RAW_DATA_SIZE = 0x1A8;
    
    final static  int IMAGE_SIZE = 0xF0;
    
    final static  int BASE_OF_DATA = 0xD0;
    
    final static  int SECTION_ALIGNMENT = 0xD8;
    
    final static int END_PROGRAM = 0x588;
    
    final static  int READ_NUM = 0x590;
    
    final static  int BASE_OF_CODE = 0xCC;
    
    final static  int IMAGE_BASE = 0xD4;
    
    final static  int RET = 0xC3;
    
}
