import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public abstract class LoaderObserver<TReturn> implements LoaderCallbacks<ReactiveAsyncResult<TReturn>> {

	private WeakReference<ProgressDialog> _prog;
	private WeakReference<Action1<TReturn>> _onFirst;

	// コンストラクタでProgressDialogを受け取った場合は初回時のみ表示させる
	public LoaderObserver() {}
	
	public LoaderObserver(ProgressDialog prog) {
		_prog = Util.toWeak(prog);
	}
	
	public LoaderObserver(Action1<TReturn> onFirst) {
		_onFirst = Util.toWeak(onFirst);
	}
	
	public LoaderObserver(ProgressDialog prog, Action1<TReturn> onFirst) {
		_prog = Util.toWeak(prog);
		_onFirst = Util.toWeak(onFirst);
	}

	@Override
	public Loader<ReactiveAsyncResult<TReturn>> onCreateLoader(int id, Bundle args) {
		if(_prog != null) _prog.get().show();
		return onCreate(id, args);
	}


	@Override
	public void onLoadFinished(Loader<ReactiveAsyncResult<TReturn>> loader, ReactiveAsyncResult<TReturn> data) {
		if(_prog != null) {
			ProgressDialog prog = _prog.get();
			if(prog != null && prog.isShowing()) {
				prog.dismiss();
				prog = null;
			}
		}

		if(!data.hasError()) {
			
			if(_onFirst == null) {
				onNext(data.getResult());
			} else {
				_onFirst.get().call(data.getResult());
				_onFirst.clear();
				_onFirst = null;
			}
			
			if(((ReactiveAsyncLoader<TReturn>) loader).isComplete()) {
				onComplete();
			}
		} else {
			onError(data.getError());
		}
	}

	@Override
	public void onLoaderReset(Loader<ReactiveAsyncResult<TReturn>> loader) {
		onReset((ReactiveAsyncLoader<TReturn>) loader);
	}

	public abstract ReactiveAsyncLoader<TReturn> onCreate(int id, Bundle args);
	public abstract void onNext(TReturn data);
	public abstract void onComplete();
	public abstract void onError(Exception e);
	public abstract void onReset(ReactiveAsyncLoader<TReturn> loader);


}
