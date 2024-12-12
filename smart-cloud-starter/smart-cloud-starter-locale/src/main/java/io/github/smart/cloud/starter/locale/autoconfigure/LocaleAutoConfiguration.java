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
package io.github.smart.cloud.starter.locale.autoconfigure;

import io.github.smart.cloud.starter.configure.SmartAutoConfiguration;
import io.github.smart.cloud.starter.configure.properties.LocaleProperties;
import io.github.smart.cloud.starter.configure.properties.SmartProperties;
import io.github.smart.cloud.starter.locale.aspect.LocaleInterceptor;
import io.github.smart.cloud.starter.locale.constant.LocaleConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * 多语言配置
 *
 * @author collin
 * @date 2019-07-15
 */
@Slf4j
@Configuration
@AutoConfigureAfter(SmartAutoConfiguration.class)
public class LocaleAutoConfiguration {

    @Bean
    public MessageSource messageSource(final SmartProperties smartProperties) {
        LocaleProperties localeProperties = smartProperties.getLocale();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = null;
        try {
            resources = resolver.getResources(LocaleConstant.LOCALE_PATTERN);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        if (resources != null) {
            String[] strResources = new String[resources.length];
            for (int i = 0; i < resources.length; i++) {
                Resource resource = resources[i];
                strResources[i] = LocaleConstant.LOCALE_DIR
                        + resource.getFilename().replace(LocaleConstant.LOCALE_PROPERTIES_SUFFIX, "");
            }
            messageSource.setBasenames(strResources);
        }
        messageSource.setDefaultEncoding(localeProperties.getEncoding());
        messageSource.setCacheSeconds(localeProperties.getCacheSeconds());
        return messageSource;
    }

    @Bean
    public LocaleInterceptor localeInterceptor(final MessageSource messageSource) {
        return new LocaleInterceptor(messageSource);
    }

    @Bean
    public Advisor localeAdvisor(final LocaleInterceptor localeInterceptor) {
        StringBuilder expression = new StringBuilder(1024);
        /**
         * 方法使用：
         * execution：用于匹配方法执行的连接点
         * args：用于匹配当前执行的方法传入的参数为指定类型的执行方法
         * @args：用于匹配当前执行的方法传入的参数是指定类型的
         * @annotation：用于匹配当前执行方法持有指定注解的方法 切点引入，在切点定义类中对应方法上定义
         * @PointCut，然后在@Aspect类中使用完整方法路径()引用，以分离切点定义和使用，方便切点集中管理
         * 类使用：
         * within：用于匹配指定类内的方法执行
         * this：用于匹配当前AOP代理对象类型的执行方法；注意是AOP代理对象的类型匹配，这样就可能包括引入接口也类型匹配
         * target：用于匹配当前目标对象类型的执行方法；注意是目标对象的类型匹配，这样就不包括引入接口也类型匹配
         * @within：用于匹配所以持有指定注解的类型内的方法
         * @target：用于配当前目标对象类型的执行方法，其中目标对象持有指定的注解
         * 对象使用：
         * bean：Spring AOP扩展的，AspectJ没有对于指示符，用于匹配特定名称的Bean对象的执行方法
         */
        expression.append("(@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController))") //NOPMD - suppressed ConsecutiveLiteralAppends - TODO explain reason for suppression
                .append("&&")
                .append('(')
                .append("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
                .append("|| @annotation(org.springframework.web.bind.annotation.GetMapping)")
                .append("|| @annotation(org.springframework.web.bind.annotation.PostMapping)")
                .append("|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
                .append("|| @annotation(org.springframework.web.bind.annotation.PutMapping)")
                .append("|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
                .append(')');

        AspectJExpressionPointcut localePointcut = new AspectJExpressionPointcut();
        localePointcut.setExpression(expression.toString());
        //此对象会在容器启动时扫描Advisor对象,然后基于切入点为目标对象创建代理对象
        //然后再执行切入点方法时,自动执行Advice对象通知方法
        DefaultBeanFactoryPointcutAdvisor apiLogAdvisor = new DefaultBeanFactoryPointcutAdvisor();
        //定义通知
        apiLogAdvisor.setAdvice(localeInterceptor);
        //定义切点
        apiLogAdvisor.setPointcut(localePointcut);

        return apiLogAdvisor;
    }

}