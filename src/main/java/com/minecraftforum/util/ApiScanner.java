package com.minecraftforum.util;

import com.minecraftforum.config.ApiScannerConfig;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.*;

/**
 * API扫描工具类
 * 用于扫描所有Controller的API路径、请求方式和描述信息
 */
@Component
public class ApiScanner {
    
    private final ApiScannerConfig apiScannerConfig;
    
    public ApiScanner(ApiScannerConfig apiScannerConfig) {
        this.apiScannerConfig = apiScannerConfig;
    }
    
    /**
     * API信息DTO
     */

    public static class ApiInfo {
        private String url;
        private String method;
        private String description;
        private String summary;
        
        public ApiInfo(String url, String method, String description, String summary) {
            this.url = url;
            this.method = method;
            this.description = description;
            this.summary = summary;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getMethod() {
            return method;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getSummary() {
            return summary;
        }
        
        public String getDisplayName() {
            if (summary != null && !summary.isEmpty()) {
                return summary + " (" + method + " " + url + ")";
            }
            return method + " " + url;
        }
    }
    
    /**
     * 扫描所有Controller的API信息
     * 只扫描com.minecraftforum.controller包下的Controller
     */
    public List<ApiInfo> scanAllApis(ApplicationContext applicationContext) {
        List<ApiInfo> apiList = new ArrayList<>();
        // 只扫描controller包下的类
        String targetPackage = "com.minecraftforum.controller";
        
        try {
            // 获取所有Controller Bean
            Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
            controllers.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
            
            for (Object controller : controllers.values()) {
                try {
                    Class<?> controllerClass = controller.getClass();
                    
                    // 处理Spring代理类，获取原始类
                    Class<?> targetClass = controllerClass;
                    // 如果是CGLIB代理类（包含$$），获取父类
                    if (targetClass.getName().contains("$$")) {
                        targetClass = targetClass.getSuperclass();
                    }
                    // 如果是JDK动态代理，尝试从接口获取
                    if (targetClass.getName().contains("$Proxy")) {
                        Class<?>[] interfaces = targetClass.getInterfaces();
                        if (interfaces.length > 0) {
                            targetClass = interfaces[0];
                        }
                    }
                    
                    // 只扫描指定包下的Controller
                    String className = targetClass.getName();
                    if (!className.startsWith(targetPackage)) {
                        continue;
                    }
                    
                    // 获取简单的类名（不含包名）
                    String simpleClassName = targetClass.getSimpleName();
                    
                    // 检查是否排除该 Controller
                    if (isExcludedController(simpleClassName)) {
                        continue;
                    }
                    
                    // 检查类级别的 @AnonymousAccess 注解，如果 excludeFromScan 为 true，则跳过整个类
                    AnonymousAccess classAnonymousAccess = targetClass.getAnnotation(AnonymousAccess.class);
                    if (classAnonymousAccess != null && classAnonymousAccess.excludeFromScan()) {
                        continue;
                    }
                    
                    // 获取类级别的RequestMapping注解
                    RequestMapping classMapping = targetClass.getAnnotation(RequestMapping.class);
                    String basePath = classMapping != null ? normalizePath(classMapping.value().length > 0 ? classMapping.value()[0] : "") : "";
                    
                    // 扫描所有public方法（包括父类方法）
                    Method[] methods = targetClass.getMethods();
                    for (Method method : methods) {
                        try {
                            // 只处理当前类或其父类中定义的方法，跳过Object类的方法
                            if (method.getDeclaringClass() == Object.class) {
                                continue;
                            }
                            // 跳过非public方法
                            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                                continue;
                            }
                            
                            // 检查方法名是否被排除
                            if (isExcludedMethod(method.getName())) {
                                continue;
                            }
                            
                            // 检查方法级别的 @AnonymousAccess 注解，如果 excludeFromScan 为 true，则跳过该方法
                            AnonymousAccess methodAnonymousAccess = method.getAnnotation(AnonymousAccess.class);
                            if (methodAnonymousAccess != null && methodAnonymousAccess.excludeFromScan()) {
                                continue;
                            }
                            
                            ApiInfo apiInfo = extractApiInfo(method, basePath);
                            if (apiInfo != null) {
                                apiList.add(apiInfo);
                            }
                        } catch (Exception e) {
                            // 跳过单个方法扫描失败的情况，继续扫描其他方法
                            System.err.println("扫描方法失败: " + method.getName() + ", 错误: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    // 跳过单个Controller扫描失败的情况，继续扫描其他Controller
                    System.err.println("扫描Controller失败: " + controller.getClass().getName() + ", 错误: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // 如果整体扫描失败，返回空列表
            System.err.println("扫描API失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
        
        // 按URL和方法排序
        apiList.sort(Comparator.comparing(ApiInfo::getUrl).thenComparing(ApiInfo::getMethod));
        
        return apiList;
    }
    
    /**
     * 从方法中提取API信息
     */
    private ApiInfo extractApiInfo(Method method, String basePath) {
        // 检查是否有HTTP方法注解
        String httpMethod = null;
        String path = "";
        
        if (method.isAnnotationPresent(GetMapping.class)) {
            httpMethod = "GET";
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            httpMethod = "POST";
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            httpMethod = "PUT";
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            httpMethod = "DELETE";
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            RequestMethod[] methods = mapping.method();
            if (methods.length > 0) {
                httpMethod = methods[0].name();
            } else {
                httpMethod = "GET"; // 默认GET
            }
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        }
        
        if (httpMethod == null) {
            return null; // 不是API方法
        }
        
        // 构建完整路径
        String fullPath;
        if (basePath.isEmpty() && path.isEmpty()) {
            fullPath = "/";
        } else if (basePath.isEmpty()) {
            fullPath = normalizePath(path);
        } else if (path.isEmpty()) {
            fullPath = normalizePath(basePath);
        } else {
            fullPath = normalizePath(basePath + "/" + path);
        }
        
        // 获取Operation注解信息
        String description = "";
        String summary = "";
        if (method.isAnnotationPresent(Operation.class)) {
            Operation operation = method.getAnnotation(Operation.class);
            description = operation.description();
            summary = operation.summary();
        } else {
            // 如果没有@Operation注解，生成默认描述
            summary = method.getName();
            description = "API接口: " + method.getName();
        }
        
        return new ApiInfo(fullPath, httpMethod, description, summary);
    }
    
    /**
     * 规范化路径
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        // 移除多余的斜杠
        path = path.replaceAll("/+", "/");
        // 确保以/开头
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // 移除末尾的斜杠（除非是根路径）
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    /**
     * 检查 Controller 是否被排除
     */
    private boolean isExcludedController(String controllerName) {
        if (apiScannerConfig == null || apiScannerConfig.getExclude() == null) {
            return false;
        }
        
        List<String> excludedControllers = apiScannerConfig.getExclude().getControllers();
        if (excludedControllers == null || excludedControllers.isEmpty()) {
            return false;
        }
        
        for (String pattern : excludedControllers) {
            if (matchesPattern(controllerName, pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查方法是否被排除
     */
    private boolean isExcludedMethod(String methodName) {
        if (apiScannerConfig == null || apiScannerConfig.getExclude() == null) {
            return false;
        }
        
        List<String> excludedMethods = apiScannerConfig.getExclude().getMethods();
        if (excludedMethods == null || excludedMethods.isEmpty()) {
            return false;
        }
        
        for (String pattern : excludedMethods) {
            if (matchesPattern(methodName, pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 通配符匹配
     * 支持 * 通配符（匹配任意字符）
     * 例如：matchesPattern("AdminController", "*Controller") -> true
     *      matchesPattern("getUserInfo", "get*") -> true
     *      matchesPattern("getUserInfo", "*Info") -> true
     */
    private boolean matchesPattern(String text, String pattern) {
        if (text == null || pattern == null) {
            return false;
        }
        
        // 如果模式不包含通配符，直接比较
        if (!pattern.contains("*")) {
            return text.equals(pattern);
        }
        
        // 将通配符模式转换为正则表达式
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*");
        
        return text.matches(regex);
    }
}

