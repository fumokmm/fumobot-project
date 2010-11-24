package dao

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*

import hatenahaiku4j.util.*

/**
 * キーワード情報に関するDAO
 *
 * [kind]
 *   KeywordInfo
 *
 * [field]
 *   util.DaoUtil.ENTRY_COUNT 投稿数
 *   util.DaoUtil.UPDATED_AT  更新日時
 */
class KeywordInfoDao {

	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'KeywordInfo'

	//------------------------------------------------
	// Property of this class

	/** キーワードタイトル */
	String title
	/** データストアサービス */
	DatastoreService datastoreService

	//------------------------------------------------
	// Constructor

	/**
	 * コンストラクタ
	 * @param title キーワードタイトル
	 */
	KeywordInfoDao(String title) {
		this.title = title
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * キーワード情報取得
	 */
	def getKeywordInfo() {
		def q = new Query(KeyFactory.createKey(KIND, title))
		def info = datastoreService.prepare(q).asSingleEntity()
		if (!info) {
			info = new Entity(KIND, title)
			info.save()
		}
		setDefault(info)
		return info
	}
	
	//------------------------------------------------
	// Utilities

	/**
	 * 初期値を設定
	 */
	static def setDefault(def info) {
		if (!info[ENTRY_COUNT])
			info[ENTRY_COUNT]	= 0
		if (!info[UPDATED_AT])
			info[UPDATED_AT]	= DateUtil.now
	}

}
