(function (root) {

    var KEY_LAST_ID = "lastId";
    var KEY_ALL_DATA = "allData";

    var _canUseStorage = localStorage !== "undifined";
    var _storage = _canUseStorage ? localStorage : null;
    var _isLoading = false;

    function load(key) {
        /// <summary>キャッシュの読み込み</summary>
        /// <param name="key" type="String">Storageから読み込むデータのKey</param>
        /// <returns type="Object" />

        if (!_canUseStorage
            || _storage == null
            || !(key in _storage)) return null;

        return JSON.parse(_storage[key]);
    };

    function save(map) {
        /// <summary>キャッシュの保存</summary>
        /// <param name="map" type="Object">保存するデータのKey/Value</param>

        if (!_canUseStorage) return;

        for (key in map) {
            _storage[key] = JSON.stringify(map[key]);
        }
    };

    function Cache() {
        /// <summary>Tumblrのデータキャッシュへアクセスするクラス</summary>

        /// <field name="canUseStorage" type="Boolean">Local Storageが使用可能かどうかを判定</field>
        this.canUseStorage = _canUseStorage;

        this.setIsLoading = function (v) {
            /// <summary>読み込み中設定</summary>
            /// <param name="v" type="Boolean"></param>
            _isLoading = v;
        }

        this.isLoading = function () {
            /// <summary>読み込み中</summary>
            /// <returns type="Boolean" />
            return _isLoading;
        };

        this.getLastId = function () {
            /// <summary>最後に取得した記事のID</summary>
            /// <returns type="Number" />
            return load(KEY_LAST_ID);
        };

        this.setLastId = function (value) {
            /// <summary>最後に取得した記事のID</summary>
            /// <param name="value" type="Number">value</param>
            save({ lastId: value });
        };

        this.setAllData = function (allData) {
            /// <summary>全件データセット</summary>
            /// <param name="allData" type="Array">Tumblrから取得した全件データ</param>
            save({ allData: allData });
        };

        this.getAllData = function () {
            /// <summary>全件データ取得</summary>
            /// <returns type="Array" />
            return load(KEY_ALL_DATA);
        };
    }

    root.Cache = Cache;

})(this);