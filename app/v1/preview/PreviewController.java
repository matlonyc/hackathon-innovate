package v1.preview;

import com.google.inject.Inject;
import java.util.stream.Collectors;
import play.Logger;
import play.api.libs.json.JsArray;
import play.api.libs.json.JsValue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.LinkPreview;

public class PreviewController extends Controller {
  private final Logger.ALogger logger = Logger.of(this.getClass());
  private final LinkPreview linkPreview;

  @Inject
  public PreviewController(LinkPreview linkPreview) {
    this.linkPreview = linkPreview;
  }

  public Result list() {
//    logger.error("Yo: " + linkPreview.get().size());

//    return ok(Json.toJson(linkPreview.get().stream().map(JsValue::toString).collect(Collectors.toList())));
    return ok(Json.toJson(linkPreview.get()));
  }

  public Result allTags() {
    return ok(Json.toJson(linkPreview.getAllTags()));
  }
}
