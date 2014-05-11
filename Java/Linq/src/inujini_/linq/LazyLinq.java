package inujini_.linq;


final class LazyLinq<T> /*implements Iterable<T>*/ {

	/*private Evaluator _evaluator;

	private LazyLinq(Collection<T> items) {
		_evaluator = new Evaluator(items);
	}

	private LazyLinq(T[] array) {
		_evaluator = new Evaluator(Arrays.asList(array));
	}

	private LazyLinq(Evaluator evaluator) {
		_evaluator = evaluator;
	}

	public static <T> LazyLinq<T> linq(Collection<T> items) {
		return new LazyLinq<T>(items);
	}

	public static <T> LazyLinq<T>linq(T[] array) {
		return new LazyLinq<T>(array);
	}

	public LazyLinq<T> where(final Predicate<T> p) {

		_evaluator.add(new Func1<Collection<T>, Collection<T>>() {
			@Override
			public Collection<T> call(Collection<T> items) {
				ArrayList<T> tmp = new ArrayList<T>();

				for(T obj : items) {
					if(p.call(obj)) {
						tmp.add(obj);
					}
				}

				return tmp;
			}
		});

		return this;
	}

	public <U> LazyLinq<U> select(final Func1<T, U> f) {

		_evaluator.add(new Func1<Collection<T>, Collection<U>>(){
			@Override
			public Collection<U> call(Collection<T> items) {
				ArrayList<U> tmp = new ArrayList<U>();

				for(T obj : items) {
					tmp.add(f.call(obj));
				}

				return tmp;
			}
		});

		return new LazyLinq<U>(_evaluator);
	}

	public <U> LazyLinq<U> selectMany(final Func1<T, Collection<U>> f) {

		_evaluator.add(new Func1<Collection<T>, Collection<U>>(){
			@Override
			public Collection<U> call(Collection<T> items) {
				ArrayList<U> tmp = new ArrayList<U>();

				for(T obj : items) {
					for(U converted : f.call(obj)) {
						tmp.add(converted);
					}
				}
			}
		});

		return new LazyLinq<U>(_evaluator);
	}

	public LazyLinq<T> skip(final int count) {

		_evaluator.add(new Func1<Collection<T>, Collection<T>>(){
			@Override
			public Collection<T> call(Collection<T> items) {
				ArrayList<T> tmp = new ArrayList<T>();

				if(count <= 0) return tmp;

				int i = 1;

				for(T obj : items) {
					if(i > count) {
						tmp.add(obj);
					} else {
						i++;
					}
				}

				return tmp;
			}
		});

		return this;
	}

	public LazyLinq<T> skipWhile(final Predicate<T> p) {

		_evaluator.add(new Func1<Collection<T>, Collection<T>>(){
			@Override
			public Collection<T> call(Collection<T> items) {
				ArrayList<T> tmp = new ArrayList<T>();
				boolean isSkiped = false;

				for(T obj : items) {
					if(!isSkiped) {
						isSkiped = p.call(obj);
					} else {
						tmp.add(obj);
					}
				}

				return tmp;
			}
		});

		return this;
	}

	public LazyLinq<T> take(final int count) {

		_evaluator.add(new Func1<Collection<T>, Collection<T>>(){
			@Override
			public Collection<T> call(Collection<T> items) {
				ArrayList<T> tmp = new ArrayList<T>();

				if(count <= 0) return tmp;

				int i = 1;

				for(T obj : items) {
					if(i < count) {
						tmp.add(obj);
					} else {
						i++;
					}
				}

				return tmp;
			}
		});

		return this;
	}

	public LazyLinq<T> takeWhile(final Predicate<T> p) {

		_evaluator.add(new Func1<Collection<T>, Collection<T>>(){
			@Override
			public Collection<T> call(Collection<T> items) {
				ArrayList<T> tmp = new ArrayList<T>();

				for(T obj : _items) {
					if(p.call(obj)){
						tmp.add(obj);
					} else {
						break;
					}
				}

				return tmp;
			}
		});

		return this;
	}

	public <V, U, W> LazyLinq<W> join(final Collection<U> rightItems, final Func1<T, V> leftKeyProvider, final Func1<U, V> rightKeyProvider, final Func2<T, U, W> joinProvider) {

		_evaluator.add(new Func1<Collection<T>, Collection<W>>(){
			@Override
			public Collection<W> call(Collection<T> items) {
				ArrayList<W> tmp = new ArrayList<W>();
				ArrayList<W> cache = new ArrayList<W>();
				boolean isImplementedRemoveAll = true;
				boolean isTestedRemoveAll = false;

				for(T left : items) {

					V leftKey = leftKeyProvider.call(left);

					for(U right : rightItems) {

						V rightKey = rightKeyProvider.call(right);

						if(leftKey.eqauls(rightKey)) {
							W obj = joinProvider.call(left, right);
							tmp.add(obj);
							if(isImplementedRemoveAll) cache.add(obj);
						}
					}

					if(!cache.isEmpty()) {
						if(!isTestedRemoveAll) {
							try {
								right.removeAll(cache);
							} catch(UnsupportedOperationException e) {
								isImplementedRemoveAll = false;
							}

							isTestedRemoveAll = true;
						} else if(isImplementedRemoveAll) {
							right.removeAll(cache);
						}

						cache.clear();
					}
				}

				return tmp;
			}
		});

		return new LazyLinq<W>(_evaluator);
	}

	public <U extends T> LazyLinq<U> cast() {
		_evaluator.add(new Func1<Collection<T>, Collection<U>>(){
			@Override
			public Collection<U> call(Collection<T> items) {
				ArrayList<U> tmp = new ArrayList<U>();

				for(T obj : tmp) {
					tmp.add((U)obj);
				}

				return tmp;
			}
		});

		return new LazyLinq<U>(_evaluator);
	}

	public <TKey, TValue> Map<TKey, List<TValue>> groupBy(Func1<T, TKey> keyProvider, Func1<T, TValue> valueProvider) {
		Collection<T> items = _evaluator.eval();

		Map<TKey, List<TValue>> map = new HashMap<TKey, List<TValue>>();

		for(T obj : items) {

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

		Collection<T> items = _evaluator.eval();

		if(items instanceof List<?>) return (List<T>) items;

		List<T> list = new ArrayList<T>();

		for(T obj : items) {
			list.add(obj);
		}

		return list;
	}

	public <TKey, TValue> Map<TKey, TValue> toMap(Func1<T, TKey> keyProvider, Func1<T, TValue> valueProvider) {

		Collection<T> items = _evaluator.eval();

		Map<TKey, TValue> map = new HashMap<TKey, TValue>();

		for(T obj : items) {
			map.put(keyProvider.call(obj), valueProvider.call(obj));
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return (T[]) toList().toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) _evaluator.eval().iterator();
	}*/

}