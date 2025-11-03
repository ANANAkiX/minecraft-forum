package com.minecraftforum.config;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AnonymousUrlCollector implements ApplicationContextAware {
    private static final List<String> anonymousUrls = new ArrayList<>();

    public static List<String> getAnonymousUrls() {
        return anonymousUrls;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        Map<String, Object> beans = context.getBeansWithAnnotation(RestController.class);

        beans.values().forEach(bean -> {
            Class<?> clazz = AopUtils.getTargetClass(bean);
            RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            String[] classPaths = classMapping != null ? classMapping.value() : new String[]{""};

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AnonymousAccess.class)) {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    GetMapping get = method.getAnnotation(GetMapping.class);
                    PostMapping post = method.getAnnotation(PostMapping.class);
                    String[] methodPaths = new String[]{};

                    if (mapping != null) methodPaths = mapping.value();
                    else if (get != null) methodPaths = get.value();
                    else if (post != null) methodPaths = post.value();

                    for (String classPath : classPaths) {
                        for (String methodPath : methodPaths) {
                            anonymousUrls.add(classPath + methodPath);
                        }
                    }
                }
            }
        });
    }
}
