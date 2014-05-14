package com.example.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class DynamicFragmentPagerAdapter extends PagerAdapter {

	protected FragmentManager _fm;
	protected FragmentTransaction _ft;
	protected Fragment _primaryItem;
	protected List<FragmentInfo> _fragments = new ArrayList<FragmentInfo>();
	private boolean _isNeedAllChange;

	protected final class FragmentInfo {
		private Fragment fragment;
		private String name;
		private boolean isShown;

		public FragmentInfo(String name, Fragment fragment) {
			this.name = name;
			this.fragment = fragment;
		}

		public String getName() {
			return this.name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Fragment getFragment() {
			return this.fragment;
		}
		public void setFragment(Fragment fragment) {
			this.fragment = fragment;
		}
		public boolean isShown() {
			return isShown;
		}
		public void setShown(boolean isShown) {
			this.isShown = isShown;
		}

	}

	public DynamicFragmentPagerAdapter(FragmentManager fm) {
		_fm = fm;
	}

	public DynamicFragmentPagerAdapter(FragmentManager fm, Map<String, Fragment> fragments) {
		_fm = fm;

		for(Entry<String, Fragment> entry : fragments.entrySet()) {
			_fragments.add(new FragmentInfo(entry.getKey(), entry.getValue()));
		}
	}

	@Override
	public void startUpdate(ViewGroup container) { }

	@Override
	public CharSequence getPageTitle(int position) {
		return _fragments.get(position).getName();
	}

	@Override
	public int getCount() {
		return _fragments.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(_ft == null) _ft = _fm.beginTransaction();

		FragmentInfo fi = _fragments.get(position);
		StringBuilder tag = new StringBuilder();
		tag.append(container.getId()).append(":").append(fi.getName());

		// destroyItemでdetachされていたFragmentの場合はattachする
		Fragment f = _fm.findFragmentByTag(tag.toString());
		if(f != null && fi.getFragment().equals(f)) {
			_ft.attach(f);
			return f;
		}

		f = fi.getFragment();

		if(!f.equals(_primaryItem)) {
			f.setMenuVisibility(false);
			f.setUserVisibleHint(false);
		}

		fi.setShown(true);
		_ft.add(container.getId(), f, tag.toString());

		return f;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// 本来はViewPager（container）の中からView（object）を取り除く処理っぽい
		// PagerAdapter#getItemPositionでPOSITION_NONEが返ってくるとこれが呼ばれる
		// と言うか、思っている以上にViewPagerの色んなところから呼ばれる

		// 厄介なのは「画面上で表示しきれなくなったobjectもここを通過する」と言う点だろう
		// その一点のためだけにFragmentPagerAdapterとFragmentStatePagerAdapterの処理が意味不明になっていると言っても過言ではない

		Fragment fragment = (Fragment)object;
		if(_ft == null) _ft = _fm.beginTransaction();
		_ft.detach(fragment);
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if(object == null) return;

		Fragment fragment = (Fragment) object;

		if(!fragment.equals(_primaryItem)) {

			if(_primaryItem != null) {
				_primaryItem.setMenuVisibility(false);
				_primaryItem.setUserVisibleHint(false);
			}

			fragment.setMenuVisibility(true);
			fragment.setUserVisibleHint(true);
			_primaryItem = fragment;
		}
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		if(_ft != null) {
			_ft.commitAllowingStateLoss();
			_ft = null;
			_fm.executePendingTransactions();
		}

		_isNeedAllChange = false;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return ((Fragment)object).getView() == view;
	}

	/*@Override
	public Parcelable saveState() {
		Bundle state = new Bundle();

		for(int i = 0, size = _fragments.size(); i < size; i++) {
			FragmentInfo fi = _fragments.get(i);
			state.putString("name", fi.getName());
			state.putBoolean("isNeedRemove", fi.isNeedRemove());
			_fm.putFragment(state, "f" + i, fi.getFragment());
		}

		return state;
		return null;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if(state == null) return;

		Bundle bundle = (Bundle)state;
		bundle.setClassLoader(loader);

		for(String key : bundle.keySet()) {
			if(!key.startsWith("f")) continue;

			int index = Integer.parseInt(key.substring(1));

			Fragment f = _fm.getFragment(bundle, key);
			FragmentInfo fi = new FragmentInfo(bundle.getString("name"), f);
			//fi.setNeedRemove(bundle.getBoolean("isNeedRemove"));

			_fragments.set(index, fi);
		}
	}*/

	@Override
	public int getItemPosition(Object object) {
		return _isNeedAllChange ? POSITION_NONE : POSITION_UNCHANGED;
	}

	/**
	 * Fragmentを追加します
	 * @param name タブの表示名
	 * @param fragment 追加するFragment
	 */
	public void add(String name, Fragment fragment) {
		if(hasName(name)) throw new IllegalArgumentException("表示名が重複しています。");

		_fragments.add(new FragmentInfo(name, fragment));
	}

	/**
	 * Fragmentを取得します
	 * @param position 0から始まる取得する場所
	 * @return
	 */
	public Fragment get(int position) {
		return _fragments.get(position).getFragment();
	}

	/**
	 * Fragmentを削除します
	 * @param position 0から始まる削除する場所
	 */
	public void remove(int position) {
		if(_ft == null) _ft = _fm.beginTransaction();
		_fragments.remove(position);
		_isNeedAllChange = true;
	}

	/**
	 * Fragmentを入れ替えます
	 * @param position 0から始まる入れ替える位置
	 * @param name 新しい表示名
	 * @param fragment 入れ替え後に表示するFragment
	 */
	public void replace(int position, String name, Fragment fragment) {
		if(hasName(name, position)) throw new IllegalArgumentException("表示名が重複しています。");

		FragmentInfo fi = _fragments.get(position);

		if(fi.isShown()) {
			if(_ft == null) _ft = _fm.beginTransaction();
			_ft.remove(fi.getFragment());
			_isNeedAllChange = true;
		}

		_fragments.set(position, new FragmentInfo(name, fragment));
	}

	/**
	 * Fragmentを挿入します
	 * @param position 0から始まる挿入する位置（元々あったFragmentは右にずれる）
	 * @param name 表示名
	 * @param fragment 挿入するFragment
	 */
	public void insert(int position, String name, Fragment fragment) {
		if(hasName(name)) throw new IllegalArgumentException("表示名が重複しています。");
		_fragments.add(position, new FragmentInfo(name, fragment));
		_isNeedAllChange = true;
	}

	private boolean hasName(String name) {
		for (FragmentInfo fi : _fragments) {
			if(fi.getName().equals(name)) return true;
		}

		return false;
	}

	private boolean hasName(String name, int position) {
		for (int i = 0, size = _fragments.size(); i < size; i++) {
			if(_fragments.get(i).getName().equals(name) && i != position)
				return true;
		}

		return false;
	}

}