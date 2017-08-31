package Semant;

import Types.*;

public class FuncEntry extends Entry{
    //函数相关记录
	RECORD paramlist;
	Type returnTy;
	public Translate.Level level;
	public Temp.Label label;
	
	public FuncEntry( Translate.Level level, Temp.Label label, RECORD p, Type rt)
	{
		paramlist = p;
		returnTy = rt;
		this.level = level;
		this.label = label;
	}
}
