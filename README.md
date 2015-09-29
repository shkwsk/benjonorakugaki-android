# benjonorakugaki-android
### Version 0.2.1
- New: QRReaderActivity, AppUUID
- Fix: UUIDが起動毎に変わってしまう問題を解決。

### Version 0.2.0
- Add: 文化祭用のフレーバー作成
- Add: 文化祭用のポート番号設定

### Version 0.1.8 (release 1.0.1)
- Mod: 不要な権限を削除
- Mod: masterRelease時はパッケージ名を変更するよう設定
- Mod: .gitignoreにkeystoreファイルを追加
- Mod: build.gradleのバージョンコード、バージョンネームを変更。（アップデートリリース時に必須）

### Version 0.1.7 (release 1.0)
- Mod: 機種の解像度によって線幅が異なるのを修正
- Add: デバッグ用の構成(flavor)を追加

### Version 0.1.6
- Mod: サーバからのレスポンスがバイナリの直接送信に変更。これに対応する修正。
- Mod: .gitignoreにAndroid Studioの自動生成ファイル群を追加

### Version 0.1.5
- Mod: Toastの表示時間を短くした
- Add: POST済の描画パスリスト drewList
- Add: POST通信処理

### Version 0.1.4
- 描画画面の縦・横サイズ取得。
- 描画パス・色・座標をPOST形式に変換。
- マーカー選択画面で最近傍周辺を表示。

### Version 0.1.3
- 描画をbitmapからpath形式で扱うようにした。
- 各pathの色・点をリストで保持することで、「もどる」ボタン実装。

### Version 0.1.2
- ポート番号を本番環境へ移行。
- Toastメッセージのタイミング調整。

### Version 0.1.1
- SDカードから内部ストレージにtmp画像を保存。
- 地図マーカーをタップするよう促すトーストの表示。

### Version 0.1.0
- Yアワードに投稿
- トップ画面作成
- 描画前の画像送信時に落ちる問題を解決
- タイトルバー非表示

### Version 0.0.2
- 描画背景画像の受信
- SDカードへの描画画像書き出し
- 描画画像の送信
- 描画色変更機能

### Version 0.0.1
- GPSかWi-Fiから位置情報取得
- らくがき位置リストをリクエスト
- らくがき位置をマップ上にマーカー表示
- マーカーをタップすると描画画面に遷移
