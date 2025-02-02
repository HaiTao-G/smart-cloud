/*
 * Copyright © 2019 collin (1634753825@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.smart.cloud.starter.monitor.admin.component.metrics.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import io.github.smart.cloud.starter.monitor.admin.component.metrics.IInstanceMetricsMonitorComponent;
import io.github.smart.cloud.starter.monitor.admin.dto.MatchIncreaseResultDTO;
import io.github.smart.cloud.starter.monitor.admin.dto.MetricCheckResultDTO;
import io.github.smart.cloud.starter.monitor.admin.enums.MetricCheckStatus;
import io.github.smart.cloud.starter.monitor.admin.event.MetricAlertEvent;
import io.github.smart.cloud.starter.monitor.admin.properties.MonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务指标监控父类
 *
 * @param <T>
 * @author collin
 * @date 2024-07-28
 */
@Slf4j
public abstract class AbstractInstanceMetricsMonitorComponent<T extends Number, U> implements IInstanceMetricsMonitorComponent<U>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    protected ObjectMapper objectMapper;
    protected MonitorProperties monitorProperties;

    protected final ConcurrentMap<String, CopyOnWriteArrayList<T>> HISTORY_DATA = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
        this.monitorProperties = applicationContext.getBean(MonitorProperties.class);
    }

    @Override
    public void truncateHistory() {
        HISTORY_DATA.forEach((serviceName, data) -> {
            Integer keepIncreasingCountThreshold = getKeepIncreasingCount(serviceName);
            int needRemoveCount = data.size() - keepIncreasingCountThreshold;
            if (needRemoveCount > 0) {
                for (int i = 0; i < needRemoveCount; i++) {
                    data.remove(0);
                }
            }
        });
    }

    @Override
    public boolean alert(Instance instance) throws IOException {
        MetricCheckResultDTO metricCheckResult = check(instance);
        MetricCheckStatus metricCheckStatus = metricCheckResult.getMetricCheckStatus();
        if (metricCheckStatus == MetricCheckStatus.OK) {
            return false;
        }

        MetricAlertEvent metricAlert = new MetricAlertEvent(this);
        metricAlert.setInstance(instance);
        metricAlert.setInstanceMetric(getInstanceMetric());
        metricAlert.setMetricCheckResult(metricCheckResult);
        applicationContext.publishEvent(metricAlert);
        return true;
    }

    protected MatchIncreaseResultDTO matchKeepIncreasing(String serviceName, String instanceId, T metricValue) {
        List<T> instanceData = HISTORY_DATA.computeIfAbsent(instanceId, (key) -> new CopyOnWriteArrayList<>());
        instanceData.add(metricValue);
        int historyCount = instanceData.size();
        Integer keepIncreasingCount = getKeepIncreasingCount(serviceName);
        if (historyCount < keepIncreasingCount) {
            return MatchIncreaseResultDTO.normal();
        }

        // 后面的大于前面的值
        T lastValue = instanceData.get(historyCount - 1);
        double diffThreshold = getDiffThreshold(serviceName);
        for (int i = 1; i < keepIncreasingCount; i++) {
            T currentValue = instanceData.get(historyCount - 1 - i);
            double diff = lastValue.doubleValue() - currentValue.doubleValue();
            // 超过阈值
            if (diff < diffThreshold) {
                return MatchIncreaseResultDTO.normal();
            }
            lastValue = currentValue;
        }
        double speed = (instanceData.get(historyCount - 1).doubleValue() - instanceData.get(historyCount - 2).doubleValue()) * 60.0D / getCheckIntervalSeconds();

        return MatchIncreaseResultDTO.match(speed);
    }

    /**
     * 获取差值阈值
     *
     * @param serviceName
     * @return
     */
    protected abstract double getDiffThreshold(String serviceName);

}