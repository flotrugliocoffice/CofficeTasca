var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec');

var CofficeTasca = {
    isDebug: true,
    userProgress: null,
    isInBrowser: false,
    browserChecked: false,
    browserMessage: function () {
        console.warn("NATIVE FEATURE NOT AVAILABLE IN BROWSER");
    },
    browserCallBack: {"error": "NATIVE FEATURE NOT AVAILABLE IN BROWSER"},
    innerFunctions: {
        nullFunction: function () {
        }
    },
    checkIsInBrowser: function () {
        if (this.browserChecked == false) {
            this.isInBrowser = (window.cordova && (window.cordova.platformId === 'browser' || window.cordova.platformId === 'osx')) || !(window.phonegap || window.cordova);
            this.browserChecked = true;
        }
        return this.isInBrowser;
    },
    onProgress: function (data) {
        var event = new CustomEvent("native_progress", {detail: data});
        document.dispatchEvent(event);
        if (this.userProgress) {
            this.userProgress(data);
        }
    },
    getInfo: function (arg0, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(success, error, "CofficeTasca", "getInfo", [arg0]);
    },

    requestPermissions: function (success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(success, error, "CofficeTasca", "requestPermissions", []);
    },
    reverseGeocode: function (success, error, latitude, longitude) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(success, error, "CofficeTasca", "reverseGeocode", [latitude, longitude]);
    },
    getWineById: function (language, id, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(id) == 'undefined') id = "";
        exec(success, error, "CofficeTasca", "getWineById", [language, id]);
    },
    getEstateById: function (language, id, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(id) == 'undefined') id = "";
        exec(success, error, "CofficeTasca", "getEstateById", [language, id]);
    },
    clearWines: function () {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(this.innerFunctions.nullFunction, this.innerFunctions.nullFunction, "CofficeTasca", "clearWines", [])
    },
    clearEstates: function () {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(this.innerFunctions.nullFunction, this.innerFunctions.nullFunction, "CofficeTasca", "clearEstates", [])
    },
    clearTastes: function () {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(this.innerFunctions.nullFunction, this.innerFunctions.nullFunction, "CofficeTasca", "clearTastes", [])
    },
    clearCache: function () {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        exec(this.innerFunctions.nullFunction, this.innerFunctions.nullFunction, "CofficeTasca", "clearCache", [])
    },
    clearAll: function (clear_image) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(clear_image == 'undefined')) clear_image = false;
        exec(this.innerFunctions.nullFunction, this.innerFunctions.nullFunction, "CofficeTasca", "clearAll", [clear_image])
    },
    downloadAll: function (language, enable_downloadImages, success, error, progress) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(enable_downloadImages) == 'undefined') enable_downloadImages = false;

        if (progress) {
            this.userProgress = progress;
        }
        var innerSuccess = function (data) {
            if (data && data.type && data.type == 'progress') {
                if (typeof(cofficetasca)) {
                    cofficetasca.onProgress(data);
                }
                return;
            }
            success(data);
        }
        exec(innerSuccess, error, "CofficeTasca", "downloadAll", [language, enable_downloadImages]);
    },
    downloadAllMedia: function (array, success, error, progress) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(array) == 'undefined') enable_downloadImages = [];
        if (progress) {
            this.userProgress = progress;
        }
        var innerSuccess = function (data) {
            if (data && data.type && data.type == 'progress') {
                if (typeof(cofficetasca)) {
                    cofficetasca.onProgress(data);
                }
                return;
            }
            success(data);
        }
        exec(innerSuccess, error, "CofficeTasca", "downloadAllMedia", [array]);
    },
    getImageByUrl: function (imageUrl, success, error, useCordovaPath, overrideCache) {
        if (typeof(useCordovaPath) == 'undefined') {
            useCordovaPath = true;
        }
        if (typeof(overrideCache) == 'undefined') {
            overrideCache = false;
        }
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(imageUrl) == 'undefined') imageUrl = "";
        exec(success, error, "CofficeTasca", "getImageByUrl", [imageUrl, useCordovaPath, overrideCache]);
    },
    getImageMap: function (useCordovaPath, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(useCordovaPath) == 'undefined') useCordovaPath = false;
        exec(success, error, "CofficeTasca", "getImageMap", [useCordovaPath]);
    },
    downloadWines: function (language, enable_downloadImages, success, error, progress) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(enable_downloadImages) == 'undefined') enable_downloadImages = false;

        if (progress) {
            this.userProgress = progress;
        }
        var innerSuccess = function (data) {
            if (data && data.type && data.type == 'progress') {
                if (typeof(cofficetasca)) {
                    cofficetasca.onProgress(data);
                }
                return;
            }
            success(data);
        }
        exec(innerSuccess, error, "CofficeTasca", "downloadWines", [language, enable_downloadImages]);
    },
    getWinesTypeAhead: function (language, term, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(term) == 'undefined') term = "";

        exec(success, error, "CofficeTasca", "getWinesTypeAhead", [language, term]);
    },
    getWines: function (language, term, limit, offset, sort, sortDirection, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(term) == 'undefined') term = "";
        if (typeof(limit) == 'undefined') limit = -1;
        if (typeof(offset) == 'undefined') offset = -1;
        if (typeof(sort) == 'undefined') sort = "";
        if (typeof(sortDirection) == 'undefined') term = false;

        exec(success, error, "CofficeTasca", "getWines", [language, term, limit, offset, sort, sortDirection]);
    },
    downloadEstates: function (language, enable_downloadImages, success, error, progress) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(enable_downloadImages) == 'undefined') enable_downloadImages = false;

        if (progress) {
            this.userProgress = progress;
        }
        var innerSuccess = function (data) {
            if (data && data.type && data.type == 'progress') {
                if (typeof(cofficetasca)) {
                    cofficetasca.onProgress(data);
                }
                return;
            }
            success(data);
        }
        exec(innerSuccess, error, "CofficeTasca", "downloadEstates", [language, enable_downloadImages]);
    },
    getEstatesTypeAhead: function (language, term, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(term) == 'undefined') term = "";

        exec(success, error, "CofficeTasca", "getEstatesTypeAhead", [language, term]);
    },
    getEstates: function (language, term, limit, offset, sort, sortDirection, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(language) == 'undefined') language = "";
        if (typeof(term) == 'undefined') term = "";
        if (typeof(limit) == 'undefined') limit = -1;
        if (typeof(offset) == 'undefined') offset = -1;
        if (typeof(sort) == 'undefined') sort = "";
        if (typeof(sortDirection) == 'undefined') term = false;

        exec(success, error, "CofficeTasca", "getEstates", [language, term, limit, offset, sort, sortDirection]);
    },
    getTasteById: function (id, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(id) == 'undefined') id = "";
        exec(success, error, "CofficeTasca", "getTasteById", [id]);
    },
    insertTaste: function (taste, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(taste) == 'undefined') taste = "";
        exec(success, error, "CofficeTasca", "insertTaste", [taste]);
    },
    editTaste: function (id, taste, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(id) == 'undefined') id = "";
        if (typeof(taste) == 'undefined') taste = "";
        exec(success, error, "CofficeTasca", "insertTaste", [id, taste]);
    },
    deleteTaste: function (id, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(id) == 'undefined') id = "";
        exec(success, error, "CofficeTasca", "deleteTaste", [id, taste]);
    },
    getTastesTypeAhead: function (term, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(term) == 'undefined') term = "";

        exec(success, error, "CofficeTasca", "getTastesTypeAhead", [term]);
    },
    getTastes: function (term, limit, offset, sort, sortDirection, success, error) {
        if (this.checkIsInBrowser()) {
            this.browserMessage();
            if (error) {
                error(this.browserCallBack);
            }
            return
        }
        if (typeof(term) == 'undefined') term = "";
        if (typeof(limit) == 'undefined') limit = -1;
        if (typeof(offset) == 'undefined') offset = -1;
        if (typeof(sort) == 'undefined') sort = "";
        if (typeof(sortDirection) == 'undefined') term = false;

        exec(success, error, "CofficeTasca", "getTastes", [term, limit, offset, sort, sortDirection]);
    }
};
module.exports = CofficeTasca;