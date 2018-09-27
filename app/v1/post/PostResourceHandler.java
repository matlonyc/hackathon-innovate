package v1.post;

import com.palominolabs.http.url.UrlBuilder;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import javax.inject.Inject;
import java.nio.charset.CharacterCodingException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import services.LinkPreview;
import services.LinkPreview.LinkPreviewData;

/**
 * Handles presentation of Post resources, which map to JSON.
 */
public class PostResourceHandler {

    private final PostRepository repository;
    private final HttpExecutionContext ec;
    private final LinkPreview linkPreview;

    @Inject
    public PostResourceHandler(PostRepository repository, HttpExecutionContext ec, LinkPreview linkPreview) {
        this.repository = repository;
        this.ec = ec;
        this.linkPreview = linkPreview;
    }

    public CompletionStage<Stream<PostResource>> find() {
        return repository.list().thenApplyAsync(postDataStream -> {
            return postDataStream.map(data -> new PostResource(data, link(data)));
        }, ec.current());
    }

    public CompletionStage<PostResource> create(PostResource resource) {
        final PostData data = new PostData(resource.getUserId(), resource.getUrls());
        LinkPreviewData preview = linkPreview.getLinkPreview(data.urls);
        return repository.create(data).thenApplyAsync(savedData -> {
            return new PostResource(savedData, link(savedData));
        }, ec.current());
    }

    public CompletionStage<Optional<PostResource>> lookup(String id) {
        return repository.get(Long.parseLong(id)).thenApplyAsync(optionalData -> {
            return optionalData.map(data -> new PostResource(data, link(data)));
        }, ec.current());
    }

    public CompletionStage<Optional<PostResource>> update(String id, PostResource resource) {
        final PostData data = new PostData(resource.getUserId(), resource.getUrls());
        return repository.update(Long.parseLong(id), data).thenApplyAsync(optionalData -> {
            return optionalData.map(op -> new PostResource(op, link(op)));
        }, ec.current());
    }

    private String link(PostData data) {
        // Make a point of using request context here, even if it's a bit strange
        final Http.Request request = Http.Context.current().request();
        final String[] hostPort = request.host().split(":");
        String host = hostPort[0];
        int port = (hostPort.length == 2) ? Integer.parseInt(hostPort[1]) : -1;
        final String scheme = request.secure() ? "https" : "http";
        try {
            return UrlBuilder.forHost(scheme, host, port)
                    .pathSegments("v1", "posts", data.id.toString())
                    .toUrlString();
        } catch (CharacterCodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
