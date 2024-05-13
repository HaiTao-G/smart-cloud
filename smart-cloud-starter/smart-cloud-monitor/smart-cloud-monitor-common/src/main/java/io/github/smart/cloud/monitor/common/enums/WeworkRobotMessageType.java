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
package io.github.smart.cloud.monitor.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 企业微信机器人消息类型
 *
 * @author collin
 * @date 2024-05-13
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WeworkRobotMessageType {

    /**
     * 文本格式
     */
    TEXT("text"),
    /**
     * markdown格式
     */
    MARKDOWN("markdown");

    /**
     * 消息类型值
     */
    private String value;

}