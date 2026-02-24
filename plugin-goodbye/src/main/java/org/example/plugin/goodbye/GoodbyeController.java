package org.example.plugin.goodbye;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugins")
public class GoodbyeController {

  @GetMapping("/goodbye")
  public String greet(String input) {
    return "Goodbye " + (input != null ? input : "World");
  }
}
