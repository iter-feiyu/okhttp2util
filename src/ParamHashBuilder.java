package extension;

import java.util.HashMap;

/**
 * Created by guoyiyou on 16/6/17.
 */
public class ParamHashBuilder {
    private HashMap<String,String> hashMap = new HashMap<>();

    public ParamHashBuilder addParam(String key,String value)
    {
        hashMap.put(key,value);
        return this;
    }

    public HashMap<String,String> getAllParams()
    {
        return hashMap;
    }
}
