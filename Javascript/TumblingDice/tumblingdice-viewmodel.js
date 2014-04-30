/// <reference path="knockout-3.1.0.debug.js">
/// <reference path="tumblingdice-1.3.2.js">
/// <reference path="jquery-1.6.2.js">
(function (root) {

    var recentViewModel = new RecentViewModel();

    function RecentViewModel() {
        /// <summary>最新記事一覧のViewModel</summary>

        /// <field name="isLoading" type="Boolean">読み込み中フラグ</field>
        this.isLoading = ko.observable(true);
        /// <field name="datas" type="observableArray">日付に紐付く記事のリンク</field>
        this.datas = ko.observableArray();
        /// <field name="dateLink" type="Function">日付のリンク</field>
        this.dateLink = function (date) { return "http://outofmem.tumblr.com/day/" + date; };

        this.setData = function (post) {
            
            // 日付変換
            var dateArray = post.date.split(" ");
            var match = dateArray[0].match(/(\d+)-(\d+)-(\d+)/);
            var day = [match[1], match[2], match[3]];

            // UTCで15時以降の時間なら日付を1増やす
            match = dateArray[1].match(/(\d+):\d+:\d+/);
            var h = match[1];
            if (h >= 15) {
                var d = day[2];
                d = 1 + (+d);
                if (d < 10) day[2] = "0" + d;
            }

            var date = day.join("/");

            if (!this.datas().some(function (x) { return date == x.date; })) {
                // キーがなければ新たに作成
                this.datas.push({ date: date, posts: ko.observableArray([post]) });
            } else {
                // キーがある場合はposts配列にデータを追加
                this.datas().filter(function (x) { return date == x.date; })
                                        .forEach(function (x) { x.posts.push(post); });
            }
        };
    };

    ko.applyBindings(recentViewModel, document.getElementById("recent"));

    var tagListViewModel = new TagListViewModel();

    function TagListViewModel() {
        /// <summary>タグ一覧のViewModel</summary>

        var self = this;

        /// <field name="isLoading" type="Boolean">読み込み中フラグ</field>
        this.isLoading = ko.observable(true);

        /// <field name="datas" type="observableArray">そのタグがついた記事の一覧</field>
        this.datas = ko.observableArray();

        this.setData = function (data, posts) {
            /// <summary>タグに紐付く情報のセット</summary>
            /// <param name="data" type="Object">Description</param>
            /// <param name="posts" type="Array">Description</param>

            self.datas.push({
                tag: data.tagName,
                count: data.count,
                tagWithCount: data.tagName + " (" + data.count + ")",
                posts: posts,
                isExpandable: posts != null && posts.length > 0,
                isExpanded: ko.observable(false),
                getAllTagLink: function () {
                    /// <summary>タグそのものへのリンク取得</summary>
                    /// <returns type="String" />

                    return !this.isExpandable ? "http://outofmem.tumblr.com/tagged/" + this.tag : "/#/";
                },
                expandLink: function () {
                    /// <summary>タグリンクのクリック時挙動</summary>
                    if (this.isExpandable) {

                        if (this.isExpanded()) {
                            this.isExpanded(false);
                        } else {
                            this.isExpanded(true);
                        }

                        return false;
                    } else {
                        return true;
                    }
                }
            });
        };
    }

    ko.applyBindings(tagListViewModel, document.getElementById("tags"));

    function RelationalViewModel() {
        /// <field name="isLoading" type="Boolean">読み込み中フラグ</field>
        this.isLoading = ko.observable(true);

        /// <field name="relational" type="observableArray">関連記事データ</field>
        this.relational = ko.observableArray();

        this.setData = function (tag, randomLink) {
            /// <summary>Description</summary>
            /// <param name="tag" type="String">タグ</param>
            /// <param name="randomLink" type="Array">タグから取得したランダムなデータ</param>

            this.relational.push({
                tag: tag,
                tagLink: "http://outofmem.tumblr.com/tagged/" + tag,
                // Tagと紐付くPostをキャッシュに登録されているデータから取得する
                randomLink: randomLink
            });
        };
    }

    jQuery(".text").each(function () {
        var $text = jQuery(this);
        var $div = $text.find(".relational-container");

        // Text記事のタグを取得する
        var $tags = $text.find("footer > .tags");
        if ($tags.length == 0) return;

        ko.applyBindings(new RelationalViewModel(), $div[0]);
    });

    root.getRecentViewModel = function () {
        /// <summary>最新記事一覧ViewModelの取得</summary>
        /// <returns type="RecentViewModel" />
        return recentViewModel;
    };

    root.getTagListViewModel = function () {
        /// <summary>タグ一覧ViewModelの取得</summary>
        /// <returns type="TagListViewModel" />
        return tagListViewModel;
    };

    root.getRelationalViewModel = function (node) {
        /// <summary>関連記事ViewModelの取得</summary>
        /// <returns type="RelationalViewModel" />
        return ko.dataFor(node);
    };

})(this);