package pl.bushee.php2jvm;

import java.util.LinkedHashMap;
import java.util.Map;

public class PhpArray<T> extends LinkedHashMap<String, T> {

    int currentIndex = 0;

    public PhpArray() {
        super();
    }

    public void append(T t) {
        put(currentIndex, t);
    }

    public T put(Integer key, T value) {
        currentIndex = Math.max(currentIndex, key + 1);
        return super.put(key.toString(), value);
    }

    @Override
    public T put(String key, T value) {
        try {
            currentIndex = Math.max(currentIndex, Integer.parseInt(key) + 1);
        } catch (NumberFormatException e) {
            // nothing to do
        }
        return super.put(key, value);
    }

    @Override
    public T get(Object key) {
        if (key instanceof Integer) {
            return super.get(key.toString());
        }
        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Integer) {
            return super.containsKey(key.toString());
        }
        return super.containsKey(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        //  TODO
        throw new UnsupportedOperationException();
    }
}
