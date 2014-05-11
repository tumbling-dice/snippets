
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ReactiveAsyncLoader<TReturn> extends AsyncTaskLoader<ReactiveAsyncResult<TReturn>> {

	private ReactiveAsyncResult<TReturn> _data;

	public abstract boolean isComplete();

	public ReactiveAsyncLoader(Context context) {
		super(context);
	}

	@Override
	public void deliverResult(ReactiveAsyncResult<TReturn> data) {
		if (isReset()) {
			return;
		}
		_data = data;
		super.deliverResult(data);
	}

	@Override
	protected void onStartLoading() {
		if (_data != null) {
			deliverResult(_data);
		}

		if (takeContentChanged() || _data == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
		_data = null;
	}


}
