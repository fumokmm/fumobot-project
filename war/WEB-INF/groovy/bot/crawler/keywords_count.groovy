package botcommand.crawler

import java.util.logging.Logger

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import hatenahaiku4j.*
import hatenahaiku4j.util.*

log = Logger.getLogger(this.class.name)

KEYWORDS_OF_PAGE			= 50				// PC用キーワードページ取得件数
KEYWORDS_OF_PAGE_FOR_MOBILE	= 10				// 携帯用キーワードページ取得件数
KIND						= 'KeywordsCount'	// データカインド 

try {
	println "/bot/crawler/keywords_count.groovy<br/><br/>"

	// HTMLスクレイピング版
	def api = new HatenaHaikuAPIHTML()

	// データストアよりデータを取得してくる
	def kcInfo = getKeywordCountMap()
	// 実行前
	log.info '■実行前<br/>'
	show(kcInfo)

	// lastPageを使ってキーワードリストを取得してみる
	def result = api.getKeywordList(kcInfo.lastPage as int)

	// 取得できた
	if (result) {
		println "${result.size()}件取得。"
		
		// それが答えだ
		kcInfo.count			= ((kcInfo.lastPage as int) - 1) * KEYWORDS_OF_PAGE + result.size()
		kcInfo.lastPageMobile	= toMobilePage(kcInfo.count as int)
		kcInfo.updatedAt		= DateUtil.now
	
		// 取得件数がキーワード件数とぴったり同じか？
		if (result.size() == KEYWORDS_OF_PAGE) {
			// 無条件で+1
			kcInfo.lastPage			= (kcInfo.lastPage as int) + 1
			kcInfo.count			= ((kcInfo.lastPage as int) - 1) * KEYWORDS_OF_PAGE + 0
			kcInfo.lastPageMobile	= toMobilePage(kcInfo.count as int)
			kcInfo.updatedAt		= DateUtil.now
		}
	}

	// 状態を保存
	kcInfo.save()

	// 結果を出力
	log.info '■結果<br/>'
	show(kcInfo)

} catch(Exception e) {
	e.printStackTrace()
}

/**
 * 必ず1件のみ取得される(1件しかデータがない)
 */
def getKeywordCountMap() {
	// 1件の結果のみ受け取る。
	def q = new Query(KIND)
	def info = datastoreService.prepare(q).asSingleEntity()
	if (!info) {
		info				= new Entity(KIND)
		info.count			= 0					// 件数
		info.updatedAt		= DateUtil.now		// 更新日時
		info.lastPage		= 3034				// 最終ページ
		info.lastPageMobile	= 1					// 最終ページ(携帯)
		info.save()
	}
	info
}

def show(kcInfo) {
	// 状態を出力
	log.info """
		現在のカウント数: ${kcInfo.count as int}<br/>
		現在の最終ページ: ${kcInfo.lastPage as int}<br/>
		現在の最終ページ(モバイル): ${kcInfo.lastPageMobile as int}<br/>
		最終更新日: ${HatenaUtil.formatDate(kcInfo.updatedAt)}<br/>
	"""
}


/**
 * 携帯用ページ番号に変換
 *
 * @param count キーワード数
 */
def toMobilePage(int count) {
    int base = (count / KEYWORDS_OF_PAGE_FOR_MOBILE) as int
    int amari = count % KEYWORDS_OF_PAGE_FOR_MOBILE
    base + (amari ? 1 : 0)
}
