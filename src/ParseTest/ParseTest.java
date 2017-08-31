package ParseTest;

import java.util.Scanner;
import Parse.*;
import Absyn.*;

public class ParseTest{
    
	public static void main(String[] argv) throws java.io.IOException 
	{
		System.out.println("请输入测试文件:");
		Scanner read = new Scanner(System.in);
		String filename = read.nextLine();         		//输入测试文件
		ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);  
		java.io.FileInputStream inp = new java.io.FileInputStream(filename);
		Lexer lexer = new yyLex(inp,errorMsg);
	    read.close();
	    //语法分析
	    System.out.print("\n语法分析\n");
	    parser par = new parser(lexer,errorMsg);
	    Print print = new Print(System.out);   //生成语法树
	    //打印语法树
	    try
	    {
	    	par.parse();
	        System.out.println("没有发现语法错误！ "
	        		+ "\n\n"
	        		+ "生成语法树：");
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    print.prExp(par.parseResult,0);  
	   	}
}


