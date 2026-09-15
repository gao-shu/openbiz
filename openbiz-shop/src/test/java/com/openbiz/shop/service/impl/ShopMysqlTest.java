package com.openbiz.shop.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.openbiz.saas.context.TenantContext;
import com.openbiz.shop.api.OrderLineRequest;
import com.openbiz.shop.api.ShopService;
import com.openbiz.shop.domain.OpenbizShopInventory;
import com.openbiz.shop.domain.OpenbizShopOrder;
import com.openbiz.shop.domain.OpenbizShopOrderItem;
import com.openbiz.shop.domain.OpenbizShopProduct;
import com.openbiz.shop.port.CurrentUserPort;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.zaxxer.hikari.HikariDataSource;

@SpringJUnitConfig(ShopMysqlTest.Cfg.class)
class ShopMysqlTest
{
    private static final String JDBC =
            "jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";

    @Autowired
    private ShopService shopService;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeAll
    static void applySchema() throws Exception
    {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(JDBC);
        ds.setUsername("root");
        ds.setPassword("123456");
        Path sql = Path.of("..", "sql", "openbiz_shop_1.sql");
        if (!Files.exists(sql))
        {
            sql = Path.of("sql", "openbiz_shop_1.sql");
        }
        try (var conn = ds.getConnection())
        {
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(sql.toFile()));
        }
        ds.close();
    }

    @BeforeEach
    void tenant()
    {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void clear()
    {
        TenantContext.clear();
    }

    @Test
    void placeOrder_multiLine_andCrossTenant()
    {
        String codeA = "A-" + System.nanoTime();
        String codeB = "B-" + System.nanoTime();
        OpenbizShopProduct a = shopService.createProduct(codeA, "ItemA", new BigDecimal("10.00"), 5);
        OpenbizShopProduct b = shopService.createProduct(codeB, "ItemB", new BigDecimal("20.00"), 5);

        OpenbizShopOrder order = shopService.placeOrder("Bob", "13800000001",
                List.of(new OrderLineRequest(a.getId(), 2), new OrderLineRequest(b.getId(), 1)),
                "ord-" + System.nanoTime());
        assertEquals(OpenbizShopOrder.STATUS_SUCCESS, order.getStatus());
        assertEquals(0, new BigDecimal("40.00").compareTo(order.getTotalAmount()));

        List<OpenbizShopOrderItem> items = shopService.listOrderItems(order.getId());
        assertEquals(2, items.size());
        assertEquals(0, new BigDecimal("10.00").compareTo(
                items.stream().filter(i -> i.getProductId().equals(a.getId())).findFirst().orElseThrow().getUnitPrice()));

        assertEquals(3, shopService.getInventory(a.getId()).getQuantity());
        assertEquals(4, shopService.getInventory(b.getId()).getQuantity());

        TenantContext.setTenantId(2L);
        ServiceException ex = assertThrows(ServiceException.class, () -> shopService.getOrder(order.getId()));
        assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND), ex.getCode());
    }

    @Test
    void idempotentAndInsufficient()
    {
        OpenbizShopProduct p = shopService.createProduct("P-" + System.nanoTime(), "Solo", new BigDecimal("5.00"), 1);
        String key = "idem-" + System.nanoTime();
        OpenbizShopOrder first = shopService.placeOrder("A", "139", List.of(new OrderLineRequest(p.getId(), 1)), key);
        OpenbizShopOrder second = shopService.placeOrder("A", "139", List.of(new OrderLineRequest(p.getId(), 1)), key);
        assertEquals(first.getId(), second.getId());
        assertEquals(0, shopService.getInventory(p.getId()).getQuantity());

        assertThrows(ServiceException.class,
                () -> shopService.placeOrder("A", "139", List.of(new OrderLineRequest(p.getId(), 1)), "other-" + System.nanoTime()));
        assertEquals(0, shopService.getInventory(p.getId()).getQuantity());
        Integer orderCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM openbiz_shop_order WHERE tenant_id = 1 AND status = 'SUCCESS' AND id = ?",
                Integer.class, first.getId());
        assertEquals(1, orderCount);
    }

    @Test
    void concurrentBuyStockOne() throws Exception
    {
        OpenbizShopProduct p = shopService.createProduct("C-" + System.nanoTime(), "Race", new BigDecimal("9.00"), 1);
        Long productId = p.getId();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try
        {
            for (int i = 0; i < 2; i++)
            {
                final int idx = i;
                pool.submit(() -> {
                    TenantContext.setTenantId(1L);
                    try
                    {
                        start.await(10, TimeUnit.SECONDS);
                        shopService.placeOrder("R", "139", List.of(new OrderLineRequest(productId, 1)),
                                "race-" + System.nanoTime() + "-" + idx);
                        success.incrementAndGet();
                    }
                    catch (ServiceException | InterruptedException ex)
                    {
                        fail.incrementAndGet();
                        if (ex instanceof InterruptedException)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                    finally
                    {
                        TenantContext.clear();
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertTrue(done.await(30, TimeUnit.SECONDS));
        }
        finally
        {
            pool.shutdownNow();
        }

        TenantContext.setTenantId(1L);
        OpenbizShopInventory inv = shopService.getInventory(productId);
        assertEquals(1, success.get(), "exactly one SUCCESS");
        assertEquals(1, fail.get(), "exactly one FAIL");
        assertEquals(0, inv.getQuantity());
        assertTrue(inv.getQuantity() >= 0);
        Integer successOrders = jdbc.queryForObject(
                "SELECT COUNT(*) FROM openbiz_shop_order_item oi "
                        + "JOIN openbiz_shop_order o ON o.id = oi.order_id "
                        + "WHERE oi.product_id = ? AND o.status = 'SUCCESS'",
                Integer.class, productId);
        assertEquals(1, successOrders);
    }

    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.openbiz.shop.mapper")
    @Import(ShopServiceImpl.class)
    static class Cfg
    {
        @Bean
        DataSource dataSource()
        {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(JDBC);
            ds.setUsername("root");
            ds.setPassword("123456");
            ds.setMaximumPoolSize(8);
            return ds;
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource)
        {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource)
        {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception
        {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSource);
            bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:mapper/shop/*Mapper.xml"));
            return bean.getObject();
        }

        @Bean
        CurrentUserPort currentUserPort()
        {
            return () -> 100L;
        }
    }
}
