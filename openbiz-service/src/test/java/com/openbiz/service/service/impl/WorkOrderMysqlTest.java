package com.openbiz.service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.openbiz.saas.context.TenantContext;
import com.openbiz.service.api.WorkOrderService;
import com.openbiz.service.domain.OpenbizWorkOrder;
import com.openbiz.service.domain.WorkOrderStatus;
import com.openbiz.service.port.CurrentUserPort;
import com.openbiz.service.port.StaffUserPort;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.zaxxer.hikari.HikariDataSource;

@SpringJUnitConfig(WorkOrderMysqlTest.Cfg.class)
class WorkOrderMysqlTest
{
    private static final String JDBC =
            "jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";

    @Autowired
    private WorkOrderService workOrderService;

    @BeforeAll
    static void applySchema() throws Exception
    {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(JDBC);
        ds.setUsername("root");
        ds.setPassword("123456");
        Path sql = Path.of("..", "sql", "openbiz_service_1.sql");
        if (!Files.exists(sql))
        {
            sql = Path.of("sql", "openbiz_service_1.sql");
        }
        try (var conn = ds.getConnection())
        {
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(sql.toFile()));
        }
        ds.close();
    }

    @BeforeEach
    void tenantA()
    {
        TenantContext.setTenantId(1L);
        Cfg.CURRENT.set(100L);
    }

    @AfterEach
    void clear()
    {
        TenantContext.clear();
        Cfg.CURRENT.set(100L);
    }

    @Test
    void fullLoopAndCrossTenant()
    {
        String key = "svc-" + System.nanoTime();
        OpenbizWorkOrder created = workOrderService.create("Leak", "kitchen", "Ann", "13900000001", key);
        assertEquals(WorkOrderStatus.CREATED.name(), created.getStatus());

        OpenbizWorkOrder assigned = workOrderService.assign(created.getId(), 200L);
        assertEquals(WorkOrderStatus.ASSIGNED.name(), assigned.getStatus());
        assertEquals(200L, assigned.getAssigneeUserId());

        Cfg.CURRENT.set(200L);
        OpenbizWorkOrder accepted = workOrderService.accept(created.getId());
        assertEquals(WorkOrderStatus.ACCEPTED.name(), accepted.getStatus());
        OpenbizWorkOrder completed = workOrderService.complete(created.getId(), "fixed");
        assertEquals(WorkOrderStatus.COMPLETED.name(), completed.getStatus());
        assertEquals("fixed", completed.getCompleteNote());

        TenantContext.setTenantId(2L);
        Cfg.CURRENT.set(300L);
        ServiceException ex = assertThrows(ServiceException.class, () -> workOrderService.get(created.getId()));
        assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND), ex.getCode());
        assertThrows(ServiceException.class, () -> workOrderService.complete(created.getId(), "hack"));
    }

    @Test
    void idempotentCreate()
    {
        String key = "idem-" + System.nanoTime();
        OpenbizWorkOrder a = workOrderService.create("A", "c", "N", "13800000002", key);
        OpenbizWorkOrder b = workOrderService.create("A2", "c2", "N2", "13800000003", key);
        assertEquals(a.getId(), b.getId());
        assertEquals("A", b.getTitle());
    }

    @Test
    void illegalTransitionPersisted()
    {
        String key = "bad-" + System.nanoTime();
        OpenbizWorkOrder wo = workOrderService.create("X", "c", "N", "13800000004", key);
        ServiceException ex = assertThrows(ServiceException.class, () -> workOrderService.complete(wo.getId(), "no"));
        assertTrue(ex.getMessage().contains("ILLEGAL_TRANSITION"));
    }

    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.openbiz.service.mapper")
    @Import(WorkOrderServiceImpl.class)
    static class Cfg
    {
        static final ThreadLocal<Long> CURRENT = ThreadLocal.withInitial(() -> 100L);

        @Bean
        DataSource dataSource()
        {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(JDBC);
            ds.setUsername("root");
            ds.setPassword("123456");
            return ds;
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource)
        {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception
        {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSource);
            bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:mapper/service/*Mapper.xml"));
            return bean.getObject();
        }

        @Bean
        CurrentUserPort currentUserPort()
        {
            return () -> CURRENT.get();
        }

        @Bean
        StaffUserPort staffUserPort()
        {
            return userId -> {
                if (userId == null)
                {
                    throw new ServiceException("assigneeUserId is required", HttpStatus.BAD_REQUEST);
                }
            };
        }
    }
}
