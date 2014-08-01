public final class Util {
	public static <T> WeakReference<T> toWeak(T o) {
		return new WeakReference<T>(o);
	}
}