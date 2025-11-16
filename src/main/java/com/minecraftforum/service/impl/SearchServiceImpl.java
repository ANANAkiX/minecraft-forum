package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minecraftforum.dto.SearchResultDTO;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.Resource;
import com.minecraftforum.entity.SysFile;
import com.minecraftforum.entity.User;
import com.minecraftforum.entity.es.SearchDocument;
import com.minecraftforum.mapper.ForumPostMapper;
import com.minecraftforum.mapper.ResourceMapper;
import com.minecraftforum.mapper.SysFileMapper;
import com.minecraftforum.mapper.UserMapper;
import com.minecraftforum.repository.es.SearchDocumentRepository;
import com.minecraftforum.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    
    private final SearchDocumentRepository searchDocumentRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ForumPostMapper forumPostMapper;
    private final ResourceMapper resourceMapper;
    private final SysFileMapper sysFileMapper;
    private final UserMapper userMapper;
    private final com.minecraftforum.config.ElasticsearchHealthChecker healthChecker;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String INDEX_NAME = "minecraft_forum_search";
    
    /**
     * 确保索引存在，如果不存在则创建
     */
    private void ensureIndexExists() {
        try {
            boolean exists = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(INDEX_NAME))).value();
            if (!exists) {
                // 创建索引
                elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .settings(IndexSettings.of(s -> s
                                .numberOfShards("1")
                                .numberOfReplicas("0")
                        ))
                ));
                log.info("Elasticsearch 索引创建成功: {}", INDEX_NAME);
            }
        } catch (Exception e) {
            log.warn("检查或创建 Elasticsearch 索引失败: {}", INDEX_NAME, e);
        }
    }
    
    @Override
    public List<SearchResultDTO> search(String keyword, int page, int pageSize) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.debug("Elasticsearch 不可用，返回空搜索结果");
            // 触发一次连接检查（异步）
            healthChecker.checkConnection();
            return new ArrayList<>();
        }
        
        try {
            // 确保索引存在
            ensureIndexExists();
            
            // 构建高亮字段，使用带 class 的 span 标签包裹关键词
            // 前端通过 class 选择器统一设置样式，后端只负责标记
            // 不设置内联样式，完全由前端控制
            String highlightPreTag = "<span class=\"search-highlight\">";
            String highlightPostTag = "</span>";
            
            Highlight highlight = Highlight.of(h -> h
                    .fields("title", HighlightField.of(f -> f
                            .preTags(highlightPreTag)
                            .postTags(highlightPostTag)))
                    .fields("content", HighlightField.of(f -> f
                            .preTags(highlightPreTag)
                            .postTags(highlightPostTag)
                            .fragmentSize(200)))
                    .fields("fileNames", HighlightField.of(f -> f
                            .preTags(highlightPreTag)
                            .postTags(highlightPostTag)))
            );
            
            // 构建查询
            // 对于短关键词（长度 <= 2），使用简单匹配，不使用模糊匹配
            Query query;
            if (keyword.length() <= 2) {
                // 短关键词使用多字段匹配，但不使用模糊匹配（fuzziness）
                // 使用 OR 操作符，只要匹配任意一个词即可
                query = Query.of(q -> q
                        .multiMatch(MultiMatchQuery.of(m -> m
                                .query(keyword)
                                .fields("title^3", "content", "fileNames^2")  // title 权重更高
                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)  // 使用 OR 操作符，更宽松
                                .minimumShouldMatch("1")))  // 至少匹配一个字段
                );
            } else {
                // 长关键词使用多字段匹配，支持模糊匹配
                query = Query.of(q -> q
                        .multiMatch(MultiMatchQuery.of(m -> m
                                .query(keyword)
                                .fields("title^3", "content", "fileNames^2")  // title 权重更高
                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                .fuzziness("AUTO")))
                );
            }
            
            // 构建搜索请求
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .query(query)
                    .highlight(highlight)
                    .from((page - 1) * pageSize)
                    .size(pageSize)
            );
            
            // 执行搜索
            SearchResponse<SearchDocument> searchResponse = elasticsearchClient.search(
                    searchRequest, SearchDocument.class);
            
            // 转换为DTO
            List<SearchResultDTO> results = new ArrayList<>();
            for (Hit<SearchDocument> hit : searchResponse.hits().hits()) {
                SearchDocument document = hit.source();
                if (document == null) {
                    continue;
                }
                
                SearchResultDTO dto = new SearchResultDTO();
                dto.setType(document.getType());
                dto.setId(document.getOriginalId());
                dto.setCategory(document.getCategory());
                dto.setAuthorName(document.getAuthorName());
                dto.setCreateTime(document.getCreateTime() != null ? 
                        document.getCreateTime().format(DATE_FORMATTER) : "");
                
                // 处理高亮
                List<String> highlights = new ArrayList<>();
                
                // 标题高亮
                if (hit.highlight() != null && hit.highlight().containsKey("title") && 
                        !hit.highlight().get("title").isEmpty()) {
                    dto.setTitle(hit.highlight().get("title").get(0));
                } else {
                    dto.setTitle(document.getTitle());
                }
                
                // 内容高亮
                if (hit.highlight() != null && hit.highlight().containsKey("content") && 
                        !hit.highlight().get("content").isEmpty()) {
                    String highlightedContent = hit.highlight().get("content").get(0);
                    dto.setSummary(highlightedContent);
                    highlights.add(highlightedContent);
                } else {
                    // 如果没有高亮，截取前200个字符
                    String content = document.getContent();
                    if (content != null && content.length() > 200) {
                        dto.setSummary(content.substring(0, 200) + "...");
                    } else {
                        dto.setSummary(content != null ? content : "");
                    }
                }
                
                // 文件名高亮
                if (hit.highlight() != null && hit.highlight().containsKey("fileNames") && 
                        !hit.highlight().get("fileNames").isEmpty()) {
                    highlights.addAll(hit.highlight().get("fileNames"));
                }
                
                dto.setHighlights(highlights);
                results.add(dto);
            }
            
            return results;
        } catch (Exception e) {
            log.warn("搜索失败: keyword={}, error={}", keyword, e.getMessage());
            // 标记连接失败，触发重连检查
            healthChecker.checkConnection();
            // 返回空结果，不影响主流程
            return new ArrayList<>();
        }
    }
    
    @Override
    public void indexPost(Long postId) {
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.debug("Elasticsearch 不可用，跳过索引操作: postId={}", postId);
            return;
        }
        
        try {
            ForumPost post = forumPostMapper.selectById(postId);
            if (post == null) {
                log.warn("帖子不存在，无法索引: postId={}", postId);
                return;
            }
            
            User author = userMapper.selectById(post.getAuthorId());
            
            SearchDocument document = new SearchDocument();
            document.setId("POST_" + postId);
            document.setType("POST");
            document.setOriginalId(postId);
            document.setTitle(post.getTitle());
            document.setContent(post.getContent());
            document.setCategory(post.getCategory());
            document.setAuthorId(post.getAuthorId());
            document.setAuthorName(author != null ? author.getNickname() : "");
            document.setCreateTime(post.getCreateTime());
            document.setUpdateTime(post.getUpdateTime());
            
            // 确保索引存在
            ensureIndexExists();
            
            // 使用 ElasticsearchClient 保存，避免添加 _class 字段
            IndexRequest<SearchDocument> indexRequest = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(document.getId())
                    .document(document)
            );
            elasticsearchClient.index(indexRequest);
            log.info("索引帖子成功: postId={}", postId);
        } catch (Exception e) {
            log.warn("索引帖子失败: postId={}, error={}", postId, e.getMessage());
            // 标记连接失败，触发重连检查
            healthChecker.checkConnection();
        }
    }
    
    @Override
    public void indexResource(Long resourceId) {
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.debug("Elasticsearch 不可用，跳过索引操作: resourceId={}", resourceId);
            return;
        }
        
        try {
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource == null) {
                log.warn("资源不存在，无法索引: resourceId={}", resourceId);
                return;
            }
            
            // 只有审核通过的资源才索引
            if (!"APPROVED".equals(resource.getStatus())) {
                log.debug("资源未审核通过，跳过索引: resourceId={}, status={}", resourceId, resource.getStatus());
                // 如果索引已存在，删除它
                try {
                    deleteResourceIndex(resourceId);
                } catch (Exception e) {
                    // 忽略删除失败的错误
                }
                return;
            }
            
            User author = userMapper.selectById(resource.getAuthorId());
            
            // 获取资源关联的文件名列表
            List<SysFile> files = sysFileMapper.selectList(
                    new LambdaQueryWrapper<SysFile>()
                            .eq(SysFile::getResourceId, resourceId)
            );
            List<String> fileNames = files.stream()
                    .map(SysFile::getOriginalName)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            
            SearchDocument document = new SearchDocument();
            document.setId("RESOURCE_" + resourceId);
            document.setType("RESOURCE");
            document.setOriginalId(resourceId);
            document.setTitle(resource.getTitle());
            document.setContent(resource.getDescription() + " " + resource.getContent());
            document.setCategory(resource.getCategory());
            document.setAuthorId(resource.getAuthorId());
            document.setAuthorName(author != null ? author.getNickname() : "");
            document.setFileNames(fileNames);
            document.setCreateTime(resource.getCreateTime());
            document.setUpdateTime(resource.getUpdateTime());
            
            // 确保索引存在
            ensureIndexExists();
            
            // 使用 ElasticsearchClient 保存，避免添加 _class 字段
            IndexRequest<SearchDocument> indexRequest = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(document.getId())
                    .document(document)
            );
            elasticsearchClient.index(indexRequest);
            log.info("索引资源成功: resourceId={}", resourceId);
        } catch (Exception e) {
            log.warn("索引资源失败: resourceId={}, error={}", resourceId, e.getMessage());
            // 标记连接失败，触发重连检查
            healthChecker.checkConnection();
        }
    }
    
    @Override
    public void deletePostIndex(Long postId) {
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.debug("Elasticsearch 不可用，跳过删除索引操作: postId={}", postId);
            return;
        }
        
        try {
            elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id("POST_" + postId)
            );
            log.info("删除帖子索引成功: postId={}", postId);
        } catch (Exception e) {
            log.warn("删除帖子索引失败: postId={}, error={}", postId, e.getMessage());
            // 标记连接失败，触发重连检查
            healthChecker.checkConnection();
        }
    }
    
    @Override
    public void deleteResourceIndex(Long resourceId) {
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.debug("Elasticsearch 不可用，跳过删除索引操作: resourceId={}", resourceId);
            return;
        }
        
        try {
            elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id("RESOURCE_" + resourceId)
            );
            log.info("删除资源索引成功: resourceId={}", resourceId);
        } catch (Exception e) {
            log.warn("删除资源索引失败: resourceId={}, error={}", resourceId, e.getMessage());
            // 标记连接失败，触发重连检查
            healthChecker.checkConnection();
        }
    }
    
    @Override
    public int indexAllPosts() {
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.warn("Elasticsearch 不可用，跳过批量索引帖子操作");
            return 0;
        }
        
        int count = 0;
        try {
            List<ForumPost> posts = forumPostMapper.selectList(
                    new LambdaQueryWrapper<ForumPost>()
                            .eq(ForumPost::getStatus, "NORMAL")
            );
            for (ForumPost post : posts) {
                try {
                    indexPost(post.getId());
                    count++;
                } catch (Exception e) {
                    log.error("索引帖子失败: postId={}", post.getId(), e);
                }
            }
            log.info("批量索引帖子完成，共索引 {} 条", count);
        } catch (Exception e) {
            log.error("批量索引帖子失败", e);
        }
        return count;
    }
    
    @Override
    public int indexAllResources() {
        // 检查 Elasticsearch 是否可用
        if (!healthChecker.isAvailable()) {
            log.warn("Elasticsearch 不可用，跳过批量索引资源操作");
            return 0;
        }
        
        int count = 0;
        try {
            List<Resource> resources = resourceMapper.selectList(
                    new LambdaQueryWrapper<Resource>()
                            .eq(Resource::getStatus, "APPROVED")
            );
            for (Resource resource : resources) {
                try {
                    indexResource(resource.getId());
                    count++;
                } catch (Exception e) {
                    log.error("索引资源失败: resourceId={}", resource.getId(), e);
                }
            }
            log.info("批量索引资源完成，共索引 {} 条", count);
        } catch (Exception e) {
            log.error("批量索引资源失败", e);
        }
        return count;
    }
}

