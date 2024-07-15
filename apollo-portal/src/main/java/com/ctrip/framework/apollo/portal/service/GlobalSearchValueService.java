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

    public List<ItemInfo> get_PerEnv_AllProperties_ItemInfo_BySearch(Env env, String key, String value) {
        List<ItemInfo> allItemInfos = new ArrayList<>();
        List<ItemInfoDTO> allItemInfoDTOs = itemAPI.getPerEnvAllPropertiesItemInfoBySearch(env, key, value);
        allItemInfoDTOs.forEach(itemInfoDTO -> {
            ItemInfo itemInfo = new ItemInfo(itemInfoDTO.getAppName(),env.getName(),itemInfoDTO.getClusterName(),itemInfoDTO.getNamespaceName(),itemInfoDTO.getStatus(),itemInfoDTO.getKey(),itemInfoDTO.getValue());
            allItemInfos.add(itemInfo);
        });
        return allItemInfos;
    }

    public List<ItemInfo> get_PerEnv_AllExcludeProperties_ItemInfo_BySearch(Env env, String value) {
        List<ItemInfo> allItemInfos = new ArrayList<>();
        List<ItemInfoDTO> allItemInfoDTOs = itemAPI.getPerEnvAllExcludePropertiesItemInfoBySearch(env, value);
        allItemInfoDTOs.forEach(itemInfoDTO -> {
            ItemInfo itemInfo = new ItemInfo(itemInfoDTO.getAppName(),env.getName(),itemInfoDTO.getClusterName(),itemInfoDTO.getNamespaceName(),itemInfoDTO.getStatus(),itemInfoDTO.getKey(),itemInfoDTO.getValue());
            allItemInfos.add(itemInfo);
        });
        return allItemInfos;
    }

}
