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
	
	private Fragment _primaryItem;
	private List<FragmentInfo> _fragments = new ArrayList<FragmentInfo>();
	private FragmentManager _fm;
	private FragmentTransaction _ft;
	private FragmentTransactionProxy _ftp;
	private boolean _isNeedAllChange;

	protected class FragmentInfo {
		private Fragment fragment;
		private CharSequence name;
		private boolean isShown;

		public FragmentInfo(CharSequence name, Fragment fragment) {
			this.name = name;
			this.fragment = fragment;
		}

		public CharSequence getName() {
			return this.name;
		}
		public void setName(CharSequence name) {
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
	
	protected final class FragmentTransactionProxy {
		private FragmentTransaction _ft;
		
		public FragmentTransactionProxy(FragmentTransaction ft) {
			_ft = ft;
		}
		
		public FragmentTransactionProxy attach(Fragment fragment) {
			_ft.attach(fragment);
			return this;
		}
		
		public FragmentTransactionProxy detach(Fragment fragment) {
			_ft.detach(fragment);
			return this;
		}
		
		public FragmentTransactionProxy hide(Fragment fragment) {
			_ft.hide(fragment);
			return this;
		}
		
		public boolean isEmpty() {
			return _ft.isEmpty();
		}
		
		public FragmentTransactionProxy remove(Fragment fragment) {
			_ft.remove(fragment);
			return this;
		}
		
		public FragmentTransactionProxy show(Fragment fragment) {
			_ft.show(fragment);
			return this;
		}
		
		FragmentTransaction getTransaction() {
			return _ft;
		}
	}

	public DynamicFragmentPagerAdapter(FragmentManager fm) {
		_fm = fm;
	}

	public DynamicFragmentPagerAdapter(FragmentManager fm, Map<CharSequence, Fragment> fragments) {
		_fm = fm;

		for(Entry<CharSequence, Fragment> entry : fragments.entrySet()) {
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
	public final Object instantiateItem(ViewGroup container, int position) {
		
		FragmentInfo fi = _fragments.get(position);
		StringBuilder tag = new StringBuilder();
		tag.append(container.getId()).append(":").append(fi.getName());

		// destroyItemでdetachされていたFragmentの場合はattachする
		Fragment f = _fm.findFragmentByTag(tag.toString());
		
		transaction();
		
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
	public final void destroyItem(ViewGroup container, int position, Object object) {
		// 本来はViewPager（container）の中からView（object）を取り除く処理
		// PagerAdapter#getItemPositionでPOSITION_NONEが返ってくるとこれが呼ばれる
		// と言うか、思っている以上にViewPagerの色んなところから呼ばれる

		// 厄介なのは「画面上で表示しきれなくなったobjectもここを通過する」と言う点だろう
		// その一点のためだけにFragmentPagerAdapterとFragmentStatePagerAdapterの処理が意味不明になっていると言っても過言ではない

		// このクラスではあくまでもFragmentTransaction#detach(Fragment)しか行わない
		// 本当に削除する必要がある場合は各メソッドでFragmentTransaction#remove(Fragment)を呼び出す必要がある
		
		Fragment fragment = (Fragment)object;
		transaction();
		_ft.detach(fragment);
	}

	@Override
	public final void setPrimaryItem(ViewGroup container, int position, Object object) {
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
	public final void finishUpdate(ViewGroup container) {
		if(_ft != null) {
			_ft.commitAllowingStateLoss();
			_ft = null;
			_fm.executePendingTransactions();
		}

		_isNeedAllChange = false;
	}

	@Override
	public final boolean isViewFromObject(View view, Object object) {
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
	public final int getItemPosition(Object object) {
		return _isNeedAllChange ? POSITION_NONE : POSITION_UNCHANGED;
	}
	
	/**
	 * Fragmentを追加します
	 * @param name タブの表示名
	 * @param fragment 追加するFragment
	 */
	public void add(CharSequence name, Fragment fragment) {
		if(hasName(name)) throw new IllegalArgumentException("表示名が重複しています。");

		_fragments.add(new FragmentInfo(name, fragment));
	}

	/**
	 * Fragmentを取得します
	 * @param position 0から始まる取得する場所
	 * @return positionで指定された位置のFragment
	 */
	public Fragment get(int position) {
		return _fragments.get(position).getFragment();
	}

	/**
	 * Fragmentを削除します
	 * @param position 0から始まる削除する場所
	 */
	public void remove(int position) {
		transaction();
		_ft.remove(_fragments.getFragment());
		_fragments.remove(position);
		_isNeedAllChange = true;
	}

	/**
	 * Fragmentを入れ替えます
	 * @param position 0から始まる入れ替える位置
	 * @param name 新しい表示名
	 * @param fragment 入れ替え後に表示するFragment
	 */
	public void replace(int position, CharSequence name, Fragment fragment) {
		if(hasName(name, position)) throw new IllegalArgumentException("表示名が重複しています。");

		FragmentInfo fi = _fragments.get(position);

		if(fi.isShown()) {
			transaction();
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
	public void insert(int position, CharSequence name, Fragment fragment) {
		if(hasName(name)) throw new IllegalArgumentException("表示名が重複しています。");
		_fragments.add(position, new FragmentInfo(name, fragment));
		_isNeedAllChange = true;
	}
	
	/**
	 * Adapterに設定されているFragmentManagerを取得します
	 * @return FragmentManager
	 */
	protected FragmentManager getFragmentManager() {
		return _fm;
	}
	
	/**
	 * FragmentTransactionのProxyを取得します
	 * @return FragmentTransactionProxy
	 */
	protected FragmentTransactionProxy getFragmentTransactionProxy() {
		transaction();
		return new FragmentTransactionProxy(_ft);
	}
	
	/**
	 * PagerAdapter#notifyDataSetChanged時にFragmentを再配置するフラグを設定します
	 * 既存のFragmentの位置が変更される場合は必ずtrueにする必要があります
	 * また、notifyDataSetChanged後、もしくはnotifyDataSetChanged前にタブを移動するなどの操作が行われた場合、
	 * このフラグは強制的にfalseになります
	 * @see PagerAdapter#finishUpdate()
	 * @see PagerAdapter#notifyDataSetChanged()
	 */
	protected void needAllChange() {
		_isNeedAllChange = true;
	}
	
	/**
	 * FragmentInfoを取得します
	 * @param position 0から始まる取得する位置
	 * @return positionで指定された位置のFragmentInfo
	 */
	protected FragmentInfo getFragmentInfo(int positon) {
		return _fragments.get(position);
	}
	
	/**
	 * FragmentInfoのリストを取得します
	 * @return FragmentInfoのリスト
	 */
	protected List<FragmentInfo> getFragmentInfoes() {
		return _fragments;
	}
	
	/**
	 * 表示名の重複があるかどうかを調査します
	 * @param name 調査する表示名
	 * @return 重複する表示名があればtrue
	 */
	protected boolean hasName(CharSequence name) {
		for (FragmentInfo fi : _fragments) {
			if(fi.getName().equals(name)) return true;
		}
		return false;
	}

	/**
	 * 表示名の重複があるかどうかを調査します
	 * @param name 調査する表示名
	 * @param position 調査を除外する位置
	 * @return 指定されたpositonと違う位置に重複する表示名がある場合はtrue
	 */
	protected boolean hasName(CharSequence name, int position) {
		for (int i = 0, size = _fragments.size(); i < size; i++) {
			if(_fragments.get(i).getName().equals(name)){
				return i != position ? true : false;
			}
		}
		return false;
	}
	
	private void transaction() {
		
		if(_ftp != null) {
			_ft = _ftp.getTransaction();
			_ftp = null;
			return;
		}
		
		if(_ft == null) _ft = _fm.beginTransaction();
	}

}