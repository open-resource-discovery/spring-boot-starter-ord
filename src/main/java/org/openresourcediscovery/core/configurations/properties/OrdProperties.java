package org.openresourcediscovery.core.configurations.properties;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "ord")
public class OrdProperties {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Document {

    private String name;
    private String path;

    @Builder.Default
    private Set<String> accessStrategies = emptySet();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ApiResource {

    private String name;
    private String path;
    private String mediaType;

    @Builder.Default
    private Set<String> accessStrategies = emptySet();
  }

  @Builder.Default
  private boolean autoconfigure = true;

  @Builder.Default
  private String documentsPath = "/ord/v1/documents";

  @Builder.Default
  private String resourcesPath = "/ord/v1/resources";

  @Builder.Default
  private String wellKnownPath = "/.well-known/open-resource-discovery";

  @Builder.Default
  private String application = "";

  @Builder.Default
  private String namespace = "customer";

  @Builder.Default
  private List<String> packages = emptyList();

  @Builder.Default
  private List<Document> documents = emptyList();

  @Builder.Default
  private List<ApiResource> apiResources = emptyList();

  @Builder.Default
  private Map<String, String> credentials = emptyMap();
}
