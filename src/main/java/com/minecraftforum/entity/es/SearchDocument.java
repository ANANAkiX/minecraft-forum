package com.minecraftforum.entity.es;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 搜索文档实体
 * 用于索引帖子和资源
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "minecraft_forum_search")
public class SearchDocument {
    
    @Id
    private String id;
    
    /**
     * 文档类型：POST（帖子）或 RESOURCE（资源）
     */
    @Field(type = FieldType.Keyword)
    private String type;
    
    /**
     * 原始ID（帖子ID或资源ID）
     */
    @Field(type = FieldType.Long)
    private Long originalId;
    
    /**
     * 标题
     */
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;
    
    /**
     * 内容（帖子内容或资源描述）
     */
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String content;
    
    /**
     * 分类
     */
    @Field(type = FieldType.Keyword)
    private String category;
    
    /**
     * 作者ID
     */
    @Field(type = FieldType.Long)
    private Long authorId;
    
    /**
     * 作者名称
     */
    @Field(type = FieldType.Keyword)
    private String authorName;
    
    /**
     * 文件名列表（仅资源类型，包含资源关联的文件名）
     */
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private List<String> fileNames;
    
    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
}

