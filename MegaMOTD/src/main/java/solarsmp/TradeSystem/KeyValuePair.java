package solarsmp.TradeSystem;

public class KeyValuePair<TK, TV> {

    public TK Key;
    public TV Value;

    public KeyValuePair(TK Key, TV Value)
    {
        this.Key = Key;
        this.Value = Value;
    }

    public boolean Equals(KeyValuePair<TK, TV> reference)
    {
        return reference.Key.equals(Key) && reference.Value.equals(Value);
    }
}