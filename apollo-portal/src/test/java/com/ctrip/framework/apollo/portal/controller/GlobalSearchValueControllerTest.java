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

import com.ctrip.framework.apollo.portal.component.PortalSettings;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class GlobalSearchValueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortalSettings portalSettings;

    @Mock
    private GlobalSearchValueService globalSearchValueService;

    @InjectMocks
    private GlobalSearchValueController globalSearchValueController;

    private final List<Env> activeEnvs = new ArrayList<>();

    @Before
    public void setUp() {
        when(portalSettings.getActiveEnvs()).thenReturn(activeEnvs);
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
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(any(Env.class), anyString(), anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "query-key")
                        .param("value", "query-value"))
                .andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(new ArrayList<>())));
        verify(portalSettings,times(1)).getActiveEnvs();
        verify(globalSearchValueService,times(1)).get_PerEnv_ItemInfo_BySearch(any(Env.class), anyString(), anyString());
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
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(eq(Env.DEV), eq("query-key"), eq("query-value"))).thenReturn(devMockItemInfos);
        when(globalSearchValueService.get_PerEnv_ItemInfo_BySearch(eq(Env.PRO), eq("query-key"), eq("query-value"))).thenReturn(proMockItemInfos);
        allEnvMockItemInfos.addAll(devMockItemInfos);
        allEnvMockItemInfos.addAll(proMockItemInfos);
        mockMvc.perform(MockMvcRequestBuilders.get("/global-search/item-info/by-key-or-value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("key", "query-key")
                        .param("value", "query-value"))
                .andExpect(status().isOk())
                .andExpect(content().json(new Gson().toJson(allEnvMockItemInfos)));
        verify(portalSettings,times(1)).getActiveEnvs();
        verify(globalSearchValueService, times(1)).get_PerEnv_ItemInfo_BySearch(eq(Env.DEV), eq("query-key"), eq("query-value"));
        verify(globalSearchValueService, times(1)).get_PerEnv_ItemInfo_BySearch(eq(Env.PRO), eq("query-key"), eq("query-value"));
        verify(globalSearchValueService, times(2)).get_PerEnv_ItemInfo_BySearch(any(Env.class), eq("query-key"), eq("query-value"));
    }
}
