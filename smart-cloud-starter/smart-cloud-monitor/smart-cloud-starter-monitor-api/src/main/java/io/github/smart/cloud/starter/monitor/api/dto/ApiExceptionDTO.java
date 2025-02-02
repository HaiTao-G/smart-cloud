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
package io.github.smart.cloud.starter.monitor.api.dto;

import io.github.smart.cloud.starter.monitor.api.enums.ApiExceptionRemindType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 接口异常信息
 *
 * @author collin
 * @date 2024-01-6
 */
@Getter
@Setter
@ToString
public class ApiExceptionDTO {

    /**
     * 接口名（类名#方法名）
     */
    private String name;
    /**
     * 请求总数
     */
    private Long total;
    /**
     * 失败数
     */
    private Long failCount;
    /**
     * 失败率
     */
    private String failRate;
    /**
     * 失败信息
     */
    private Throwable throwable;
    /**
     * 接口异常提醒类型
     */
    private ApiExceptionRemindType remindType;

}