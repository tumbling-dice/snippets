
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
	private Action _onPreExecute;
	/** バックグラウンド処理 */
	private Funct1<TSource, TReturn> _onBackground;
	/** _onProgressに渡す引数を作成するFunction */
	private Func<TProgress> _progressArg;
	/** 途中経過 */
	private Action1<TProgress> _onProgress;
	/** 実行後イベント */
	private Action1<TReturn> _onPostExecute;
	/** _onBackgroundでエラーが発生した時の処理 */
	private Action1<Exception> _onError;
	/** キャンセル時処理 */
	private Action _onCanceled;

	/**
	 * コンストラクタ
	 * @param onBackground バックグラウンドで実行する処理
	 */
	public ReactiveAsyncTask(Func1TSource, TReturn> onBackground) {
		_onBackground = onBackground;
	}

	/**
	 * 実行前イベント
	 * @param onPreExecute
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnPreExecute(Action onPreExecute) {
		_onPreExecute = onPreExecute;
		return this;
	}

	/**
	 * 途中経過
	 * @param progressArg 途中経過として渡したい引数を作成するFunction
	 * @param onProgress
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnProgress(Func1<TProgress> progressArg, Action1<TProgress> onProgress) {
		_progressArg = progressArg;
		_onProgress = onProgress;
		return this;
	}

	/**
	 * 実行後イベント
	 * @param onPostExecute
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnPostExecute(Action1<TReturn> onPostExecute) {
		_onPostExecute = onPostExecute;
		return this;
	}

	/**
	 * エラー時イベント
	 * @param onError
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnError(Action1<Exception> onError) {
		_onError = onError;
		return this;
	}
	
	/**
	 * キャンセル時イベント
	 * @param onCanceled
	 * @return
	 */
	public ReactiveAsyncTask<TSource, TProgress, TReturn> setOnCanceled(Action onCanceled) {
		_onCanceled = onCanceled;
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
			if(_progressArg != null) publishProgress(_progressArg.call());
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
	
	@Override
	protected void onCanceled() {
		if(_onCanceled != null) _onCanceled.call();
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

	
}
