package org.example.plugin.app;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.example.plugin.api.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class MetadataExtractor {

  private static final Logger log = LoggerFactory.getLogger(MetadataExtractor.class);

  private static final String META_INF_MAVEN = "META-INF/maven/";
  private static final String META_INF_SERVICES = "META-INF/services/";
  private static final String PLUGIN_GROUP_ID = "org.example.plugin";
  private static final String API_ARTIFACT_ID = "api";

  public record PomInfo(String artifactId, String version) {

  }

  public PomInfo extractPomInfo(File jarFile) {
    try (JarFile jar = new JarFile(jarFile)) {
      JarEntry entry = findEntry(jar, "pom.properties");
      if (entry != null) {
        Properties props = new Properties();
        try (InputStream is = jar.getInputStream(entry)) {
          props.load(is);
          return new PomInfo(
              props.getProperty("artifactId", "unknown"),
              props.getProperty("version", "unknown")
          );
        }
      }
    } catch (Exception e) {
      log.warn("Could not extract pom properties from {}", jarFile.getName(), e);
    }
    return new PomInfo("unknown", "unknown");
  }

  public List<String> extractDependencies(File jarFile) {
    List<String> deps = new ArrayList<>();
    try (JarFile jar = new JarFile(jarFile)) {
      JarEntry entry = findEntry(jar, "pom.xml");
      if (entry != null) {
        try (InputStream is = jar.getInputStream(entry)) {
          Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
          NodeList depNodes = doc.getElementsByTagName("dependency");
          for (int i = 0; i < depNodes.getLength(); i++) {
            Element dep = (Element) depNodes.item(i);
            String groupId = dep.getElementsByTagName("groupId").item(0).getTextContent();
            String artifactId = dep.getElementsByTagName("artifactId").item(0).getTextContent();
            if (PLUGIN_GROUP_ID.equals(groupId) && !API_ARTIFACT_ID.equals(artifactId)) {
              deps.add(artifactId);
            }
          }
        }
      }
    } catch (Exception e) {
      log.warn("Could not extract dependencies from {}", jarFile.getName(), e);
    }
    return deps;
  }

  private JarEntry findEntry(JarFile jar, String suffix) {
    return jar.stream()
        .filter(e -> e.getName().startsWith(META_INF_MAVEN) && e.getName().endsWith(suffix))
        .findFirst()
        .orElse(null);
  }

  public boolean isPluginJar(File jarFile) {
    try (JarFile jar = new JarFile(jarFile)) {
      String serviceFile = META_INF_SERVICES + Plugin.class.getName();
      return jar.getJarEntry(serviceFile) != null;
    } catch (Exception e) {
      return false;
    }
  }
}
