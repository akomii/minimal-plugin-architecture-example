package org.example.plugin.four;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plugins")
public class FourController {

  @GetMapping("/four")
  public ResponseEntity<String> greet(@RequestParam(name = "input", required = false) String input) {
    String result = "4 Hello's for " + (input != null ? input : "World");
    return ResponseEntity.ok(result);
  }
}
