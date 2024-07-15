package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.entity.vo.ItemInfo;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.GlobalSearchValueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
public class GlobalSearchValueController {

    private final PortalSettings portalSettings;
    private final GlobalSearchValueService globalSearchValueService;

    public GlobalSearchValueController(final PortalSettings portalSettings, final GlobalSearchValueService globalSearchValueService) {
        this.portalSettings = portalSettings;
        this.globalSearchValueService = globalSearchValueService;
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @GetMapping("/global-search/properties/by-env-or-key-or-value")
    public List<ItemInfo> get_DifferentEnvPropertiesItemInfo_BySearch(@RequestParam(value = "env", required = false, defaultValue = "allenvs") String envName,
                                                                      @RequestParam(value = "key", required = false, defaultValue = "") String key,
                                                                      @RequestParam(value = "value", required = false, defaultValue = "") String value) {
        List<ItemInfo> itemInfos = new ArrayList<>();
        if(key.equals("content")){
            return itemInfos;
        };
        if(envName.equals("allenvs")){
            List<Env> allEnvs = portalSettings.getAllEnvs();
            allEnvs.forEach(env -> {
                List<ItemInfo> peerEnvItemInfos = new ArrayList<>();
                peerEnvItemInfos = globalSearchValueService.get_PerEnv_AllProperties_ItemInfo_BySearch(env, key, value);
                itemInfos.addAll(peerEnvItemInfos);
            });
            return itemInfos;
        }else if(!envName.isEmpty()){
            List<ItemInfo> peerEnvItemInfos = new ArrayList<>();
            Env env = Env.transformEnv(envName);
            peerEnvItemInfos = globalSearchValueService.get_PerEnv_AllProperties_ItemInfo_BySearch(env, key, value);
            itemInfos.addAll(peerEnvItemInfos);
            return itemInfos;
        }
        return itemInfos;
    }

    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @GetMapping("/global-search/exclude-properties/by-env-or-value")
    public List<ItemInfo> get_DifferentEnvExcludePropertiesItemInfo_BySearch(@RequestParam(value = "env", required = false, defaultValue = "allenvs") String envName,
                                                                             @RequestParam(value = "value", required = false, defaultValue = "") String value) {
        List<ItemInfo> itemInfos = new ArrayList<>();
        if(envName.equals("allenvs")){
            List<Env> allEnvs = portalSettings.getAllEnvs();
            allEnvs.forEach(env -> {
                List<ItemInfo> peerEnvItemInfos = new ArrayList<>();
                peerEnvItemInfos = globalSearchValueService.get_PerEnv_AllExcludeProperties_ItemInfo_BySearch(env, value);
                itemInfos.addAll(peerEnvItemInfos);
            });
            return itemInfos;
        }else if(!envName.isEmpty()){
            List<ItemInfo> peerEnvItemInfos = new ArrayList<>();
            Env env = Env.transformEnv(envName);
            peerEnvItemInfos = globalSearchValueService.get_PerEnv_AllExcludeProperties_ItemInfo_BySearch(env, value);
            itemInfos.addAll(peerEnvItemInfos);
            return itemInfos;
        }
        return itemInfos;
    }



}
