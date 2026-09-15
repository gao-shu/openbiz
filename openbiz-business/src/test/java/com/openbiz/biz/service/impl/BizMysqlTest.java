package com.openbiz.biz.service.impl;

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
import com.openbiz.biz.api.BizService;
import com.openbiz.biz.api.OrderLineRequest;
import com.openbiz.biz.domain.OpenbizAccount;
import com.openbiz.biz.domain.OpenbizAccountLedger;
import com.openbiz.biz.domain.OpenbizCustomer;
import com.openbiz.biz.domain.OpenbizItem;
import com.openbiz.biz.domain.OpenbizOrder;
import com.openbiz.saas.context.TenantContext;
import com.ruoyi.common.exception.ServiceException;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Real MySQL proof for transaction, unique idempotent keys, and concurrent debit.
 */
@SpringJUnitConfig(BizMysqlTest.Cfg.class)
class BizMysqlTest
{
    private static final String JDBC =
            "jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";

    @Autowired
    private BizService bizService;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeAll
    static void applySchema() throws Exception
    {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(JDBC);
        ds.setUsername("root");
        ds.setPassword("123456");
        Path sql = Path.of("..", "sql", "openbiz_biz_1.sql");
        if (!Files.exists(sql))
        {
            sql = Path.of("sql", "openbiz_biz_1.sql");
        }
        try (var conn = ds.getConnection())
        {
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(sql.toFile()));
        }
        ds.close();
    }

    @BeforeEach
    void clearTenant()
    {
        TenantContext.clear();
    }

    @Test
    void hairRechargeAndCut()
    {
        TenantContext.setTenantId(1L);
        OpenbizCustomer c = bizService.createCustomer("HairCust", phone());
        bizService.recharge(c.getId(), new BigDecimal("500"), "hair-r-" + c.getId());
        assertEquals(new BigDecimal("500.00"), bizService.getAccount(c.getId()).getBalance());
        OpenbizItem cut = bizService.getItemByCode("CUT");
        assertEquals(OpenbizItem.TYPE_SERVICE, cut.getItemType());
        OpenbizOrder order = bizService.placeOrder(c.getId(), List.of(new OrderLineRequest(cut.getId(), 1)),
                "hair-o-" + c.getId());
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());
        assertEquals(OpenbizOrder.STATUS_SUCCESS, order.getStatus());
        assertEquals(new BigDecimal("450.00"), bizService.getAccount(c.getId()).getBalance());
        List<OpenbizAccountLedger> ledgers = bizService.listLedgers(c.getId());
        assertEquals(2, ledgers.size());
        assertEquals(OpenbizAccountLedger.TYPE_RECHARGE, ledgers.get(0).getTxnType());
        assertEquals(OpenbizAccountLedger.TYPE_CONSUME, ledgers.get(1).getTxnType());
        assertEquals(new BigDecimal("50.00"), bizService.listOrderItems(order.getId()).get(0).getUnitPrice());
    }

    @Test
    void waterStationTwoBarrels()
    {
        TenantContext.setTenantId(2L);
        OpenbizCustomer c = bizService.createCustomer("WaterCust", phone());
        bizService.recharge(c.getId(), new BigDecimal("500"), "water-r-" + c.getId());
        OpenbizItem water = bizService.getItemByCode("WATER");
        assertEquals(OpenbizItem.TYPE_PRODUCT, water.getItemType());
        OpenbizOrder order = bizService.placeOrder(c.getId(), List.of(new OrderLineRequest(water.getId(), 2)),
                "water-o-" + c.getId());
        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
        assertEquals(new BigDecimal("460.00"), bizService.getAccount(c.getId()).getBalance());
    }

    @Test
    void duplicateRechargeAndConsumeAreIdempotent()
    {
        TenantContext.setTenantId(1L);
        OpenbizCustomer c = bizService.createCustomer("IdemCust", phone());
        String rKey = "idem-r-" + c.getId();
        bizService.recharge(c.getId(), new BigDecimal("100"), rKey);
        bizService.recharge(c.getId(), new BigDecimal("100"), rKey);
        assertEquals(new BigDecimal("100.00"), bizService.getAccount(c.getId()).getBalance());
        assertEquals(1, bizService.listLedgers(c.getId()).size());

        OpenbizItem cut = bizService.getItemByCode("CUT");
        String oKey = "idem-o-" + c.getId();
        OpenbizOrder first = bizService.placeOrder(c.getId(), List.of(new OrderLineRequest(cut.getId(), 1)), oKey);
        OpenbizOrder second = bizService.placeOrder(c.getId(), List.of(new OrderLineRequest(cut.getId(), 1)), oKey);
        assertEquals(first.getId(), second.getId());
        assertEquals(new BigDecimal("50.00"), bizService.getAccount(c.getId()).getBalance());
        assertEquals(2, bizService.listLedgers(c.getId()).size());
        Integer orders = jdbc.queryForObject(
                "SELECT COUNT(*) FROM openbiz_order WHERE customer_id = ?", Integer.class, c.getId());
        assertEquals(1, orders);
    }

    @Test
    void insufficientRollsBackOrderAndLedger()
    {
        TenantContext.setTenantId(1L);
        OpenbizCustomer c = bizService.createCustomer("PoorCust", phone());
        bizService.recharge(c.getId(), new BigDecimal("50"), "poor-r-" + c.getId());
        OpenbizItem dye = bizService.getItemByCode("DYE");
        ServiceException ex = assertThrows(ServiceException.class, () -> bizService.placeOrder(c.getId(),
                List.of(new OrderLineRequest(dye.getId(), 1)), "poor-o-" + c.getId()));
        assertTrue(ex.getMessage().contains("INSUFFICIENT"));
        assertEquals(new BigDecimal("50.00"), bizService.getAccount(c.getId()).getBalance());
        assertEquals(1, bizService.listLedgers(c.getId()).size());
        Integer orders = jdbc.queryForObject(
                "SELECT COUNT(*) FROM openbiz_order WHERE customer_id = ?", Integer.class, c.getId());
        assertEquals(0, orders);
        Integer consume = jdbc.queryForObject(
                "SELECT COUNT(*) FROM openbiz_account_ledger WHERE customer_id = ? AND txn_type = 'CONSUME'",
                Integer.class, c.getId());
        assertEquals(0, consume);
    }

    @Test
    void tenantIsolationOnCustomerAccountOrder()
    {
        TenantContext.setTenantId(1L);
        OpenbizCustomer a = bizService.createCustomer("TenantA", phone());
        bizService.recharge(a.getId(), new BigDecimal("100"), "iso-r-" + a.getId());
        OpenbizItem cut = bizService.getItemByCode("CUT");
        OpenbizOrder order = bizService.placeOrder(a.getId(), List.of(new OrderLineRequest(cut.getId(), 1)),
                "iso-o-" + a.getId());

        TenantContext.setTenantId(2L);
        assertThrows(ServiceException.class, () -> bizService.getCustomer(a.getId()));
        assertThrows(ServiceException.class, () -> bizService.getAccount(a.getId()));
        assertThrows(ServiceException.class, () -> bizService.getOrder(order.getId()));
        assertThrows(ServiceException.class, () -> bizService.recharge(a.getId(), new BigDecimal("10"), "iso-b"));
    }

    @Test
    void concurrentConsumeNeverGoesNegative() throws Exception
    {
        TenantContext.setTenantId(1L);
        OpenbizCustomer c = bizService.createCustomer("RaceCust", phone());
        bizService.recharge(c.getId(), new BigDecimal("100"), "race-r-" + c.getId());
        OpenbizItem cut = bizService.getItemByCode("CUT");
        Long customerId = c.getId();
        Long itemId = cut.getId();

        AtomicInteger success = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(3);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try
        {
            for (int i = 0; i < 3; i++)
            {
                String key = "race-o-" + customerId + "-" + i;
                pool.submit(() -> {
                    TenantContext.setTenantId(1L);
                    try
                    {
                        start.await(10, TimeUnit.SECONDS);
                        bizService.placeOrder(customerId, List.of(new OrderLineRequest(itemId, 1)), key);
                        success.incrementAndGet();
                    }
                    catch (ServiceException ignored)
                    {
                        // insufficient or CAS exhausted is expected for the losing request
                    }
                    catch (InterruptedException ie)
                    {
                        Thread.currentThread().interrupt();
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
        OpenbizAccount acc = bizService.getAccount(customerId);
        assertTrue(success.get() <= 2, "at most two 50 consumes can succeed from 100");
        assertTrue(acc.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(0, acc.getBalance().remainder(new BigDecimal("50.00")).compareTo(BigDecimal.ZERO));
        BigDecimal expected = new BigDecimal("100.00")
                .subtract(new BigDecimal("50.00").multiply(BigDecimal.valueOf(success.get())));
        assertEquals(0, expected.compareTo(acc.getBalance()));
        long consumeRows = bizService.listLedgers(customerId).stream()
                .filter(l -> OpenbizAccountLedger.TYPE_CONSUME.equals(l.getTxnType()))
                .count();
        assertEquals(success.get(), consumeRows);
        Integer orderCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM openbiz_order WHERE customer_id = ?", Integer.class, customerId);
        assertEquals(success.get(), orderCount);
        assertTrue(success.get() >= 1);
    }

    private static String phone()
    {
        return "1" + String.format("%010d", Math.abs(System.nanoTime() % 10_000_000_000L));
    }

    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.openbiz.biz.mapper")
    @Import(BizServiceImpl.class)
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
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception
        {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSource);
            bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources("classpath:mapper/biz/*Mapper.xml"));
            return bean.getObject();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource)
        {
            return new JdbcTemplate(dataSource);
        }
    }
}
