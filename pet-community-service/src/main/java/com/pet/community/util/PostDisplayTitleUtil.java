package com.pet.community.util;

import org.springframework.util.StringUtils;

/** 从 Markdown 正文推导列表用标题（首条非空行，若以 # 开头则去掉井号前缀）。 */
public final class PostDisplayTitleUtil {

    private static final int MAX_LEN = 80;

    private PostDisplayTitleUtil() {
    }

    public static String fromContent(String content) {
        if (!StringUtils.hasText(content)) {
            return "无标题";
        }
        String[] lines = content.split("\\R");
        for (String line : lines) {
            String t = line.trim();
            if (!StringUtils.hasText(t)) {
                continue;
            }
            if (t.startsWith("#")) {
                t = t.replaceFirst("^#{1,6}\\s*", "").trim();
            }
            if (StringUtils.hasText(t)) {
                return truncate(t, MAX_LEN);
            }
        }
        return "无标题";
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "…";
    }
}
