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
global_search_value_module.controller('GlobalSearchValueController',
    ['$scope', '$window', '$translate', 'toastr', 'AppUtil', 'GlobalSearchValueService', 'PermissionService',
        'EnvService', GlobalSearchValueController]);

function GlobalSearchValueController($scope, $window, $translate, toastr, AppUtil, GlobalSearchValueService, PermissionService, EnvService) {

    $scope.propertiesItemInfo = [];
    $scope.excludePropertiesItemInfo = [];
    $scope.envs = [];
    $scope.selectedExcludePropertiesEnv = 'allenvs';
    $scope.selectedPropertiesEnv = 'allenvs';
    $scope.displayModule = 'home';
    $scope.propertiesItemInfoSearchKey = '';
    $scope.propertiesItemInfoSearchValue = '';
    $scope.excludePropertiesItemInfoSearchValue = '';
    $scope.gobackPropertiesValueSearchTabs = gobackPropertiesValueSearchTabs;
    $scope.gobackExcludePropertiesValueSearchTabs = gobackExcludePropertiesValueSearchTabs;
    $scope.allowSwitchingTabs = true;
    $scope.getPropertiesItemInfoByKeyAndValue = getPropertiesItemInfoByKeyAndValue;
    $scope.getExcludePropertiesItemInfoByValue = getExcludePropertiesItemInfoByValue;
    $scope.switchPropertiesEnvs = switchPropertiesEnvs;
    $scope.switchExcludePropertiesEnvs = switchExcludePropertiesEnvs;




    init();
    function init() {
        initPermission();
        initEnv();
        getPropertiesItemInfoByKeyAndValue('','');
    }
    function initEnv() {
        EnvService.find_all_envs().then(function (result) {
            $scope.envs = result;
        });
    }
    function initPermission() {
        PermissionService.has_root_permission()
            .then(function (result) {
                $scope.isRootUser = result.hasPermission;
            });
    }

    function switchPropertiesEnvs(env) {
        $scope.selectedPropertiesEnv = env;
        console.log($scope.selectedPropertiesEnv);
    }

    function switchExcludePropertiesEnvs(env) {
        $scope.selectedExcludePropertiesEnv = env;
        console.log($scope.selectedExcludePropertiesEnv);
    }

    function getPropertiesItemInfoByKeyAndValue(propertiesItemInfoSearchKey, propertiesItemInfoSearchValue) {
        console.log('Function getPropertiesItemInfoByKeyAndValue is called', arguments);
        $scope.propertiesItemInfoSearchKey = propertiesItemInfoSearchKey;
        $scope.propertiesItemInfoSearchValue = propertiesItemInfoSearchValue;
        console.log($scope.selectedPropertiesEnv,"+",$scope.propertiesItemInfoSearchKey,"+",$scope.propertiesItemInfoSearchValue);
        GlobalSearchValueService.findDifferentEnvPropertiesItemInfo($scope.selectedPropertiesEnv,$scope.propertiesItemInfoSearchKey,$scope.propertiesItemInfoSearchValue)
            .then(function (result) {
                $scope.propertiesItemInfo = [];
                result.forEach(function (iteminfo) {
                    $scope.propertiesItemInfo.push(iteminfo);
                });
                console.log($scope.propertiesItemInfo);
            },function (result) {
                $scope.propertiesItemInfo = [];
                toastr.error(AppUtil.errorMsg(result), $translate.instant('Item.GlobalSearchSystemError'));
            });
    }

    function getExcludePropertiesItemInfoByValue(excludePropertiesItemInfoSearchValue) {
        $scope.excludePropertiesItemInfoSearchValue = excludePropertiesItemInfoSearchValue;
        console.log($scope.selectedExcludePropertiesEnv,"+",$scope.excludePropertiesItemInfoSearchValue);
        GlobalSearchValueService.findDifferentEnvExcludePropertiesItemInfo($scope.selectedExcludePropertiesEnv,$scope.excludePropertiesItemInfoSearchValue)
            .then(function (result) {
                $scope.excludePropertiesItemInfo = [];
                result.forEach(function (iteminfo) {
                    $scope.excludePropertiesItemInfo.push(iteminfo);
                });
                console.log($scope.excludePropertiesItemInfo);
            },function (result) {
                $scope.excludePropertiesItemInfo = [];
                toastr.error(AppUtil.errorMsg(result), $translate.instant('Item.GlobalSearchSystemError'));
            });
    }

    function gobackPropertiesValueSearchTabs(){
        $scope.displayModule = 'home';
        $scope.allowSwitchingTabs = true;
        getPropertiesItemInfoByKeyAndValue($scope.propertiesItemInfoSearchKey,$scope.propertiesItemInfoSearchValue);
    }

    function gobackExcludePropertiesValueSearchTabs(){
        $scope.displayModule = 'home';
        $scope.allowSwitchingTabs = true;
        getExcludePropertiesItemInfoByValue($scope.excludePropertiesItemInfoSearchValue);
    }


}
