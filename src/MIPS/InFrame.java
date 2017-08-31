package MIPS;

import Tree.*;

public class InFrame extends Frame.Access {
	private MipsFrame frame;
	public int offset;
	
	public InFrame(MipsFrame frame, int offset) 
	{
		this.frame = frame;
		this.offset = offset;
	}
	
	public Tree.Exp exp(Tree.Exp framePt) 
	{
		return new MEM(new BINOP(BINOP.PLUS, framePt, new CONST(offset)));
	}
	
	public Tree.Exp expFromStack(Tree.Exp stackPtr) 
	{
		return new MEM(new BINOP(BINOP.PLUS, stackPtr, new CONST(offset - frame.allocDown - Translate.Library.WORDSIZE)));
	}
	
}