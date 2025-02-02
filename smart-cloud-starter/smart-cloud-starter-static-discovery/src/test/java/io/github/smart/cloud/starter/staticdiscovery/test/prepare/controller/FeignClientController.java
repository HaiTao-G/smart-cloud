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
package io.github.smart.cloud.starter.staticdiscovery.test.prepare.controller;

import io.github.smart.cloud.starter.staticdiscovery.test.prepare.rpc.FeignClientRpc;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeignClientController implements FeignClientRpc {

    @Override
    public String get(String name) {
        return name;
    }

}