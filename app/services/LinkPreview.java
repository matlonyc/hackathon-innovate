package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Entity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import play.Logger;
import play.api.libs.json.JsValue;
import play.api.libs.ws.WSClient;
import play.api.libs.ws.WSRequest;
import play.api.libs.ws.WSResponse;
import play.libs.Json;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

@Singleton
public class LinkPreview {

  private static final String key = "5bacf6e3d7a55d81f3fc41fc0037e8a9044dc05ede41e";
  private static final String urlPattern = "http://api.linkpreview.net/?key=%s&q=%s";
  private static final String taggingKey = "42d32b086b4aef04a2e00dde1f28261df315c88488b23fa594c8f32c";
  private final WSClient ws;
  private final ExecutionContext ec;
  private final Logger.ALogger logger = Logger.of(this.getClass());
  private final Map<String, JsonNode> previews = Maps.newConcurrentMap();
  private final TextRazor client = new TextRazor(taggingKey);
  private final Set<String> allTags = Sets.newConcurrentHashSet();
  private final Set<String> goodTags = Sets.newHashSet(
      "tv",
      "education",
      "music",
      "government",
      "business",
      "sports",
      "food"
  );
  private final String noTag = "finance";
  @Inject
  public LinkPreview(WSClient ws, ExecutionContext ec) {
    this.ws = ws;
    this.ec = ec;
    client.addExtractor("topics");
    client.addExtractor("entities");
  }

  public LinkPreviewData getLinkPreview(String url) {
    String completeUrl = String.format(urlPattern, key, url);
    logger.error(completeUrl);
    WSRequest request = ws.url(completeUrl);
    Future<WSResponse> responsePromise = request.get();
    responsePromise.map(response -> doSomethingWithResponse(url, response), ec);
    return null;
  }

  public List<JsonNode> get() {
    return ImmutableList.copyOf(previews.values());
  }

  public List<String> getAllTags() {
    return allTags.stream().collect(Collectors.toList());
  }

  private Object doSomethingWithResponse(String url, WSResponse response) {
    JsonNode test = Json.parse(response.json().toString());
    ((ObjectNode) test).put("date", LocalDateTime.now().toString());

    Set<String> tags = Sets.newHashSet();
    try {
      AnalyzedText taggingResponse =
          client.analyze(test.at("/description").toString());
      for (Entity entity : taggingResponse.getResponse().getEntities()) {
//        logger.error("tagging: " + entity.getEntityId());
        List<String> newTags =
            entity.getFreebaseTypes().stream()
                .map(s -> s.split("/")[1])
                .filter(goodTags::contains)
                .collect(Collectors.toList());
        if (newTags.isEmpty()) {
          newTags.add(noTag);
        }
        tags.addAll(newTags);
        allTags.addAll(newTags);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    if(tags.isEmpty())
      tags.add(noTag);
    ((ObjectNode) test).put("tags", tags.stream().collect(Collectors.joining(",")));

    previews.put(url, test);
    logger.error(test.toString());
    return null;
  }

  static public class LinkPreviewData {

    private final String title;
    private final String description;
    private final String image;
    private final String url;

    LinkPreviewData(String title, String description, String image, String url) {
      this.title = title;
      this.description = description;
      this.image = image;
      this.url = url;
    }
  }

}
