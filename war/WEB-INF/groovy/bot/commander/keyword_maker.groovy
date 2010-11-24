import action.*
import util.*
import dao.*
import util.*

import static util.DaoUtil.*

def action = null
def statusDao = new HaikuStatusDao()

// 処理対象のステータスを集める
def targetStatusList = []
// 「キーワードメーカー」から取得
targetStatusList += statusDao.getStatusByKeyword('キーワードメーカー', 4, FUMOBOT_FLG)
// 「id:fumobot」から取得
targetStatusList += statusDao.getStatusByKeyword('id:fumobot', 1, FUMOBOT_FLG)

// 取得できた分繰り返し
targetStatusList.each { status ->
	action = new KeywordMakerAction('fumobot', status)
	// キーワードメークできた場合
	if (action.isAvailable()) {
		// コマンド作成
		action.createCommand()
		println "コマンドを登録しました。"

		// データ更新
		action.update()
		println "データ更新しました。"
	}
	// ふもぼフラグを立てる
	statusDao.setFlgOn status[STATUS_ID], FUMOBOT_FLG
	println "${status[STATUS_ID]} 処理完了"
}

println "処理終了"
