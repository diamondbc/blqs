package com.iwdnb.blqs.example;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.iwdnb.blqs.core.BlqsContext;
import com.iwdnb.blqs.core.Options;

/**
 * @title Blqs示例文档
 * @description 通过javadoc设置文档描述信息 优先级大于通过Environment.description()设置的值
 * @readme 所有接口均使用Https调用 /app路径下的接口为app专用 /mini路径下的接口为小程序专用
 */
public class BlqsContextTest {

    @Test
    public void blqs() {
        Options options = new Options().source(Paths.get("src", "test",
                                                         "java")).ignore("ResponseEntity").id("blqs.wdnb.com").title("示例接口文档").description("示例接口文档，使用默认模板").yapiToken("202cc47b5b8dd071824d13aa9344fd4c1dbc3ec2e28817a1d771f1c3d987c124").jar(Paths.get("/Users/XXX/test.jar"),
                                                                                                                                                                                                                                              Paths.get("/Users/XXX/test.jar"));
        BlqsContext blqs = new BlqsContext(options);
        blqs.lookup().build();

        // Path buildAdoc = options.getOutPath().resolve(options.getId() + ".adoc");
        // Path template = options.getOutPath().resolve("../../src/test/resources/template.adoc");
        Path templateHtml = options.getOutPath().resolve("../../src/test/resources/template.html");
        // Path resultHtml = options.getOutPath().resolve("diff.html");
        //
        // FileMatcher fileMatcher = new FileMatcher();
        // int changed = fileMatcher.compare(template, buildAdoc);
        // if(changed>0){
        // fileMatcher.rederHtml(templateHtml, resultHtml);
        // }
        // Assert.assertEquals(0, changed);
        System.out.println("BUILD SUCCESS");
    }

}
