/*
 * Copyright 2024 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ItemInfoDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.entity.vo.ItemInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GlobalSearchValueService {

    private final AdminServiceAPI.ItemAPI itemAPI;

    public GlobalSearchValueService(AdminServiceAPI.ItemAPI itemAPI) {
        this.itemAPI = itemAPI;
    }

    public List<ItemInfo> get_PerEnv_ItemInfo_BySearch(Env env, String key, String value) {
        List<ItemInfo> perEnvItemInfos = new ArrayList<>();
        List<ItemInfoDTO> perEnvItemInfoDTOs = itemAPI.getPerEnvItemInfoBySearch(env, key, value);
        perEnvItemInfoDTOs.forEach(itemInfoDTO -> {
            ItemInfo itemInfo = new ItemInfo(itemInfoDTO.getAppId(),env.getName(),itemInfoDTO.getClusterName(),itemInfoDTO.getNamespaceName(),itemInfoDTO.getStatus(),itemInfoDTO.getKey(),itemInfoDTO.getValue());
            perEnvItemInfos.add(itemInfo);
        });
        return perEnvItemInfos;
    }

}
