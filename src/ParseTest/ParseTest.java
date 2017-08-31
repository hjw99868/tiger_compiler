package ParseTest;

import java.util.Scanner;
import Parse.*;
import Absyn.*;

public class ParseTest{
    
	public static void main(String[] argv) throws java.io.IOException 
	{
		System.out.println("����������ļ�:");
		Scanner read = new Scanner(System.in);
		String filename = read.nextLine();         		//��������ļ�
		ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);  
		java.io.FileInputStream inp = new java.io.FileInputStream(filename);
		Lexer lexer = new yyLex(inp,errorMsg);
	    read.close();
	    //�﷨����
	    System.out.print("\n�﷨����\n");
	    parser par = new parser(lexer,errorMsg);
	    Print print = new Print(System.out);   //�����﷨��
	    //��ӡ�﷨��
	    try
	    {
	    	par.parse();
	        System.out.println("û�з����﷨���� "
	        		+ "\n\n"
	        		+ "�����﷨����");
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    print.prExp(par.parseResult,0);  
	   	}
}


