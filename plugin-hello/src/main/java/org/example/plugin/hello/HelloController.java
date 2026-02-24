package org.example.plugin.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugins")
public class HelloController {

  @GetMapping("/hello")
  public String greet(String input) {
    return "Hello " + (input != null ? input : "World");
  }
}
