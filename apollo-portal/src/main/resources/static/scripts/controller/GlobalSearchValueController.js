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
    ['$scope', '$window', '$translate', 'toastr', 'AppUtil', 'GlobalSearchValueService', 'PermissionService', GlobalSearchValueController]);

function GlobalSearchValueController($scope, $window, $translate, toastr, AppUtil, GlobalSearchValueService, PermissionService) {

    $scope.allItemInfo = [];
    $scope.pageItemInfo = [];
    $scope.itemInfoSearchKey = '';
    $scope.itemInfoSearchValue = '';
    $scope.needToBeHighlightedKey = '';
    $scope.needToBeHighlightedValue = '';
    $scope.isShowHighlightKeyword = [];
    $scope.isAllItemInfoDirectlyDisplayKey = [];
    $scope.isAllItemInfoDirectlyDisplayValue = [];
    $scope.isPageItemInfoDirectlyDisplayKey = [];
    $scope.isPageItemInfoDirectlyDisplayValue = [];
    $scope.currentPage = 1;
    $scope.pageSize = '10';
    $scope.totalItems = 0;
    $scope.totalPages = 0;
    $scope.pagesArray = [];
    $scope.tempKey = '';
    $scope.tempValue = '';

    $scope.getItemInfoByKeyAndValue = getItemInfoByKeyAndValue;
    $scope.highlightKeyword = highlightKeyword;
    $scope.jumpToTheEditingPage = jumpToTheEditingPage;
    $scope.isShowAllValue = isShowAllValue;
    $scope.convertPageSizeToInt = convertPageSizeToInt;
    $scope.changePage = changePage;
    $scope.getPagesArray = getPagesArray;
    $scope.determineDisplayValue = determineDisplayValue;
    $scope.determineDisplayKey = determineDisplayKey;

    init();
    function init() {
        initPermission();
    }

    function initPermission() {
        PermissionService.has_root_permission()
            .then(function (result) {
                $scope.isRootUser = result.hasPermission;
            });
    }

    function getItemInfoByKeyAndValue(itemInfoSearchKey, itemInfoSearchValue) {
        $scope.currentPage = 1;
        $scope.itemInfoSearchKey = itemInfoSearchKey || '';
        $scope.itemInfoSearchValue = itemInfoSearchValue || '';
        $scope.allItemInfo = [];
        $scope.pageItemInfo = [];
        $scope.isAllItemInfoDirectlyDisplayKey = [];
        $scope.isPageItemInfoDirectlyDisplayKey = [];
        $scope.isAllItemInfoDirectlyDisplayValue = [];
        $scope.isPageItemInfoDirectlyDisplayValue = [];
        $scope.tempKey = itemInfoSearchKey || '';
        $scope.tempValue = itemInfoSearchValue || '';
        $scope.isShowHighlightKeyword = [];
        GlobalSearchValueService.findItemInfoByKeyAndValue($scope.itemInfoSearchKey, $scope.itemInfoSearchValue)
            .then(handleSuccess).catch(handleError);
        function handleSuccess(result) {
            let allItemInfo = [];
            let isAllItemInfoDirectlyDisplayValue = [];
            let isAllItemInfoDirectlyDisplayKey = [];
            if(($scope.itemInfoSearchKey === '') && !($scope.itemInfoSearchValue === '')){
                $scope.needToBeHighlightedValue = $scope.itemInfoSearchValue;
                $scope.needToBeHighlightedKey = '';
                result.data.forEach((itemInfo, index) => {
                    allItemInfo.push(itemInfo);
                    isAllItemInfoDirectlyDisplayValue[index] = determineDisplayValue(itemInfo.value, itemInfoSearchValue);
                    isAllItemInfoDirectlyDisplayKey[index] = determineDisplayKey(itemInfo.key, '');
                });
            }else if(!($scope.itemInfoSearchKey === '') && ($scope.itemInfoSearchValue === '')){
                $scope.needToBeHighlightedKey = $scope.itemInfoSearchKey;
                $scope.needToBeHighlightedValue = '';
                result.data.forEach((itemInfo, index) => {
                    allItemInfo.push(itemInfo);
                    isAllItemInfoDirectlyDisplayKey[index] = determineDisplayKey(itemInfo.key, itemInfoSearchKey);
                    isAllItemInfoDirectlyDisplayValue[index] = '0';
                });
            }else{
                $scope.needToBeHighlightedKey = $scope.itemInfoSearchKey;
                $scope.needToBeHighlightedValue = $scope.itemInfoSearchValue;
                result.data.forEach((itemInfo, index) => {
                    allItemInfo.push(itemInfo);
                    isAllItemInfoDirectlyDisplayValue[index] = determineDisplayValue(itemInfo.value, itemInfoSearchValue);
                    isAllItemInfoDirectlyDisplayKey[index] = determineDisplayKey(itemInfo.key, itemInfoSearchKey);
                });
            }
            $scope.totalItems = allItemInfo.length;
            $scope.allItemInfo = allItemInfo;
            $scope.totalPages = Math.ceil($scope.totalItems / parseInt($scope.pageSize, 10));
            const startIndex = ($scope.currentPage - 1) * parseInt($scope.pageSize, 10);
            const endIndex = Math.min(startIndex + parseInt($scope.pageSize, 10), allItemInfo.length);
            $scope.pageItemInfo = allItemInfo.slice(startIndex, endIndex);
            $scope.isAllItemInfoDirectlyDisplayValue = isAllItemInfoDirectlyDisplayValue;
            $scope.isAllItemInfoDirectlyDisplayKey = isAllItemInfoDirectlyDisplayKey;
            $scope.isPageItemInfoDirectlyDisplayValue = isAllItemInfoDirectlyDisplayValue.slice(startIndex, endIndex);
            $scope.isPageItemInfoDirectlyDisplayKey = isAllItemInfoDirectlyDisplayKey.slice(startIndex, endIndex);
            getPagesArray();
            if(result.hasMoreData){
                toastr.warning(result.message, $translate.instant('Item.GlobalSearch.Tips'));
            }
        }

        function handleError(error) {
            $scope.itemInfo = [];
            switch (error.status) {
                case 400:
                    toastr.warning(error.data, $translate.instant('Item.GlobalSearch.Tips'));
                    break;
                default:
                    toastr.error(AppUtil.errorMsg(error), $translate.instant('Item.GlobalSearchSystemError'));
                    break;
            }
        }
    }

    function convertPageSizeToInt() {
        getItemInfoByKeyAndValue($scope.tempKey, $scope.tempValue);
    }

    function changePage(page) {
        if (page >= 1 && page <= $scope.totalPages) {
            $scope.currentPage = page;
            $scope.isShowHighlightKeyword = [];
            $scope.isPageItemInfoDirectlyDisplayKey = [];
            $scope.isPageItemInfoDirectlyDisplayValue = [];
            $scope.itemInfoSearchKey = $scope.tempKey;
            $scope.itemInfoSearchValue = $scope.tempValue;
            const startIndex = ($scope.currentPage - 1)* parseInt($scope.pageSize, 10);
            const endIndex = Math.min(startIndex + parseInt($scope.pageSize, 10), $scope.totalItems);
            $scope.pageItemInfo = $scope.allItemInfo.slice(startIndex, endIndex);
            $scope.isPageItemInfoDirectlyDisplayValue = $scope.isAllItemInfoDirectlyDisplayValue.slice(startIndex, endIndex);
            $scope.isPageItemInfoDirectlyDisplayKey = $scope.isAllItemInfoDirectlyDisplayKey.slice(startIndex, endIndex);
            getPagesArray();
        }
    }

    function getPagesArray() {
        const pageRange = 2;
        let pagesArray = [];
        let currentPage = $scope.currentPage;
        let totalPages = $scope.totalPages;
        if (totalPages <= (pageRange * 2) + 4) {
            for (let i = 1; i <= totalPages; i++) {
                pagesArray.push(i);
            }
        } else {
            if (currentPage <= (pageRange + 2)) {
                for (let i = 1; i <= pageRange * 2 + 2; i++) {
                    pagesArray.push(i);
                }
                pagesArray.push('...');
                pagesArray.push(totalPages);
            } else if (currentPage >= (totalPages - (pageRange + 1))) {
                for (let i = totalPages - pageRange * 2 - 1 ; i <= totalPages; i++) {
                    pagesArray.push(i);
                }
                pagesArray.unshift('...');
                pagesArray.unshift(1);
            } else {
                for (let i = (currentPage - pageRange); i <= currentPage + pageRange; i++) {
                    pagesArray.push(i);
                }
                pagesArray.unshift('...');
                pagesArray.unshift(1);
                pagesArray.push('...');
                pagesArray.push(totalPages);
            }
        }
        $scope.pagesArray = pagesArray;
    }

    function determineDisplayValue(value, highlight) {
        if (value === highlight) return '0';
        const position = value.indexOf(highlight);
        if (position === -1) return '-1';
        if (position === 0) return '1';
        if (position + highlight.length === value.length) return '2';
        return "3";
    }

    function determineDisplayKey(key, highlight) {
        return key === highlight ? '0' : '-1';
    }

    function jumpToTheEditingPage(appid,env,cluster){
        let url = AppUtil.prefixPath() + "/config.html#/appid=" + appid + "&" +"env=" + env + "&" + "cluster=" + cluster;
        window.open(url, '_blank');
    }

    function highlightKeyword(fulltext,keyword) {
        if (!keyword || keyword.length === 0) return fulltext;
        let regex = new RegExp("(" + keyword + ")", "g");
        return fulltext.replace(regex, '<span class="highlight" style="background: yellow;padding: 1px 4px;">$1</span>');
    }

    function isShowAllValue(index){
        $scope.isShowHighlightKeyword[index] = !$scope.isShowHighlightKeyword[index];
    }

}
