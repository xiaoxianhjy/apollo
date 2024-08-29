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
package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.ItemInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.GlobalSearchValueService;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


@RestController
public class GlobalSearchValueController {
    private final PortalSettings portalSettings;
    private final GlobalSearchValueService globalSearchValueService;
    private final PortalConfig portalConfig;

    public GlobalSearchValueController(final PortalSettings portalSettings, final GlobalSearchValueService globalSearchValueService, final PortalConfig portalConfig) {
        this.portalSettings = portalSettings;
        this.globalSearchValueService = globalSearchValueService;
        this.portalConfig = portalConfig;
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @GetMapping("/global-search/item-info/by-key-or-value")
    public ResponseEntity<?> get_ItemInfo_BySearch(@RequestParam(value = "key", required = false, defaultValue = "") String key,
                                                   @RequestParam(value = "value", required = false , defaultValue = "") String value) {

        if(key.isEmpty() && value.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new Gson().toJson("Please enter at least one search criterion in either key or value."));
        }

        List<Env> activeEnvs = portalSettings.getActiveEnvs();
        if(activeEnvs.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new Gson().toJson("Request accepted. Looking for available admin service."));

        }

        List<ItemInfo> allEnvItemInfos = new ArrayList<>();
        List<String> envBeyondLimit = new ArrayList<>();
        AtomicBoolean hasMoreData = new AtomicBoolean(false);
        activeEnvs.forEach(env -> {
            PageDTO<ItemInfo> perEnvItemInfos = globalSearchValueService.get_PerEnv_ItemInfo_BySearch(env, key, value,0, portalConfig.getPerEnvSearchMaxResults());
            if(perEnvItemInfos.getTotal() > portalConfig.getPerEnvSearchMaxResults()){
                envBeyondLimit.add(env.getName());
                hasMoreData.set(true);
            }
            allEnvItemInfos.addAll(perEnvItemInfos.getContent());
        });

        Map<String, Object> body = new HashMap<>();
        if(hasMoreData.get()){
            body.put("data", allEnvItemInfos);
            body.put("hasMoreData", true);
            body.put("message", String.format(
                    "In %s , more than %d items found (Exceeded the maximum search quantity for a single environment). Please enter more precise criteria to narrow down the search scope.",
                    String.join(" , ", envBeyondLimit), portalConfig.getPerEnvSearchMaxResults()));
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }

        body.put("data", allEnvItemInfos);
        body.put("hasMoreData", false);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

}
