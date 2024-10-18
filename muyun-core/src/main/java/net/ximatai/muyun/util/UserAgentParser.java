package net.ximatai.muyun.util;

public class UserAgentParser {

    /**
     * 从 User-Agent 头中获取操作系统信息
     *
     * @param userAgent User-Agent 字符串
     * @return 操作系统名称
     */
    public static String getOS(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown OS";
        }

        // 检测常见的操作系统类型
        if (userAgent.contains("Windows NT")) {
            if (userAgent.contains("Windows NT 11.0")) {
                return "Windows 11";
            } else if (userAgent.contains("Windows NT 10.0")) {
                return "Windows 10";
            } else if (userAgent.contains("Windows NT 6.3")) {
                return "Windows 8.1";
            } else if (userAgent.contains("Windows NT 6.2")) {
                return "Windows 8";
            } else if (userAgent.contains("Windows NT 6.1")) {
                return "Windows 7";
            } else {
                return "Windows (Unknown version)";
            }
        } else if (userAgent.contains("Mac OS X")) {
            return "Mac OS X";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone")) {
            return "iOS (iPhone)";
        } else if (userAgent.contains("iPad")) {
            return "iOS (iPad)";
        } else if (userAgent.contains("Unix")) {
            return "Unix";
        }

        return "Unknown OS";
    }

    /**
     * 从 User-Agent 头中获取浏览器信息
     *
     * @param userAgent User-Agent 字符串
     * @return 浏览器名称
     */
    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Browser";
        }

        // 检测常见的浏览器类型
        if (userAgent.contains("Edge") || userAgent.contains("Edg/")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("Chrome")) {
            return "Google Chrome";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        } else if (userAgent.contains("Firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "Internet Explorer";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        }

        return "Unknown Browser";
    }
}
