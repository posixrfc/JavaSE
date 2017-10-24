package lib.json;

public abstract class Serializer
{
	public byte accesserModifier = Serializer.PUBLIC;
    public boolean standardAccessor = true;//setXxx getXxx isXxx
    public boolean excludeRootClass = true;//java.lang.Object
    public boolean excludeLetterNull = true;//null nil none undefined
    public boolean excludeEffectNull = true;//"" {} []
    
	public static final byte PUBLIC = 0B1;
    public static final byte PROTECTED = 0B10;
    public static final byte PACKAGE = 0B100;
    public static final byte PRIVATE = 0B1000;
}
