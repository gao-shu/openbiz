package com.openbiz.iot.mqtt;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT adapter Spring entry. Phase 1: no broker client dependency.
 */
@Configuration
@ComponentScan("com.openbiz.iot.mqtt")
public class OpenBizMqttAutoConfiguration
{
}
