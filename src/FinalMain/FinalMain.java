package FinalMain;

import java.util.Scanner;
import Parse.*;
import Absyn.*;
import Semant.*;

public class FinalMain{

	public static void main(String[] argv) throws java.io.IOException 
	{
		System.out.println("����������ļ�:");
		Scanner read = new Scanner(System.in);
		String filename = read.nextLine();         		//��������ļ�
		ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);  
		java.io.FileInputStream inp = new java.io.FileInputStream(filename);
		Lexer lexer = new yyLex(inp,errorMsg);
	    java_cup.runtime.Symbol tok;
	    read.close();

	    //�ʷ�����
	    System.out.println("\n�ʷ�����:");
	    try
	    {
	    	do 
	    	{ 
	    	 tok=lexer.nextToken();
             if (symnames[tok.sym] == "ID")
	    	 System.out.println("Token(sym." + symnames[tok.sym] + ",\"" + tok.value + "\")");
             else
    	     System.out.println("Token(sym." + symnames[tok.sym] + "," + tok.value + ")");
	    	}
	    	while (tok.sym != sym.EOF);
	    	inp.close(); 
	      }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
        
	    //�﷨����
		inp = new java.io.FileInputStream(filename);
		lexer = new yyLex(inp,errorMsg);
	    System.out.print("\n�﷨����:\n");
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
        System.out.println("\n");
        
        //������������ͼ��
	    Frame.Frame frame = new MIPS.MipsFrame();
	    Translate.Translate translator = new Translate.Translate(frame);
	    Semant smt = new Semant(translator, errorMsg);
	    Frag.Frag frags = smt.transProg(par.parseResult);
	    if(ErrorMsg.ErrorMsg.anyErrors==false) System.out.println("No Error!Translating ......");
	    else return;
	    
	    //�м�������ɣ�����IR��
	    java.io.PrintStream irOut = new java.io.PrintStream(new java.io.FileOutputStream(filename + ".ir"));
		Tree.Print irprint;
		Tree.StmList stms;
		Canon.BasicBlocks b;
		Tree.StmList traced;
	    for(Frag.Frag f = frags; f!=null; f=f.next)
	    {
	    	Frag.ProcFrag pf = (Frag.ProcFrag)f;
		    irprint = new Tree.Print(irOut); 
			stms = Canon.Canon.linearize(pf.body); 
			b = new Canon.BasicBlocks(stms); 
			traced = (new Canon.TraceSchedule(b)).stms; 
			for(Tree.StmList l = traced; l!=null; l=l.tail)   irprint.prStm(l.head);
		 }
	}

	//��������ʷ��������ַ���������
	  static String symnames[] = new String[100];
	  static {	     
	     symnames[sym.FUNCTION] = "FUNCTION";
	     symnames[sym.EOF] = "EOF";
	     symnames[sym.INT] = "INT";
	     symnames[sym.GT] = "GT";
	     symnames[sym.DIVIDE] = "DIVIDE";
	     symnames[sym.COLON] = "COLON";
	     symnames[sym.ELSE] = "ELSE";
	     symnames[sym.OR] = "OR";
	     symnames[sym.NIL] = "NIL";
	     symnames[sym.DO] = "DO";
	     symnames[sym.GE] = "GE";
	     symnames[sym.error] = "error";
	     symnames[sym.LT] = "LT";
	     symnames[sym.OF] = "OF";
	     symnames[sym.MINUS] = "MINUS";
	     symnames[sym.ARRAY] = "ARRAY";
	     symnames[sym.TYPE] = "TYPE";
	     symnames[sym.FOR] = "FOR";
	     symnames[sym.TO] = "TO";
	     symnames[sym.TIMES] = "TIMES";
	     symnames[sym.COMMA] = "COMMA";
	     symnames[sym.LE] = "LE";
	     symnames[sym.IN] = "IN";
	     symnames[sym.END] = "END";
	     symnames[sym.ASSIGN] = "ASSIGN";
	     symnames[sym.STRING] = "STRING";
	     symnames[sym.DOT] = "DOT";
	     symnames[sym.LPAREN] = "LPAREN";
	     symnames[sym.RPAREN] = "RPAREN";
	     symnames[sym.IF] = "IF";
	     symnames[sym.SEMICOLON] = "SEMICOLON";
	     symnames[sym.ID] = "ID";
	     symnames[sym.WHILE] = "WHILE";
	     symnames[sym.LBRACK] = "LBRACK";
	     symnames[sym.RBRACK] = "RBRACK";
	     symnames[sym.NEQ] = "NEQ";
	     symnames[sym.VAR] = "VAR";
	     symnames[sym.BREAK] = "BREAK";
	     symnames[sym.AND] = "AND";
	     symnames[sym.PLUS] = "PLUS";
	     symnames[sym.LBRACE] = "LBRACE";
	     symnames[sym.RBRACE] = "RBRACE";
	     symnames[sym.LET] = "LET";
	     symnames[sym.THEN] = "THEN";
	     symnames[sym.EQ] = "EQ";
	     symnames[sym.NUM] = "NUM";
	     symnames[sym.STR] = "STR";
	   }
}


