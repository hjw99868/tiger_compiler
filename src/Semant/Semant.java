package Semant;

import Absyn.FieldList;
import ErrorMsg.*; 
import Translate.Level;
import Types.*;
import Util.BoolList;
import Symbol.Symbol;

public class Semant {
	private Env env;
	private Translate.Translate trans;
	private Translate.Level level = null;
	private java.util.Stack<Temp.Label> loopStack = new java.util.Stack<Temp.Label>();
	private Boolean TDecFlag=false,FDecFlag=false,TypeDecFlag=false,FuncDecFlag=false;
	
	public ExpTy transVar(Absyn.Var e)
	{
		if (e instanceof Absyn.SimpleVar) 
			return transVar((Absyn.SimpleVar)e);
		if (e instanceof Absyn.SubscriptVar) 
			return transVar((Absyn.SubscriptVar)e);
		if (e instanceof Absyn.FieldVar) 
			return transVar((Absyn.FieldVar)e);
		return null;
	}
	
	ExpTy transVar(Absyn.SimpleVar e)
	{
		Entry ex = (Entry)env.vEnv.get(e.name);
		if (ex == null || !(ex instanceof VarEntry))
		{
			env.errorMsg.error(e.pos, "error : undefined variable");
			return null;
		}
		VarEntry evx = (VarEntry)ex;
		return new ExpTy(trans.transSimpleVar(evx.acc, level), evx.Ty);
	}
	
	ExpTy transVar(Absyn.SubscriptVar e)
	{
		if (!(transExp(e.index).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error : subscript not int");
			return null;
		}		
		ExpTy ev = transVar(e.var);
		ExpTy ei = transExp(e.index);
		if (ev == null || !(ev.ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "error : array not exist");
			return null;
		}
		ARRAY ae = (ARRAY)(ev.ty.actual());
		return new ExpTy(trans.transSubscriptVar(ev.exp, ei.exp), ae.element);
	}
	
	ExpTy transVar(Absyn.FieldVar e)
	{
		ExpTy et = transVar(e.var);
		if (!(et.ty.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "error : variable not record");
			return null;
		}
		RECORD rc = (RECORD)(et.ty.actual());
		int count = 1;
		while (rc != null)
		{
			if (rc.fieldName == e.field)
			{
				return new ExpTy(trans.transFieldVar(et.exp, count), rc.fieldType);
			}
			count++;
			rc = rc.tail;
		}
		env.errorMsg.error(e.pos, "error : field not in record type");
		return null;
	}
	
	ExpTy transExp(Absyn.Exp e)
	{
		if (e instanceof Absyn.IntExp) 
			return transExp((Absyn.IntExp)e);
		if (e instanceof Absyn.StringExp) 
			return transExp((Absyn.StringExp)e);
		if (e instanceof Absyn.NilExp) 
			return transExp((Absyn.NilExp)e);
		if (e instanceof Absyn.VarExp) 
			return transExp((Absyn.VarExp)e);
		if (e instanceof Absyn.OpExp) 
			return transExp((Absyn.OpExp)e);
		if (e instanceof Absyn.AssignExp) 
			return transExp((Absyn.AssignExp)e);
		if (e instanceof Absyn.CallExp) 
			return transExp((Absyn.CallExp)e);
		if (e instanceof Absyn.RecordExp) 
			return transExp((Absyn.RecordExp)e);
		if (e instanceof Absyn.ArrayExp) 
			return transExp((Absyn.ArrayExp)e);
		if (e instanceof Absyn.IfExp) 
			return transExp((Absyn.IfExp)e);
		if (e instanceof Absyn.WhileExp) 
			return transExp((Absyn.WhileExp)e);
		if (e instanceof Absyn.ForExp)
			return transExp((Absyn.ForExp)e);
		if (e instanceof Absyn.BreakExp)
			return transExp((Absyn.BreakExp)e);
		if (e instanceof Absyn.LetExp) 
			return transExp((Absyn.LetExp)e);
		if (e instanceof Absyn.SeqExp) 
			return transExp((Absyn.SeqExp)e);
		return null;
	}
	
	ExpTy transExp(Absyn.VarExp e)
	{
		return transVar(e.var);
	}
	
	ExpTy transExp(Absyn.IntExp e)
	{
		return new ExpTy(trans.transIntExp(e.value), new INT());
	}
	
	ExpTy transExp(Absyn.StringExp e)
	{
		return new ExpTy(trans.transStringExp(e.value), new STRING());
	}
	
	ExpTy transExp(Absyn.NilExp e)
	{
		return new ExpTy(trans.transNilExp(), new NIL());
	}
	
	ExpTy transExp(Absyn.CallExp e)
	{
		FuncEntry fe;
		Object x = env.vEnv.get(e.func);
		if (x == null || !(x instanceof FuncEntry))
		{
			env.errorMsg.error(e.pos, "error : function "+e.func.toString()+" define wrong");
			return null;
		}
		Absyn.ExpList ex =e.args;
		fe = (FuncEntry)x;
		RECORD rc = fe.paramlist;
		while (ex != null)
		{
			if (rc == null)
			{
				env.errorMsg.error(e.pos, "error : formals are fewer than actuals");
				return null;
			}

			if (!transExp(ex.head).ty.coerceTo(rc.fieldType))
			{
				env.errorMsg.error(e.pos, "error : Type mismatch in parameter");
				return null;
			}
			ex = ex.tail;
			rc = rc.tail;
		}
		if (ex == null && !(RECORD.isNull(rc)))
		{
			env.errorMsg.error(e.pos, "error : formals are more than actuals");
			return null;
		}
		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (Absyn.ExpList i = e.args; i != null; i = i.tail)
			arrl.add(transExp(i.head).exp);
		if (x instanceof StdFuncEntry)
		{
			StdFuncEntry sf = (StdFuncEntry)x;
			return new ExpTy(trans.transStdCallExp(level, sf.label, arrl), sf.returnTy);
		}
		return new ExpTy(trans.transCallExp(level, fe.level, fe.label, arrl), fe.returnTy);
	}
	
	ExpTy transExp(Absyn.OpExp e)
	{
		ExpTy el = transExp(e.left);
		ExpTy er = transExp(e.right);
		if (el == null || er == null)
		{
			return null;
		}
		if (e.oper == Absyn.OpExp.EQ || e.oper == Absyn.OpExp.NE) 
		{
			if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof NIL)
			{
				env.errorMsg.error(e.pos, "error : nil can't compare with nil");
				return null;
			}
			if (el.ty.actual() instanceof VOID || er.ty.actual() instanceof VOID)
			{
				env.errorMsg.error(e.pos, "error : void can't compare");
				return null;
			}
			if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof RECORD)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.actual() instanceof RECORD && er.ty.actual() instanceof NIL)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.coerceTo(er.ty))
			{
				if (el.ty.actual() instanceof STRING && e.oper == Absyn.OpExp.EQ)
				{
					return new ExpTy(trans.transStringRelExp(level, e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
				}
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			}
			env.errorMsg.error(e.pos, "error : Type mismatch in OpExp");
			return null;
		}
		if (e.oper > Absyn.OpExp.NE)
		{
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.actual() instanceof STRING && er.ty.actual() instanceof STRING)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new STRING());
			env.errorMsg.error(e.pos, "error : comparison of incompatible types");
			return null;
		}
		if (e.oper < Absyn.OpExp.EQ)
		{	
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			env.errorMsg.error(e.pos, "error : Type mismatch");
			return null;
		}

		return new ExpTy(trans.transOpExp(e.oper, el.exp, er.exp), new INT());
	}
	
	ExpTy transExp(Absyn.RecordExp e)
	{
		Type t =(Type)env.tEnv.get(e.typ);
		if (t == null || !(t.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "error : unknown record type");
			return null;
		}
		Absyn.FieldExpList fe = e.fields;
		RECORD rc = (RECORD)(t.actual());
		if (fe == null && rc != null)
		{
			env.errorMsg.error(e.pos, "error : record variable wrong");
			return null;
		}
		
		while (fe != null)
		{	
			ExpTy ie = transExp(fe.init);
			if (rc == null || ie == null ||!ie.ty.coerceTo(rc.fieldType) || fe.name != rc.fieldName)
			{
				env.errorMsg.error(e.pos, "error : record variable wrong");
				return null;
			}
			fe = fe.tail;
			rc = rc.tail;
		}	
		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (Absyn.FieldExpList i = e.fields; i != null; i = i.tail)
			arrl.add(transExp(i.init).exp);
		return new ExpTy(trans.transRecordExp(level, arrl), t.actual()); 
	}
	
	ExpTy transExp(Absyn.SeqExp e)
	{
		Translate.Exp ex = null;
		for (Absyn.ExpList t = e.list; t != null; t = t.tail)
		{
			ExpTy x = transExp(t.head);

			if (t.tail == null)
			{	
				if(x!=null)
				{
					if (x.ty.actual() instanceof VOID)
					{
						ex = trans.stmcat(ex, x.exp);
					}
					else 
					{
						ex = trans.exprcat(ex, x.exp);
					}
				}
			if(x!=null) return new ExpTy(ex, x.ty);
			else return new ExpTy(ex, new VOID());
			}
			ex = trans.stmcat(ex, x.exp);	
		}
		return null;
	}
	
	ExpTy transExp(Absyn.AssignExp e)
	{
		int pos=e.pos;
		Absyn.Var var=e.var;
		Absyn.Exp exp=e.exp;
		ExpTy er = transExp(exp);
		if (er.ty.actual() instanceof VOID)
		{
			env.errorMsg.error(pos, "error : void can not assign");
			return null;
		}
		if (var instanceof Absyn.SimpleVar)
		{
			Absyn.SimpleVar ev = (Absyn.SimpleVar)var;
			Entry x= (Entry)(env.vEnv.get(ev.name));
			if (x instanceof VarEntry && ((VarEntry)x).isFor)
			{
				env.errorMsg.error(pos, "error : for index assigned to");
				return null;
			}
		}
		ExpTy vr = transVar(var);
		if (!er.ty.coerceTo(vr.ty))
		{
				env.errorMsg.error(pos,"error : Type mismatch " + er.ty.actual().getClass().getSimpleName()+" can't give "+vr.ty.actual().getClass().getSimpleName());
				return null;	
		}
		return new ExpTy(trans.transAssignExp(vr.exp, er.exp), new VOID());		
	}
	
	ExpTy transExp(Absyn.IfExp e)
	{
		ExpTy testET = transExp(e.test);
		ExpTy thenET = transExp(e.thenclause);
		ExpTy elseET = transExp(e.elseclause);
		if (e.test == null || testET == null || !(testET.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error : Type mismatch");
			return null;
		}
		if (e.elseclause == null && (!(thenET.ty.actual() instanceof VOID)))
		{
			env.errorMsg.error(e.pos, "error : if-then returns non unit");
			return null;
		}		
		if (e.elseclause != null && !thenET.ty.coerceTo(elseET.ty))
		{
			env.errorMsg.error(e.pos, "error : then else type differ");
			return null;
		}
		if (elseET == null)
			return new ExpTy(trans.transIfExp(testET.exp, thenET.exp, trans.transNoExp()), thenET.ty);
		return new ExpTy(trans.transIfExp(testET.exp, thenET.exp, elseET.exp), thenET.ty);
	}
	
	ExpTy transExp(Absyn.WhileExp e)
	{
		ExpTy transt = transExp(e.test);
		if (transt == null)	return null;
		if (!(transt.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error : for test not int");		
			return null;
		}
		
		Temp.Label out = new Temp.Label();
		loopStack.push(out);
		ExpTy bdy = transExp(e.body);
		loopStack.pop();
		
		if (bdy == null)	return null;
		if (!(bdy.ty.actual() instanceof VOID))
		{
			env.errorMsg.error(e.pos, "error : body of while not unit");
			return null;
		}
		
		return new ExpTy(trans.transWhileExp(transt.exp, bdy.exp, out), new VOID());
	}
	
	ExpTy transExp(Absyn.ForExp e)
	{
		boolean flag = false;
		if (!(transExp(e.hi).ty.actual() instanceof INT) || !(transExp(e.var.init).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error : for variable not int");
		}
		env.vEnv.beginScope();
		Temp.Label label = new Temp.Label();
		loopStack.push(label);
		Translate.Access acc = level.allocLocal(true);
		env.vEnv.put(e.var.name, new VarEntry(new INT(), acc, true));
		ExpTy body = transExp(e.body);
		ExpTy high = transExp(e.hi);
		ExpTy low = transExp(e.var.init);
		if (body == null)	flag = true;
		loopStack.pop();
		env.vEnv.endScope();
		
		if (flag)	return null;
		return new ExpTy(trans.transForExp(level, acc, low.exp, high.exp, body.exp, label), new VOID());
	}
	
	ExpTy transExp(Absyn.BreakExp e)
	{
		if (loopStack.isEmpty())
		{
			env.errorMsg.error(e.pos, "error : break out of a loop");
			return null;
		}
		return new ExpTy(trans.transBreakExp(loopStack.peek()), new VOID());
	}
	
	ExpTy transExp(Absyn.LetExp e)
	{
		Translate.Exp ex = null;
		env.vEnv.beginScope();
		env.tEnv.beginScope();	
		ExpTy td = transDecList(e.decs);
		if (td != null)
			ex = td.exp;
		ExpTy tb = transExp(e.body);
		if (tb == null)
			ex = trans.stmcat(ex, null);
		else if (tb.ty.actual() instanceof VOID)
			ex = trans.stmcat(ex, tb.exp);
		else 
			ex = trans.exprcat(ex, tb.exp);
				
		env.tEnv.endScope();
		env.vEnv.endScope();
		return new ExpTy(ex, tb.ty);
	}
	
	ExpTy transExp(Absyn.ArrayExp e)
	{
		Type ty = (Type)env.tEnv.get(e.typ);
		if (ty == null || !(ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "error : there is no array");
			return null;
		}
		ExpTy size = transExp(e.size);
		if (!(size.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "error : length of array not int");
			return null;
		}	
		ARRAY ar = (ARRAY)ty.actual();
		ExpTy ini = transExp(e.init);
		if (!ini.ty.coerceTo(ar.element.actual()))
		{
			env.errorMsg.error(e.pos, "error : initial exp and array type differ");
			return null;
		}
		return new ExpTy(trans.transArrayExp(level, ini.exp, size.exp), new ARRAY(ar.element));			
	}
	
	void transDec0(Absyn.Dec e)
	{
		if (e instanceof Absyn.VarDec) transDec0((Absyn.VarDec)e);
		if (e instanceof Absyn.TypeDec) transDec0((Absyn.TypeDec)e);
		if (e instanceof Absyn.FunctionDec) transDec0((Absyn.FunctionDec)e);
	}
	
	void transDec0(Absyn.TypeDec e)
	{
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		for (Absyn.TypeDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{ 
				env.errorMsg.error(e.pos, "error : type define repeatedly");
				return ;
			}
			hs.add(i.name);
			env.tEnv.put(i.name, new NAME(i.name));
		}
	}
	
	void transDec0(Absyn.FunctionDec e)
	{
		for (Absyn.FunctionDec i = e; i != null; i = i.next)
		{
			Absyn.RecordTy rt = new Absyn.RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null)	return;
			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.put(i.name, new FuncEntry(level, new Temp.Label(i.name), r, transTy(i.result)));
			level = level.parent;
		}
	}
	
	void transDec0(Absyn.VarDec e)
	{
		
	}
	
	Translate.Exp transDec(Absyn.Dec e)
	{
		if (e instanceof Absyn.VarDec) 
		{
			if(TypeDecFlag==true) {
				TDecFlag=true;
			}
			if(FuncDecFlag==true) {
				FDecFlag=true;
			}
				return transDec((Absyn.VarDec)e);
		}
		if (e instanceof Absyn.TypeDec) 
		{
			if(TypeDecFlag==false) {
				TypeDecFlag=true;
				return transDec((Absyn.TypeDec)e);
			}
			if(TDecFlag==true){
				env.errorMsg.error(e.pos, "error : definition of types is interrupted");
				return null;
			}
		}
		if (e instanceof Absyn.FunctionDec) 
		{
			if(FuncDecFlag==false) {
				FuncDecFlag=true;
				return transDec((Absyn.FunctionDec)e);
			}
			if(FDecFlag==true){
				env.errorMsg.error(e.pos, "error : definition of types is interrupted");
				return null;
			}
		}
		return null;
	}
	
	Translate.Exp transDec(Absyn.VarDec e)
	{
		ExpTy et = transExp(e.init);
		if (e.typ == null && e.init instanceof Absyn.NilExp)
		{
			env.errorMsg.error(e.pos, "error : initializing nil expressions wrong");
			return null;
		}
		if (et == null && e.init==null)	
		{
			env.errorMsg.error(e.pos,"error : variable must have an initial");
			 return null;
		}
		if(et == null) 
			{
			et=new ExpTy(trans.transNilExp(), new NIL());
			e.init=new Absyn.NilExp(e.pos);
			}
		if (e.typ != null && !(transExp(e.init).ty.coerceTo((Type)env.tEnv.get(e.typ.name))))
		{
			env.errorMsg.error(e.pos,"error : initial value differ");
			return null;
		}
		if (e.init == null )
		{
			env.errorMsg.error(e.pos, "error : variable must have an initial");
			return null;
		}
		Translate.Access acc = level.allocLocal(true);
		if (e.typ != null)
		{
			env.vEnv.put(e.name, new VarEntry((Type)env.tEnv.get(e.typ.name), acc));
		}
		else
		{
			env.vEnv.put(e.name, new VarEntry(transExp(e.init).ty, acc));
		}
		return trans.transAssignExp(trans.transSimpleVar(acc, level), et.exp);
	}

	Translate.Exp transDec(Absyn.TypeDec e)
	{
		for (Absyn.TypeDec i = e; i != null; i = i.next)
		{
			env.tEnv.put(i.name, new NAME(i.name));
			((NAME)env.tEnv.get(i.name)).bind(transTy(i.ty).actual());
			NAME field = (NAME)env.tEnv.get(i.name);
			if(field.isLoop() == true) 
				{
				env.errorMsg.error(i.pos, "error : recursive types wrong");
				return null;
				}
			
		}	
	for (Absyn.TypeDec i = e; i != null; i = i.next)
		env.tEnv.put(i.name, transTy(i.ty));
		
		return trans.transNoExp();
	}

	Translate.Exp transDec(Absyn.FunctionDec e)
	{
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		ExpTy et = null;
		for (Absyn.FunctionDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{
				env.errorMsg.error(e.pos, "error : function define repeatedly");
				return null;
			}
			if (env.stdFuncSet.contains(i.name))
			{
				env.errorMsg.error(e.pos, "error : repeat name with stdfunction");
				return null;
			}
			
			Absyn.RecordTy rt = new Absyn.RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null)	return null;
			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.beginScope();
			Translate.AccessList al = level.formals.next;
			for (RECORD j = r; j!= null; j = j.tail)
			{
				if (j.fieldName != null)
				{
					env.vEnv.put(j.fieldName, new VarEntry(j.fieldType, al.head));
					al = al.next;
				}
			}			
			et = transExp(i.body);
			if (et == null)
			{	env.vEnv.endScope();	return null;	}
			if(!(et.ty.coerceTo((transTy(i.result).actual()))))
			{
				env.errorMsg.error(i.pos,"error : procedure returns error type");
				return null;
			}
			if (!(et.ty.actual() instanceof VOID)) 
				trans.procEntryExit(level, et.exp, true);
			else 
				trans.procEntryExit(level, et.exp, false);
			
			env.vEnv.endScope();
			level = level.parent;
			hs.add(i.name);
		}
		return trans.transNoExp();

	}
	
	Type transTy(Absyn.Ty e)
	{
		if (e instanceof Absyn.ArrayTy) 
			return transTy((Absyn.ArrayTy)e);
		if (e instanceof Absyn.RecordTy) 
			return transTy((Absyn.RecordTy)e);
		if (e instanceof Absyn.NameTy) 
			return transTy((Absyn.NameTy)e);
		return null;
	}
	
	Type transTy(Absyn.NameTy e)
	{
		if (e == null)
			return new VOID();
		
		Type t =(Type)env.tEnv.get(e.name);
		if (t == null)
		{
			env.errorMsg.error(e.pos, "error : type not define");
			return null;
		}
		return t;
	}
	
	ARRAY transTy(Absyn.ArrayTy e)
	{
		Type t = (Type)env.tEnv.get(e.typ);
		if (t == null)
		{
			env.errorMsg.error(e.pos, "error : type not exist");
			return null;
		}
		return new ARRAY(t);
	}
	
	RECORD transTy(Absyn.RecordTy e)
	{
		RECORD rc = new RECORD(),  r = new RECORD();
		if (e == null || e.fields == null)
		{
			rc.gen(null, null, null);
			return rc;
		}
		Absyn.FieldList fl = e.fields;
		boolean first = true;
		while (fl != null)
		{
			if (env.tEnv.get(fl.typ) == null)
			{
				env.errorMsg.error(e.pos, "error : field not exist");
				return null;
			}
			
			rc.gen(fl.name, (Type)env.tEnv.get(fl.typ), new RECORD());
			if (first)
			{
				r = rc;
				first = false;
			}
			if (fl.tail == null)
				rc.tail = null;
			rc = rc.tail;
			fl = fl.tail;
		}		
		return r;
	}
	
	ExpTy transDecList(Absyn.DecList e)
	{
		Translate.Exp ex = null;
		for (Absyn.DecList i = e; i!= null; i = i.tail)
			transDec0(i.head);
		for (Absyn.DecList i = e; i!= null; i = i.tail)
		{
			ex = trans.stmcat(ex, transDec(i.head));
		}
		return new ExpTy(ex, new VOID());
	}
	
	public Semant(Translate.Translate t, ErrorMsg err)
	{
		trans = t;
		level = new Level(t.frame);
		level = new Level(level, Symbol.symbol("main"), null);
		env = new Env(err, level);
	}
	
	public Frag.Frag transProg(Absyn.Exp e)
	{
		ExpTy et = transExp(e);
		if(ErrorMsg.anyErrors)
		{
			System.out.println("Find Error And Stop!!!");
			return null;
		}
		trans.procEntryExit (level, et.exp, false); 
		level = level.parent;
		
		return trans.getResult(); 
	}
}
	
	