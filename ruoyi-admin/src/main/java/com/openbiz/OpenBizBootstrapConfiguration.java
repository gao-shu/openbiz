package com.openbiz;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.openbiz.access.OpenBizAccessAutoConfiguration;
import com.openbiz.agent.OpenBizAgentAutoConfiguration;
import com.openbiz.biz.OpenBizBizAutoConfiguration;
import com.openbiz.charging.OpenBizChargingAutoConfiguration;
import com.openbiz.foundation.OpenBizFoundationAutoConfiguration;
import com.openbiz.iot.core.OpenBizIotAutoConfiguration;
import com.openbiz.iot.mqtt.OpenBizMqttAutoConfiguration;
import com.openbiz.locker.OpenBizLockerAutoConfiguration;
import com.openbiz.mes.OpenBizMesAutoConfiguration;
import com.openbiz.saas.OpenBizSaasAutoConfiguration;
import com.openbiz.service.OpenBizServiceAutoConfiguration;
import com.openbiz.shop.OpenBizShopAutoConfiguration;

/**
 * Aggregates OpenBiz module configurations for the admin boot app.
 * Domain modules stay independent; wiring only happens here.
 */
@Configuration
@Import({
        OpenBizFoundationAutoConfiguration.class,
        OpenBizSaasAutoConfiguration.class,
        OpenBizIotAutoConfiguration.class,
        OpenBizMqttAutoConfiguration.class,
        OpenBizAccessAutoConfiguration.class,
        OpenBizLockerAutoConfiguration.class,
        OpenBizChargingAutoConfiguration.class,
        OpenBizMesAutoConfiguration.class,
        OpenBizBizAutoConfiguration.class,
        OpenBizServiceAutoConfiguration.class,
        OpenBizAgentAutoConfiguration.class,
        OpenBizShopAutoConfiguration.class
})
public class OpenBizBootstrapConfiguration
{
}
