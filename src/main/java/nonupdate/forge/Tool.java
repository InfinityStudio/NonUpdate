package nonupdate.forge;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class Tool {
	
	public static final void coverString(String src, String to) {
		try {
			Field value = setAccessible(String.class.getDeclaredField("value"));
			FinalFieldSetter.instance().set(src, value, value.get(to));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final <T extends AccessibleObject> T setAccessible(T t) {
		t.setAccessible(true);
		return t;
	}
	
}
