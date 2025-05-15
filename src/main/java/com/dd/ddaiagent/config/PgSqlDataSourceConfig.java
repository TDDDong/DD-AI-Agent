package com.dd.ddaiagent.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.dd.ddaiagent.mapper.postgresql",
        sqlSessionTemplateRef = "pgsqlSqlSessionTemplate")
public class PgSqlDataSourceConfig {
    //数据源配置
    @Bean("pgsqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource pgsqlDataSource() {
        return new DruidDataSource();
    }

    //SqlSessionFactory配置
    @Bean("pgsqlSqlSessionFactory")
    public SqlSessionFactory pgsqlSqlSessionFactory(@Qualifier("pgsqlDataSource") DataSource pgsqlDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(pgsqlDataSource);
        /*factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResource("classpath:mapper/postgresql/*.xml"));*/
        factoryBean.setTypeAliasesPackage("com.dd.ddaiagent.entity");
        return factoryBean.getObject();
    }

    //事务管理器配置
    @Bean("pgsqlTransactionManager")
    public DataSourceTransactionManager pgsqlTransactionManager(@Qualifier("pgsqlDataSource") DataSource pgsqlDataSource) {
        return new DataSourceTransactionManager(pgsqlDataSource);
    }

    //模板配置
    @Bean("pgsqlSqlSessionTemplate")
    public SqlSessionTemplate primarySqlSessionTemplate(@Qualifier("pgsqlSqlSessionFactory") SqlSessionFactory pgsqlSqlSessionFactory) {
        return new SqlSessionTemplate(pgsqlSqlSessionFactory);
    }
}
