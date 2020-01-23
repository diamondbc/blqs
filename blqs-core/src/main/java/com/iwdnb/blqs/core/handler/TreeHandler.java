package com.iwdnb.blqs.core.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import com.iwdnb.blqs.core.Options;
import com.iwdnb.blqs.core.schema.Tree;

/**
 * 文档结构树访问器
 */
public interface TreeHandler {

    void handle(Tree tree, Options options);

    default void write(Path file, String content, Charset charset, OpenOption... openOptions) {
        if (file.getParent() != null) {
            try {
                Files.createDirectories(file.getParent());
            } catch (IOException e) {
                throw new RuntimeException("Failed create directory", e);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file, charset, openOptions)) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }

}
