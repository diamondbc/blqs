# 欢迎使用blqs,一款web应用API接口文档生成插件

blqs---博览群书,形容形容学识渊博,与本插件的主题比较契合~~~
# 前言
* 1.本插件需要配合yapi使用，yapi是一个接口文档管理系统，支持Swagger2,postman格式的接口数据导入导出，[yapi链接](http://yapi.demo.qunar.com/)。本插件使用前需要提前搭建yapi环境，需要把接口推送到yapi上去使用。
* 2.本插件代码借鉴了apigcc的部分代码，在此感谢作者。[apigcc链接](https://github.com/apigcc/apigcc)
* 3.本插件适用于springmvc项目，只需要2步，即可把项目的接口文档推送到yapi文档管理系统中去。


# 插件目标
现在java通常使用swagger2作为接口管理文档的通用框架，使用swagger2提供的注解编写文档对应的注释，本人以前的公司也是这么干的。
对接步骤一般为为3步:
* 第一步：在maven 中加入Swagger2的依赖。
* 第二步: 代码中配置Swagger2的配置信息。
* 第三步: controller中添加@Api、@ApiOperation等各种swagger2的注解。

但是这样真的不麻烦吗？如果一个应用的接口很多的话，这个手写接口注解的工作量不会很低吧。
那有没有一种框架能够提供一种更轻便的方式来生成接口文档呢？答案是必须有哇，这就是本插件能够做到的事情。
####本插件相对于Swagger2这种框架的优势(个人主观认定)：
* 1.配置简单。
* 2.保持对原应用无任何侵入性，对应用代码无任何影响。
* 3.无需手写接口注释信息。

# 插件使用
### 第一步 创建yapi应用
在yapi环境中创建应用，点击应用名-->设置-->token配置，获取应用的token，供后面步骤使用。给一个本人的临时yapi服务器，用于体验。yapi体验地址:http://106.13.231.37:3000/ ,测试账号密码:test@qq.com/123456
### 第二步 引入maven-plugin
本插件已上传到maven中央仓库，也可以下载源码，打包编译到maven本地仓库，具体编译过程不做赘述。
pom.xml
```xml
<build>
        <plugins>
            <plugin>
                <groupId>com.iwdnb</groupId>
                <artifactId>blqs-maven-plugin</artifactId>
                <version>1.0.2</version>
                <configuration>
                    <yapiSynDataUrl>http://yapiUrl/api/open/import_data</yapiSynDataUrl>
                    <yapiToken>yapiToken</yapiToken>
                    <urls>/api/user/**,/api/role/**</urls>
                    <incluedArtifacts>com.alibaba:fastjson*,com.google:**</incluedArtifact>
                </configuration>
                <executions>
                    <execution>
                        <phase>compile</phase>
                        <goals>
                            <goal>yapi</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
#### 参数定义：
* yapiSynDataUrl:yapi环境的地址,同步请求url为/api/open/import_data,所以地址写成${yapiUrl}/api/open/import_data
* yapiToken:第一步操作中应用的token。
* incluedArtifacts:项目中需要解析参数的jar包的artifact信息，支持模糊查询，逗号分隔。
* urls：项目中需要拦截的url前缀,默认读取全部url，支持模糊查询,逗号分隔,示例中/api/user/\*\*,/api/role/\*\*,会读取/api/user/\*\*和/api/role/\*\*的全部接口信息。
* blqsEnable:本参数标识本插件是否可用，默认为false，不启用，可以通过配置启用或者mvn命令行加**-DblqsEnable=true**启用配置。
* yapiBatch:本参数用于配置接口上传单次数量限制，接口超过限制会分批进行上传，默认100，一般无需设置该参数。

### 第三步 编译项目，查看接口文档
在项目的根目录下执行编译命令：**mvn clean install -Dmaven.test.skip=true -DblqsEnable=true**,查看编译结果。
```html
2020-01-01 11:30:25,651 [main] INFO  com.iwndb.blqs.maven.MojoCommon [MojoCommon.java : 56] - load jar:/Users/diamondbc/soft/repository/com/iwdnb/test/1.0.0-SNAPSHOT/test-1.0.0-SNAPSHOT.jar
2020-01-01 11:30:25,827 [main] INFO  com.iwdnb.blqs.core.Options [Options.java : 217] - yapiMockParam:{"code":"000000","message":"你瞅啥","isSuccess":"true"}
2020-01-01 11:30:28,052 [main] INFO  c.iwdnb.blqs.core.visitor.Framework [Framework.java : 57] - use framewokr:SPRINGMVC
2020-01-01 11:30:30,177 [main] INFO  com.iwdnb.blqs.core.BlqsContext [BlqsContext.java : 97] - Found 1 Controllers, 2 Endpoints
2020-01-01 11:30:30,335 [main] INFO  c.i.b.c.h.swagger.SwaggerTreeHandler [SwaggerTreeHandler.java : 47] - Build swagger /Users/diamondbc/IdeaProjects/test/target/blqs/test-swagger.json
2020-01-01 11:30:30,342 [main] INFO  com.iwdnb.blqs.core.yapi.YapiUtils [YapiUtils.java : 28] - YapiUtils.sysData,token:abcdefghijklmnopqrstuvwxyz count:2
2020-01-01 11:30:31,145 [main] INFO  com.iwdnb.blqs.core.yapi.YapiUtils [YapiUtils.java : 150] - YapiUtils.synData,result:{"errcode":0,"errmsg":"成功导入接口 2 个, 已存在的接口 2 个","data":null}
```
编译结果出现类似提示信息，即表示插件执行成功。
然后去yapi上查看应用的接口信息：

![接口文档截图](https://s2.ax1x.com/2020/01/23/1VSQm9.png "接口文档截图")

# 学习各位大佬，觉得好用的话打赏下呗~~~
![支付宝收款码](http://config.zhituanyou.com/zfb.png "支付宝收款码")
