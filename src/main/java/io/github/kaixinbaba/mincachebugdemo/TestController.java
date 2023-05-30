package io.github.kaixinbaba.mincachebugdemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author xunjunjie
 */
@RestController
public class TestController {

    @Resource
    private TestService testService;


    @GetMapping("/test")
    public String test() {
        return "Ok";
    }


    @GetMapping("/cache/{name}")
    public String cache(@PathVariable("name") String name) {
        return testService.cache(name);
    }

}
