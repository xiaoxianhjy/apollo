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
appService.service('GlobalSearchValueService', ['$resource', '$q', 'AppUtil', function ($resource, $q, AppUtil) {
    let global_search_resource = $resource('', {}, {
        find_different_env_properties_item_info: {
            method: 'GET',
            isArray: true,
            url: AppUtil.prefixPath() + '/global-search/properties/by-env-or-key-or-value?env=:env&key=:key&value=:value',
        },
        find_different_env_exclude_properties_item_info: {
            method: 'GET',
            isArray: true,
            url: AppUtil.prefixPath() + '/global-search/exclude-properties/by-env-or-value?env=:env&value=:value',
        },
    });
    return {
        findDifferentEnvPropertiesItemInfo:function (env,key,value){
            let d = $q.defer();
            global_search_resource.find_different_env_properties_item_info({env: env,key: key,value: value},function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        findDifferentEnvExcludePropertiesItemInfo:function (env,value){
            let d = $q.defer();
            global_search_resource.find_different_env_exclude_properties_item_info({env: env,value: value}, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }
}]);
