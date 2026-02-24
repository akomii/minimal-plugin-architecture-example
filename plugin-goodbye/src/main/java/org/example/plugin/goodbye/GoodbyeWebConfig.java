package org.example.plugin.goodbye;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GoodbyeWebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/api/plugins/goodbye/ui/**").addResourceLocations(new ClassPathResource("ui/", GoodbyeWebConfig.class.getClassLoader()).getPath());
  }
}
