package dao

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*

import hatenahaiku4j.util.*

/**
 * ユーザ情報に関するDAO
 *
 * [kind]
 *   UserInfo
 *
 * [field]
 *   util.DaoUtil.COUNT_OF_KEYWORD_URANAI キーワード占いの回数
 *   util.DaoUtil.COUNT_OF_KEYWORD_MAKER  キーワードメーカーの回数
 *   util.DaoUtil.UPDATED_AT              更新日時
 */
class UserInfoDao {

	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'UserInfo'

	//------------------------------------------------
	// Property of this class

	/** ユーザID */
	String userId
	/** データストアサービス */
	DatastoreService datastoreService

	//------------------------------------------------
	// Constructor

	/**
	 * コンストラクタ
	 * @param userId ユーザID
	 */
	UserInfoDao(String userId) {
		this.userId = userId
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * ユーザ情報取得
	 */
	def getUserInfo() {
		def q = new Query(KeyFactory.createKey(KIND, userId))
		def info = datastoreService.prepare(q).asSingleEntity()
		if (!info) {
			info = new Entity(KIND, userId)
			info.save()
		}
		setDefault(info)
		return info
	}
	
	/**
	 * あるフィールドをカウントアップする。カウントアップした後にsave()します。
	 * カウントアップに失敗した場合、何も行いません。
	 * @param field カウントアップするフィールド
	 * @param num アップする数(デフォルト=1)
	 */
	def countUp(def field, def num = 1) {
		datastoreService.withTransaction{
			def info = getUserInfo()
			try {
				// フィールドをカウントアップ
				info[field] += num
				// 更新日付を更新して登録
				info[UPDATED_AT] = DateUtil.now
				info.save()
			} catch(Exception e) {
				// ignore it
			}
		}
	}

	/**
	 * あるフィールドをカウントを返却する。
	 * カウント取得に失敗した場合0を返却する。
	 * @param field カウントを取得するフィールド
	 */
	def getCount(def field) {
		def info = getUserInfo()
		try {
			// フィールドのカウントを取得
			return info[field].toInteger()
		} catch(Exception e) {
			return 0
		}
	}

	//------------------------------------------------
	// Utilities

	/**
	 * 初期値を設定
	 */
	static def setDefault(def info) {
		if (!info[COUNT_OF_KEYWORD_URANAI])
			info[COUNT_OF_KEYWORD_URANAI]	= 0
		if (!info[COUNT_OF_KEYWORD_MAKER])
			info[COUNT_OF_KEYWORD_MAKER]	= 0
		if (!info[UPDATED_AT])
			info[UPDATED_AT]				= DateUtil.now
	}

}
