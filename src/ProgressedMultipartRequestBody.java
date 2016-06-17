package extension;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.IOException;
import java.util.List;

import okio.Buffer;
import okio.BufferedSink;
import okio.ByteString;
import okio.ForwardingSink;
import okio.Okio;

/**
 * Created by guoyiyou on 16/5/17.
 */
public class ProgressedMultipartRequestBody extends RequestBody
{
    private final ByteString boundary;
    private final MediaType contentType;
    private final List<Buffer> partHeadings;
    private final List<RequestBody> partBodies;
    private final long length;

    private OkHttp2ResultListener okHttpProgressListener = null;
    public void setProgressListener(OkHttp2ResultListener l)
    {
        okHttpProgressListener = l;
    }

    public ProgressedMultipartRequestBody(MediaType type, ByteString boundary, List<Buffer> partHeadings, List<RequestBody> partBodies, long length) {
        if(type == null) {
            throw new NullPointerException("type == null");
        } else {
            this.boundary = boundary;
            this.contentType = MediaType.parse(type + "; boundary=" + boundary.utf8());
            this.partHeadings = Util.immutableList(partHeadings);
            this.partBodies = Util.immutableList(partBodies);
            if(length != -1L) {
                length += (long)(ProgressedMultipartBuilder.CRLF.length + ProgressedMultipartBuilder.DASHDASH.length + boundary.size() + ProgressedMultipartBuilder.DASHDASH.length + ProgressedMultipartBuilder.CRLF.length);
            }

            this.length = length;
        }
    }

    public long contentLength() {
        return this.length;
    }

    public MediaType contentType() {
        return this.contentType;
    }

    public void writeTo(BufferedSink sink) throws IOException {

        BufferedSink bufferedSink = Okio.buffer(new ForwardingSink(sink){
            long writtedLength = 0;
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                writtedLength+=byteCount;
                if(okHttpProgressListener != null)
                {

                    okHttpProgressListener.onProgress(length,writtedLength);
                }
            }
        });

        int i = 0;
        for(int size = this.partHeadings.size(); i < size; ++i) {
            bufferedSink.writeAll(((Buffer)this.partHeadings.get(i)).clone());
            ((RequestBody)this.partBodies.get(i)).writeTo(bufferedSink);
        }

        bufferedSink.write(ProgressedMultipartBuilder.CRLF);
        bufferedSink.write(ProgressedMultipartBuilder.DASHDASH);
        bufferedSink.write(this.boundary);
        bufferedSink.write(ProgressedMultipartBuilder.DASHDASH);
        bufferedSink.write(ProgressedMultipartBuilder.CRLF);

        bufferedSink.flush();

    }

}
