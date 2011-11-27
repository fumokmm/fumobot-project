package dao

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*

import hatenahaiku4j.util.*

// TODO: そのうち実装。現在はべた書きになっている。

/**
 * キーワードカウントに関するDAO
 *
 * [kind]
 *   KeywordsCount
 *
 * [field]
 *   util.DaoUtil.LAST_PAGE        最終ページ
 *   util.DaoUtil.LAST_PAGE_MOBILE 最終ページ（携帯）
 *   util.DaoUtil.COUNT            投稿数
 *   util.DaoUtil.UPDATED_AT       更新日時
 */
class KeywordsCountDao {

	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'KeywordsCount'

	//------------------------------------------------
	// Property of this class

	/** データストアサービス */
	DatastoreService datastoreService

	//------------------------------------------------
	// Constructor

	/**
	 * コンストラクタ
	 * @param title キーワードタイトル
	 */
	KeywordsCountDao() {
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * キーワード情報取得
	 */
	def getKeywordsCount() {
//		def q = new Query(KeyFactory.createKey(KIND, title))
//		def info = datastoreService.prepare(q).asSingleEntity()
//		if (!info) {
//			info = new Entity(KIND, title)
//			info.save()
//		}
//		setDefault(info)
//		return info
	}
	
	//------------------------------------------------
	// Utilities

	/**
	 * 初期値を設定
	 */
	static def setDefault(def info) {
//		if (!info[ENTRY_COUNT])
//			info[ENTRY_COUNT]	= 0
//		if (!info[UPDATED_AT])
//			info[UPDATED_AT]	= DateUtil.now
	}

}
