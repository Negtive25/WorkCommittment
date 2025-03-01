package org.com.code.webcommunity.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarkdownController {

    //创建Markdown解析器和HTML渲染器
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    //接收前端的Markdown文本并返回HTML
    @PostMapping("/markdownConvert")
    public ResponseEntity<String> convertMarkdownToHtml(@RequestBody String markdown) {
        // 解析Markdown文本
        Node document = parser.parse(markdown);
        // 转换为HTML
        String html = renderer.render(document);
        return ResponseEntity.ok(html);
    }
}
