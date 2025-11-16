package com.minecraftforum.repository.es;

import com.minecraftforum.entity.es.SearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch 搜索文档 Repository
 */
@Repository
public interface SearchDocumentRepository extends ElasticsearchRepository<SearchDocument, String> {
}

