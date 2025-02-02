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
package io.github.smart.cloud.starter.monitor.admin.component;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.smart.cloud.starter.monitor.admin.properties.GitlabProperties;
import io.github.smart.cloud.starter.monitor.admin.properties.MonitorProperties;
import io.github.smart.cloud.starter.monitor.admin.properties.ServiceInfoProperties;
import io.github.smart.cloud.utility.DateUtil;
import io.github.smart.cloud.utility.HttpUtil;
import io.github.smart.cloud.utility.JacksonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * gitlab操作
 *
 * @author collin
 * @date 2024-01-18
 */
@RequiredArgsConstructor
public class GitLabComponent implements InitializingBean {

    private final MonitorProperties monitorProperties;
    private String jobsUrlTemplate;
    /**
     * gitlab请求header信息
     */
    private Header[] headers;

    /**
     * 获取最后一个tag创建时间
     *
     * @param serviceName
     * @return
     * @throws IOException
     */
    public Long getLastTagCreateAtTs(String serviceName) throws IOException {
        ServiceInfoProperties serviceInfoProperties = monitorProperties.getServiceInfos().get(serviceName);
        if (serviceInfoProperties == null) {
            return null;
        }

        String result = HttpUtil.get(String.format(jobsUrlTemplate, serviceInfoProperties.getId()), headers, StandardCharsets.UTF_8.name(), null, 3000, 3000, null);
        if (!StringUtils.hasText(result)) {
            return 0L;
        }

        JsonNode jsonArray = JacksonUtil.parse(result);
        if (jsonArray.isEmpty()) {
            return 0L;
        }

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode jobInfo = jsonArray.get(i);
            boolean isTag = jobInfo.get("tag").asBoolean();
            if (isTag) {
                String jobStartedAtUtcStr = jobInfo.get("started_at").asText();
                return DateUtil.toCurrentMillis(jobStartedAtUtcStr);
            }
        }

        // 需要提醒（随便设置一个比阈值小的值）
        return System.currentTimeMillis() - 2 * serviceInfoProperties.getRemindTagMinDiffTs();
    }

    /**
     * gitlab是否可用
     *
     * @return
     */
    public boolean enable() {
        GitlabProperties gitlab = monitorProperties.getGitlab();
        return gitlab != null && StringUtils.hasText(gitlab.getUrlPrefix());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (enable()) {
            this.jobsUrlTemplate = String.format("%s/api/v4/projects/%d/jobs?scope[]=running&scope[]=success", monitorProperties.getGitlab().getUrlPrefix());

            this.headers = new Header[1];
            headers[0] = new BasicHeader("PRIVATE-TOKEN", monitorProperties.getGitlab().getToken());
        }
    }

}