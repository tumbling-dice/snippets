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

/**
 * Fragmentを表示するPagerAdapter（動的変更版）
 */
public class DynamicFragmentPagerAdapter extends PagerAdapter {

	private Fragment _primaryItem;
	private List<FragmentInfo> _fragments = new ArrayList<FragmentInfo>();
	private FragmentManager _fm;
	private FragmentTransactionProxy _ftp;
	private boolean _isNeedAllChange;

	/**
	 * Fragmentの情報を所持するクラス
	 */
	protected class FragmentInfo {
		private Fragment fragment;
		private CharSequence name;
		private boolean isShown;

		/**
		 * Fragmentの情報を所持するクラス
		 * @param name 表示名
		 * @param fragment 表示するFragment
		 */
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

		@Override
		public String toString() {
			return (String) this.name;
		}
	}

	/**
	 * FragmentTransactionのproxy…と言うか、機能制限版。
	 * （containerのリソースIDやtagを用いるメソッド、commit関連のメソッドを呼べない）
	 * DynamicFragmentPagerAdapter（or サブクラス）ではこのproxyを介してFragmentTransactionを制御する。
	 * add及びcommitは#getTransaction()で何とかする。でも#getTransaction()はサブクラスでは呼ばせたくない。
	 */
	protected final class FragmentTransactionProxy {
		private FragmentTransaction _ft;

		/**
		 * FragmentTransactionの機能制限版Proxy
		 * DynamicFragmentPagerAdapter（or サブクラス）ではこのproxyを介してFragmentTransactionを制御する。
		 * @param ft FragmentTransactionの実体
		 */
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

		private FragmentTransaction getTransaction() {
			return _ft;
		}

	}

	/**
	 * Fragmentを表示するPagerAdapter（動的変更版）
	 * @param fm FragmentManager
	 */
	public DynamicFragmentPagerAdapter(FragmentManager fm) {
		_fm = fm;
	}

	/**
	 * Fragmentを表示するPagerAdapter（動的変更版）
	 * @param fm FragmentManager
	 * @param fragments 表示名とFragmentのMap（これを用いて表示を初期化する）
	 */
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
		/*
		 * このメソッドは以下の3パターンで呼ばれる
		 *
		 * 1.今まで表示されていなかったFragmentを新規に表示する必要が出てきた
		 * 2.非表示になっていたけど再度表示する必要が出てきた
		 * 3.#getItemPosition(Object)でPOSITION_NONEが返された
		 *
		 * ここで（このクラスのコメント全体で）言う「表示」とは実際に画面で表示されているFragmentだけではなく、
		 * 例えばgetPageTitleで取得するタブ名だけがちょっと見えているFragmentも「表示されている」と見なす。
		 * タブレットetcだとどうなるかわからないが、おおよそ「自分自身＋両隣」ぐらいの範囲だと思って良い。
		 *
		 * 注意する必要があるのは、1のケースだとFragmentManagerに未登録である、と言うこと。
		 * 裏を返せばそれ以外のケースでは（明示的にremoveしていない限り）必ずFragmentManagerに登録されている。
		 *
		 * #getItemPosition(Object)が呼ばれるのはPagerAdapter#notifyDataSetChanged()が呼ばれたときだけである。（多分…）
		 */

		FragmentInfo fi = _fragments.get(position);
		Fragment registFragment = fi.getFragment();

		StringBuilder tag = new StringBuilder();
		tag.append(container.getId()).append(":").append(fi.getName());


		// ここに来る前にFragmentTransaction#remove(Fragment)を呼んでいないと、
		// どうあがいてもFragmentManagerのキャッシュが表示されてしまうので注意。
		Fragment f = _fm.findFragmentByTag(tag.toString());
		transaction();

		// FragmentInfoに登録されているFragmentと一致するかどうかをチェックし、
		// 一致する（＝変更がない）場合はそのままattachする。
		if(registFragment.equals(f)) {
			_ftp.attach(f);
			return f;
		}

		// 一致しない場合はremoveメソッドなどによって位置が変わっているので、
		// FragmentInfoに登録されているfragmentを返す。
		if(!registFragment.equals(_primaryItem)) {
			registFragment.setMenuVisibility(false);
			registFragment.setUserVisibleHint(false);
		}

		fi.setShown(true);
		_ftp.getTransaction().add(container.getId(), registFragment, tag.toString());

		return registFragment;
	}

	@Override
	public final void destroyItem(ViewGroup container, int position, Object object) {
		/*
		 * 本来はViewPager（container）の中からView（object）を取り除く処理。
		 * PagerAdapter#getItemPositionでPOSITION_NONEが返ってくるとこれが呼ばれる。
		 * と言うか、思っている以上にViewPagerの色んなところから呼ばれる。
		 *
		 * 厄介なのは「画面上で表示しきれなくなったobjectもここを通過する」と言う点だろう。
		 * その一点のためだけにFragmentPagerAdapterとFragmentStatePagerAdapterの処理が意味不明になっていると言っても過言ではない。
		 *
		 * このメソッドではあくまでもFragmentTransaction#detach(Fragment)しか行わない。
		 * 本当に削除する必要がある場合は各メソッドでFragmentTransaction#remove(Fragment)を呼び出す必要がある。
		 */

		Fragment fragment = (Fragment)object;
		transaction();
		_ftp.detach(fragment);
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
		/*
		 * FragmentTransactionのcommitはここで行う。
		 * notifyDataSetChangedを呼ばなくてもタブの移動etcでFragmentが表示しきれなくなると普通に呼ばれる。
		 * その場合でもFragmentTransaction#detachのcommitはしなくてはならないので、
		 * notifyDataSetChangedを呼ばなくても勝手に更新されてしまう。
		 *
		 * そもそもPagerAdapterとFragmentManagerの相性が悪いとしか言いようがない。
		 * このクラスのメソッドをほとんどfinalにしたのもその一言で大体片付く。
		 * って言うか、destroyItemとfinishUpdateに色々な役割を持たせすぎだと思う…。
		 */

		if(_ftp != null) {
			_ftp.getTransaction().commitAllowingStateLoss();
			_ftp = null;
			_fm.executePendingTransactions();
		}

		_isNeedAllChange = false;
	}

	@Override
	public final boolean isViewFromObject(View view, Object object) {
		return ((Fragment)object).getView() == view;
	}

	//TODO: この辺必要なのかな
	//      stateの保存は各々のFragmentで管理してもらえると非常にありがたいんだが
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
		/*
		 * PagerAdapter#notifyDataSetChangedが呼ばれるとここを通過する。
		 * と言ってもViewPagerが持っているItem（object）が全部来るわけではなく、
		 * 現在表示されているItemだけがやってくる。
		 *
		 * remove、insert、replaceが呼ばれた時は表示されているFragmentに影響が出る可能性があるため、
		 * 必ずPOSITION_NONEを返すようにする。
		 *
		 * ここでPOSITION_NONEを返すと次はdestoryItemが呼ばれ、その次にinstantiateItemが呼ばれる。
		 * 表示されているFragmentに影響がなければ、一旦detachして直後にattachするだけである。
		 *
		 * ちなみにattach / detachは既にremoveされているFragmentに実行しても何も起こらない。例外も出ない。
		 * そのおかげで「notifyDataSetChanged前にFragmentTransaction#remove(Fragment)を呼び出す」と言う
		 * 一見危なっかしい行為がまかり通るようになっている。
		 */
		return _isNeedAllChange ? POSITION_NONE : POSITION_UNCHANGED;
	}

	/**
	 * Fragmentを追加します。
	 * @param name タブの表示名
	 * @param fragment 追加するFragment
	 * @exception IllegalArgumentException 表示名が重複している場合に発生
	 */
	public void add(CharSequence name, Fragment fragment) {
		if(hasName(name)) throw new IllegalArgumentException("表示名が重複しています。");

		_fragments.add(new FragmentInfo(name, fragment));
	}

	/**
	 * Fragmentを取得します。
	 * @param position 0から始まる取得する場所
	 * @return positionで指定された位置のFragment
	 */
	public Fragment get(int position) {
		return _fragments.get(position).getFragment();
	}

	/**
	 * Fragmentを削除します。
	 * @param position 0から始まる削除する場所
	 */
	public void remove(int position) {
		transaction();
		_ftp.remove(_fragments.get(position).getFragment());
		_fragments.remove(position);
		_isNeedAllChange = true;
	}

	/**
	 * Fragmentを入れ替えます。
	 * @param position 0から始まる入れ替える位置
	 * @param name 新しい表示名
	 * @param fragment 入れ替え後に表示するFragment
	 * @exception IllegalArgumentException 表示名が重複している場合に発生
	 */
	public void replace(int position, CharSequence name, Fragment fragment) {
		if(hasName(name, position)) throw new IllegalArgumentException("表示名が重複しています。");

		FragmentInfo fi = _fragments.get(position);

		// 一度でも表示されてしまったFragmentをreplaceする場合は
		// 事前にFragmentTransactionProxy#remove(Fragment)を呼び出して削除しておく。
		// （これをしておかないとinstantiateItemでFragmentManagerのキャッシュを返してしまう。）

		// また、一度も表示されていない = 現在も表示されていない、となるので、
		// _isNeedAllChangeは一度でも表示されていた場合にだけtrueにする。
		if(fi.isShown()) {
			transaction();
			_ftp.remove(fi.getFragment());
			_isNeedAllChange = true;
		}

		_fragments.set(position, new FragmentInfo(name, fragment));
	}

	/**
	 * Fragmentを挿入します。
	 * @param position 0から始まる挿入する位置（元々あったFragmentは右にずれる）
	 * @param name 表示名
	 * @param fragment 挿入するFragment
	 * @exception IllegalArgumentException 表示名が重複している場合に発生
	 */
	public void insert(int position, CharSequence name, Fragment fragment) {
		if(hasName(name)) throw new IllegalArgumentException("表示名が重複しています。");
		_fragments.add(position, new FragmentInfo(name, fragment));
		_isNeedAllChange = true;
	}

	/**
	 * FragmentTransactionのProxyを取得します。<br>
	 * このメソッドで取得できるFragmentTransactionProxyはインスタンス内で共有されます。
	 * @return FragmentTransactionProxy
	 */
	protected final FragmentTransactionProxy getFragmentTransactionProxy() {
		transaction();
		return _ftp;
	}

	/**
	 * PagerAdapter#notifyDataSetChanged時にFragmentを再配置するフラグを設定します。<br>
	 * 既存のFragmentの位置が変更される場合は必ずtrueにする必要があります。<br>
	 * また、notifyDataSetChanged後、もしくはnotifyDataSetChanged前にタブを移動するなどの操作が行われた場合、<br>
	 * このフラグは強制的にfalseになります。
	 * @see PagerAdapter#finishUpdate()
	 * @see PagerAdapter#notifyDataSetChanged()
	 */
	protected final void needAllChange() {
		_isNeedAllChange = true;
	}

	/**
	 * FragmentInfoを取得します。
	 * @param position 0から始まる取得する位置
	 * @return positionで指定された位置のFragmentInfo
	 */
	protected final FragmentInfo getFragmentInfo(int position) {
		return _fragments.get(position);
	}

	/**
	 * FragmentInfoのリストを取得します。
	 * @return FragmentInfoのリスト
	 */
	protected final List<FragmentInfo> getFragmentInfoes() {
		return _fragments;
	}

	/**
	 * 表示名の重複があるかどうかを調査します。
	 * @param name 調査する表示名
	 * @return 重複する表示名があればtrue
	 */
	protected final boolean hasName(CharSequence name) {
		for (FragmentInfo fi : _fragments) {
			if(fi.getName().equals(name)) return true;
		}
		return false;
	}

	/**
	 * 表示名の重複があるかどうかを調査します。
	 * @param name 調査する表示名
	 * @param position 調査を除外する位置
	 * @return 指定されたpositonと違う位置に重複する表示名がある場合はtrue
	 */
	protected final boolean hasName(CharSequence name, int position) {
		for (int i = 0, size = _fragments.size(); i < size; i++) {
			if(_fragments.get(i).getName().equals(name)){
				return i != position ? true : false;
			}
		}
		return false;
	}

	private void transaction() {
		if(_ftp == null) _ftp = new FragmentTransactionProxy(_fm.beginTransaction());
	}

}