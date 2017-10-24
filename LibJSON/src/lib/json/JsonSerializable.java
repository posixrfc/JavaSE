package lib.json;

public interface JsonSerializable
{
	public default String toJsonKey()
	{
		return "\"" + this.toString() + "\"";
	}
	
	public default String toJsonValue()
	{
		return this.toJsonKey();
	}
}
