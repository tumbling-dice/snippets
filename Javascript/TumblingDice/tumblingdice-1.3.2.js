/// <reference path="jquery-1.6.2.js">
/// <reference path="knockout-3.1.0.debug.js">
/// <reference path="tumblingdice-cache.js">
/// <reference path="tumblingdice-viewmodel.js">
(function (root) {

    var domain = "xxx.tumblr.com";
    var apiKey = "";

    function TumblingDice() {
        /// <summary>Tumbling Dice APIs</summary>
    };

    function TumblrData() {
        /// <summary>Tumblrに登録されているデータ</summary>

        /// <field name="id" type="Number">記事のID</field>
        this.id = 0;
        /// <field name="date" type="String">投稿日</field>
        this.date = null;
        /// <field name="title" type="String">記事タイトル</field>
        this.title = null;
        /// <field name="url" type="String">記事のURL</field>
        this.url = null;
        /// <field name="type" type="String">記事のタイプ（リンク、テキストetc...）</field>
        this.type = null;
        /// <field name="tag" type="Array">記事のタグ</field>
        this.tag = new Array();
    };

    //#region Recent

    TumblingDice.prototype.getRecent = function (displayCount) {
        /// <summary>最新投稿記事を取得</summary>
        /// <param name="displayCount" type="Number">表示件数</param>
        var cache = new Cache();

        // キャッシュと記事のIDを比較する
        if (isNotUpdateLastId(cache)) {
            // キャッシュが使用できるならば展開
            var tmp = cache.getAllData();

            if (tmp != null) {
                // 全件データから表示する件数だけを取得
                createRecentList(tmp.slice(0, displayCount - 1));
                return;
            }
        }

        // キャッシュが使用できない場合は通信を行う
        var baseUri = function (count, offset) {
            return "http://api.tumblr.com/v2/blog/" + domain + "/posts?api_key=" + apiKey + "&limit=" + count + "&offset=" + offset;
        };

        var onError = function (data) { jQuery("#recent").replaceWith(jQuery("<p/>").text("Error:" + data.meta.msg)); };
        var onEmpty = function (data) { jQuery("#recent").replaceWith("<p>Empty</p>"); };

        if (displayCount <= 20) {
            // 20件以下の場合は一回の通信でOK
            tumblrApiCall(baseUri(displayCount, 0), function (json) {

                // 一件目のIDをキャッシュに保存しておく
                if (cache.canUseStorage) {
                    cache.setLastId(json[0].id);
                }

                createRecentList(json);

            }, onError, onEmpty)();
        } else {
            // 20件より多い場合は複数回通信する
            var count = 20;
            var offset = 0;
            var recentDataArray = [];

            var onSuccess = function (json) {
                // 初回時の一件目のIDをキャッシュに保存しておく
                if (offset == 0 && cache.canUseStorage) {
                    cache.setLastId(json[0].id);
                }

                json.forEach(function (x) { recentDataArray.push(x); });

                offset = offset + count;

                if (offset == displayCount) {
                    // すべてのデータを読み込み終えたら表示
                    createRecentList(recentDataArray);
                } else {
                    // 表示件数とoffsetの差が20未満になったら取得件数を変更する
                    if (displayCount - offset < 20) {
                        count = displayCount - offset;
                    }

                    tumblrApiCall(baseUri(count, offset), onSuccess, onError, onEmpty)();
                }
            };

            tumblrApiCall(baseUri(count, offset), onSuccess, onError, onEmpty)();
        }
    };

    function createRecentList(dataArray) {
        /// <summary>最新記事一覧のリストをバインドする</summary>
        /// <param name="dataArray" type="Array">最新記事一覧データ</param>

        var viewModel = getRecentViewModel();
        dataArray.forEach(function (x) {
            viewModel.setData(x);
        });
        viewModel.isLoading(false);
    };

    //#endregion

    //#region Tag List

    TumblingDice.prototype.getTagList = function () {
        /// <summary>タグ一覧取得</summary>

        var cache = new Cache();

        if (isNotUpdateLastId(cache)) {
            // キャッシュを展開する
            var tmp = cache.getAllData();
            if (tmp != null) {
                createTagList(tmp);
                return;
            }
        }

        // キャッシュが展開できなかった場合は通信を行う
        cache.setIsLoading(true);

        var baseUri = function (offset) { return "http://api.tumblr.com/v2/blog/" + domain + "/posts/?api_key=" + apiKey + "&offset=" + offset };
        var offset = 0;
        var itemList = new Array();

        var $div = jQuery("#tags");
        var lastId = cache.getLastId();

        var onError = function (data) {
            $div.find(".loading").remove();
            $div.append($("<p/>").text("Error:" + data.meta.msg));
        };

        var onEmpty = function (data) {
            $div.find(".loading").remove();

            // 既に読み込まれたデータがある場合はバインドする
            if (itemList.length > 0) {
                cache.setAllData(itemList);
                cache.setIsLoading(false);
                createTagList(itemList);

                TumblingDice.prototype.getRelational();
                return;
            }

            $div.append("<p>Empty</p>");
        };

        var onSuccess = function (json) {

            json.forEach(function (x) { itemList.push(x); });

            // jsonのlengthが20未満ならばそれ以上データはないのでキャッシュに保存し、データを展開する
            if (json.length < 20) {

                cache.setAllData(itemList);
                cache.setIsLoading(false);
                createTagList(itemList);

                TumblingDice.prototype.getRelational();

                return;
            }

            // 次の20件を取得しに行く
            offset = offset + 20;

            tumblrApiCall(baseUri(offset), onSuccess, onError, onEmpty)();
        };

        tumblrApiCall(baseUri(offset), onSuccess, onError, onEmpty)();
    }

    function createTagList(allData) {
        /// <summary>タグ一覧をバインド</summary>

        var tagList = new Array();

        allData.forEach(function (x) {
            x.tag.forEach(function (tag) {
                // 既に保存してあるTagの場合はカウントアップする
                if (tagList.some(function (y) { return y.tagName == tag; })) {

                    tagList.filter(function (y) {
                        return y.tagName == tag;
                    }).forEach(function (y) {
                        y.count = y.count + 1;
                        y.posts.push(x);
                    });
                } else {
                    tagList.push({ tagName: tag, count: 1, posts: [x] });
                }
            });
        });

        // 名前順にソートする
        tagList.sort(function (a, b) {
            var x = a.tagName;
            var y = b.tagName;

            if (x < y) return -1;
            if (x > y) return 1;

            return 0;
        });

        var viewModel = getTagListViewModel();

        tagList.forEach(function (x) {
            var posts = x.posts.filter(function (y) { return y.type == "text"; });

            // 関連記事があったら「Show All」を先頭に追加する
            if (posts != null && posts.length > 0) {
                posts.unshift({
                    url: "http://" + domain + "/tagged/" + x.tagName,
                    title: "Show All"
                });
            }

            viewModel.setData(x, posts);
        });

        viewModel.isLoading(false);
    }

    //#endregion

    //#region Relational Posts

    TumblingDice.prototype.getRelational = function () {
        /// <summary>関連記事作成 タグ一覧作成時に同時に作ったキャッシュを使用する</summary>

        var cache = new Cache();

        // タグ一覧を読み込み中なら中止
        if (cache.isLoading()) return;

        var allData = cache.getAllData();

        // Text記事のみを抽出する
        var $texts = jQuery(".text");

        if ($texts.length == 0) return;

        $texts.each(function () {
            var $text = jQuery(this);
            var $div = $text.find(".relational-container");

            // Text記事のタグを取得する
            var $tags = $text.find("footer > .tags");
            if ($tags.length == 0) return;

            var $tagLink = $tags.find("a");
            var currentTags = new Array();
            $tagLink.each(function () { currentTags.push(jQuery(this).text().toString().replace("#", "")); });

            // 記事のIDを取得する
            var id = $text.find("footer > .small > li > a").attr("href").match(/post\/(\d+)/)[0];

            var viewModel = getRelationalViewModel($div[0]);

            currentTags.forEach(function (tag) {

                var randomLink = randomize(allData.filter(function (x) {
                    return x.id != id
                        && x.tag.some(function (y) { return y == tag; })
                        && x.type == "text";
                }), 5);

                viewModel.setData(tag, randomLink);
            });

            viewModel.isLoading(false);
        });
    }

    //#endregion

    //#region Utility Methods

    function tumblrApiCall(baseUri, onSuccess, onError, onEmpty) {
        /// <summary>Tumblrと通信</summary>
        /// <param name="baseUri" type="String">URI (GET)</param>
        /// <param name="onSuccess" type="Function">成功時処理</param>
        /// <param name="onError" type="Function">エラー時処理</param>
        /// <param name="onEmpty" type="Function">値なし時処理</param>
        /// <returns type="Function" />
        return function () {
            jQuery.ajax({
                type: "GET",
                url: baseUri,
                dataType: "jsonp",
                success: function (data) {
                    // ステータスコードを見て200以外であればError
                    if (data.meta.status != 200) {
                        if (onError != null) onError(data);
                        return;
                    }

                    // postsがなければEmpty
                    var json = data.response.posts;
                    if (json == null || json.length <= 0) {
                        if (onEmpty != null) onEmpty(data);
                        return;
                    }

                    onSuccess(json.map(function (x) {
                        var td = new TumblrData();
                        td.id = x.id;
                        td.date = x.date;
                        td.title = x.title;
                        td.url = x.post_url;
                        td.type = x.type;
                        td.tag = x.tags;
                        return td;
                    }));
                },
                error: function (req, status, error) {
                    return;
                }
            });
        };
    };

    function isNotUpdateLastId(cache) {
        /// <summary>現在表示されているページの一番大きいIDとキャッシュのLastIdを比較し、取得したIDがキャッシュより古ければTrue</summary>
        /// <param name="cache" type="Cache">キャッシュ</param>
        /// <returns type="Boolean" />

        // Storageが使えないブラウザの場合かキャッシュにlastIdがなければfalse
        var lastId = cache.getLastId();

        if (lastId == null) return false;

        // パーマネントリンクからIDを取得する
        var m = jQuery("footer > .small > li > a").attr("href").match(/post\/(\d+)/);

        // 取得できなかった場合、もしくはIDがキャッシュより新しければfalse
        if (!m || m[1] > lastId) {
            return false;
        }

        return true;
    }

    function randomize(arr, count) {
        /// <summary>配列からランダムで値を取り出す</summary>
        /// <param name="arr" type="Array">配列</param>
        /// <param name="count" type="Number">取り出す数</param>
        /// <returns type="Array" />

        if (arr == null) return null;

        var tmpArr = [];
        var length = 0;

        arr.forEach(function (x) {
            length++;
            tmpArr.push(x);
        });

        if (length <= count) return tmpArr;

        var ret = [];

        for (var i = 0; i < count; i++) {
            var sr = Math.random().toString();
            var r = "0." + sr.charAt(sr.length - 1);

            var tmpItem = tmpArr[Math.floor(r * length)];
            while (jQuery.inArray(tmpItem, ret) != -1) {
                sr = Math.random().toString();
                r = "0." + sr.charAt(sr.length - 1);
                tmpItem = tmpArr[Math.floor(r * length)];
            }
            ret.push(tmpItem);
        }

        return ret;
    };

    //#endregion

    root.TumblingDice = TumblingDice;

})(this);