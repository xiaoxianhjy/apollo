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

/**
 * @author hujiyuan 2024-08-10
 */

import com.ctrip.framework.apollo.common.dto.ItemInfoDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.entity.vo.ItemInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GlobalSearchValueServiceTest {

    @Mock
    private AdminServiceAPI.ItemAPI itemAPI;

    @InjectMocks
    private GlobalSearchValueService globalSearchValueService;

    private final int perEnvSearchMaxResults = 200;

    @Test
    public void testGet_PerEnv_ItemInfo_BySearch_withKeyAndValue_ReturnExpectedItemInfos() {
        ItemInfoDTO itemInfoDTO = new ItemInfoDTO("TestApp","TestCluster","TestNamespace","1","TestKey","TestValue");
        List<ItemInfoDTO> mockItemInfoDTOs = new ArrayList<>();
        mockItemInfoDTOs.add(itemInfoDTO);
        Mockito.when(itemAPI.getPerEnvItemInfoBySearch(any(Env.class), eq("TestKey"), eq("TestValue"), eq(0), eq(perEnvSearchMaxResults))).thenReturn(new PageDTO<>(mockItemInfoDTOs, PageRequest.of(0, 1), 1L));
        PageDTO<ItemInfo> mockItemInfos = globalSearchValueService.get_PerEnv_ItemInfo_BySearch(Env.PRO, "TestKey", "TestValue", 0, 200);
        assertEquals(1, mockItemInfos.getContent().size());
        ItemInfo itemInfo = new ItemInfo("TestApp", Env.PRO.getName(), "TestCluster", "TestNamespace", "1", "TestKey", "TestValue");
        List<ItemInfo> expectedResults = new ArrayList<>();
        expectedResults.add(itemInfo);
        verify(itemAPI,times(1)).getPerEnvItemInfoBySearch(any(Env.class), eq("TestKey"), eq("TestValue"), eq(0), eq(perEnvSearchMaxResults));
        assertEquals(expectedResults.toString(), mockItemInfos.getContent().toString());
    }

    @Test
    public void testGet_PerEnv_ItemInfo_withKeyAndValue_BySearch_ReturnEmptyItemInfos() {
        Mockito.when(itemAPI.getPerEnvItemInfoBySearch(any(Env.class), anyString(), anyString(), eq(0), eq(perEnvSearchMaxResults)))
                .thenReturn(new PageDTO<>(new ArrayList<>(), PageRequest.of(0, 1), 0L));
        PageDTO<ItemInfo> result = globalSearchValueService.get_PerEnv_ItemInfo_BySearch(Env.PRO, "NonExistentKey", "NonExistentValue", 0, 200);
        assertEquals(0, result.getContent().size());
    }

}
