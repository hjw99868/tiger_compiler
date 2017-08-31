package Frame;

import Temp.*;

public abstract class Frame implements TempMap{
	public Label name = null;
	public AccessList formals = null;
	public abstract Frame newFrame(Label name, Util.BoolList formals);
	public abstract Access allocLocal(boolean escape); 
	public abstract Tree.Exp externCall(String func, Tree.ExpList args);
	public abstract Temp FP();
	public abstract Temp SP();
	public abstract Temp RA();
	public abstract Temp RV(); 
	public abstract Tree.Stm procEntryExit1(Tree.Stm body);
}
