package dict;

import java.util.HashMap;

public class defaultDict<K, V> extends HashMap<K, V> {

    Class<V> klass;
    public defaultDict(Class class1) {
        this.klass = class1;    
    }

    @Override
    public V get(Object key) {
        V returnValue = super.get(key);
        if (returnValue == null) {
            try {
                returnValue = klass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.put((K) key, returnValue);
        }
        return returnValue;
    }    
}
