package Frag;

public class ProcFrag extends Frag{
    //�������ؼ�¼
	public Frame.Frame frame;
	public Tree.Stm body;
	public ProcFrag(Tree.Stm body, Frame.Frame f)
	{
		this.body = body;
		frame = f;
	}
}
