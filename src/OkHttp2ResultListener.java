package extension;

/**
 * Created by guoyiyou on 16/5/17.
 */
public interface OkHttp2ResultListener {
    public void onProgress(long total, long cur);
    public void onFailed();
    public void onSuccess(HttpResult httpResult);
}
