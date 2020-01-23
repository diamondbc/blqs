package com.iwdnb.blqs.example.test;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Greeting {

    /**
     * 编号
     */
    private final long   id;
    /**
     * 内容
     */
    private final String content;
    /**
     * btye字段
     */
    @NotNull
    @Size(max = 64)
    private byte         byteCode;
    private Greeting     greeting;

    public Greeting(long id, String content){
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Greeting getGreeting() {
        return greeting;
    }

    public void setGreeting(Greeting greeting) {
        this.greeting = greeting;
    }
}
