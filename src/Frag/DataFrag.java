package Frag;

public class DataFrag extends Frag{
    //数据段相关记录
	Temp.Label label = null;
	public String data = null;
	public DataFrag(Temp.Label label, String data)
	{
		this.label = label;	this.data = data;
	}
}
