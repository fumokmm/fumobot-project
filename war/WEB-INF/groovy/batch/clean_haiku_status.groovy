import dao.*
import util.*
import static util.DaoUtil.*

final int DELETE_DAYS_AGO = 3 // 3日以上前は削除

// ロガー
java.util.logging.Logger log =  java.util.logging.Logger.getLogger(HaikuStatusDao.class.name)

// ステータスDAO生成
def statusDao = new HaikuStatusDao()

// 最古のものから20件取得してきて処理する
int deleteCount = 0
statusDao.getStatus(20).each { statusEntity ->
	// 3日以上前のものは削除する
	if (statusDao.deleteStatusDaysAgo(statusEntity[STATUS_ID], DELETE_DAYS_AGO)) deleteCount++
}

log.info "clean_haiku_status.groovy(${DELETE_DAYS_AGO}) 処理終了: 結果 ${deleteCount} 件削除。"
