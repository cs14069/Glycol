# Glycol

# 何をする
最近はやりのbitcoinを買います。ひたすら買います。
ドルコスト平均法で、一定金額分を購入します。

# なぜ作った
機械学習の勉強をしていろいろ作ってみたが、うまくいかない。
勉強してる間、とりあえず買い続けるbotを動かしておこう、簡単に作れそうだし。という気持ちで作った。

# 仕組み
crontabで定期的に動かす。
私の設定は以下のようにしてある。
<username>はubuntuのユーザ名に、
<path to Glycol>はGlycolへのパスに置き換える必要がある。

    2 */3 * * *   <username> cd /<path to Glycol>/Glycol && bash purchase.dat >> log/cron.log 2>&1
    2 1-22/3      * * *   <username> cd /<path to Glycol>/Glycol && bash execute.dat >> log/cron.log 2>&1
    2 2-23/3      * * *   <username> cd /<path to Glycol>/Glycol && bash execute.dat >> log/cron.log 2>&1

この記述により、3時間ごと（0, 3, 6, ..., 21 時）にpurchase.datを実行する。
また、それ以外の時間（1, 2, 4, 5, ..., 23 時）にexecute.datを実行する。

# .datファイル
src/ に存在する *.datファイルの説明。
すべて、 `bash *.dat` と記述して実行できる。
- compile.dat ・・・ コンパイルのみを行う。
- execute.dat ・・・ APIを使ってアクセスし、 資産の変化をDBへ保存する。
- purchace.dat ・・・ APIを使ってアクセスし、資産の変化をDBへ保存した後、注文する。注文の詳細をDBへ保存する。
- all.dat ・・・ compile.datとexecute.datを続けて実行する。

# 環境

- ubuntu 14.04.5 LTS, Trusty Tahr
- MySQL Ver 14.14 Distrib 5.5.59, for debian-linux-gnu (x86_64) using readline 6.3
- oracle java 1.8.0_162

以下二つは、公式サイトからダウンロードして、
src/lib/ 下に配置しなければならない。

- jsonic-1.1.3.jar
- mysql-connector-java-5.1.42-bin.jar


# 準備
git clone する。
docs/glycol.mysql をMySQLで実行し、データベース・テーブルを作成する。
src/Main/Setting.java に、APIキー、APIシークレット、データベース名などを記述する。

    $ cd src
    $ bash compile.dat
    $ bash execute.dat
これで、データベースに証拠金などのデータが入っていれば完璧。 

    $ bash purchace.dat
で購入できる。

# 結果
4/21から5/21の1か月でマイナス4万円。
最初のbull相場では順当に5万円程度儲かった。
後半のbear相場ではマイナス15万円程度まで行ったが、マイナス5万円程度まで急に戻したので止めた。
怖かった。

# 問題
利確のときにrestPositionが中途半端になると、次の売り注文が高くなってしまう。
（何を言っているのかわからないと思うが僕も何を言っているのかわからない）
50万円あれば平均購入金額の10%相場が下がっても大丈夫だった。
3週間ほど経ったとき、証拠金維持率が300%を切った。
やっぱり買うだけじゃメンタルに悪い。
