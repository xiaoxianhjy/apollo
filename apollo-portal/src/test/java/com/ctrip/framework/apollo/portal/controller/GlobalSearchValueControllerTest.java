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

/**
 * @author hujiyuan 2024-08-10
 */

import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.vo.ItemInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.GlobalSearchValueService;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class GlobalSearchValueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortalSettings portalSettings;

    @Mock
    private PortalConfig portalConfig;

    @Mock
    private GlobalSearchValueService globalSearchValueService;

    @InjectMocks
    private GlobalSearchValueController globalSearchValueController;

    private final List<Env> activeEnvs = new ArrayList<>();

    private final int perEnvSearchMaxResults = 200;

    @Before
    public void setUp() {
        when(portalSettings.getActiveEnvs()).thenReturn(activeEnvs);
        when(portalConfig.getPerEnvSearchMaxResults()).thenReturn(perEnvSearchMaxResults);
        mockMvc = MockMvcBuilders.standaloneSetup(globalSearchValueController).build();
    }

    @Test
    public void testGet_ItemInfo_BySearch_WithEmptyKeyAndValue_ReturnBadRequestAndTips() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "")
                        .param("value", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(new Gson().toJson("Please enter at least one search criterion in either key or value.")));
    }

    @Test
    public void testGet_ItemInfo_BySearch_WithNoActiveEnvs_ReturnBadRequestAndTips() throws Exception {
        activeEnvs.clear();
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "query-key")
                        .param("value", "query-value"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(new Gson().toJson("Request accepted. Looking for available admin service.")));
    }

    @Test
    public void testGet_ItemInfo_BySearch_WithKeyAndValueAndActiveEnvs_ReturnEmptyItemInfos() throws Exception {
        activeEnvs.add(Env.DEV);
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(any(Env.class), anyString(), anyString(),eq(0),eq(perEnvSearchMaxResults))).thenReturn(new PageDTO<>(new ArrayList<>(), PageRequest.of(0,1), 0L));
        List<ItemInfo> allEnvMockItemInfos = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> body = new HashMap<>();
        body.put("data", allEnvMockItemInfos);
        body.put("hasMoreData", false);
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "query-key")
                        .param("value", "query-value"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(body)));
        verify(portalSettings,times(1)).getActiveEnvs();
        verify(portalConfig,times(2)).getPerEnvSearchMaxResults();
        verify(globalSearchValueService,times(1)).get_PerEnv_ItemInfo_BySearch(any(Env.class), anyString(), anyString(),eq(0),eq(perEnvSearchMaxResults));
    }

    @Test
    public void testGet_ItemInfo_BySearch_WithKeyAndValueAndActiveEnvs_ReturnExpectedItemInfos_ButOverPerEnvLimit() throws Exception {
        activeEnvs.add(Env.DEV);
        activeEnvs.add(Env.PRO);
        List<ItemInfo> devMockItemInfos = new ArrayList<>();
        List<ItemInfo> proMockItemInfos = new ArrayList<>();
        List<ItemInfo> allEnvMockItemInfos = new ArrayList<>();
        devMockItemInfos.add(new ItemInfo("appid1","env1","cluster1","namespace1","status1","query-key","query-value"));
        proMockItemInfos.add(new ItemInfo("appid2","env2","cluster2","namespace2","status2","query-key","query-value"));
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(eq(Env.DEV), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults))).thenReturn(new PageDTO<>(devMockItemInfos, PageRequest.of(0,1), 201L));
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(eq(Env.PRO), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults))).thenReturn(new PageDTO<>(proMockItemInfos, PageRequest.of(0,1), 201L));
        allEnvMockItemInfos.addAll(devMockItemInfos);
        allEnvMockItemInfos.addAll(proMockItemInfos);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> body = new HashMap<>();
        body.put("data", allEnvMockItemInfos);
        body.put("hasMoreData", true);
        String message = "In DEV , PRO , more than "+perEnvSearchMaxResults+" items found (Exceeded the maximum search quantity for a single environment). Please enter more precise criteria to narrow down the search scope.";
        body.put("message", message);
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "query-key")
                        .param("value", "query-value"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(body)));
        verify(portalSettings,times(1)).getActiveEnvs();
        verify(portalConfig,times(5)).getPerEnvSearchMaxResults();
        verify(globalSearchValueService, times(1)).get_PerEnv_ItemInfo_BySearch(eq(Env.DEV), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults));
        verify(globalSearchValueService, times(1)).get_PerEnv_ItemInfo_BySearch(eq(Env.PRO), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults));
        verify(globalSearchValueService, times(2)).get_PerEnv_ItemInfo_BySearch(any(Env.class), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults));
    }

    @Test
    public void testGet_ItemInfo_BySearch_WithKeyAndValueAndActiveEnvs_ReturnExpectedItemInfos() throws Exception {
        activeEnvs.add(Env.DEV);
        activeEnvs.add(Env.PRO);
        List<ItemInfo> devMockItemInfos = new ArrayList<>();
        List<ItemInfo> proMockItemInfos = new ArrayList<>();
        List<ItemInfo> allEnvMockItemInfos = new ArrayList<>();
        devMockItemInfos.add(new ItemInfo("appid1","env1","cluster1","namespace1","status1","query-key","query-value"));
        proMockItemInfos.add(new ItemInfo("appid2","env2","cluster2","namespace2","status2","query-key","query-value"));
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(eq(Env.DEV), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults))).thenReturn(new PageDTO<>(devMockItemInfos, PageRequest.of(0,1), 1L));
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(eq(Env.PRO), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults))).thenReturn(new PageDTO<>(proMockItemInfos, PageRequest.of(0,1), 1L));
        allEnvMockItemInfos.addAll(devMockItemInfos);
        allEnvMockItemInfos.addAll(proMockItemInfos);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> body = new HashMap<>();
        body.put("data", allEnvMockItemInfos);
        body.put("hasMoreData", false);
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "query-key")
                        .param("value", "query-value"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(body)));
        verify(portalSettings,times(1)).getActiveEnvs();
        verify(globalSearchValueService, times(1)).get_PerEnv_ItemInfo_BySearch(eq(Env.DEV), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults));
        verify(globalSearchValueService, times(1)).get_PerEnv_ItemInfo_BySearch(eq(Env.PRO), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults));
        verify(globalSearchValueService, times(2)).get_PerEnv_ItemInfo_BySearch(any(Env.class), eq("query-key"), eq("query-value"),eq(0),eq(perEnvSearchMaxResults));
    }
}
