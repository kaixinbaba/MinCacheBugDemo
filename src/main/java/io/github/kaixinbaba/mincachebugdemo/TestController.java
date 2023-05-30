package io.github.kaixinbaba.mincachebugdemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xunjunjie
 */
@RestController
public class TestController {


    @GetMapping("/test")
    public String test() {
        return "Ok";
    }

}
