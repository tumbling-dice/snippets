
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

/**
 * AsyncTaskの各種イベントをクロージャでフックするクラス
 * @param <TSource> Background実行時に渡したいクラス
 * @param <TProgress> 途中経過で欲しいクラス
 * @param <TReturn> Backgroundから返ってくるクラス
 */
public class ReactiveAsyncTask<TSource, TProgress, TReturn> extends AsyncTask<TSource, TProgress, ReactiveAsyncResult<TReturn>> {

	/** 実行前イベント */
	private V1<Void> _onPreExecute;
	/** バックグラウンド処理 */
	private R1<TReturn, TSource> _onBackground;
	/** _onProgressに渡す引数を作成するFunction */
	private R1<TProgress, Void> _progressArg;
	/** 途中経過 */
	private V1<TProgress> _onProgress;
	/** 実行後イベント */
	private V1<TReturn> _onPostExecute;
	/** _onBackgroundでエラーが発生した時の処理 */
	private V1<Exception> _onError;

	private boolean isCanceled;

	/**
	 * コンストラクタ
	 * @param onBackground バックグラウンドで実行する処理
	 */
	public ReactiveAsyncTask(R1<TReturn, TSource> onBackground) {
		_onBackground = onBackground;
	}

	/**
	 * 実行前イベント
	 * @param action
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnPreExecute(V1<Void> action) {
		_onPreExecute = action;
		return this;
	}

	/**
	 * 途中経過
	 * @param progressArg 途中経過として渡したい引数を作成するFunction
	 * @param action
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnProgress(R1<TProgress, Void> progressArg, V1<TProgress> action) {
		_progressArg = progressArg;
		_onProgress = action;
		return this;
	}

	/**
	 * 実行後イベント
	 * @param action
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnPostExecute(V1<TReturn> action) {
		_onPostExecute = action;
		return this;
	}

	/**
	 * エラー時イベント
	 * @param action
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnError(V1<Exception> action) {
		_onError = action;
		return this;
	}

	@Override
	protected void onPreExecute() {
		if(_onPreExecute != null) _onPreExecute.call(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ReactiveAsyncResult<TReturn> doInBackground(TSource... arg0) {

		ReactiveAsyncResult<TReturn> r = new ReactiveAsyncResult<TReturn>();

		try {
			r.setResult(_onBackground.call(arg0[0]));
			if(_progressArg != null) publishProgress(_progressArg.call(null));
		} catch(RuntimeException e) {
			r.setError(e);
		}

		return r;
	}

	@Override
	protected void onProgressUpdate(TProgress... progress) {
		_onProgress.call(progress[0]);
	}

	@Override
	protected void onPostExecute(ReactiveAsyncResult<TReturn> r) {
		if(!r.hasError() && _onPostExecute != null) {
			_onPostExecute.call(r.getResult());
		} else {
			if(_onError != null) {
				_onError.call(r.getError());
			}
		}
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
	public void execute(TSource p) {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
		    super.execute(p);
		} else {
			super.executeOnExecutor(THREAD_POOL_EXECUTOR, p);
		}
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	@Override
	protected void onCancelled() {
		this.isCanceled = true;
		super.onCancelled();
	}
}
