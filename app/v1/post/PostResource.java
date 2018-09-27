package v1.post;

import java.util.List;

/**
 * Resource for the API.  This is a presentation class for frontend work.
 */
public class PostResource {
    private String id;
    private String link;
    private String userId;
    private String urls;

    public PostResource() {
    }

    public PostResource(String id, String link, String userId, String urls) {
        this.id = id;
        this.link = link;
        this.urls = urls;
        this.userId = userId;
    }

    public PostResource(PostData data, String link) {
        this.id = data.id.toString();
        this.link = link;
        this.userId = data.userId;
        this.urls = data.urls;
    }

    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }


    public String getUserId() {
        return userId;
    }

    public String getUrls() {
        return urls;
    }
}
