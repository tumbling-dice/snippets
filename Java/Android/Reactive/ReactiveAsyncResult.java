
/**
 * ReactiveAsyncTaskで実行した結果を保持するクラス
 * エラーが発生した場合はerrorに格納される
 * @param <TReturn>
 */
public class ReactiveAsyncResult<TReturn> {
	private TReturn result;
	private Exception error;

	public TReturn getResult() {
		if(hasError()) {
			Throwable t = this.error.getCause();
			if(t == null) throw new RuntimeException(error);
			
			while(t.getCause() != null) t = t.getCause();
			throw new RuntimeException(t);
		}
		
		return this.result;
	}
	public void setResult(TReturn result) { this.result = result; }
	public Exception getError() { return this.error; }
	public void setError(Exception error) { this.error = error; }
	public boolean hasError() { return this.error != null; }
}
