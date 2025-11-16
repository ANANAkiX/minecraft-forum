package com.minecraftforum.aspect;

import com.minecraftforum.config.custom.annotations.IndexToElasticsearch;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.Resource;
import com.minecraftforum.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Elasticsearch 索引切面
 * 异步处理索引操作，不阻塞主业务流程
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexAspect {
    
    private final SearchService searchService;
    
    /**
     * 切点：所有标记了 @IndexToElasticsearch 的方法
     */
    @Pointcut("@annotation(com.minecraftforum.config.custom.annotations.IndexToElasticsearch)")
    public void indexPointcut() {}
    
    /**
     * 方法执行成功后，异步索引
     */
    @AfterReturning(pointcut = "indexPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            IndexToElasticsearch annotation = method.getAnnotation(IndexToElasticsearch.class);
            
            if (annotation == null) {
                return;
            }
            
            // 如果是删除操作
            if (annotation.onDelete()) {
                Long id = extractId(joinPoint, result, annotation);
                if (id != null) {
                    deleteIndexAsync(annotation.type(), id);
                }
                return;
            }
            
            // 对于资源，需要检查状态是否为 APPROVED
            if (annotation.type() == IndexToElasticsearch.IndexType.RESOURCE) {
                Resource resource = extractResource(joinPoint, result);
                if (resource != null) {
                    // 只有审核通过的资源才索引
                    if ("APPROVED".equals(resource.getStatus())) {
                        indexAsync(annotation.type(), resource.getId());
                    } else {
                        // 如果资源被拒绝或待审核，删除索引
                        deleteIndexAsync(annotation.type(), resource.getId());
                    }
                } else {
                    Long id = extractId(joinPoint, result, annotation);
                    if (id != null) {
                        // 如果无法获取资源对象，尝试查询状态
                        checkAndIndexResource(id);
                    }
                }
            } else {
                // 帖子直接索引
                Long id = extractId(joinPoint, result, annotation);
                if (id != null) {
                    indexAsync(annotation.type(), id);
                }
            }
            
        } catch (Exception e) {
            log.error("索引切面处理失败", e);
        }
    }
    
    /**
     * 提取 Resource 对象
     */
    private Resource extractResource(JoinPoint joinPoint, Object result) {
        if (result instanceof Resource) {
            return (Resource) result;
        }
        
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Resource) {
                    return (Resource) arg;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 检查资源状态并索引（当无法从方法参数/返回值获取资源对象时）
     */
    @Async
    public void checkAndIndexResource(Long resourceId) {
        try {
            // SearchService.indexResource 内部会检查资源状态，只有 APPROVED 状态的资源才会被索引
            searchService.indexResource(resourceId);
        } catch (Exception e) {
            log.error("检查并索引资源失败: resourceId={}", resourceId, e);
        }
    }
    
    /**
     * 提取ID
     */
    private Long extractId(JoinPoint joinPoint, Object result, IndexToElasticsearch annotation) {
        // 如果返回值是实体对象，尝试从返回值中提取ID
        if (result != null) {
            if (result instanceof ForumPost) {
                return ((ForumPost) result).getId();
            } else if (result instanceof Resource) {
                return ((Resource) result).getId();
            } else if (result instanceof Long) {
                return (Long) result;
            } else if (result instanceof Number) {
                return ((Number) result).longValue();
            }
        }
        
        // 如果指定了参数名，从方法参数中提取
        String idParam = annotation.idParam();
        if (idParam != null && !idParam.isEmpty()) {
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            
            for (int i = 0; i < paramNames.length; i++) {
                if (idParam.equals(paramNames[i])) {
                    Object arg = args[i];
                    if (arg instanceof Long) {
                        return (Long) arg;
                    } else if (arg instanceof Number) {
                        return ((Number) arg).longValue();
                    }
                }
            }
        }
        
        // 尝试从第一个参数中提取ID（常见情况）
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof Long) {
                return (Long) firstArg;
            } else if (firstArg instanceof Number) {
                return ((Number) firstArg).longValue();
            } else if (firstArg instanceof ForumPost) {
                return ((ForumPost) firstArg).getId();
            } else if (firstArg instanceof Resource) {
                return ((Resource) firstArg).getId();
            }
        }
        
        return null;
    }
    
    /**
     * 异步索引
     */
    @Async
    public void indexAsync(IndexToElasticsearch.IndexType type, Long id) {
        try {
            if (type == IndexToElasticsearch.IndexType.POST) {
                searchService.indexPost(id);
            } else if (type == IndexToElasticsearch.IndexType.RESOURCE) {
                searchService.indexResource(id);
            }
        } catch (Exception e) {
            log.error("异步索引失败: type={}, id={}", type, id, e);
        }
    }
    
    /**
     * 异步删除索引
     */
    @Async
    public void deleteIndexAsync(IndexToElasticsearch.IndexType type, Long id) {
        try {
            if (type == IndexToElasticsearch.IndexType.POST) {
                searchService.deletePostIndex(id);
            } else if (type == IndexToElasticsearch.IndexType.RESOURCE) {
                searchService.deleteResourceIndex(id);
            }
        } catch (Exception e) {
            log.error("异步删除索引失败: type={}, id={}", type, id, e);
        }
    }
}

