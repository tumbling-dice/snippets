package inujini_.linq;

import inujini_.function.Function.Action1;
import inujini_.function.Function.Func1;
import inujini_.function.Function.Func2;
import inujini_.function.Function.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Linq<T> implements Iterable<T> {

	private Iterable<T> _items;

	private Linq(Iterable<T> items) {
		_items = items;
	}

	private Linq(T[] array) {
		_items = Arrays.asList(array);
	}

	public static <T> Linq<T> linq(Iterable<T> items) {
		return new Linq<T>(items);
	}

	public static <T> Linq<T> linq(T[] array) {
		return new Linq<T>(array);
	}

	public Linq<T> where(Predicate<T> p) {

		ArrayList<T> tmp = new ArrayList<T>();

		for(T obj : _items) {
			if(p.call(obj)) {
				tmp.add(obj);
			}
		}

		_items = tmp;

		return this;
	}

	public <U> Linq<U> select(Func1<T, U> f) {

		ArrayList<U> tmp = new ArrayList<U>();

		for(T obj : _items) {
			tmp.add(f.call(obj));
		}

		return new Linq<U>(tmp);
	}

	public <U> Linq<U> selectMany(Func1<T, Iterable<U>> f) {

		ArrayList<U> tmp = new ArrayList<U>();

		for(T obj : _items) {
			for(U converted : f.call(obj)) {
				tmp.add(converted);
			}
		}

		return new Linq<U>(tmp);
	}

	public Linq<T> skip(int count) {

		if(count <= 0) return this;

		ArrayList<T> tmp = new ArrayList<T>();
		int i = 1;

		for(T obj : _items) {
			if(i > count) {
				tmp.add(obj);
			} else {
				i++;
			}
		}

		_items = tmp;

		return this;
	}

	public Linq<T> skipWhile(Predicate<T> p) {

		ArrayList<T> tmp = new ArrayList<T>();
		boolean isSkiped = false;

		for(T obj : _items) {
			if(!isSkiped) {
				isSkiped = p.call(obj);
			} else {
				tmp.add(obj);
			}
		}

		_items = tmp;
		return this;
	}

	public Linq<T> take(int count) {
		ArrayList<T> tmp = new ArrayList<T>();

		if(count <= 0) {
			_items = tmp;
			return this;
		}

		int i = 1;

		for(T obj : _items) {
			if(i < count) {
				tmp.add(obj);
			} else {
				i++;
			}
		}

		_items = tmp;
		return this;
	}

	public Linq<T> takeWhile(Predicate<T> p) {
		ArrayList<T> tmp = new ArrayList<T>();

		for(T obj : _items) {
			if(p.call(obj)){
				tmp.add(obj);
			} else {
				break;
			}
		}

		_items = tmp;

		return this;
	}

	public <V, U, W> Linq<W> join(Collection<U> rightItems, Func1<T, V> leftKeyProvider, Func1<U, V> rightKeyProvider, Func2<T, U, W> joinProvider) {

		ArrayList<W> tmp = new ArrayList<W>();
		ArrayList<W> cache = new ArrayList<W>();
		boolean isImplementedRemoveAll = true;
		boolean isTestedRemoveAll = false;

		for(T left : _items) {

			V leftKey = leftKeyProvider.call(left);

			for(U right : rightItems) {

				V rightKey = rightKeyProvider.call(right);

				if(leftKey.equals(rightKey)) {
					W obj = joinProvider.call(left, right);
					tmp.add(obj);
					if(isImplementedRemoveAll) cache.add(obj);
				}
			}

			if(!cache.isEmpty()) {
				if(!isTestedRemoveAll) {
					try {
						rightItems.removeAll(cache);
					} catch(UnsupportedOperationException e) {
						isImplementedRemoveAll = false;
					}

					isTestedRemoveAll = true;
				} else if(isImplementedRemoveAll) {
					rightItems.removeAll(cache);
				}

				cache.clear();
			}
		}

		return new Linq<W>(tmp);
	}

	public Linq<T> orderBy(Comparator<T> comparator) {
		List<T> list = toList();
		Collections.sort(list, comparator);
		_items = list;
		return this;
	}

	public boolean any() {
		return _items.iterator().hasNext();
	}

	public boolean any(Predicate<T> p) {
		for (T obj : _items) {
			if(p.call(obj)) {
				return true;
			}
		}
		return false;
	}

	public T first() {
		return _items.iterator().next();
	}

	public <TKey, TValue> Map<TKey, List<TValue>> groupBy(Func1<T, TKey> keyProvider, Func1<T, TValue> valueProvider) {
		Map<TKey, List<TValue>> map = new HashMap<TKey, List<TValue>>();

		for(T obj : _items) {

			TKey key = keyProvider.call(obj);

			if(map.containsKey(key)) {
				map.get(key).add(valueProvider.call(obj));
			} else {
				List<TValue> value = new ArrayList<TValue>();
				value.add(valueProvider.call(obj));
				map.put(key, value);
			}
		}

		return map;
	}

	public List<T> toList() {
		if(_items instanceof List<?>) return (List<T>) _items;

		List<T> list = new ArrayList<T>();

		for(T obj : _items) {
			list.add(obj);
		}

		return list;
	}

	public void forEach(Action1<T> action) {
		for (T obj : _items) {
			action.call(obj);
		}
	}

	public <TKey, TValue> Map<TKey, TValue> toMap(Func1<T, TKey> keyProvider, Func1<T, TValue> valueProvider) {
		Map<TKey, TValue> map = new HashMap<TKey, TValue>();

		for(T obj : _items) {
			map.put(keyProvider.call(obj), valueProvider.call(obj));
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return (T[]) toList().toArray();
	}

	@Override
	public Iterator<T> iterator() {
		return _items.iterator();
	}

}
