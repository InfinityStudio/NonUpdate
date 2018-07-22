package nonupdate.forge;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

public class FinalFieldSetter {

    private static final FinalFieldSetter INSTANCE = new FinalFieldSetter();
    
    private static final sun.misc.Unsafe unsafe = ReflectionHelper.getUnsafe();

    @Nullable
    public static FinalFieldSetter instance() {
        return INSTANCE;
    }
    
    public void set(Object obj, Field field, Object value) throws Exception {
        unsafe.putObject(obj, unsafe.objectFieldOffset(field), value);
    }

    public void setStatic(Field field, Object value) throws Exception {
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
    }
    
}