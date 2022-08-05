package com.github.miho73.lila.controllers;

import com.github.miho73.lila.services.AuthService;
import com.github.miho73.lila.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller("MainController")
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String sitemap, robots;

    @Autowired AuthService authService;
    @Autowired SessionService sessionService;

    @PostConstruct
    public void initCommonControl() {
        ClassPathResource robotsResource = new ClassPathResource("etc/robots.txt");
        ClassPathResource sitemapResource = new ClassPathResource("etc/sitemap.xml");

        try {
            BufferedReader robotStream = new BufferedReader(new InputStreamReader(robotsResource.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader sitemapStream = new BufferedReader(new InputStreamReader(sitemapResource.getInputStream(), StandardCharsets.UTF_8));
            this.robots = readBufferedReader(robotStream);
            this.sitemap = readBufferedReader(sitemapStream);
        } catch (IOException | RuntimeException e) {
            logger.error("Cannot read file.", e);
            throw new RuntimeException(e);
        }
    }
    private String readBufferedReader(BufferedReader reader) throws IOException {
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        while ((temp = reader.readLine()) != null) {
            stringBuffer.append(temp)
                        .append("\n");
        }
        return stringBuffer.toString();
    }

    @GetMapping("")
    public String index(Model model,
                        HttpServletResponse response, HttpSession session) {
        sessionService.loadIdentity(model, session);
        return "index";
    }

    @GetMapping(value = "robots.txt", produces = "text/plain")
    @ResponseBody
    public String robots() {
        return robots;
    }

    @GetMapping(value = "sitemap.xml", produces = "application/xml")
    @ResponseBody
    public String sitemap(HttpServletResponse response) {
        return sitemap;
    }
}
