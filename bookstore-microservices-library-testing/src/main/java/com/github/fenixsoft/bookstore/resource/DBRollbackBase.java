package com.github.fenixsoft.bookstore.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

/**
 * 单元测试基类
 * <p>
 * 提供了每个单元测试自动恢复数据库、清理缓存的处理
 *
 * @author icyfenix@gmail.com
 * @date 2020/4/7 14:19
 **/
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
//@Sql(scripts = {"classpath:db/hsqldb/schema.sql", "classpath:db/hsqldb/data.sql"})
public class DBRollbackBase {

    @BeforeEach
//    @EnabledIfSystemProperty() 可否实现根据bean判断
    public void evictAllCaches(@Nullable @Autowired CacheManager cacheManager) {
        Optional.ofNullable(cacheManager).ifPresent(cm ->
                cm.getCacheNames().forEach(n ->
                        Optional.ofNullable(cm.getCache(n)).ifPresent(Cache::clear)));
    }
}
