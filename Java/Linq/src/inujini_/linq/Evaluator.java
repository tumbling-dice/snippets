package inujini_.linq;

import inujini_.function.Function.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

final class Evaluator {

	private Collection<?> _items;
	private LinkedList<Func1<Collection<?>, Collection<?>>> _functions;

	public Evaluator(Collection<?> items) {
		_items = items;
		_functions = new LinkedList<Func1<Collection<?>, Collection<?>>>();
	}

	public void add(Func1<Collection<?>, Collection<?>> function) {
		_functions.add(function);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> eval() {

		for(Func1<Collection<?>, Collection<?>> function : _functions) {
			_items = function.call(_items);
			if(_items.isEmpty()) return new ArrayList<T>();
		}

		return (Collection<T>)_items;
	}
}