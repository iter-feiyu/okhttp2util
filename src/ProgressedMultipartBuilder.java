package extension;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okio.Buffer;
import okio.ByteString;

/**
 * Created by guoyiyou on 16/5/17.
 */
public class ProgressedMultipartBuilder
{
    public static final MediaType MIXED = MediaType.parse("multipart/mixed");
    public static final MediaType ALTERNATIVE = MediaType.parse("multipart/alternative");
    public static final MediaType DIGEST = MediaType.parse("multipart/digest");
    public static final MediaType PARALLEL = MediaType.parse("multipart/parallel");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");
    public static final byte[] COLONSPACE = new byte[]{(byte)58, (byte)32};
    public static final byte[] CRLF = new byte[]{(byte)13, (byte)10};
    public static final byte[] DASHDASH = new byte[]{(byte)45, (byte)45};
    public final ByteString boundary;
    public MediaType type;
    public long length;
    public final List<Buffer> partHeadings;
    public final List<RequestBody> partBodies;

    public ProgressedMultipartBuilder() {
        this(UUID.randomUUID().toString());
    }

    public ProgressedMultipartBuilder(String boundary) {
        this.type = MIXED;
        this.length = 0L;
        this.partHeadings = new ArrayList();
        this.partBodies = new ArrayList();
        this.boundary = ByteString.encodeUtf8(boundary);
    }

    public ProgressedMultipartBuilder type(MediaType type) {
        if(type == null) {
            throw new NullPointerException("type == null");
        } else if(!type.type().equals("multipart")) {
            throw new IllegalArgumentException("multipart != " + type);
        } else {
            this.type = type;
            return this;
        }
    }

    public ProgressedMultipartBuilder addPart(RequestBody body) {
        return this.addPart((Headers)null, body);
    }

    public ProgressedMultipartBuilder addPart(Headers headers, RequestBody body) {
        if(body == null) {
            throw new NullPointerException("body == null");
        } else if(headers != null && headers.get("Content-Type") != null) {
            throw new IllegalArgumentException("Unexpected header: Content-Type");
        } else if(headers != null && headers.get("Content-Length") != null) {
            throw new IllegalArgumentException("Unexpected header: Content-Length");
        } else {
            Buffer heading = this.createPartHeading(headers, body, this.partHeadings.isEmpty());
            this.partHeadings.add(heading);
            this.partBodies.add(body);
            long bodyContentLength = body.contentLength();
            if(bodyContentLength == -1L) {
                this.length = -1L;
            } else if(this.length != -1L) {
                this.length += heading.size() + bodyContentLength;
            }

            return this;
        }
    }

    private static StringBuilder appendQuotedString(StringBuilder target, String key) {
        target.append('\"');
        int i = 0;

        for(int len = key.length(); i < len; ++i) {
            char ch = key.charAt(i);
            switch(ch) {
                case '\n':
                    target.append("%0A");
                    break;
                case '\r':
                    target.append("%0D");
                    break;
                case '\"':
                    target.append("%22");
                    break;
                default:
                    target.append(ch);
            }
        }

        target.append('\"');
        return target;
    }

    public ProgressedMultipartBuilder addFormDataPart(String name, String value) {
        return this.addFormDataPart(name, (String)null, RequestBody.create((MediaType)null, value));
    }

    public ProgressedMultipartBuilder addFormDataPart(String name, String filename, RequestBody value) {
        if(name == null) {
            throw new NullPointerException("name == null");
        } else {
            StringBuilder disposition = new StringBuilder("form-data; name=");
            appendQuotedString(disposition, name);
            if(filename != null) {
                disposition.append("; filename=");
                appendQuotedString(disposition, filename);
            }

            return this.addPart(Headers.of(new String[]{"Content-Disposition", disposition.toString()}), value);
        }
    }

    private Buffer createPartHeading(Headers headers, RequestBody body, boolean isFirst) {
        Buffer sink = new Buffer();
        if(!isFirst) {
            sink.write(CRLF);
        }

        sink.write(DASHDASH);
        sink.write(this.boundary);
        sink.write(CRLF);
        if(headers != null) {
            for(int contentType = 0; contentType < headers.size(); ++contentType) {
                sink.writeUtf8(headers.name(contentType)).write(COLONSPACE).writeUtf8(headers.value(contentType)).write(CRLF);
            }
        }

        MediaType var8 = body.contentType();
        if(var8 != null) {
            sink.writeUtf8("Content-Type: ").writeUtf8(var8.toString()).write(CRLF);
        }

        long contentLength = body.contentLength();
        if(contentLength != -1L) {
            sink.writeUtf8("Content-Length: ").writeUtf8(Long.toString(contentLength)).write(CRLF);
        }

        sink.write(CRLF);
        return sink;
    }

    public ProgressedMultipartRequestBody build() throws IllegalStateException
    {
        if(this.partHeadings.isEmpty()) {
            throw new IllegalStateException("Multipart body must have at least one part.");
        } else {
            return new ProgressedMultipartRequestBody(this.type, this.boundary, this.partHeadings, this.partBodies, this.length);
        }
    }
}
