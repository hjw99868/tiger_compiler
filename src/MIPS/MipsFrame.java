package MIPS;

import Frame.*;
import Temp.*;

public class MipsFrame extends Frame {
	public int allocDown = 0;
	public TempList argRegs = new TempList(new Temp(5), new TempList(new Temp(6), new TempList(new Temp(7), new TempList(new Temp(8), null))));
	private Temp fp = new Temp(0);
	private Temp sp = new Temp(1);
	private Temp ra = new Temp(2);
	private Temp rv = new Temp(3);
	private Temp zero = new Temp(4);
	private TempList calleeSaves = null;
	public TempList callerSaves = null;
	private int numOfcalleeSaves = 8;
	
	public MipsFrame()
	{
		for (int i = 9; i<= 18; i++)
			callerSaves = new TempList(new Temp(i), callerSaves);
		for (int i = 19; i<= 26; i++)
			calleeSaves = new TempList(new Temp(i), calleeSaves);
	}
	
	public Frame newFrame(Label name, Util.BoolList formals)
	{
		MipsFrame ret = new MipsFrame();
		ret.name = name; 
		TempList argReg = argRegs;
		for (Util.BoolList f = formals; f != null; f = f.tail, argReg = argReg.tail)
		{
			Access a = ret.allocLocal(f.head);
			ret.formals = new AccessList(a, ret.formals);
		}
		return ret;
	}
	
	public Access allocLocal(boolean escape) 
	{
		if (escape)
		{
			Access ret = new InFrame(this, allocDown);
			allocDown -= Translate.Library.WORDSIZE;
			return ret;	
		} 
		else 
			return new InReg();
	}
	
	public Tree.Stm procEntryExit1(Tree.Stm body)
	{
		Access fpAcc = allocLocal(true);
		Access raAcc = allocLocal(true);
		Access[] calleeAcc = new Access[numOfcalleeSaves];
		TempList calleeTemp = calleeSaves;
		for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail) 
		{
			calleeAcc[i] = allocLocal(true);
			body = new Tree.SEQ(new Tree.MOVE(calleeAcc[i].exp(new Tree.TEMP(fp)), new Tree.TEMP(calleeTemp.head)), body);
		}
		body = new Tree.SEQ(new Tree.MOVE(raAcc.exp(new Tree.TEMP(fp)), new Tree.TEMP(ra)), body);
		body = new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(fp), new Tree.BINOP(Tree.BINOP.PLUS, new Tree.TEMP(sp), new Tree.CONST(-allocDown - Translate.Library.WORDSIZE))), body);
		body = new Tree.SEQ(new Tree.MOVE(fpAcc.expFromStack(new Tree.TEMP(sp)), new Tree.TEMP(fp)), body);
		calleeTemp = calleeSaves; 
		for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail)
			body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(calleeTemp.head), calleeAcc[i].exp(new Tree.TEMP(fp))));
		body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(ra), raAcc.exp(new Tree.TEMP(fp))));
		body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(fp), fpAcc.expFromStack(new Tree.TEMP(sp))));
		return body;
	}

	public Temp FP()
	{
		return fp;
	}
	public Temp SP()
	{
		return sp;
	}
	public Temp RA()
	{
		return ra;
	}
	public Temp RV()
	{
		return rv;
	}
	public Tree.Exp externCall(String func, Tree.ExpList args)
	{
		return new Tree.CALL(new Tree.NAME(new Label(func)), args);
	}
	public String tempMap(Temp t)
	{
		if (t.toString().equals("t0"))
			return "$fp";
		if (t.toString().equals("t1"))
			return "$sp";
		if (t.toString().equals("t2"))
			return "$ra";
		if (t.toString().equals("t3"))
			return "$v0";
		if (t.toString().equals("t4"))
			return "$zero";
		
		for (int i = 5; i <= 8; i++)
			if (t.toString().equals("t" + i))
			{
				int r = i - 5;
				return "$a" + r;
			}
		for (int i = 9; i <= 18; i++)
			if (t.toString().equals("t" + i))
			{
				int r = i - 9;
				return "$t" + r;
			}
		for (int i = 19; i <= 26; i++)
			if (t.toString().equals("t" + i))
			{
				int r = i - 19;
				return "$s" + r;
			}
		return null;
	}
}
