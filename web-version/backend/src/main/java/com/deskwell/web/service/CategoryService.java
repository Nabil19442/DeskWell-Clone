package com.deskwell.web.service;

import com.deskwell.web.model.Application;
import com.deskwell.web.model.Category;
import com.deskwell.web.repository.ApplicationRepository;
import com.deskwell.web.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ApplicationRepository applicationRepository;

    private static final Map<String, String[]> KEYWORD_CATEGORIES = new LinkedHashMap<>();

    static {
        KEYWORD_CATEGORIES.put("Development", new String[]{
            "code", "visual studio", "intellij", "eclipse", "netbeans",
            "terminal", "cmd", "powershell", "git", "docker", "postman",
            "sublime", "atom", "vim", "neovim", "debug", "pycharm",
            "webstorm", "rider", "android studio", "xcode", "clion",
            "goland", "phpstorm", "rubymine", "datagrip", "compiler",
            "gcc", "javac", "python", "node", "rstudio", "jupyter",
            "arduino", "codeblocks", "code::blocks", "platformio",
            "energia", "keil", "mplab", "cubeide", "esp-idf", "avr studio",
            "avrdude", "putty", "minicom"
        });
        KEYWORD_CATEGORIES.put("Productivity", new String[]{
            "word", "excel", "powerpoint", "outlook", "onenote", "notion",
            "evernote", "todoist", "trello", "asana", "calendar", "notes",
            "photoshop", "illustrator", "indesign", "premiere", "after effects",
            "lightroom", "xd", "figma", "sketch", "canva", "coreldraw",
            "affinity", "blender", "davinci resolve", "procreate",
            "chatgpt", "claude", "copilot", "midjourney", "stable diffusion",
            "gemini", "dall-e", "runway", "perplexity"
        });
        KEYWORD_CATEGORIES.put("Browser", new String[]{
            "chrome", "firefox", "edge", "safari", "opera", "brave"
        });
        KEYWORD_CATEGORIES.put("Entertainment", new String[]{
            "youtube", "netflix", "spotify", "prime", "hulu", "disney",
            "twitch", "vimeo", "media player", "vlc", "video"
        });
        KEYWORD_CATEGORIES.put("Communication", new String[]{
            "slack", "discord", "teams", "zoom", "skype", "telegram",
            "whatsapp", "messenger", "signal", "chat", "mail", "gmail",
            "facebook", "instagram", "twitter", "x.com", "tiktok",
            "reddit", "linkedin", "pinterest", "snapchat", "threads",
            "tumblr", "quora", "wechat", "line", "viber"
        });
        KEYWORD_CATEGORIES.put("Gaming", new String[]{
            "steam", "epic", "minecraft", "league", "valorant", "fortnite",
            "gog", "battle.net", "origin", "uplay"
        });
    }

    public CategoryService(CategoryRepository categoryRepository, ApplicationRepository applicationRepository) {
        this.categoryRepository = categoryRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String color, Double weight) {
        Category cat = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        if (name != null && !name.isBlank()) cat.setName(name);
        if (color != null && !color.isBlank()) cat.setColor(color);
        if (weight != null) cat.setProductivityWeight(weight);
        return categoryRepository.save(cat);
    }

    public String determineCategoryName(String appName) {
        String lower = appName.toLowerCase();
        for (Map.Entry<String, String[]> entry : KEYWORD_CATEGORIES.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lower.contains(kw.toLowerCase())) {
                    return entry.getKey();
                }
            }
        }
        return "Other";
    }

    @Transactional
    public Application autoCategorize(Application app) {
        String targetCatName = determineCategoryName(app.getName());
        Category cat = categoryRepository.findByNameIgnoreCase(targetCatName)
            .orElseGet(() -> categoryRepository.findByNameIgnoreCase("Other").orElse(null));
        app.setCategory(cat);
        return applicationRepository.save(app);
    }

    @Transactional
    public Application assignCategory(Long appId, Long categoryId) {
        Application app = applicationRepository.findById(appId)
            .orElseThrow(() -> new IllegalArgumentException("App not found: " + appId));
        Category cat = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        app.setCategory(cat);
        return applicationRepository.save(app);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
}
