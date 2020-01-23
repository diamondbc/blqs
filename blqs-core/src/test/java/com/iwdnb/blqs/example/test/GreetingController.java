package com.iwdnb.blqs.example.test;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 欢迎使用blqs
 * 
 * @index 1
 */
@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong    counter  = new AtomicLong();

    /// **
    // * 示例接口
    // * @param name 名称
    // * @return
    // */
    // @RequestMapping("/greeting")
    // public Greeting greeting(@RequestParam(value="name", defaultValue="") String name) {
    // return new Greeting(counter.incrementAndGet(),
    // String.format(template, name));
    // }
    //

    /**
     * 示例接口2
     * 
     * @param name 名称
     * @return
     */
    @RequestMapping("/greeting2")
    public Result<Greeting> greeting2(@RequestParam(value = "name", defaultValue = "tesdt") String name) {
        return null;
    }

    /**
     * 示例接口2
     * 
     * @param name 名称
     * @return
     */
    @RequestMapping("/greeting3")
    public Result greeting3(@RequestParam(value = "name", defaultValue = "test") String name) {
        return null;
    }

    /// **
    // * 示例接口2
    // * @param name 名称
    // * @return
    // */
    // @RequestMapping("/greeting24")
    // public DataResult greeting4(@RequestParam(value="name", defaultValue="test") String name) {
    // return null;
    // }
}
