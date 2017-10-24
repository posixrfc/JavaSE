package lib.xml;

public interface XmlSerializable
{
	public default String toXmlTag(boolean end)
	{
		String begin = end ? "</" : "<";
		return begin + this.toString() + ">";
	}
		
	public default String toXmlValue()
	{
		return this.toString();
	}
}
