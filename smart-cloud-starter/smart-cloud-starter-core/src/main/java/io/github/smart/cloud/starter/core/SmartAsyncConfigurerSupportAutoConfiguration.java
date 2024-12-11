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
package io.github.smart.cloud.starter.core;

import io.github.smart.cloud.starter.configure.properties.AsyncProperties;
import io.github.smart.cloud.starter.configure.properties.SmartProperties;
import io.github.smart.cloud.utility.JacksonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步配置
 *
 * @author collin
 * @date 2021-10-31
 */
@EnableAsync
@Configuration
@ConditionalOnProperty(prefix = "smart.async", name = "enable", havingValue = "true", matchIfMissing = true)
public class SmartAsyncConfigurerSupportAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    public SmartAsyncConfigurerSupport smartAsyncConfigurerSupport(final SmartProperties smartProperties) {
        return new SmartAsyncConfigurerSupport(smartProperties);
    }

    /**
     * 异步注解线程池配置
     *
     * @author collin
     * @date 2022-11-19
     */
    @Slf4j
    @RequiredArgsConstructor
    static class SmartAsyncConfigurerSupport extends AsyncConfigurerSupport {

        private final SmartProperties smartProperties;

        @Override
        public Executor getAsyncExecutor() {
            AsyncProperties asyncProperties = smartProperties.getAsync();
            ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
            threadPoolTaskExecutor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
            threadPoolTaskExecutor.setCorePoolSize(asyncProperties.getCorePoolSize());
            threadPoolTaskExecutor.setKeepAliveSeconds(asyncProperties.getKeepAliveSeconds());
            threadPoolTaskExecutor.setQueueCapacity(asyncProperties.getQueueCapacity());
            threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
            threadPoolTaskExecutor.setAwaitTerminationSeconds(asyncProperties.getAwaitTerminationSeconds());
            threadPoolTaskExecutor.initialize();

            return threadPoolTaskExecutor;
        }

        @Override
        @Nullable
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            return (throwable, method, obj) -> log.error("asyncException@method={}; param={}", method.getName(), JacksonUtil.toJson(obj), throwable);
        }

    }

}